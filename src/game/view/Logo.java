package game.view;
import javax.swing.*;
import java.awt.*;
public final class Logo extends JComponent {
    public Logo() { setPreferredSize(new Dimension(480,140)); }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(Theme.font(Font.BOLD,Math.min(76,Math.max(24,getWidth()/5)))); FontMetrics fm=g.getFontMetrics();
        int x=(getWidth()-fm.stringWidth(Lang.BRAND))/2,y=(getHeight()-fm.getHeight())/2+fm.getAscent();
        g.setColor(Theme.DARK); g.drawString(Lang.BRAND,x+3,y+4); g.setColor(Theme.BRASS); g.drawString(Lang.BRAND,x,y); g.dispose();
    }
}
