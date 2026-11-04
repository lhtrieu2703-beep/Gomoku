package game.view;

import game.model.Piece;
import javax.swing.Icon;
import java.awt.*;

/** Small player-card marker, using the board's existing stone palette. */
public final class PieceBadgeIcon implements Icon {
    public static final int SIZE=18;
    private final Piece piece;

    public PieceBadgeIcon(Piece piece) {
        if(piece!=Piece.X&&piece!=Piece.O) throw new IllegalArgumentException("A badge requires X or O");
        this.piece=piece;
    }

    /** Pure presentation mapping; local side matters only in online mode. */
    public static Piece pieceForCard(BoardPanel.GameMode mode,Piece localSide,boolean leftCard) {
        Piece left=mode==BoardPanel.GameMode.ONLINE?localSide:Piece.X;
        return leftCard?left:left.opposite();
    }

    public Piece getPiece() { return piece; }
    public int getIconWidth() { return SIZE; }
    public int getIconHeight() { return SIZE; }
    public void paintIcon(Component component,Graphics graphics,int x,int y) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.translate(x,y); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(piece==Piece.X?Theme.BLACK_STONE:Theme.WHITE_STONE); g.fillOval(2,2,14,14);
            if(piece==Piece.O) { g.setColor(Theme.WHITE_EDGE);g.setStroke(new BasicStroke(1));g.drawOval(2,2,14,14); }
        } finally { g.dispose(); }
    }
}
