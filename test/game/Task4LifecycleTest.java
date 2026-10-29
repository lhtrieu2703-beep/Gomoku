package game;

import game.ai.MinimaxAI;
import game.model.*;
import game.network.NetworkManager;
import game.view.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.MouseEvent;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import static org.junit.jupiter.api.Assertions.*;

class Task4LifecycleTest {
    private Lang.Language language;
    private boolean sound;
    private MainFrame frame;
    @BeforeEach void open() throws Exception {
        language = Lang.get(); sound = SoundManager.isSoundEnabled();
        SwingUtilities.invokeAndWait(() -> { frame = Task4StartScreenTest.frame(Lang.Language.EN); frame.startGame(); });
    }
    @AfterEach void close() throws Exception {
        SwingUtilities.invokeAndWait(() -> { frame.dispose(); Lang.set(language); SoundManager.setSoundEnabled(sound); });
    }

    @Test void menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame() throws Exception {
        SwingWorker<?, ?>[] oldWorker = new SwingWorker<?, ?>[1];
        SwingUtilities.invokeAndWait(() -> {
            frame.beginGame(BoardPanel.GameMode.PV_AI, MinimaxAI.Difficulty.HARD);
            BoardPanel panel = frame.getBoardPanel(); int oldId = panel.getGameId();
            assertTrue(panel.playMove(7, 7)); assertTrue(panel.isAiThinking());
            SwingWorker<?, ?> worker = P2LifecycleTest.field(panel, "aiWorker");
            oldWorker[0] = worker;
            frame.returnToMenu(); assertTrue(worker.isCancelled()); assertFalse(panel.isAiThinking());
            assertEquals(oldId + 1, panel.getGameId()); assertEquals(0, panel.getMoveHistory().size());
            frame.startGame(); assertFalse(frame.isMenuVisible());
        });
        SwingUtilities.invokeAndWait(() -> {
            // Replay the actual product completion after the next card transition.
            // A worker cancelled before STARTED need not emit a DONE property event.
            try {
                var completion = oldWorker[0].getClass().getDeclaredMethod("done");
                completion.setAccessible(true); completion.invoke(oldWorker[0]);
            } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            assertTrue(frame.getBoardPanel().getMoveHistory().isEmpty());
            Board board = P2LifecycleTest.field(frame, "board"); assertEquals(0, board.getMoveCount());
        });
    }

