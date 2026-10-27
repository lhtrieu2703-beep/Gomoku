package game;

import game.ai.HeuristicEvaluator;
import game.model.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.*;

class F3HeuristicTest {
    static Stream<Arguments> rulesAndColors() {
        return Stream.of(Board.WinRule.values()).flatMap(rule -> Stream.of(Piece.X, Piece.O).map(p -> Arguments.of(rule, p)));
    }
    static Board board(Board.WinRule rule) { Board b = new Board(); b.setWinRule(rule); return b; }
    static void row(Board b, Piece p, int r, int from, int count) { for (int c = from; c < from + count; c++) b.makeMove(r, c, p); }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void blockedSixIsNotFive(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 7, 3, 6); b.makeMove(7, 2, p.opposite()); b.makeMove(7, 9, p.opposite());
        assertFalse(b.checkWin(7, 5)); assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(b, p) < HeuristicEvaluator.FIVE);
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void singleThreeIsNotDoubleThree(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 7, 6, 3);
        assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(b, p) < HeuristicEvaluator.DOUBLE_THREE);
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void actualCrossDoubleThreeIsRecognized(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 7, 6, 3); b.makeMove(6, 7, p); b.makeMove(8, 7, p);
        int score = HeuristicEvaluator.evaluatePlayerAdvanced(b, p);
        assertTrue(score >= HeuristicEvaluator.DOUBLE_THREE); assertTrue(score < HeuristicEvaluator.FIVE);
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void openFourIsThreatButNotTerminal(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 7, 5, 4);
        int score = HeuristicEvaluator.evaluatePlayerAdvanced(b, p);
        assertTrue(score >= HeuristicEvaluator.OPEN_FOUR); assertTrue(score < HeuristicEvaluator.FIVE);
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void edgeFiveMatchesBoardAndDominatesOtherThreats(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 0, 0, 5); b.makeMove(0, 5, p.opposite());
        assertTrue(b.checkWin(0, 0)); assertEquals(HeuristicEvaluator.FIVE, HeuristicEvaluator.evaluatePlayerAdvanced(b, p));
        assertEquals(HeuristicEvaluator.FIVE, HeuristicEvaluator.evaluateBoardAdvanced(b, p));
        assertEquals(-HeuristicEvaluator.FIVE, HeuristicEvaluator.evaluateBoardAdvanced(b, p.opposite()));
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void openSixMatchesSelectedRule(Board.WinRule rule, Piece p) {
        Board b = board(rule); row(b, p, 7, 3, 6);
        assertEquals(b.checkWin(7, 5), HeuristicEvaluator.evaluatePlayerAdvanced(b, p) == HeuristicEvaluator.FIVE);
    }
    @ParameterizedTest @MethodSource("rulesAndColors")
    void nonTerminalThreatsNeverOutrankWin(Board.WinRule rule, Piece p) {
        Board b = board(rule);
        for (int r : new int[]{1, 4, 7, 10, 13}) row(b, p, r, 5, 4);
        assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(b, p) < HeuristicEvaluator.FIVE);
        assertTrue(Math.abs(HeuristicEvaluator.evaluateBoardAdvanced(b, p)) < HeuristicEvaluator.FIVE);
    }
}
