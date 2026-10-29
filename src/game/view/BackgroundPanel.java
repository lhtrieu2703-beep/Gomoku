package game.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Cached cover image in device pixels, with a resource-free wood fallback. */
public class BackgroundPanel extends JPanel {
    public static final String RESOURCE = "/images/background.png";
    private final BufferedImage source;
    private BufferedImage cache;
    public BackgroundPanel() { this(ResourceImages.load(RESOURCE)); }
    public BackgroundPanel(BufferedImage source) { this.source = source; }
    public boolean hasImage() { return source != null; }
    public BufferedImage getCachedImage() { return cache; }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) { g.dispose(); return; }
        if (source == null) {
            g.setPaint(new GradientPaint(0,0,Theme.WOOD_LIGHT,w,h,Theme.WOOD_DARK));
            g.fillRect(0,0,w,h);
        } else {
            int dw = (int)Math.ceil(w * Math.abs(g.getTransform().getScaleX()));
            int dh = (int)Math.ceil(h * Math.abs(g.getTransform().getScaleY()));
            if (cache == null || cache.getWidth()!=dw || cache.getHeight()!=dh) {
                cache = new BufferedImage(dw,dh,BufferedImage.TYPE_INT_RGB);
                Graphics2D cg = cache.createGraphics();
                cg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = Math.max((double)dw/source.getWidth(),(double)dh/source.getHeight());
                int sw=(int)Math.ceil(source.getWidth()*scale), sh=(int)Math.ceil(source.getHeight()*scale);
                cg.drawImage(source,(dw-sw)/2,(dh-sh)/2,sw,sh,null); cg.dispose();
            }
            g.drawImage(cache,0,0,w,h,null);
        }
        g.dispose();
    }
}
