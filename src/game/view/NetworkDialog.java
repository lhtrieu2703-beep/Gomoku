package game.view;

import game.network.NetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hộp thoại thiết lập kết nối Chơi Online (Host / Join).
 */
public class NetworkDialog extends JDialog {
    private final NetworkManager networkManager;
    private final Runnable onConnectedCallback;

    private JTextField txtIp;
    private JTextField txtPort;
    private JLabel lblStatus;
    private JButton btnHost;
    private JButton btnJoin;

    public NetworkDialog(JFrame parent, NetworkManager networkManager, Runnable onConnectedCallback) {
        super(parent, Lang.isVI() ? "Kết nối chơi Online (LAN)" : "Online Connection (LAN)", true);
        this.networkManager = networkManager;
        this.onConnectedCallback = onConnectedCallback;

        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 248));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setOpaque(false);

        // Tiêu đề hướng dẫn
        JLabel lblTitle = new JLabel(Lang.isVI() ? "CHỌN CÁCH THỨC KẾT NỐI" : "CHOOSE CONNECTION MODE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setForeground(new Color(40, 45, 55));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(12));

        // Khung thông tin IP & Port
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.setOpaque(false);

        JLabel lblIpText = new JLabel(Lang.isVI() ? "Địa chỉ IP đối thủ:" : "Opponent IP:");
        txtIp = new JTextField("127.0.0.1"); // Mặc định localhost cho dễ test
        txtIp.setFont(new Font("Consolas", Font.PLAIN, 13));

        JLabel lblPortText = new JLabel(Lang.isVI() ? "Cổng (Port):" : "Port:");
        txtPort = new JTextField(String.valueOf(NetworkManager.DEFAULT_PORT));
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 13));

        formPanel.add(lblIpText);
        formPanel.add(txtIp);
        formPanel.add(lblPortText);
        formPanel.add(txtPort);
        content.add(formPanel);

        // Hiển thị IP máy của chính mình để đưa cho bạn bè
        JLabel lblMyIp = new JLabel((Lang.isVI() ? "IP máy của bạn: " : "Your local IP: ") + NetworkManager.getLocalIP());
        lblMyIp.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMyIp.setForeground(new Color(100, 100, 110));
        lblMyIp.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(8));
        content.add(lblMyIp);

        // Nút bấm Hành động
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);

        btnHost = new JButton(Lang.isVI() ? "Tạo Phòng (Host - Quân Đen)" : "Create Room (Host - Black)");
        styleButton(btnHost, new Color(46, 160, 67), Color.WHITE);

        btnJoin = new JButton(Lang.isVI() ? "Vào Phòng (Join - Quân Trắng)" : "Join Room (White)");
        styleButton(btnJoin, new Color(33, 113, 195), Color.WHITE);

        btnPanel.add(btnHost);
        btnPanel.add(btnJoin);
        content.add(Box.createVerticalStrut(16));
        content.add(btnPanel);

        // Nhãn trạng thái
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setForeground(new Color(220, 100, 20));
        content.add(Box.createVerticalStrut(10));
        content.add(lblStatus);

        add(content, BorderLayout.CENTER);

        // Sự kiện Tạo phòng (Host)
        btnHost.addActionListener(e -> {
            int port = parsePort();
            if (port <= 0) return;
            setLoadingState(true, Lang.isVI() ? "Đang mở phòng & chờ đối thủ kết nối..." : "Hosting & waiting for player...");
            networkManager.hostGame(port);
        });

        // Sự kiện Vào phòng (Join)
        btnJoin.addActionListener(e -> {
            String ip = txtIp.getText().trim();
            int port = parsePort();
            if (ip.isEmpty() || port <= 0) return;
            setLoadingState(true, Lang.isVI() ? "Đang kết nối tới " + ip + "..." : "Connecting to " + ip + "...");
            networkManager.joinGame(ip, port);
        });

        pack();
        setLocationRelativeTo(parent);
    }

    private int parsePort() {
        try {
            return Integer.parseInt(txtPort.getText().trim());
        } catch (Exception ex) {
            lblStatus.setText(Lang.isVI() ? "Cổng (Port) không hợp lệ!" : "Invalid port number!");
            lblStatus.setForeground(Color.RED);
            return -1;
        }
    }

    private void setLoadingState(boolean loading, String statusText) {
        btnHost.setEnabled(!loading);
        btnJoin.setEnabled(!loading);
        txtIp.setEnabled(!loading);
        txtPort.setEnabled(!loading);
        lblStatus.setText(statusText);
        lblStatus.setForeground(new Color(21, 101, 192));
    }

    public void onConnectionSuccess() {
        SwingUtilities.invokeLater(() -> {
            if (onConnectedCallback != null) {
                onConnectedCallback.run();
            }
            dispose();
        });
    }

    public void onConnectionFailed(String error) {
        SwingUtilities.invokeLater(() -> {
            setLoadingState(false, error);
            lblStatus.setForeground(new Color(200, 40, 40));
        });
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
    }
}
