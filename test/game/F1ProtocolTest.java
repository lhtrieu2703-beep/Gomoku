package game;

import game.model.*;
import game.network.NetworkManager;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class F1ProtocolTest {
    @BeforeEach void mute() { SoundManager.setSoundEnabled(false); }
    static String id() { return UUID.randomUUID().toString(); }
    static PrintWriter writer(Socket socket) throws IOException { return new PrintWriter(socket.getOutputStream(), true); }

    @ParameterizedTest @ValueSource(ints = {0, 2, 3, 225})
    void invalidPlyOrColorDoesNotChangeBoard(int ply) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Board board = new Board(); BoardPanel panel = new BoardPanel(board); String round = id();
            panel.setupOnlineGame(null, false, round);
            panel.applyOpponentMove(round, ply, 4, 4);
            assertEquals(0, board.getMoveCount());
        });
    }
    @Test void wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Board board = new Board(); BoardPanel panel = new BoardPanel(board); String round = id();
            panel.setupOnlineGame(null, false, round);
            panel.applyOpponentMove(id(), 1, 4, 4); assertEquals(0, board.getMoveCount());
            panel.applyOpponentMove(round, 1, 4, 4); assertEquals(Piece.X, board.getPiece(4, 4));
            panel.applyOpponentMove(round, 1, 5, 5); // duplicate
            panel.applyOpponentMove(round, 2, 5, 5); // opponent cannot play white
            panel.applyOpponentMove(round, 3, 5, 5); // local turn
            assertEquals(1, board.getMoveCount());
            assertTrue(panel.playMove(6, 6));
            panel.applyOpponentMove(round, 3, 6, 6); assertEquals(2, board.getMoveCount());
        });
    }
    @Test void queuedWireMoveKeepsOldRoundAfterReset() throws Exception {
        Board board = new Board(); BoardPanel[] panel = new BoardPanel[1]; String base = id();
        SwingUtilities.invokeAndWait(() -> { panel[0] = new BoardPanel(board); panel[0].setupOnlineGame(null, false, base); });
        BlockingQueue<Runnable> callbacks = new LinkedBlockingQueue<>();
        NetworkManager manager = new NetworkManager(new NetworkStabilityTest.Events() {
            @Override public void onMoveReceived(String round, int ply, int r, int c) { panel[0].applyOpponentMove(round, ply, r, c); }
        }, callbacks::add);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); manager.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                writer(peer).println("START 2 " + base);
                assertNotNull(callbacks.poll(3, TimeUnit.SECONDS));
                writer(peer).println("MOVE " + base + " 1 4 4");
                Runnable move = callbacks.poll(3, TimeUnit.SECONDS); assertNotNull(move);
                SwingUtilities.invokeAndWait(() -> { panel[0].startOnlineRound(base, id()); move.run(); assertEquals(0, board.getMoveCount()); });
            }
        } finally { manager.disconnect(); }
    }
    @Test void simultaneousSameBaseRestartAndStaleRequestResetOnce() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events(); NetworkManager host = new NetworkManager(events);
        try {
            host.hostGame(0); Integer port = events.ports.poll(3, TimeUnit.SECONDS); assertNotNull(port);
            try (Socket client = new Socket("127.0.0.1", port)) {
                client.setSoTimeout(3000); BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String base = in.readLine().split(" ")[2]; assertEquals("connected:true", events.next());
                assertTrue(host.requestRestart(base));
                writer(client).println("RESTART_REQUEST " + base);
                assertTrue(in.readLine().startsWith("RESTART " + base + " "));
                assertEquals("restart", events.next());
                assertFalse(host.requestRestart(base));
                writer(client).println("RESTART_REQUEST " + base);
                // A valid subsequent MOVE is a FIFO barrier after both stale requests.
                writer(client).println("MOVE " + host.getRoundId() + " 2 3 3");
                assertEquals("move:3:3", events.next()); assertTrue(events.events.isEmpty());
            }
        } finally { host.disconnect(); }
    }
    @Test void clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events(); NetworkManager client = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); client.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                peer.setSoTimeout(150); BufferedReader in = new BufferedReader(new InputStreamReader(peer.getInputStream()));
                String base = id(); writer(peer).println("START 2 " + base); assertEquals("connected:false", events.next());
                assertTrue(client.requestRestart(base)); assertFalse(client.requestRestart(base));
                assertEquals("RESTART_REQUEST " + base, in.readLine());
                assertThrows(SocketTimeoutException.class, in::readLine);
                writer(peer).println("RESTART " + id() + " " + id());
                writer(peer).println("MOVE " + base + " 1 1 1");
                assertEquals("move:1:1", events.next()); assertEquals(base, client.getRoundId()); assertTrue(client.isWaitingForRestart());
                String next = id(); writer(peer).println("RESTART " + base + " " + next);
                assertEquals("restart", events.next()); assertEquals(next, client.getRoundId()); assertFalse(client.isWaitingForRestart());
                assertTrue(client.requestRestart(next));
            }
            assertEquals("disconnected", events.next()); assertFalse(client.isWaitingForRestart());
        } finally { client.disconnect(); }
    }
    @Test void startTimeoutClosesSession() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events(); NetworkManager client = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); client.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                assertEquals("error:START_TIMEOUT", events.events.poll(7, TimeUnit.SECONDS));
                assertFalse(client.isConnected()); peer.setSoTimeout(3000); assertEquals(-1, peer.getInputStream().read());
            }
        } finally { client.disconnect(); }
    }
    @Test void mismatchedVersionClosesSession() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events(); NetworkManager client = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); client.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                writer(peer).println("START 99 " + id()); assertEquals("error:VERSION", events.next()); assertFalse(client.isConnected());
            }
        } finally { client.disconnect(); }
    }
    @Test void garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect() throws Exception {
        NetworkStabilityTest.Events events = new NetworkStabilityTest.Events(); NetworkManager client = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000); client.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                String round = id(); PrintWriter out = writer(peer); out.println("START 2 " + round);
                assertEquals("connected:false", events.next());
                for (String line : new String[]{"DISCONNECT", "garbage", "MOVE", "MOVE " + round + " 1 2", "MOVE " + round + " a 1 2",
                        "MOVE " + round + " 2 1 2", "MOVE " + round + " 1 1 2 extra", "X".repeat(10000)}) out.println(line);
                out.println("MOVE " + round + " 1 7 7");
                assertEquals("move:7:7", events.next()); assertTrue(client.isConnected()); assertTrue(events.events.isEmpty());
            }
        } finally { client.disconnect(); }
    }
    @Test void waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(new AppSettings(new P2LifecycleTest.MemoryPreferences()));
            try {
                BoardPanel panel = frame.getBoardPanel(); String base = id(), next = id();
                panel.setupOnlineGame(null, true, base); panel.setRestartWaiting(true);
                assertFalse(panel.playMove(7, 7)); panel.setRestartWaiting(false);
                int oldGameId = panel.getGameId();
                for (int c = 0; c < 4; c++) panel.executeMoveForTest(7, c, Piece.X);
                NetworkManager manager = P2LifecycleTest.field(frame, "networkManager");
                NetworkManager.NetworkListener networkListener = P2LifecycleTest.field(manager, "listener");
                SwingUtilities.invokeLater(() -> {
                    JDialog popup = P2LifecycleTest.field(frame, "resultDialog"); assertNotNull(popup); assertTrue(popup.isVisible());
                    networkListener.onRoundStarted(base, next);
                    assertFalse(popup.isDisplayable());
                });
                panel.executeMoveForTest(7, 4, Piece.X);
                assertFalse(panel.isGameOver()); assertEquals(next, panel.getNetworkRoundId()); assertTrue(panel.getMoveHistory().isEmpty());
                int wins = frame.getScoreTracker().getHumanWins();
                BoardPanel.GameStateListener state = P2LifecycleTest.field(panel, "stateListener");
                state.onGameOver(oldGameId, "stale", Piece.X);
                assertEquals(wins, frame.getScoreTracker().getHumanWins()); assertNull(P2LifecycleTest.field(frame, "resultDialog"));
            } finally { frame.dispose(); }
        });
    }
}
