package game;

import game.model.Board;
import game.model.Board.Cell;
import game.model.Piece;
import game.view.BoardPanel;
import game.view.SoundManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinningLineTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "horizontal, 7, 5, 0, 1",
            "vertical, 5, 7, 1, 0",
            "main diagonal, 5, 5, 1, 1",
            "anti-diagonal, 5, 9, 1, -1"
    })
    void returnsFiveCellsInEachDirection(String direction, int row, int col, int dr, int dc) {
        Board board = new Board();
        List<Cell> expected = line(row, col, dr, dc, 5);
        placeStones(board, expected, Piece.O);
        Cell last = expected.get(2);
        assertTrue(board.checkWin(last.row(), last.col()));
        assertEquals(expected, board.findWinningLine(last.row(), last.col()));
    }

    @Test
    void ruleBReturnsTheWholeOverlineWithOneBlockedEnd() {
        Board board = new Board();
        List<Cell> expected = line(7, 3, 0, 1, 7);
        placeStones(board, expected, Piece.X);
        assertTrue(board.makeMove(7, 2, Piece.O));

        assertTrue(board.checkWin(7, 6));
        assertEquals(expected, board.findWinningLine(7, 6));
    }

    @Test
    void ruleBRejectsAnOverlineBlockedAtBothEnds() {
        Board board = new Board();
        placeStones(board, line(7, 3, 0, 1, 6), Piece.X);
        assertTrue(board.makeMove(7, 2, Piece.O));
        assertTrue(board.makeMove(7, 9, Piece.O));

        assertFalse(board.checkWin(7, 6));
        assertTrue(board.findWinningLine(7, 6).isEmpty());
    }

    @Test
    void ruleAReturnsExactlyFiveButRejectsSix() {
        Board board = new Board();
        board.setWinRule(Board.WinRule.RULE_A);
        List<Cell> expected = line(7, 3, 0, 1, 5);
        placeStones(board, expected, Piece.X);
        assertEquals(expected, board.findWinningLine(7, 6));

        assertTrue(board.makeMove(7, 8, Piece.X));
        assertFalse(board.checkWin(7, 6));
        assertTrue(board.findWinningLine(7, 6).isEmpty());
    }

    @Test
    void simultaneousWinsUseTheFirstDirectionInExistingScanOrder() {
        Board board = new Board();
        List<Cell> horizontal = line(7, 5, 0, 1, 5);
        placeStones(board, horizontal, Piece.X);
        placeStones(board, List.of(new Cell(5, 7), new Cell(6, 7), new Cell(8, 7), new Cell(9, 7)), Piece.X);

        assertEquals(horizontal, board.findWinningLine(7, 7));
    }

    @Test
    void undoClearsHighlightAndRestoresTheWinningTurn() throws Exception {
        withPanel((board, panel) -> {
            winHorizontally(panel);
            assertTrue(panel.canUndo());

            panel.undoMove();

            assertTrue(panel.getWinningLine().isEmpty());
            assertFalse(panel.isGameOver());
            assertEquals(Piece.X, panel.getCurrentTurn());
            assertEquals(Piece.NONE, board.getPiece(7, 9));
        });
    }

    @Test
    void newGameClearsHighlight() throws Exception {
        withPanel((board, panel) -> {
            winHorizontally(panel);
            panel.resetGame();
            assertTrue(panel.getWinningLine().isEmpty());
            assertFalse(panel.isGameOver());
            assertEquals(0, board.getMoveCount());
        });
    }

    @Test
    void returningToMenuClearsHighlight() throws Exception {
        withPanel((board, panel) -> {
            winHorizontally(panel);
            panel.resetForMenu();
            assertTrue(panel.getWinningLine().isEmpty());
            assertFalse(panel.isEnabled());
            assertEquals(0, board.getMoveCount());
        });
    }

    @Test
    void aFullBoardDrawHasNoHighlight() throws Exception {
        withPanel((board, panel) -> {
            List<Cell> black = new ArrayList<>();
            List<Cell> white = new ArrayList<>();
            // Alternate XXOO with a two-column shift on odd rows, so no direction reaches five.
            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    int pattern = (col + 2 * (row % 2)) % 4;
                    (pattern < 2 ? black : white).add(new Cell(row, col));
                }
            }
            for (int i = 0; i < white.size(); i++) {
                play(panel, black.get(i));
                play(panel, white.get(i));
            }
            assertFalse(panel.isGameOver());
            assertTrue(panel.getWinningLine().isEmpty());
            play(panel, black.getLast());

            assertTrue(board.isFull());
            assertTrue(panel.isGameOver());
            assertEquals(Piece.NONE, panel.getWinner());
            assertTrue(panel.getWinningLine().isEmpty());
        });
    }

    private static List<Cell> line(int row, int col, int dr, int dc, int size) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cells.add(new Cell(row + i * dr, col + i * dc));
        }
        return cells;
    }

    private static void placeStones(Board board, List<Cell> cells, Piece piece) {
        for (Cell cell : cells) {
            assertTrue(board.makeMove(cell.row(), cell.col(), piece));
        }
    }

    private static void withPanel(BiConsumer<Board, BoardPanel> test) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            boolean soundEnabled = SoundManager.isSoundEnabled();
            SoundManager.setSoundEnabled(false);
            try {
                Board board = new Board();
                BoardPanel panel = new BoardPanel(board);
                panel.setGameMode(BoardPanel.GameMode.PVP);
                test.accept(board, panel);
            } finally {
                SoundManager.setSoundEnabled(soundEnabled);
            }
        });
    }

    private static void winHorizontally(BoardPanel panel) {
        List<Cell> winningCells = line(7, 5, 0, 1, 5);
        for (int i = 0; i < winningCells.size() - 1; i++) {
            play(panel, winningCells.get(i));
            play(panel, new Cell(1, 1 + 2 * i));
        }
        assertTrue(panel.getWinningLine().isEmpty());
        play(panel, winningCells.getLast());

        assertTrue(panel.isGameOver());
        assertEquals(Piece.X, panel.getWinner());
        assertEquals(winningCells, panel.getWinningLine());
    }

    private static void play(BoardPanel panel, Cell cell) {
        assertTrue(panel.playMove(cell.row(), cell.col()), "Legal move at " + cell);
    }
}
