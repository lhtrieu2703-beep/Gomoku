package game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
public final class Logo extends JComponent {
    private static final Stroke OUTLINE_STROKE = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Shape outline;
    private Shape shadow;
    private FontRenderContext outlineContext;
    private int outlineWidth, outlineHeight;

    public Logo() { setPreferredSize(new Dimension(480,140)); }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext context = g.getFontRenderContext();
        int width = getWidth();
        int height = getHeight();
        if (width != outlineWidth || height != outlineHeight || !context.equals(outlineContext)) {
            Font font = Theme.font(Font.BOLD, Math.min(76, Math.max(24, width / 5)));
            FontMetrics metrics = g.getFontMetrics(font);
            int x = (width - metrics.stringWidth(Lang.BRAND)) / 2;
            int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            TextLayout text = new TextLayout(Lang.BRAND, font, context);
            outline = text.getOutline(AffineTransform.getTranslateInstance(x, y));
            shadow = AffineTransform.getTranslateInstance(3, 4).createTransformedShape(outline);
            outlineWidth = width;
            outlineHeight = height;
            outlineContext = context;
        }
        g.setColor(Theme.DARK);
        g.fill(shadow);
        g.setStroke(OUTLINE_STROKE);
        g.draw(outline);
        g.setColor(Theme.BRASS);
        g.fill(outline);
        g.dispose();
    }
}
