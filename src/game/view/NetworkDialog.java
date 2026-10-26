package game.view;

import game.network.NetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hộp thoại thiết lập kết nối Chơi Online (Host / Join).
 * Dialog là MODAL — mọi chuỗi được đọc từ Lang.*() tại thời điểm tạo.
 * Không đăng ký Lang.addChangeListener (thừa với modal).
 */
public class NetworkDialog extends JDialog {
    private final NetworkManager networkManager;
    private final Runnable onConnectedCallback;

    private JTextField txtIp;
    private JTextField txtPort;
    private JLabel lblStatus;
    private JButton btnHost;
    private JButton btnJoin;
    private boolean finished;
    private boolean connected;

    public NetworkDialog(JFrame parent, NetworkManager networkManager, Runnable onConnectedCallback) {
        super(parent, Lang.networkDialogTitle(), true);
        this.networkManager = networkManager;
        this.onConnectedCallback = onConnectedCallback;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 248));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setOpaque(false);

        // Tiêu đề hướng dẫn
        JLabel lblTitle = new JLabel(Lang.networkDialogHeader());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setForeground(new Color(40, 45, 55));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(12));

        // Khung thông tin IP & Port
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.setOpaque(false);

        JLabel lblIpText   = new JLabel(Lang.ipLabel());
        txtIp = new JTextField("127.0.0.1");
        txtIp.setFont(new Font("Consolas", Font.PLAIN, 13));

        JLabel lblPortText = new JLabel(Lang.portLabel());
        txtPort = new JTextField(String.valueOf(NetworkManager.DEFAULT_PORT));
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 13));

        formPanel.add(lblIpText);
        formPanel.add(txtIp);
        formPanel.add(lblPortText);
        formPanel.add(txtPort);
        content.add(formPanel);

        // IP máy mình
        JLabel lblMyIp = new JLabel(Lang.myIpLabel() + NetworkManager.getLocalIP());
        lblMyIp.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMyIp.setForeground(new Color(100, 100, 110));
        lblMyIp.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(8));
        content.add(lblMyIp);

        // Nút bấm
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);

        btnHost = new JButton(Lang.hostBtn());
        styleButton(btnHost, new Color(46, 160, 67), Color.WHITE);

        btnJoin = new JButton(Lang.joinBtn());
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
            setLoadingState(true, Lang.hostingMsg());
            networkManager.hostGame(port);
        });

        // Sự kiện Vào phòng (Join)
        btnJoin.addActionListener(e -> {
            String ip = txtIp.getText().trim();
            int port = parsePort();
            if (ip.isEmpty() || port <= 0) return;
            setLoadingState(true, Lang.connectingMsg(ip));
            networkManager.joinGame(ip, port);
        });

        pack();
        setLocationRelativeTo(parent);
    }

    private int parsePort() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (Exception ex) {
            lblStatus.setText(Lang.invalidPort());
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
        runOnEdt(() -> {
            if (finished) return;
            connected = true;
            if (onConnectedCallback != null) onConnectedCallback.run();
            dispose();
        });
    }

    public void onConnectionFailed(String error) {
        runOnEdt(() -> {
            if (finished) return;
            setLoadingState(false, error);
            lblStatus.setForeground(new Color(200, 40, 40));
        });
    }

    public boolean wasConnected() { return connected; }

    @Override
    public void dispose() {
        if (!finished) {
            finished = true;
            if (!connected) networkManager.disconnect();
        }
        super.dispose();
    }

    private static void runOnEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) task.run();
        else SwingUtilities.invokeLater(task);
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
