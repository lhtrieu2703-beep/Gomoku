package game.ai;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RULE_B fixtures fixed before observing search results. Coordinates are zero-based (row,col).
 * These are isolated tactical positions, not alternating-move replays: stone-count parity is
 * deliberately not a fixture requirement. Every input is checked to contain no existing win.
 * No seed injection, reflection, disabled tests, or production changes are used.
 */
class AITacticsTest {
    record Cell(int row, int col) {}

    static Board board() { Board b = new Board(); b.setWinRule(Board.WinRule.RULE_B); return b; }
    static void put(Board b, Piece p, int r, int c) { assertTrue(b.makeMove(r, c, p)); }
    static void row(Board b, Piece p, int r, int from, int count) {
        for (int c = from; c < from + count; c++) put(b, p, r, c);
    }
    static void noExistingWin(Board b) {
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            assertFalse(b.checkWin(r, c), "Invalid fixture: existing win at " + r + "," + c);
    }
    static String ascii(Board b) {
        StringBuilder out = new StringBuilder("    012345678901234\n");
        for (int r = 0; r < Board.SIZE; r++) {
            out.append(String.format("%02d  ", r));
            for (int c = 0; c < Board.SIZE; c++) out.append(switch (b.getPiece(r, c)) {
                case X -> 'X'; case O -> 'O'; case NONE -> '.';
            });
            out.append('\n');
        }
        return out.toString();
    }
    static void unchanged(Board before, Board after) {
        assertEquals(before.getWinRule(), after.getWinRule());
        assertEquals(before.getMoveCount(), after.getMoveCount());
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            assertEquals(before.getPiece(r, c), after.getPiece(r, c), "Mutated cell " + r + "," + c);
    }
    static Move search(String id, Board b, Piece ai, MinimaxAI.Difficulty level, String expected) {
        noExistingWin(b);
        Board before = b.copy();
        Move result = new MinimaxAI(ai, level).findBestMove(b);
        System.out.printf("TACTIC %s level=%s ai=%s expected=%s actual=%s%n%s",
                id, level.name(), ai.name(), expected, result, ascii(before));
        unchanged(before, b);
        assertNotNull(result, id + ": expected a legal move");
        assertTrue(b.isValidMove(result.row, result.col), id + ": returned occupied/out-of-bounds cell");
        return result;
    }
    static void expect(Move m, Set<Cell> allowed) {
        assertTrue(allowed.contains(new Cell(m.row, m.col)),
                () -> "Expected " + allowed + "; actual " + m + ". Keep this failure as a finding.");
    }

    // r7: ...XOOOO....... ; only (7,8) completes an unblocked five.
    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void winsImmediatelyWithOneOpenEnd(MinimaxAI.Difficulty level) {
        Board b = board(); row(b, Piece.O, 7, 4, 4); put(b, Piece.X, 7, 3);
        Move m = search("win-now", b, Piece.O, level, "(7,8): unblocked five");
        expect(m, Set.of(new Cell(7, 8)));
        put(b, Piece.O, m.row, m.col); assertTrue(b.checkWin(m.row, m.col));
    }

    // r7: ...OXXXX....... ; only (7,8) prevents X winning next move.
    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void blocksImmediateLossWithOneOpenEnd(MinimaxAI.Difficulty level) {
        Board b = board(); row(b, Piece.X, 7, 4, 4); put(b, Piece.O, 7, 3);
        expect(search("block-now", b, Piece.O, level, "(7,8): unique forced block"), Set.of(new Cell(7, 8)));
    }

    // r4: ...XOOOO....... and r10: ...OXXXX....... ; winning now ends the game.
    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void takesWinInsteadOfBlockingOpponentWin(MinimaxAI.Difficulty level) {
        Board b = board(); row(b, Piece.O, 4, 4, 4); put(b, Piece.X, 4, 3);
        row(b, Piece.X, 10, 4, 4); put(b, Piece.O, 10, 3);
        expect(search("win-over-block", b, Piece.O, level, "(4,8): win, rather than (10,8) block"), Set.of(new Cell(4, 8)));
    }

    // r7: ......XXX...... ; occupy either adjacent endpoint, preventing an open four.
    // (7,5) closes the left endpoint; (7,9) closes the right endpoint. More distant
    // cells leave a four with two empty ends available and are not accepted answers.
    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void blocksOpponentOpenThree(MinimaxAI.Difficulty level) {
        Board b = board(); row(b, Piece.X, 7, 6, 3); put(b, Piece.O, 1, 1);
        expect(search("block-open-three", b, Piece.O, level, "(7,5) or (7,9): close one endpoint"),
                Set.of(new Cell(7, 5), new Cell(7, 9)));
    }

