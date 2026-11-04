# Mã hoàn chỉnh của năm tệp test mới

Các khối dưới đây là nguyên tệp đã được biên dịch và chạy trong lượt ghi log. Đặt đúng đường dẫn trong tiêu đề; không thêm hay sửa mã sản phẩm. Tệp chạy PowerShell riêng: [run-headless-2026-11-03.ps1](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/run-headless-2026-11-03.ps1>).

## test/game/model/BoardModelContractTest.java

```java
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
```

## test/game/model/ScoreTrackerContractTest.java

```java
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
```

## test/game/ai/MinimaxEasyContractTest.java

```java
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
```

## test/game/ai/HeuristicOrderingTest.java

```java
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
```

## test/game/network/NetworkMessageTest.java

```java
package game.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Only in-memory protocol processing; no connect/listen/host/join call or socket is used. */
class NetworkMessageTest {
    private static final String BASE = "00000000-0000-0000-0000-000000000001";
    private static final String NEXT = "00000000-0000-0000-0000-000000000002";

    @ParameterizedTest(name = "host={0}, ply={1}, row={2}, col={3}")
    @CsvSource({"false,1,0,14", "true,2,14,0", "false,225,14,14", "true,224,0,0"})
    void moveLineDispatchesPeerRoundPlyAndCoordinates(boolean host, int ply, int row, int col)
            throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(host)) {
            fixture.process("  MOVE   " + BASE + "   " + ply + "   " + row + "   " + col + "  ");

            assertEquals(List.of(BASE + " " + ply + " " + row + " " + col), fixture.moves);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "MOVE", "MOVE " + BASE + " 1 7", "MOVE " + BASE + " a 7 7",
            "MOVE " + BASE + " 0 7 7", "MOVE " + BASE + " 226 7 7",
            "MOVE " + BASE + " 1 -1 7", "MOVE " + BASE + " 1 15 7",
            "MOVE " + BASE + " 1 7 -1", "MOVE " + BASE + " 1 7 15",
            "MOVE not-a-uuid 1 7 7", "MOVE " + BASE + " 2 7 7",
            "MOVE " + BASE + " 1 7 7 extra"
    })
    void invalidMoveLineDoesNotDispatchAMove(String line) throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(false)) {
            fixture.process(line);

            assertTrue(fixture.moves.isEmpty(), "Unexpected move for: " + line);
        }
    }

    @Test
    void sendMoveFormatsTheCurrentRoundMessage() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(true)) {
            fixture.manager.sendMove(BASE, 1, 7, 8);

            assertEquals("MOVE " + BASE + " 1 7 8" + System.lineSeparator(), fixture.output.toString());
        }
    }

    @Test
    void clientRestartRequestFormatsTheBaseRoundMessage() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(false)) {
            assertTrue(fixture.manager.requestRestart(BASE));

            assertEquals("RESTART_REQUEST " + BASE + System.lineSeparator(), fixture.output.toString());
        }
    }

    @Test
    void restartRequestLineOnHostCommitsANewRound() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(true)) {
            fixture.process("RESTART_REQUEST " + BASE);

            String next = fixture.manager.getRoundId();
            assertNotEquals(BASE, next);
            assertEquals(next, UUID.fromString(next).toString());
            assertEquals(List.of(BASE + " " + next), fixture.rounds);
        }
    }

    @Test
    void hostRestartFormatsBaseAndNewRoundMessage() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(true)) {
            assertTrue(fixture.manager.requestRestart(BASE));

            String next = fixture.manager.getRoundId();
            assertNotEquals(BASE, next);
            assertEquals("RESTART " + BASE + " " + next + System.lineSeparator(), fixture.output.toString());
        }
    }

    @Test
    void restartLineOnClientReplacesTheMatchingRound() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(false)) {
            fixture.process("RESTART " + BASE + " " + NEXT);

            assertEquals(NEXT, fixture.manager.getRoundId());
            assertEquals(List.of(BASE + " " + NEXT), fixture.rounds);
        }
    }

    @Test
    void restartLineForDifferentBaseDoesNotChangeRound() throws ReflectiveOperationException {
        try (SessionFixture fixture = new SessionFixture(false)) {
            fixture.process("RESTART " + NEXT + " " + BASE);

            assertEquals(BASE, fixture.manager.getRoundId());
            assertTrue(fixture.rounds.isEmpty());
        }
    }

    /** Reflects the existing private Session because no separate protocol API exists. */
    private static final class SessionFixture implements AutoCloseable {
        final StringWriter output = new StringWriter();
        final List<String> moves = new ArrayList<>();
        final List<String> rounds = new ArrayList<>();
        final NetworkManager manager;
        final Object session;
        final Method processLine;

        SessionFixture(boolean host) throws ReflectiveOperationException {
            manager = new NetworkManager(new NetworkManager.NetworkListener() {
                @Override public void onConnected(boolean isHost) { fail("Unexpected connection callback"); }
                @Override public void onMoveReceived(int row, int col) { fail("Missing protocol metadata"); }
                @Override public void onMoveReceived(String round, int ply, int row, int col) {
                    moves.add(round + " " + ply + " " + row + " " + col);
                }
                @Override public void onRestartRequested() { fail("Missing round metadata"); }
                @Override public void onRoundStarted(String base, String next) { rounds.add(base + " " + next); }
                @Override public void onOpponentDisconnected() { fail("Unexpected disconnect callback"); }
                @Override public void onError(NetworkManager.NetworkError error) { fail("Unexpected network error: " + error); }
            });
            Class<?> sessionType = Class.forName("game.network.NetworkManager$Session");
            Constructor<?> constructor = sessionType.getDeclaredConstructor(boolean.class, String.class, int.class);
            constructor.setAccessible(true);
            session = constructor.newInstance(host, "", 0);
            set(session, "connected", true);
            set(session, "roundId", BASE);
            set(session, "out", new PrintWriter(output, true));
            set(manager, "current", session);
            processLine = NetworkManager.class.getDeclaredMethod("processLine", sessionType, String.class);
            processLine.setAccessible(true);
        }

        void process(String line) throws ReflectiveOperationException {
            processLine.invoke(manager, session, line);
        }

        private static void set(Object target, String name, Object value) throws ReflectiveOperationException {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        }

        @Override public void close() { manager.disconnect(); }
    }
}
```
