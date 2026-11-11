package game.view;

import game.model.Piece;
import javax.swing.Icon;
import java.awt.*;

/** Small player-card marker using the selected stone finish. */
public final class PieceBadgeIcon implements Icon {
    public static final int SIZE=18;
    private final Piece piece;
    private final PieceStyle style;

    public PieceBadgeIcon(Piece piece) {
        this(piece, PieceStyle.CLASSIC);
    }

    public PieceBadgeIcon(Piece piece, PieceStyle style) {
        if(piece!=Piece.X&&piece!=Piece.O) throw new IllegalArgumentException("A badge requires X or O");
        this.piece=piece;
        this.style=java.util.Objects.requireNonNull(style);
    }

    /** Pure presentation mapping; local side matters only in online mode. */
    public static Piece pieceForCard(BoardPanel.GameMode mode,Piece localSide,boolean leftCard) {
        Piece left=mode==BoardPanel.GameMode.ONLINE?localSide:Piece.X;
        return leftCard?left:left.opposite();
    }

    public Piece getPiece() { return piece; }
    public PieceStyle getStyle() { return style; }
    public int getIconWidth() { return SIZE; }
    public int getIconHeight() { return SIZE; }
    public void paintIcon(Component component,Graphics graphics,int x,int y) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.translate(x,y); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if (style == PieceStyle.CLASSIC) {
                PieceStyle.Palette palette = style.palette(piece);
                g.setColor(palette.face()); g.fillOval(2,2,14,14);
                if(piece==Piece.O) { g.setColor(palette.edge());g.setStroke(new BasicStroke(1));g.drawOval(2,2,14,14); }
            } else {
                StonePainter.paint(g, SIZE / 2, SIZE / 2, 7, piece, style);
            }
        } finally { g.dispose(); }
    }
}