    // r7: ......OOO...... ; (7,5) leaves c4/c9 empty; (7,9) leaves c5/c10 empty.
    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void createsOpenFourWhenSafe(MinimaxAI.Difficulty level) {
        Board b = board(); row(b, Piece.O, 7, 6, 3); put(b, Piece.X, 1, 1);
        expect(search("create-open-four", b, Piece.O, level, "(7,5) or (7,9): four with two empty ends"),
                Set.of(new Cell(7, 5), new Cell(7, 9)));
    }

    static Board cross(Piece p) {
        Board b = board(); put(b, p, 7, 6); put(b, p, 7, 8);
        put(b, p, 6, 7); put(b, p, 8, 7); put(b, p.opposite(), 1, 1); return b;
    }
    // Missing centre (7,7) is the unique intersection of two O.O lines.
    @Test void hardCreatesCrossDoubleThree() {
        expect(search("create-double-three", cross(Piece.O), Piece.O, MinimaxAI.Difficulty.HARD,
                "(7,7): two independent open threes"), Set.of(new Cell(7, 7)));
    }
    @Test void hardPreventsOpponentCrossDoubleThree() {
        expect(search("block-double-three", cross(Piece.X), Piece.O, MinimaxAI.Difficulty.HARD,
                "(7,7): remove opponent's crossing fork"), Set.of(new Cell(7, 7)));
    }

    // r7: ...XOO.OOX..... ; filling (7,6) makes XOOOOOX, NOT a RULE_B win.
    // Independent r2: ...OXXXX....... requires a real block at (2,8).
    // EASY may randomly choose the blocked gap; its API exposes no win classification,
    // so forbidding that legal random move would be a dishonest tactical expectation.
    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void blockedFiveIsNotAWinAndRealThreatRemains(MinimaxAI.Difficulty level) {
        Board b = board(); put(b, Piece.X, 7, 3); put(b, Piece.X, 7, 9);
        for (int c : new int[]{4, 5, 7, 8}) put(b, Piece.O, 7, c);
        row(b, Piece.X, 2, 4, 4); put(b, Piece.O, 2, 3);
        Board probe = b.copy(); put(probe, Piece.O, 7, 6);
        assertFalse(probe.checkWin(7, 6));
        assertTrue(HeuristicEvaluator.evaluatePlayerAdvanced(probe, Piece.O) < HeuristicEvaluator.FIVE);
        Move m = search("blocked-five", b, Piece.O, level,
                level == MinimaxAI.Difficulty.EASY ? "legal move; blocked five is nonterminal" : "(2,8): block actual loss, not false win (7,6)");
        if (level != MinimaxAI.Difficulty.EASY) expect(m, Set.of(new Cell(2, 8)));
    }

    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void completedSearchPreservesEveryCellCountAndRule(MinimaxAI.Difficulty level) {
        Board b = board(); put(b, Piece.X, 7, 7); put(b, Piece.O, 7, 8); put(b, Piece.X, 6, 8);
        search("completed-search-invariance", b, Piece.O, level, "legal move; all cells/count/rule unchanged");
    }

    static Stream<Arguments> nearlyFullCases() {
        return Stream.of(1, 2, 5, 10).flatMap(empty -> Stream.of(Piece.X, Piece.O).map(p -> Arguments.of(empty, p)));
    }
    @ParameterizedTest @MethodSource("nearlyFullCases")
    void easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int empty, Piece ai) {
        Board b = board();
        // Existing rule-suite draw pattern: alternating XXOO / OOXX rows.
        // Each row has runs <=2; vertical/diagonal runs are also shorter than five.
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++) {
            if (r * Board.SIZE + c >= Board.SIZE * Board.SIZE - empty) continue;
            int phase = (c + (r % 2 == 0 ? 0 : 2)) % 4;
            put(b, phase < 2 ? Piece.X : Piece.O, r, c);
        }
        noExistingWin(b); Board before = b.copy();
        MinimaxAI engine = new MinimaxAI(ai, MinimaxAI.Difficulty.EASY);
        for (int trial = 0; trial < 50; trial++) {
            Move m = engine.findBestMove(b); unchanged(before, b);
            assertNotNull(m); assertTrue(b.isValidMove(m.row, m.col), "trial=" + trial + " actual=" + m);
        }
        System.out.printf("TACTIC easy-nearly-full ai=%s empty=%d legal=50/50%n", ai.name(), empty);
    }

    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void emptyBoardReturnsCentreWithoutMutation(MinimaxAI.Difficulty level) {
        Board b = board(); expect(search("empty-board", b, Piece.X, level, "(7,7): centre"), Set.of(new Cell(7, 7)));
    }
    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void fullDrawBoardReturnsNullWithoutMutation(MinimaxAI.Difficulty level) {
        Board b = board();
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            put(b, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O, r, c);
        noExistingWin(b); Board before = b.copy();
        assertNull(new MinimaxAI(Piece.O, level).findBestMove(b)); unchanged(before, b);
        System.out.printf("TACTIC full-draw level=%s actual=null unchanged=true%n", level.name());
    }
}