    @Test void menuClosesRealLanSocketAndAllConnectionDialogs() throws Exception {
        NetworkManager manager = P2LifecycleTest.field(frame, "networkManager");
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); manager.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                peer.setSoTimeout(3000);
                PrintWriter writer = new PrintWriter(peer.getOutputStream(), true);
                String round = UUID.randomUUID().toString(); writer.println("START 2 " + round);
                awaitEdt(() -> frame.getBoardPanel().getGameMode() == BoardPanel.GameMode.ONLINE);
                writer.println("MOVE " + round + " 1 7 7");
                awaitEdt(() -> frame.getBoardPanel().getMoveHistory().size() == 1);
                SwingUtilities.invokeAndWait(() -> {
                    int gameId = frame.getBoardPanel().getGameId();
                    frame.returnToMenu();
                    assertFalse(manager.isConnected()); assertFalse(manager.isWaitingForRestart());
                    assertNull(frame.getBoardPanel().getNetworkRoundId());
                    assertEquals(Piece.X, frame.getBoardPanel().getMyPiece());
                    assertEquals(gameId + 1, frame.getBoardPanel().getGameId());
                    for (Window window : frame.getOwnedWindows()) assertFalse(window.isDisplayable());
                    frame.startGame();
                });
                assertEquals(-1, peer.getInputStream().read());
            }
        }
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(frame.getSelectedMode(), frame.getBoardPanel().getGameMode());
            assertTrue(frame.getBoardPanel().getMoveHistory().isEmpty());
        });
    }

    @Test void menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            frame.getBoardPanel().playMove(7, 7);
            NetworkManager manager = P2LifecycleTest.field(frame, "networkManager");
            SwingUtilities.invokeLater(() -> {
                NetworkDialog dialog = P2LifecycleTest.field(frame, "networkDialog");
                assertNotNull(dialog); assertTrue(dialog.isVisible());
                manager.hostGame(0);
                Object oldSession = P2LifecycleTest.field(manager, "current");
                frame.returnToMenu(); assertFalse(dialog.isDisplayable());
                assertEquals(Boolean.TRUE, P2LifecycleTest.field(oldSession, "closed"));
                frame.startGame();
            });
            frame.openNetworkDialog();
            assertFalse(frame.isMenuVisible());
            assertEquals(BoardPanel.GameMode.PVP, frame.getSelectedMode());
            assertEquals(frame.getSelectedMode(), frame.getBoardPanel().getGameMode());
            assertTrue(frame.getBoardPanel().getMoveHistory().isEmpty()); assertFalse(manager.isConnected());
        });
    }

    @Test void queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart() throws Exception {
        AtomicInteger callbacks = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            NetworkManager manager = P2LifecycleTest.field(frame, "networkManager");
            Consumer<Runnable> dispatcher = P2LifecycleTest.field(manager, "dispatcher");
            dispatcher.accept(callbacks::incrementAndGet);
            frame.returnToMenu(); frame.startGame();
        });
        SwingUtilities.invokeAndWait(() -> assertEquals(0, callbacks.get()));
    }

    @Test void menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BoardPanel panel = frame.getBoardPanel();
            SwingUtilities.invokeLater(() -> panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(), 0, 298, 298, 1, false)));
            frame.returnToMenu();
        });
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(frame.getBoardPanel().isEnabled());
            assertTrue(frame.getBoardPanel().getMoveHistory().isEmpty());
            assertFalse(frame.getBoardPanel().isAiThinking());
        });
    }

    @Test void menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1), interrupted = new CountDownLatch(1), release = new CountDownLatch(1);
        SwingWorker<Void, Void> running = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                started.countDown();
                try { release.await(); } catch (InterruptedException error) { interrupted.countDown(); throw error; }
                return null;
            }
        };
        try {
            running.execute(); assertTrue(started.await(3, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                // Controlled running worker isolates lifecycle cancellation from AI search duration.
                try {
                    var slot = BoardPanel.class.getDeclaredField("aiWorker"); slot.setAccessible(true); slot.set(frame.getBoardPanel(), running);
                    var thinking = BoardPanel.class.getDeclaredField("isAiThinking"); thinking.setAccessible(true); thinking.setBoolean(frame.getBoardPanel(), true);
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                frame.returnToMenu(); assertTrue(running.isCancelled()); assertFalse(frame.getBoardPanel().isAiThinking());
            });
            assertTrue(interrupted.await(3, TimeUnit.SECONDS));
        } finally { release.countDown(); running.cancel(true); }
    }

    @Test void queuedOpponentMoveCannotChangeBoardAfterMenu() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BoardPanel panel = frame.getBoardPanel(); String round = UUID.randomUUID().toString();
            panel.setupOnlineGame(null, false, round);
            Thread receiver = new Thread(() -> panel.applyOpponentMove(round, 1, 4, 4));
            receiver.start();
            try { receiver.join(2000); } catch (InterruptedException e) { throw new AssertionError(e); }
            assertFalse(receiver.isAlive());
            frame.returnToMenu(); frame.startGame();
        });
        SwingUtilities.invokeAndWait(() -> assertTrue(frame.getBoardPanel().getMoveHistory().isEmpty()));
    }

    @Test void menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            BoardPanel panel = frame.getBoardPanel(); int oldId = panel.getGameId();
            for (int c = 0; c < 4; c++) { panel.playMove(7, c); panel.playMove(0, c * 2); }
            JDialog scores = new RulesDialog(frame); scores.setModal(false); scores.setVisible(true);
            SwingUtilities.invokeLater(() -> {
                JDialog result = P2LifecycleTest.field(frame, "resultDialog");
                assertNotNull(result); assertTrue(result.isVisible());
                frame.returnToMenu(); assertFalse(result.isDisplayable()); assertFalse(scores.isDisplayable());
                frame.startGame();
            });
            panel.playMove(7, 4);
            assertEquals(1, frame.getScoreTracker().getHumanWins());
            BoardPanel.GameStateListener listener = P2LifecycleTest.field(panel, "stateListener");
            listener.onGameOver(oldId, "stale", Piece.O);
            assertEquals(0, frame.getScoreTracker().getAiWins()); assertNull(P2LifecycleTest.field(frame, "resultDialog"));
            assertFalse(panel.isGameOver()); assertFalse(panel.canUndo());
        });
    }

    @Test void undoRemainsAvailableAfterWin() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            BoardPanel panel = frame.getBoardPanel();
            for (int c = 0; c < 4; c++) { panel.playMove(7, c); panel.playMove(0, c * 2); }
            SwingUtilities.invokeLater(() -> {
                JDialog result = P2LifecycleTest.field(frame, "resultDialog"); result.dispose();
            });
            panel.playMove(7, 4); assertTrue(panel.isGameOver()); assertTrue(panel.canUndo());
            JButton undo = P2LifecycleTest.field(frame, "btnUndo"); assertTrue(undo.isEnabled()); undo.doClick();
            assertFalse(panel.isGameOver()); assertEquals(8, panel.getMoveHistory().size());
            assertEquals(1, frame.getScoreTracker().getHumanWins());
        });
    }

    private static void awaitEdt(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        boolean[] met = {false};
        do {
            SwingUtilities.invokeAndWait(() -> met[0] = condition.getAsBoolean());
            if (met[0]) return;
            Thread.sleep(5);
        } while (System.nanoTime() < deadline);
        fail("Timed out waiting for real LAN callback on EDT");
    }
}
