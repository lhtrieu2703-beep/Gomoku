package game.ai;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CancellationException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AlphaBetaAblationTest {
    private static Board fixture(int index) {
        Board board = new Board();
        int[][][] placements = {
                {{7, 7}, {7, 8}, {6, 8}},
                {{0, 0}, {0, 1}, {1, 1}, {1, 2}},
                {{7, 7}, {6, 6}, {8, 8}, {6, 8}, {8, 6}},
                {{7, 7}},
                {{4, 4}, {10, 10}, {4, 6}, {10, 8}, {5, 5}, {9, 9}}
        };
        for (int i = 0; i < placements[index].length; i++)
            assertTrue(board.makeMove(placements[index][i][0], placements[index][i][1], i % 2 == 0 ? Piece.X : Piece.O));
        return board;
    }

    private static void unchanged(Board before, Board after) {
        assertEquals(before.getWinRule(), after.getWinRule());
        assertEquals(before.getMoveCount(), after.getMoveCount());
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            assertEquals(before.getPiece(r, c), after.getPiece(r, c), "cell " + r + "," + c);
    }

    static Stream<Arguments> searchCases() {
        return IntStream.range(0, 5).boxed().flatMap(index -> Stream.of(MinimaxAI.Difficulty.MEDIUM,
                MinimaxAI.Difficulty.HARD).map(level -> Arguments.of(index, level)));
    }

    @ParameterizedTest(name = "fixture={0}, level={1}") @MethodSource("searchCases")
    void pruningPreservesMoveValueAndBoard(int index, MinimaxAI.Difficulty level) {
        Board board = fixture(index), before = board.copy();
        Piece turn = board.getMoveCount() % 2 == 0 ? Piece.X : Piece.O;
        MinimaxAI ai = new MinimaxAI(turn, level);
        var on = ai.searchOnlyForMeasurement(board, level, true);
        unchanged(before, board);
        var off = ai.searchOnlyForMeasurement(board, level, false);
        unchanged(before, board);
        assertNotNull(on.move()); assertNotNull(off.move());
        assertNotNull(on.rootValue());
        assertEquals(on.move().row, off.move().row);
        assertEquals(on.move().col, off.move().col);
        assertEquals(on.rootValue(), off.rootValue());
        assertTrue(on.minimaxNodes() > 0);
        assertTrue(off.minimaxNodes() >= on.minimaxNodes());
        assertEquals(0, off.pruningCount());
        assertTrue(ai.isPruningEnabled(), "Measurement must restore the engine configuration");
    }

    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void gameDefaultsToPruningEnabled(MinimaxAI.Difficulty level) {
        assertTrue(new MinimaxAI(Piece.O, level).isPruningEnabled());
    }

    @ParameterizedTest @EnumSource(value = MinimaxAI.Difficulty.class, names = {"MEDIUM", "HARD"})
    void fullModeReportsNoRootValueForTacticalShortcut(MinimaxAI.Difficulty level) {
        Board board = new Board();
        assertTrue(board.makeMove(7, 3, Piece.X));
        for (int c = 4; c < 8; c++) assertTrue(board.makeMove(7, c, Piece.O));
        Board before = board.copy();
        MinimaxAI ai = new MinimaxAI(Piece.O, level);
        // Force a previous non-null measurement to catch stale full-mode values.
        assertNotNull(ai.searchOnlyForMeasurement(fixture(0), level, true).rootValue());
        for (boolean pruning : new boolean[]{true, false}) {
            ai.setPruningEnabled(pruning);
            Move result = ai.findBestMove(board);
            assertNotNull(result);
            assertEquals(7, result.row); assertEquals(8, result.col);
            assertNull(ai.getLastRootValue());
            assertEquals(0, ai.getNodesEvaluated());
            assertEquals(0, ai.getPruningCount());
            unchanged(before, board);
        }
        var search = ai.searchOnlyForMeasurement(board, level, false);
        assertTrue(search.minimaxNodes() > 0, "search-only must bypass win-now shortcut");
        assertNotNull(search.rootValue());
        assertEquals(0, search.pruningCount());
        unchanged(before, board);
    }

    @Test void noCandidatesHasNoInventedRootValue() {
        MinimaxAI ai = new MinimaxAI(Piece.X, MinimaxAI.Difficulty.MEDIUM);
        Board full = new Board();
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            assertTrue(full.makeMove(r, c, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O));
        for (Board board : new Board[]{new Board(), full}) for (boolean pruning : new boolean[]{true, false}) {
            Board before = board.copy();
            var result = ai.searchOnlyForMeasurement(board, MinimaxAI.Difficulty.MEDIUM, pruning);
            assertNull(result.move()); assertNull(result.rootValue());
            assertEquals(0, result.minimaxNodes()); assertEquals(0, result.pruningCount());
            unchanged(before, board);
        }
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void cancellationUnwindsTrialStonesWithEitherPruningFlag(boolean pruning) {
        Board board = new Board() {
            @Override public boolean checkWin(int row, int col) {
                // Called inside minimax after placing an opponent trial stone.
                Thread.currentThread().interrupt();
                return super.checkWin(row, col);
            }
        };
        assertTrue(board.makeMove(7, 7, Piece.X));
        Board before = board.copy();
        MinimaxAI ai = new MinimaxAI(Piece.O, MinimaxAI.Difficulty.HARD);
        try {
            assertThrows(CancellationException.class,
                    () -> ai.searchOnlyForMeasurement(board, MinimaxAI.Difficulty.HARD, pruning));
        } finally {
            Thread.interrupted();
        }
        unchanged(before, board);
        assertTrue(ai.isPruningEnabled());
        if (!pruning) assertEquals(0, ai.getPruningCount());
    }

    @Test void searchOnlyRejectsEasy() {
        MinimaxAI ai = new MinimaxAI(Piece.X, MinimaxAI.Difficulty.EASY);
        assertThrows(IllegalArgumentException.class,
                () -> ai.searchOnlyForMeasurement(fixture(0), MinimaxAI.Difficulty.EASY, false));
        assertTrue(ai.isPruningEnabled());
    }
}
