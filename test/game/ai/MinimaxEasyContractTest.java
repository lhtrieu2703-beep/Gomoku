package game.ai;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MinimaxEasyContractTest {
    @Test
    void easyChoosesMoveWithoutEvaluatingMinimaxNodes() throws ReflectiveOperationException {
        Board board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        assertTrue(board.makeMove(7, 7, Piece.X));
        assertTrue(board.makeMove(7, 8, Piece.O));
        MinimaxAI ai = new MinimaxAI(Piece.X, MinimaxAI.Difficulty.EASY);
        // The existing API has no seed parameter. Seed the actual Random field in test only.
        Field randomField = MinimaxAI.class.getDeclaredField("random");
        randomField.setAccessible(true);
        ((Random) randomField.get(ai)).setSeed(20261103L);

        Move result = ai.findBestMove(board);

        assertNotNull(result);
        assertEquals(0L, ai.getNodesEvaluated(), "EASY must not enter minimax");
        assertEquals(0L, ai.getPruningCount(), "EASY must not perform alpha-beta pruning");
    }
}
