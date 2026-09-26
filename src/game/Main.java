package game;

import game.view.MainFrame;

import javax.swing.*;

/**
 * Điểm khởi động ứng dụng Game Cờ Caro.
 */
public class Main {
    public static void main(String[] args) {
        // Thiết lập giao diện theo chuẩn hệ điều hành (Look and Feel)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Chạy giao diện trên luồng an toàn Event Dispatch Thread (EDT) của Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
