package game.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Quản lý kết nối mạng Socket cho chế độ chơi Online qua LAN/Localhost.
 * Xử lý đa luồng (threading) độc lập để không làm đơ giao diện Swing.
 */
public class NetworkManager {
    public static final int DEFAULT_PORT = 8888;

    public interface NetworkListener {
        void onConnected(boolean isHost);
        void onMoveReceived(int row, int col);
        void onRestartRequested();
        void onOpponentDisconnected();
        void onError(String message);
    }

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;

    private boolean isHost = false;
    private boolean isConnected = false;
    private NetworkListener listener;

    public NetworkManager(NetworkListener listener) {
        this.listener = listener;
    }

    /**
     * Lấy IP nội bộ hiện tại của máy để hiển thị cho Host.
     */
    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * Tạo phòng (Host) - Lắng nghe kết nối từ Client.
     */
    public void hostGame(int port) {
        disconnect();
        this.isHost = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
                setupStreams();
            } catch (Exception e) {
                if (!isConnected) {
                    notifyError("Lỗi tạo phòng: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Vào phòng (Join) - Kết nối tới IP của Host.
     */
    public void joinGame(String ip, int port) {
        disconnect();
        this.isHost = false;
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                setupStreams();
            } catch (Exception e) {
                notifyError("Không thể kết nối đến " + ip + ":" + port + " (" + e.getMessage() + ")");
            }
        }).start();
    }

    private void setupStreams() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;

            if (listener != null) {
                listener.onConnected(isHost);
            }

            // Bắt đầu luồng đọc dữ liệu từ đối thủ
            listenThread = new Thread(this::listenForMessages);
            listenThread.start();
        } catch (Exception e) {
            notifyError("Lỗi thiết lập kênh truyền: " + e.getMessage());
            disconnect();
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while (isConnected && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("MOVE ")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        int r = Integer.parseInt(parts[1]);
                        int c = Integer.parseInt(parts[2]);
                        if (listener != null) {
                            listener.onMoveReceived(r, c);
                        }
                    }
                } else if (line.equals("RESTART")) {
                    if (listener != null) {
                        listener.onRestartRequested();
                    }
                } else if (line.equals("DISCONNECT")) {
                    break;
                }
            }
        } catch (Exception e) {
            // Mất kết nối hoặc socket bị đóng
        } finally {
            if (isConnected) {
                isConnected = false;
                if (listener != null) {
                    listener.onOpponentDisconnected();
                }
            }
            disconnect();
        }
    }

    /**
     * Gửi nước đi sang máy đối thủ.
     */
    public void sendMove(int row, int col) {
        if (out != null) {
            out.println("MOVE " + row + " " + col);
        }
    }

    /**
     * Gửi yêu cầu ván mới.
     */
    public void sendRestart() {
        if (out != null) {
            out.println("RESTART");
        }
    }

    /**
     * Ngắt kết nối an toàn và dọn dẹp tài nguyên socket.
     */
    public void disconnect() {
        isConnected = false;
        try {
            if (out != null) {
                out.println("DISCONNECT");
                out.flush();
            }
        } catch (Exception ignored) {}

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (Exception ignored) {}

        socket = null;
        serverSocket = null;
        out = null;
        in = null;
    }

    private void notifyError(String msg) {
        if (listener != null) {
            listener.onError(msg);
        }
    }

    public boolean isConnected() { return isConnected; }
    public boolean isHost() { return isHost; }
}
