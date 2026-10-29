package game.view;
import javax.swing.*;
import java.awt.*;
/** Action symbols are geometry, never font glyphs. */
public final class UiIcon implements Icon {
    public enum Kind { MENU, BOOK, PENCIL, CLEAR, CHECK, PERSON }
    private final Kind kind; private final int size;
    public UiIcon(Kind kind,int size) { this.kind=kind; this.size=size; }
    public int getIconWidth() { return size; } public int getIconHeight() { return size; }
    public void paintIcon(Component c,Graphics graphics,int x,int y) {
        Graphics2D g=(Graphics2D)graphics.create(); g.translate(x,y); g.scale(size/24.0,size/24.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Theme.CREAM); g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        switch(kind) {
            case MENU -> { for(int row:new int[]{6,12,18}) g.drawLine(4,row,20,row); }
            case BOOK -> { g.drawRoundRect(3,4,18,16,2,2); g.drawLine(12,4,12,20); g.drawLine(6,8,9,8); g.drawLine(15,8,18,8); }
            case PENCIL -> { g.drawPolygon(new int[]{5,8,20,17},new int[]{19,20,8,5},4); g.drawLine(5,19,4,21); }
            case CLEAR -> { g.drawLine(6,6,18,18); g.drawLine(6,18,18,6); }
            case CHECK -> { g.drawLine(4,12,10,18); g.drawLine(10,18,21,5); }
            case PERSON -> { g.drawOval(8,3,8,8); g.drawArc(4,12,16,14,0,180); }
        } g.dispose();
    }
}
