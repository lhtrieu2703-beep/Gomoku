package game.view;
import game.model.Piece;
import java.awt.*;
/** The board and rule illustrations use identical stone geometry. */
public final class StonePainter {
    private StonePainter() {}
    public static void paint(Graphics2D g,int cx,int cy,int radius,Piece piece) {
        if(piece==Piece.X) {
            g.setColor(Theme.BLACK_STONE); g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(Theme.BLACK_HIGHLIGHT); g.setStroke(new BasicStroke(1.5f));
            g.drawArc(cx-radius+3,cy-radius+3,radius,radius,45,120);
        } else if(piece==Piece.O) {
            g.setColor(Theme.WHITE_STONE); g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(Theme.WHITE_EDGE); g.setStroke(new BasicStroke(1.5f)); g.drawOval(cx-radius,cy-radius,radius*2,radius*2);
            g.setColor(Theme.WHITE_GLEAM); g.setStroke(new BasicStroke(2)); g.drawArc(cx-radius+4,cy-radius+4,radius,radius,50,100);
        }
    }
}
