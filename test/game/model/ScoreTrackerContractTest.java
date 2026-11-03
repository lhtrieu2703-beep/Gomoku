package game.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTrackerContractTest {
    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void humanWinIsRecordedForEitherHumanPiece(Piece humanPiece) {
        ScoreTracker scores = new ScoreTracker();

        assertTrue(scores.recordResult(11, humanPiece, humanPiece));

        assertTrue(scores.isGameRecorded(11));
        assertTotals(scores, 1, 0, 0);
    }

    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void opponentWinIsRecordedForEitherHumanPiece(Piece humanPiece) {
        ScoreTracker scores = new ScoreTracker();

        assertTrue(scores.recordResult(12, humanPiece.opposite(), humanPiece));

        assertTrue(scores.isGameRecorded(12));
        assertTotals(scores, 0, 1, 0);
    }

    @ParameterizedTest
    @EnumSource(value = Piece.class, names = {"X", "O"})
    void drawIsRecordedForEitherHumanPiece(Piece humanPiece) {
        ScoreTracker scores = new ScoreTracker();

        assertTrue(scores.recordResult(13, Piece.NONE, humanPiece));

        assertTrue(scores.isGameRecorded(13));
        assertTotals(scores, 0, 0, 1);
    }

    @ParameterizedTest(name = "first={0}, second={1}")
    @CsvSource({"X,O", "O,X", "NONE,X"})
    void duplicateGameIdRejectsSecondResultAndPreservesFirst(Piece first, Piece second) {
        ScoreTracker scores = new ScoreTracker();
        assertTrue(scores.recordResult(14, first));
        int humanWins = scores.getHumanWins();
        int aiWins = scores.getAiWins();
        int draws = scores.getDraws();

        assertFalse(scores.recordResult(14, second));

        assertTotals(scores, humanWins, aiWins, draws);
    }

    @Test
    void resetScoresClearsAllThreeCounters() {
        ScoreTracker scores = new ScoreTracker();
        assertTrue(scores.recordResult(21, Piece.X));
        assertTrue(scores.recordResult(22, Piece.O));
        assertTrue(scores.recordResult(23, Piece.NONE));
        assertTotals(scores, 1, 1, 1);

        scores.resetScores();

        assertTotals(scores, 0, 0, 0);
    }

    private static void assertTotals(ScoreTracker scores, int human, int ai, int draws) {
        assertAll(
                () -> assertEquals(human, scores.getHumanWins()),
                () -> assertEquals(ai, scores.getAiWins()),
                () -> assertEquals(draws, scores.getDraws()));
    }
}
