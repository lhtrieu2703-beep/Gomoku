package game;

import game.model.Board;
import game.model.Piece;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;

import static org.junit.jupiter.api.Assertions.*;

/** Regression tests run Swing on the EDT and never access production Preferences. */
class P1RegressionTest {
    private Lang.Language originalLanguage;
    private boolean originalSound;

    @BeforeEach
    void setup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            originalLanguage = Lang.get();
            originalSound = SoundManager.isSoundEnabled();
            SoundManager.setSoundEnabled(false);
        });
    }

    @AfterEach
    void restore() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Lang.set(originalLanguage);
            SoundManager.setSoundEnabled(originalSound);
        });
    }

    @ParameterizedTest
    @EnumSource(BoardPanel.GameMode.class)
    void languageChangePreservesActiveGame(BoardPanel.GameMode mode) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(new AppSettings(new MemoryPreferences()));
            try {
                BoardPanel panel = frame.getBoardPanel();
                if (mode == BoardPanel.GameMode.ONLINE) panel.setupOnlineGame(null, true);
                else panel.setGameMode(mode);
                assertTrue(panel.playMove(7, 7));
                int gameId = panel.getGameId();
                boolean thinking = panel.isAiThinking();
                boolean canUndo = panel.canUndo();
                frame.getScoreTracker().recordResult(gameId - 1, Piece.X);
                for (Lang.Language language : Lang.Language.values()) {
                    Lang.set(language);
                    assertEquals(gameId, panel.getGameId());
                    assertEquals(1, panel.getMoveHistory().size());
                    assertEquals(Piece.O, panel.getCurrentTurn());
                    assertEquals(mode, panel.getGameMode());
                    assertEquals(thinking, panel.isAiThinking());
                    assertEquals(canUndo, panel.canUndo());
                    assertEquals(7, panel.getLastMoveRow());
                    assertEquals(7, panel.getLastMoveCol());
                    assertFalse(panel.isGameOver());
                    assertEquals(1, frame.getScoreTracker().getHumanWins());
                    assertEquals(Lang.mode(), frame.getLblMode().getText());
                    assertEquals(mode == BoardPanel.GameMode.ONLINE ? Lang.onlineOpponentTurn()
                            : mode == BoardPanel.GameMode.PV_AI ? Lang.aiThinking() : Lang.p2Turn(),
                            statusText(frame));
                }
            } finally {
                frame.getBoardPanel().resetGame(); // cancel a possible AI worker
                frame.dispose();
            }
        });
    }

    @ParameterizedTest
    @EnumSource(Piece.class)
    void languageChangePreservesFinishedGameAndTranslatesResult(Piece winner) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(new AppSettings(new MemoryPreferences()));
            try {
                BoardPanel panel = frame.getBoardPanel();
                panel.setGameMode(BoardPanel.GameMode.PVP);
                panel.setGameStateListener(null); // suppress the modal result popup in this test
                if (winner == Piece.NONE) {
                    for (int r = 0; r < Board.SIZE; r++) {
                        for (int c = 0; c < Board.SIZE; c++) {
                            int phase = r % 2 == 0 ? 0 : 2;
                            panel.executeMoveForTest(r, c, (c + phase) % 4 < 2 ? Piece.X : Piece.O);
                        }
                    }
                } else {
                    for (int c = 0; c < 5; c++) panel.executeMoveForTest(7, c, winner);
                }
                assertTrue(panel.isGameOver());
                int gameId = panel.getGameId();
                int history = panel.getMoveHistory().size();
                for (Lang.Language language : Lang.Language.values()) {
                    Lang.set(language);
                    assertTrue(panel.isGameOver());
                    assertEquals(winner, panel.getWinner());
                    assertEquals(gameId, panel.getGameId());
                    assertEquals(history, panel.getMoveHistory().size());
                    String expected = winner == Piece.NONE ? Lang.drawGame()
                            : winner == Piece.X ? Lang.p1Wins() : Lang.p2Wins();
                    assertEquals(expected, panel.getGameOverMessage());
                    assertEquals(expected, statusText(frame));
                }
            } finally {
                frame.dispose();
            }
        });
    }

    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void pvpUndoAfterWinRestoresWinningPlayerTurn(Piece winner) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Board board = new Board();
            BoardPanel panel = new BoardPanel(board);
            panel.setGameMode(BoardPanel.GameMode.PVP);
            int gameId = panel.getGameId();
            for (int c = 0; c < 4; c++) {
                assertTrue(panel.playMove(winner == Piece.X ? 7 : 0, winner == Piece.X ? c : c * 2));
                assertTrue(panel.playMove(winner == Piece.X ? 0 : 7, winner == Piece.X ? c * 2 : c));
            }
            if (winner == Piece.O) assertTrue(panel.playMove(2, 10));
            assertTrue(panel.playMove(7, 4));
            assertTrue(panel.isGameOver());
            int history = panel.getMoveHistory().size();
            panel.undoMove();
            assertEquals(winner, panel.getCurrentTurn());
            assertFalse(panel.isGameOver());
            assertEquals(Piece.NONE, panel.getWinner());
            assertNull(panel.getGameOverMessage());
            assertEquals(Piece.NONE, board.getPiece(7, 4));
            assertEquals(history - 1, panel.getMoveHistory().size());
            assertEquals(gameId, panel.getGameId());
            assertTrue(panel.playMove(7, 4));
            assertEquals(winner, board.getPiece(7, 4));
            assertTrue(panel.isGameOver());
        });
    }

    @Test
    void pvpUndoAfterDrawRestoresLastPlayerTurn() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Board board = new Board();
            BoardPanel panel = new BoardPanel(board);
            panel.setGameMode(BoardPanel.GameMode.PVP);
            // Draw fixture: no five-in-a-row; final square is X.
            for (int r = 0; r < Board.SIZE; r++) {
                for (int c = 0; c < Board.SIZE; c++) {
                    if (r == 14 && c == 14) continue;
                    int phase = r % 2 == 0 ? 0 : 2;
                    int p = (c + phase) % 4;
                    board.makeMove(r, c, p < 2 ? Piece.X : Piece.O);
                }
            }
            assertTrue(panel.playMove(14, 14));
            assertTrue(panel.isGameOver());
            panel.undoMove();
            assertFalse(panel.isGameOver());
            assertEquals(Piece.X, panel.getCurrentTurn());
            assertEquals(224, board.getMoveCount());
        });
    }

    @Test
    void onlineRejectsOpponentMoveOnLocalTurn() throws Exception {
        Board board = new Board();
        BoardPanel[] panel = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BoardPanel(board);
            panel[0].setupOnlineGame(null, true);
            panel[0].applyOpponentMove(5, 5);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(Piece.NONE, board.getPiece(5, 5));
            assertTrue(panel[0].getMoveHistory().isEmpty());
            assertEquals(Piece.X, panel[0].getCurrentTurn());
        });
    }

    @ParameterizedTest
    @EnumSource(value = BoardPanel.GameMode.class, names = {"PVP", "PV_AI"})
    void onlineRejectsMoveAfterSwitchingMode(BoardPanel.GameMode mode) throws Exception {
        Board board = new Board();
        BoardPanel[] panel = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BoardPanel(board);
            panel[0].setupOnlineGame(null, false);
            panel[0].setGameMode(mode);
            panel[0].applyOpponentMove(5, 5);
        });
        SwingUtilities.invokeAndWait(() -> assertEquals(0, board.getMoveCount()));
    }

    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void onlineAcceptsOnlyOneMoveFromCorrectOpponent(Piece localPiece) throws Exception {
        Board board = new Board();
        BoardPanel[] panel = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BoardPanel(board);
            panel[0].setupOnlineGame(null, localPiece == Piece.X);
            if (localPiece == Piece.X) assertTrue(panel[0].playMove(7, 7));
            panel[0].applyOpponentMove(5, 5);
            panel[0].applyOpponentMove(6, 6);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(localPiece.opposite(), board.getPiece(5, 5));
            assertEquals(Piece.NONE, board.getPiece(6, 6));
            assertEquals(localPiece, panel[0].getCurrentTurn());
            assertEquals(localPiece == Piece.X ? 2 : 1, board.getMoveCount());
        });
    }

    @Test
    void onlineRejectsQueuedMoveAfterNewGame() throws Exception {
        Board board = new Board();
        BoardPanel[] panel = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BoardPanel(board);
            panel[0].setupOnlineGame(null, false);
            // Receive on a socket thread while the EDT is busy with a new game.
            Thread receiver = new Thread(() -> panel[0].applyOpponentMove(5, 5));
            receiver.start();
            try {
                receiver.join(1000);
                assertFalse(receiver.isAlive());
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            panel[0].resetGame();
        });
        SwingUtilities.invokeAndWait(() -> assertEquals(0, board.getMoveCount()));
    }

    @Test
    void onlineRejectsInvalidSquaresAndMovesAfterGameOver() throws Exception {
        Board board = new Board();
        BoardPanel[] panel = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BoardPanel(board);
            panel[0].setupOnlineGame(null, true);
            assertTrue(panel[0].playMove(7, 7));
            panel[0].applyOpponentMove(-1, 5);
            panel[0].applyOpponentMove(15, 5);
            panel[0].applyOpponentMove(7, 7);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(1, board.getMoveCount());
            assertEquals(Piece.O, panel[0].getCurrentTurn());
            for (int c = 0; c < 5; c++) panel[0].executeMoveForTest(0, c, Piece.X);
            assertTrue(panel[0].isGameOver());
            panel[0].applyOpponentMove(5, 5);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(6, board.getMoveCount());
            assertEquals(Piece.NONE, board.getPiece(5, 5));
        });
    }

    private static String statusText(MainFrame frame) {
        try {
            var field = MainFrame.class.getDeclaredField("turnLabel");
            field.setAccessible(true);
            return ((JLabel) field.get(frame)).getText();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static final class MemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        MemoryPreferences() { super(null, ""); values.put("soundEnabled", "false"); }
        protected void putSpi(String key, String value) { values.put(key, value); }
        protected String getSpi(String key) { return values.get(key); }
        protected void removeSpi(String key) { values.remove(key); }
        protected void removeNodeSpi() { values.clear(); }
        protected String[] keysSpi() { return values.keySet().toArray(new String[0]); }
        protected String[] childrenNamesSpi() { return new String[0]; }
        protected AbstractPreferences childSpi(String name) { throw new UnsupportedOperationException(); }
        protected void syncSpi() {}
        protected void flushSpi() {}
    }
}
