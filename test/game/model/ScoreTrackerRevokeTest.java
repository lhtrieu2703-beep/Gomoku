package game.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class ScoreTrackerRevokeTest {
    @ParameterizedTest
    @CsvSource({"X,X", "O,X", "NONE,X", "X,O", "O,O", "NONE,O"})
    void revokingEachResultRestoresPreviousTotals(Piece winner, Piece human) {
        ScoreTracker scores = new ScoreTracker();
        scores.recordResult(1, Piece.X); scores.recordResult(2, Piece.O); scores.recordResult(3, Piece.NONE);
        assertTrue(scores.recordResult(4, winner, human));
        assertTrue(scores.revokeResult(4));
        totals(scores, 1, 1, 1);
        assertFalse(scores.isGameRecorded(4));
    }

    @Test void revokingUnknownIdLeavesExistingResultsUnchanged() {
        ScoreTracker scores = new ScoreTracker(); scores.recordResult(1, Piece.X);
        assertFalse(scores.revokeResult(99));
        totals(scores, 1, 0, 0); assertTrue(scores.isGameRecorded(1));
    }

    @ParameterizedTest @EnumSource(value = Piece.class, names = {"X", "O"})
    void humanWinThenRevokeThenOpponentWinReplacesResult(Piece human) {
        ScoreTracker scores = new ScoreTracker();
        assertTrue(scores.recordResult(1, human, human));
        assertTrue(scores.revokeResult(1));
        assertTrue(scores.recordResult(1, human.opposite(), human));
        totals(scores, 0, 1, 0);
    }

    @ParameterizedTest @EnumSource(value = Piece.class, names = {"X", "O"})
    void drawThenRevokeThenWinRemovesDraw(Piece human) {
        ScoreTracker scores = new ScoreTracker(); scores.recordResult(1, Piece.NONE, human);
        assertTrue(scores.revokeResult(1));
        assertTrue(scores.recordResult(1, human, human));
        totals(scores, 1, 0, 0);
    }

    @Test void duplicateIdIsRejectedWithoutRevoke() {
        ScoreTracker scores = new ScoreTracker(); assertTrue(scores.recordResult(1, Piece.X));
        assertFalse(scores.recordResult(1, Piece.O));
        totals(scores, 1, 0, 0);
    }

    @Test void revokingOneGamePreservesOtherGames() {
        ScoreTracker scores = new ScoreTracker();
        scores.recordResult(1, Piece.X); scores.recordResult(2, Piece.X);
        scores.recordResult(3, Piece.O); scores.recordResult(4, Piece.NONE);
        assertTrue(scores.revokeResult(1));
        totals(scores, 1, 1, 1);
        assertTrue(scores.isGameRecorded(2)); assertTrue(scores.isGameRecorded(3)); assertTrue(scores.isGameRecorded(4));
    }

    @Test void revokingResultClearedByResetDoesNotSubtractAnotherGame() {
        ScoreTracker scores = new ScoreTracker(); scores.recordResult(1, Piece.X);
        scores.resetScores(); assertFalse(scores.recordResult(1, Piece.O));
        scores.recordResult(2, Piece.X);
        assertTrue(scores.revokeResult(1));
        totals(scores, 1, 0, 0);
        assertTrue(scores.recordResult(1, Piece.O)); totals(scores, 1, 1, 0);
    }

    @Test void repeatedRevokeDoesNotSubtractTwice() {
        ScoreTracker scores = new ScoreTracker(); scores.recordResult(1, Piece.NONE);
        assertTrue(scores.revokeResult(1)); assertFalse(scores.revokeResult(1));
        totals(scores, 0, 0, 0);
    }

    private static void totals(ScoreTracker scores, int human, int ai, int draws) {
        assertAll(() -> assertEquals(human, scores.getHumanWins()),
                () -> assertEquals(ai, scores.getAiWins()), () -> assertEquals(draws, scores.getDraws()));
    }
}
