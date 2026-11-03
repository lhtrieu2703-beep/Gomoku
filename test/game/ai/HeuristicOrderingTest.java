package game.ai;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicOrderingTest {
    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void winningPositionOutranksOrdinaryPosition(Piece player) {
        Board ordinary = rowPosition(player, 2);
        Board winning = rowPosition(player, Board.WIN_COUNT);
        assertTrue(winning.checkWin(7, 6));
        assertFalse(ordinary.checkWin(7, 6));

        assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(winning, player)
                > HeuristicEvaluator.evaluatePlayerAdvanced(ordinary, player));
        assertTrue(HeuristicEvaluator.evaluateBoardAdvanced(winning, player)
                > HeuristicEvaluator.evaluateBoardAdvanced(ordinary, player));
    }

    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void openFourOutranksOrdinaryPosition(Piece player) {
        Board ordinary = rowPosition(player, 2);
        Board openFour = rowPosition(player, Board.WIN_COUNT - 1);
        assertFalse(openFour.checkWin(7, 6));
        Board nextMove = openFour.copy();
        assertTrue(nextMove.makeMove(7, 10, player));
        assertTrue(nextMove.checkWin(7, 10));

        assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(openFour, player)
                > HeuristicEvaluator.evaluatePlayerAdvanced(ordinary, player));
        assertTrue(HeuristicEvaluator.evaluateBoardAdvanced(openFour, player)
                > HeuristicEvaluator.evaluateBoardAdvanced(ordinary, player));
    }

    private static Board rowPosition(Piece player, int length) {
        Board board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        for (int c = 6; c < 6 + length; c++) {
            assertTrue(board.makeMove(7, c, player));
        }
        return board;
    }
}
