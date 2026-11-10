package game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
public final class Logo extends JComponent {
    public Logo() { setPreferredSize(new Dimension(480,140)); }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(Theme.font(Font.BOLD,Math.min(76,Math.max(24,getWidth()/5)))); FontMetrics fm=g.getFontMetrics();
        int x=(getWidth()-fm.stringWidth(Lang.BRAND))/2,y=(getHeight()-fm.getHeight())/2+fm.getAscent();
        TextLayout text = new TextLayout(Lang.BRAND, g.getFont(), g.getFontRenderContext());
        Shape outline = text.getOutline(AffineTransform.getTranslateInstance(x, y));
        g.setColor(Theme.DARK);
        g.fill(AffineTransform.getTranslateInstance(3, 4).createTransformedShape(outline));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);
        g.setColor(Theme.BRASS);
        g.fill(outline);
        g.dispose();
    }
}
