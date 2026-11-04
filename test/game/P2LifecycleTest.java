package game;

import game.ai.MinimaxAI;
import game.model.*;
import game.network.NetworkManager;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import javax.swing.*;
import java.awt.event.WindowEvent;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.AbstractPreferences;
import static org.junit.jupiter.api.Assertions.*;

class P2LifecycleTest {
    private Lang.Language language;
    private boolean sound;
    @BeforeEach void remember() {
        language = Lang.get(); sound = SoundManager.isSoundEnabled();
        SoundManager.setSoundEnabled(false);
    }
    @AfterEach void restore() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Lang.set(language); SoundManager.setSoundEnabled(sound);
        });
    }

    @Test void disposedFrameDoesNotReceiveLanguageEvents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame disposed = new MainFrame(new AppSettings(new MemoryPreferences()));
            MainFrame active = new MainFrame(new AppSettings(new MemoryPreferences()));
            try {
                String before = disposed.getMainMenu().getAiButton().getText();
                disposed.dispose();
                Lang.set(Lang.get() == Lang.Language.EN ? Lang.Language.PT : Lang.Language.EN);
                assertEquals(before, disposed.getMainMenu().getAiButton().getText());
                assertEquals(Lang.pvAI(), active.getMainMenu().getAiButton().getText());
            } finally { disposed.dispose(); active.dispose(); }
        });
    }

    @Test void disposingFrameCancelsItsWorkerAndPreservesBoard() throws Exception {
        MainFrame[] frame = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frame[0] = new MainFrame(new AppSettings(new MemoryPreferences()));
            frame[0].startGame();
            BoardPanel panel = frame[0].getBoardPanel();
            assertTrue(panel.playMove(7, 7));
            assertTrue(panel.isAiThinking());
            SwingWorker<?, ?> worker = field(panel, "aiWorker");
            frame[0].dispose();
            assertTrue(worker.isCancelled());
            assertFalse(panel.isAiThinking());
        });
        SwingUtilities.invokeAndWait(() -> assertEquals(1, frame[0].getBoardPanel().getMoveHistory().size()));
    }

    @Test void closingDialogCancelsPendingHostAndIgnoresLateResults() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events();
        NetworkManager manager = new NetworkManager(events);
        try {
            manager.hostGame(0);
            Integer port = events.ports.poll(3, TimeUnit.SECONDS);
            assertNotNull(port);
            SwingUtilities.invokeAndWait(() -> {
                NetworkDialog dialog = new NetworkDialog(null, manager, () -> fail("cancelled"));
                JLabel status = field(dialog, "lblStatus");
                String before = status.getText();
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                dialog.onConnectionSuccess();
                dialog.onConnectionFailed("late error");
                assertFalse(dialog.wasConnected());
                assertEquals(before, status.getText());
                assertFalse(dialog.isDisplayable());
            });
            try (ServerSocket rebound = new ServerSocket(port)) { assertTrue(rebound.isBound()); }
        } finally { manager.disconnect(); }
    }

    @Test void successfulDialogDisposalKeepsConnection() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events();
        NetworkManager manager = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000);
            manager.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                new java.io.PrintWriter(peer.getOutputStream(), true).println("START 2 " + UUID.randomUUID());
                assertEquals("connected:false", events.next());
                SwingUtilities.invokeAndWait(() -> {
                    NetworkDialog dialog = new NetworkDialog(null, manager, () -> {});
                    dialog.onConnectionSuccess();
                    assertTrue(dialog.wasConnected());
                    assertTrue(manager.isConnected());
                    assertFalse(dialog.isDisplayable());
                });
            }
        } finally { manager.disconnect(); }
    }

    @Test void cancellingOnlineSelectionRestoresModeWithoutResettingGame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(new AppSettings(new MemoryPreferences()));
            try {
                frame.startGame();
                frame.beginGame(BoardPanel.GameMode.PVP, game.ai.MinimaxAI.Difficulty.MEDIUM);
                BoardPanel panel = frame.getBoardPanel();
                assertTrue(panel.playMove(7, 7));
                int id = panel.getGameId();
                SwingUtilities.invokeLater(() -> {
                    NetworkDialog dialog = field(frame, "networkDialog");
                    dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                });
                frame.openNetworkDialog();
                assertEquals(BoardPanel.GameMode.PVP, frame.getSelectedMode());
                assertEquals(BoardPanel.GameMode.PVP, panel.getGameMode());
                assertEquals(id, panel.getGameId());
                assertEquals(1, panel.getMoveHistory().size());
                assertTrue(panel.canUndo());
            } finally { frame.dispose(); }
        });
    }

    @Test void receivedMoveIsAppliedBeforeNextEdtNetworkCallback() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Board board = new Board();
            BoardPanel panel = new BoardPanel(board);
            panel.setupOnlineGame(null, false);
            panel.applyOpponentMove(4, 4);
            assertEquals(Piece.X, board.getPiece(4, 4));
            assertEquals(Piece.O, panel.getCurrentTurn());
            panel.resetGame();
            panel.applyOpponentMove(5, 5);
            assertEquals(Piece.NONE, board.getPiece(4, 4));
            assertEquals(Piece.X, board.getPiece(5, 5));
        });
    }

    @ParameterizedTest @EnumSource(NetworkManager.ErrorType.class)
    void networkErrorsAreTranslated(NetworkManager.ErrorType type) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var error = new NetworkManager.NetworkError(type, "127.0.0.1", 1234, new Exception("diagnostic"));
            Lang.set(Lang.Language.VI); String vi = Lang.networkError(error);
            Lang.set(Lang.Language.EN); String en = Lang.networkError(error);
            Lang.set(Lang.Language.PT); String pt = Lang.networkError(error);
            assertNotEquals(vi, en); assertNotEquals(en, pt); assertNotEquals(vi, pt);
            assertTrue(en.chars().allMatch(c -> c < 128));
            for (char viChar : LangPurityTest.VI_ONLY_CHARS.toCharArray()) assertTrue(pt.indexOf(viChar) < 0);
            assertFalse(vi.contains("diagnostic"));
            if (type == NetworkManager.ErrorType.HOST || type == NetworkManager.ErrorType.JOIN) {
                assertTrue(vi.contains("1234")); assertTrue(en.contains("1234")); assertTrue(pt.contains("1234"));
            }
        });
    }

    @Test void interruptedAiAbortsWithoutMutatingBoard() throws Exception {
        Board board = new Board(); board.makeMove(7, 7, Piece.X);
        AtomicReference<Throwable> result = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            Thread.currentThread().interrupt();
            try { new MinimaxAI(Piece.O, MinimaxAI.Difficulty.HARD).findBestMove(board); }
            catch (Throwable error) { result.set(error); }
        });
        worker.start(); worker.join(2000);
        assertFalse(worker.isAlive());
        assertInstanceOf(CancellationException.class, result.get());
        assertEquals(1, board.getMoveCount());
    }

    @Test void cancellationInsideRecursiveSearchUnwindsAllMoves() throws Exception {
        Board board = new Board() {
            @Override public boolean makeMove(int r, int c, Piece piece) {
                boolean placed = super.makeMove(r, c, piece);
                if (Arrays.stream(Thread.currentThread().getStackTrace())
                        .anyMatch(frame -> frame.getClassName().equals(MinimaxAI.class.getName())
                                && frame.getMethodName().equals("minimax"))) {
                    Thread.currentThread().interrupt();
                }
                return placed;
            }
        };
        board.makeMove(7, 7, Piece.X);
        AtomicReference<Throwable> result = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try { new MinimaxAI(Piece.O, MinimaxAI.Difficulty.HARD).findBestMove(board); }
            catch (Throwable error) { result.set(error); }
        });
        worker.setDaemon(true);
        worker.start(); worker.join(3000);
        try {
            assertFalse(worker.isAlive());
            assertInstanceOf(CancellationException.class, result.get());
            assertEquals(1, board.getMoveCount());
            for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
                assertEquals(r == 7 && c == 7 ? Piece.X : Piece.NONE, board.getPiece(r, c));
        } finally { worker.interrupt(); }
    }

    @SuppressWarnings("unchecked")
    static <T> T field(Object target, String name) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    static final class MemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        MemoryPreferences() { super(null, ""); values.put("soundEnabled", "false"); }
        protected void putSpi(String k, String v) { values.put(k, v); }
        protected String getSpi(String k) { return values.get(k); }
        protected void removeSpi(String k) { values.remove(k); }
        protected void removeNodeSpi() { values.clear(); }
        protected String[] keysSpi() { return values.keySet().toArray(new String[0]); }
        protected String[] childrenNamesSpi() { return new String[0]; }
        protected AbstractPreferences childSpi(String name) { throw new UnsupportedOperationException(); }
        protected void syncSpi() {}
        protected void flushSpi() {}
    }
}
