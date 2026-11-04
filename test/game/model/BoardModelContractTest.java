package game.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BoardModelContractTest {
    @ParameterizedTest(name = "row={0}, col={1}")
    @CsvSource({"-1,7", "15,7", "7,-1", "7,15"})
    void outOfBoundsPlacementIsRejectedWithoutChangingBoard(int row, int col) {
        Board board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        assertTrue(board.makeMove(7, 7, Piece.X));
        Board before = board.copy();

        assertFalse(board.makeMove(row, col, Piece.O));

        assertEquals(before.getMoveCount(), board.getMoveCount());
        assertEquals(before.getWinRule(), board.getWinRule());
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                assertEquals(before.getPiece(r, c), board.getPiece(r, c),
                        "Unexpected mutation at " + r + "," + c);
            }
        }
    }

    @Test
    void undoRemovesOnlyRequestedStoneAndDecrementsMoveCount() {
        Board board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        assertTrue(board.makeMove(7, 7, Piece.X));
        assertTrue(board.makeMove(7, 8, Piece.O));
        Board before = board.copy();

        board.undoMove(7, 8);

        assertEquals(before.getMoveCount() - 1, board.getMoveCount());
        assertEquals(before.getWinRule(), board.getWinRule());
        assertTrue(board.isValidMove(7, 8));
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece expected = r == 7 && c == 8 ? Piece.NONE : before.getPiece(r, c);
                assertEquals(expected, board.getPiece(r, c),
                        "Unexpected mutation at " + r + "," + c);
            }
        }
    }
}
