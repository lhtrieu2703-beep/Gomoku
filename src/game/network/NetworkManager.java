package game.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** Each connection owns its resources; wire round IDs are independent of local game IDs. */
public class NetworkManager {
    public static final int DEFAULT_PORT = 8888;
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int START_TIMEOUT_MS = 5000;
    public static final int MAX_LINE_LENGTH = 256;
    public static final int PROTOCOL_VERSION = 2;
    public enum ErrorType { HOST, JOIN, CHANNEL, START_TIMEOUT, VERSION }
    public record NetworkError(ErrorType type, String address, int port, Exception cause) {}
    public interface NetworkListener {
        default void onHosting(int port) {}
        void onConnected(boolean isHost);
        default void onConnected(boolean isHost, String roundId) { onConnected(isHost); }
        void onMoveReceived(int row, int col);
        default void onMoveReceived(String roundId, int ply, int row, int col) { onMoveReceived(row, col); }
        void onRestartRequested();
        default void onRoundStarted(String base, String next) { onRestartRequested(); }
        default void onRestartWaiting(String base) {}
        void onOpponentDisconnected();
        void onError(NetworkError error);
    }
    private final NetworkListener listener;
    private final Consumer<Runnable> dispatcher;
    private volatile Session current;

    public NetworkManager(NetworkListener listener) { this(listener, Runnable::run); }
    public NetworkManager(NetworkListener listener, Consumer<Runnable> dispatcher) {
        this.listener = listener; this.dispatcher = Objects.requireNonNull(dispatcher);
    }
    public static String getLocalIP() {
        try { return InetAddress.getLocalHost().getHostAddress(); }
        catch (Exception e) { return "127.0.0.1"; }
    }
    public void hostGame(int port) { start(true, "", port); }
    public void joinGame(String ip, int port) { start(false, ip, port); }
    private synchronized void start(boolean host, String ip, int port) {
        disconnect();
        Session session = new Session(host, ip, port); current = session;
        Thread worker = new Thread(() -> connectAndListen(session), "gomoku-lan");
        worker.setDaemon(true); worker.start();
    }
    private void connectAndListen(Session session) {
        ErrorType stage = session.host ? ErrorType.HOST : ErrorType.JOIN;
        boolean established = false;
        try {
            Socket socket;
            if (session.host) {
                ServerSocket server = new ServerSocket();
                if (!session.attach(server)) { server.close(); return; }
                server.bind(new InetSocketAddress(session.port));
                dispatch(session, () -> listener.onHosting(server.getLocalPort()));
                socket = server.accept();
                if (!session.attach(socket)) { socket.close(); return; }
                server.close();
            } else {
                socket = new Socket();
                if (!session.attach(socket)) { socket.close(); return; }
                socket.connect(new InetSocketAddress(session.address, session.port), CONNECT_TIMEOUT_MS);
            }
            stage = ErrorType.CHANNEL;
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            if (session.host) {
                session.roundId = UUID.randomUUID().toString();
                out.println("START " + PROTOCOL_VERSION + " " + session.roundId);
            } else {
                long deadline = System.nanoTime() + START_TIMEOUT_MS * 1_000_000L;
                while (!session.closed && session.roundId == null) {
                    String line = readLine(in, socket, deadline);
                    if (line == null) throw new EOFException("EOF before START");
                    String[] p = line.trim().split("\\s+");
                    if (p.length == 3 && p[0].equals("START")) {
                        try {
                            if (Integer.parseInt(p[1]) != PROTOCOL_VERSION) throw new ProtocolFailure(ErrorType.VERSION);
                            if (validId(p[2])) session.roundId = p[2];
                        } catch (NumberFormatException ignored) {}
                    }
                }
                socket.setSoTimeout(0);
            }
            synchronized (session) {
                if (session.closed) return;
                session.out = out; session.connected = true;
            }
            established = true;
            String initialRound = session.roundId;
            dispatch(session, () -> listener.onConnected(session.host, initialRound));
            String line;
            while (!session.closed && (line = readLine(in, socket, 0)) != null) processLine(session, line);
        } catch (Exception error) {
            if (!session.closed && !established) {
                ErrorType type = error instanceof ProtocolFailure p ? p.type
                        : error instanceof SocketTimeoutException ? ErrorType.START_TIMEOUT : stage;
                dispatch(session, () -> listener.onError(new NetworkError(type, session.address, session.port, error)));
            }
        } finally {
            boolean notify = established && !session.closed;
            session.close();
            if (notify) dispatch(session, () -> listener.onOpponentDisconnected());
        }
    }
    /** Bounded storage, drain overlong lines; handshake uses an absolute deadline. */
    private static String readLine(Reader in, Socket socket, long deadline) throws IOException {
        StringBuilder line = new StringBuilder(); boolean overlong = false;
        while (true) {
            if (deadline != 0) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new SocketTimeoutException("START timeout");
                socket.setSoTimeout((int) Math.max(1, (remaining + 999999) / 1000000));
            }
            int c = in.read();
            if (c == -1) return line.isEmpty() && !overlong ? null : "";
            if (c == '\n') return overlong ? "" : line.toString();
            if (c != '\r' && !overlong) {
                if (line.length() == MAX_LINE_LENGTH) { overlong = true; line.setLength(0); }
                else line.append((char) c);
            }
        }
    }
    private void processLine(Session session, String line) {
        String[] p = line.trim().split("\\s+");
        try {
            if (p.length == 5 && p[0].equals("MOVE") && validId(p[1])) {
                int ply = Integer.parseInt(p[2]), row = Integer.parseInt(p[3]), col = Integer.parseInt(p[4]);
                if (ply < 1 || ply > 225 || row < 0 || row >= 15 || col < 0 || col >= 15) return;
                // X/black starts. Host owns odd plies; client owns even plies.
                if ((ply % 2 == 1) == session.host) return;
                String round = p[1];
                dispatch(session, () -> listener.onMoveReceived(round, ply, row, col));
            } else if (p.length == 2 && p[0].equals("RESTART_REQUEST") && session.host && validId(p[1])) {
                commitRestart(session, p[1]);
            } else if (p.length == 3 && p[0].equals("RESTART") && !session.host
                    && validId(p[1]) && validId(p[2]) && !p[1].equals(p[2])) {
                synchronized (session) {
                    if (session.closed || !p[1].equals(session.roundId)) return;
                    session.roundId = p[2]; session.waiting = false;
                    dispatch(session, () -> listener.onRoundStarted(p[1], p[2]));
                }
            }
        } catch (NumberFormatException ignored) { /* garbage does not disconnect the peer */ }
    }
    private static boolean validId(String id) {
        try { return UUID.fromString(id).toString().equals(id); }
        catch (IllegalArgumentException e) { return false; }
    }
    private void dispatch(Session session, Runnable callback) {
        if (listener != null) dispatcher.accept(() -> { if (current == session) callback.run(); });
    }
    public String getRoundId() { Session s = current; return s == null ? null : s.roundId; }
    public boolean isWaitingForRestart() { Session s = current; return s != null && s.waiting; }
    public void sendMove(String roundId, int ply, int row, int col) {
        Session s = current;
        if (s != null) synchronized (s) {
            if (s.connected && !s.closed && !s.waiting && Objects.equals(roundId, s.roundId))
                s.out.println("MOVE " + roundId + " " + ply + " " + row + " " + col);
        }
    }
    /** Request phase, kept separate from host commit for Task 5's future consent policy. */
    public boolean requestRestart(String base) {
        Session s = current;
        if (s == null) return false;
        synchronized (s) {
            if (!s.connected || s.closed || s.waiting || !Objects.equals(base, s.roundId)) return false;
            if (s.host) return commitRestart(s, base);
            s.waiting = true;
            s.out.println("RESTART_REQUEST " + base);
            dispatch(s, () -> { if (s.waiting && base.equals(s.roundId)) listener.onRestartWaiting(base); });
            return true;
        }
    }
    private boolean commitRestart(Session s, String base) {
        synchronized (s) {
            if (s.closed || !s.connected || !s.host || !base.equals(s.roundId)) return false;
            String next = UUID.randomUUID().toString(); s.roundId = next;
            s.out.println("RESTART " + base + " " + next);
            dispatch(s, () -> listener.onRoundStarted(base, next));
            return true;
        }
    }
    /** EOF closes the peer. Never write a DISCONNECT packet or wait for an ACK. */
    public synchronized void disconnect() {
        Session old = current; current = null; if (old != null) old.close();
    }
    public boolean isConnected() { Session s = current; return s != null && s.connected; }
    public boolean isHost() { Session s = current; return s != null && s.host; }
    private static final class ProtocolFailure extends IOException {
        final ErrorType type;
        ProtocolFailure(ErrorType type) { this.type = type; }
    }
    private static final class Session {
        final boolean host; final String address; final int port;
        volatile boolean closed, connected, waiting;
        volatile String roundId;
        volatile PrintWriter out;
        private Socket socket; private ServerSocket server;
        Session(boolean host, String address, int port) { this.host = host; this.address = address; this.port = port; }
        synchronized boolean attach(Socket socket) { if (closed) return false; this.socket = socket; return true; }
        synchronized boolean attach(ServerSocket server) { if (closed) return false; this.server = server; return true; }
        void close() {
            Socket oldSocket; ServerSocket oldServer;
            synchronized (this) {
                if (closed) return;
                closed = true; connected = false; waiting = false; oldSocket = socket; oldServer = server;
            }
            try { if (oldSocket != null) oldSocket.close(); } catch (IOException ignored) {}
            try { if (oldServer != null) oldServer.close(); } catch (IOException ignored) {}
        }
    }
}