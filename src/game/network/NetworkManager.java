package game.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/** One session owns its sockets; cancelled sessions cannot notify or close a new one. */
public class NetworkManager {
    public static final int DEFAULT_PORT = 8888;
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public enum ErrorType { HOST, JOIN, CHANNEL }
    public record NetworkError(ErrorType type, String address, int port, Exception cause) {}
    public interface NetworkListener {
        default void onHosting(int port) {}
        void onConnected(boolean isHost);
        void onMoveReceived(int row, int col);
        void onRestartRequested();
        void onOpponentDisconnected();
        void onError(NetworkError error);
    }
    private final NetworkListener listener;
    private final Consumer<Runnable> dispatcher;
    private volatile Session current;

    public NetworkManager(NetworkListener listener) { this(listener, Runnable::run); }
    /** UI supplies SwingUtilities::invokeLater; validation occurs on that thread. */
    public NetworkManager(NetworkListener listener, Consumer<Runnable> dispatcher) {
        this.listener = listener;
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }
    public static String getLocalIP() {
        try { return InetAddress.getLocalHost().getHostAddress(); }
        catch (Exception e) { return "127.0.0.1"; }
    }
    public void hostGame(int port) { start(true, "", port); }
    public void joinGame(String ip, int port) { start(false, ip, port); }

    private synchronized void start(boolean host, String ip, int port) {
        disconnect();
        Session session = new Session(host, ip, port);
        current = session;
        Thread worker = new Thread(() -> connectAndListen(session), "gomoku-lan");
        worker.setDaemon(true);
        worker.start();
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
            synchronized (session) {
                if (session.closed) return;
                session.out = out;
                session.connected = true;
            }
            established = true;
            dispatch(session, () -> listener.onConnected(session.host));
            String line;
            while (!session.closed && (line = in.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3 && parts[0].equals("MOVE")) {
                    try {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        dispatch(session, () -> listener.onMoveReceived(row, col));
                    } catch (NumberFormatException ignored) {
                        // Malformed packets do not terminate a healthy session.
                    }
                } else if (line.trim().equals("RESTART")) {
                    dispatch(session, () -> listener.onRestartRequested());
                } else if (line.trim().equals("DISCONNECT")) {
                    break;
                }
            }
        } catch (Exception error) {
            if (!session.closed && !established) {
                ErrorType type = stage;
                dispatch(session, () -> listener.onError(
                        new NetworkError(type, session.address, session.port, error)));
            }
        } finally {
            boolean notifyPeerLeft = established && !session.closed;
            session.close();
            if (notifyPeerLeft) dispatch(session, () -> listener.onOpponentDisconnected());
        }
    }
    private void dispatch(Session session, Runnable callback) {
        if (listener == null) return;
        dispatcher.accept(() -> {
            if (current == session) callback.run();
        });
    }
    public void sendMove(int row, int col) { send("MOVE " + row + " " + col); }
    public void sendRestart() { send("RESTART"); }
    private void send(String message) {
        Session session = current;
        if (session != null && session.connected) {
            PrintWriter writer = session.out;
            if (writer != null) writer.println(message);
        }
    }
    /** Invalidate callbacks first, then close even a pending bind/connect/accept. */
    public synchronized void disconnect() {
        Session old = current;
        current = null;
        if (old != null) old.close(); // EOF notifies the peer without a blocking protocol write
    }
    public boolean isConnected() {
        Session session = current;
        return session != null && session.connected;
    }
    public boolean isHost() { Session session = current; return session != null && session.host; }

    private static final class Session {
        final boolean host;
        final String address;
        final int port;
        volatile boolean closed;
        volatile boolean connected;
        volatile PrintWriter out;
        private Socket socket;
        private ServerSocket server;
        Session(boolean host, String address, int port) {
            this.host = host; this.address = address; this.port = port;
        }
        synchronized boolean attach(Socket socket) {
            if (closed) return false;
            this.socket = socket;
            return true;
        }
        synchronized boolean attach(ServerSocket server) {
            if (closed) return false;
            this.server = server;
            return true;
        }
        void close() {
            Socket oldSocket;
            ServerSocket oldServer;
            synchronized (this) {
                if (closed) return;
                closed = true;
                connected = false;
                oldSocket = socket;
                oldServer = server;
            }
            try { if (oldSocket != null) oldSocket.close(); } catch (IOException ignored) {}
            try { if (oldServer != null) oldServer.close(); } catch (IOException ignored) {}
        }
    }
}
