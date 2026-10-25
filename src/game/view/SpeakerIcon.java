package game.view;

import javax.swing.*;
import java.awt.*;

/**
 * Icon loa tự vẽ bằng Java2D — không cần file ảnh ngoài.
 * Đọc trạng thái trực tiếp từ SoundManager.isSoundEnabled() mỗi lần paint.
 *
 * Bật  → loa + 2 sóng âm trắng.
 * Tắt  → loa + dấu X đỏ.
 */
public class SpeakerIcon implements Icon {
    private final int size;

    public SpeakerIcon(int size) {
        this.size = size;
    }

    @Override public int getIconWidth()  { return size; }
    @Override public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        boolean on = SoundManager.isSoundEnabled();

        // --- Thân loa (hình chữ nhật nhỏ bên trái) ---
        g2.setColor(Color.WHITE);
        int bx1 = size / 10;
        int bx2 = size * 3 / 10;
        int by1 = size * 3 / 8;
        int by2 = size * 5 / 8;
        g2.fillRect(bx1, by1, bx2 - bx1, by2 - by1);

        // --- Hình nón loa (tam giác) ---
        int[] px = { bx2, size * 6 / 10, bx2 };
        int[] py = { size / 5,  size / 2,  size * 4 / 5 };
        g2.fillPolygon(px, py, 3);

        if (on) {
            // --- Sóng âm: 2 cung ---
            g2.setStroke(new BasicStroke(Math.max(1.2f, size / 16f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int arc1x = size * 6 / 10;
            g2.drawArc(arc1x - size / 10, size / 4,
                    size / 5, size / 2, -45, 90);
            int arc2x = size * 7 / 10;
            g2.drawArc(arc2x - size / 10, size / 8,
                    size * 3 / 10, size * 3 / 4, -45, 90);
        } else {
            // --- Dấu X đỏ ---
            g2.setColor(new Color(220, 50, 50));
            float stroke = Math.max(1.5f, size / 12f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int m = size * 6 / 10;
            int pad = size / 8;
            g2.drawLine(m, pad, size - pad, size - pad);
            g2.drawLine(size - pad, pad, m, size - pad);
        }

        g2.dispose();
    }
}
