package game.view;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;

/** A Java2D cup representing the existing score summary, without badge state. */
public final class AchievementIcon implements Icon {
    private final int size;

    public AchievementIcon(int size) { this.size = size; }
    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(x, y);
            g.scale(size / 24.0, size / 24.0);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(240, 191, 83));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRoundRect(2, 4, 20, 8, 7, 7);
            Path2D cup = new Path2D.Double();
            cup.moveTo(6, 3); cup.lineTo(18, 3); cup.lineTo(17, 11);
            cup.quadTo(12, 19, 7, 11); cup.closePath();
            g.fill(cup);
            g.fillRect(11, 14, 2, 5);
            g.fillRoundRect(7, 19, 10, 3, 2, 2);
        } finally { g.dispose(); }
    }
}
