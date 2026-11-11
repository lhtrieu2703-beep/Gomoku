package game.view;
import game.model.Piece;
import java.awt.*;
/** Shared Java2D stone rendering for the board, badges and rule illustrations. */
public final class StonePainter {
    private static final float[] FINISH_STOPS = {0f, 0.5f, 1f};
    private static final Color GLASS_REFLECTION = new Color(255, 255, 255, 190);

    private StonePainter() {}
    public static void paint(Graphics2D g,int cx,int cy,int radius,Piece piece) {
        paint(g, cx, cy, radius, piece, PieceStyle.CLASSIC);
    }

    public static void paint(Graphics2D g, int cx, int cy, int radius, Piece piece, PieceStyle style) {
        if (piece != Piece.X && piece != Piece.O) return;
        PieceStyle.Palette palette = style.palette(piece);
        if (style != PieceStyle.CLASSIC) {
            paintFinish(g, cx, cy, radius, style, palette);
            return;
        }
        if(piece==Piece.X) {
            g.setColor(palette.face()); g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(palette.light()); g.setStroke(new BasicStroke(1.5f));
            g.drawArc(cx-radius+3,cy-radius+3,radius,radius,45,120);
        } else {
            g.setColor(palette.face()); g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(palette.edge()); g.setStroke(new BasicStroke(1.5f)); g.drawOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(palette.light()); g.setStroke(new BasicStroke(2)); g.drawArc(cx-radius+4,cy-radius+4,radius,radius,50,100);
        }
    }

    private static void paintFinish(Graphics2D g, int cx, int cy, int radius, PieceStyle style, PieceStyle.Palette palette) {
        int x = cx - radius, y = cy - radius, diameter = 2 * radius;
        Color[] colors = {palette.light(), palette.face(), palette.edge()};
        if (style == PieceStyle.GLASS) {
            g.setPaint(new RadialGradientPaint(cx - radius * 0.35f, cy - radius * 0.45f, radius * 1.5f,
                    FINISH_STOPS, colors));
        } else {
            g.setPaint(new LinearGradientPaint(x, y, cx + radius, cy + radius, FINISH_STOPS, colors));
        }
        g.fillOval(x, y, diameter, diameter);
        g.setColor(palette.edge());
        g.setStroke(new BasicStroke(radius / 10f));
        g.drawOval(x, y, diameter, diameter);
        if (style == PieceStyle.GLASS) {
            g.setColor(GLASS_REFLECTION);
            g.fillOval(cx - radius / 2, cy - 2 * radius / 3, 2 * radius / 3, Math.max(2, radius / 4));
        }
    }
}
