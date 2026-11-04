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
