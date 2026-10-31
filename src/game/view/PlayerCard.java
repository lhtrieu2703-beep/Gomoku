package game.view;
import game.model.Piece;
import javax.swing.*;
import java.awt.*;

/** Fixed-width card with explicit, code-point-safe name ellipsis. */
public final class PlayerCard extends JPanel {
    private final JLabel avatar=new JLabel(),name=new JLabel(),wins=new JLabel(),pieceBadge=new JLabel();
    private final JPanel text=new JPanel(new GridLayout(2,1,0,2));
    private String fullName="";
    private boolean highlighted;
    public PlayerCard() {
        super(new BorderLayout(10,0)); setBackground(Theme.DARK); setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        name.setFont(Theme.font(Font.BOLD,15)); name.setForeground(Theme.CREAM); name.putClientProperty("html.disable",true);
        wins.setFont(Theme.font(Font.PLAIN,13)); wins.setForeground(Theme.MUTED);
        JPanel nameRow=new JPanel(new BorderLayout(6,0)); nameRow.setOpaque(false);
        nameRow.add(pieceBadge,BorderLayout.WEST); nameRow.add(name);
        text.setOpaque(false); text.add(nameRow); text.add(wins);
        add(avatar,BorderLayout.WEST); add(text);
    }
    public void update(String fullName,int index,int count,boolean highlighted,Piece piece) {
        this.fullName=fullName; this.highlighted=highlighted;
        avatar.setIcon(new AvatarIcon(index,fullName,48)); wins.setText(Lang.wins()+": "+count); name.setToolTipText(fullName);
        pieceBadge.setIcon(new PieceBadgeIcon(piece)); pieceBadge.setToolTipText(Lang.pieceName(piece));
        pieceBadge.getAccessibleContext().setAccessibleName(Lang.pieceName(piece));
        revalidate(); repaint();
    }
    @Override public void doLayout() {
        super.doLayout(); FontMetrics fm=name.getFontMetrics(name.getFont()); int available=Math.max(0,text.getWidth()-PieceBadgeIcon.SIZE-6);
        String visible=fullName;
        if(fm.stringWidth(visible)>available) {
            while(!visible.isEmpty()&&fm.stringWidth(visible+"...")>available) visible=visible.substring(0,visible.offsetByCodePoints(visible.length(),-1));
            visible+="...";
        }
        name.setText(visible);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(highlighted?Theme.BRASS:Theme.WOOD_DARK); g.setStroke(new BasicStroke(highlighted?2.5f:1));
        g.drawRoundRect(2,2,getWidth()-5,getHeight()-5,18,18); g.dispose();
    }
    public boolean isHighlighted() { return highlighted; }
    public JLabel getNameLabel() { return name; } public JLabel getWinsLabel() { return wins; }
    public JLabel getPieceBadge() { return pieceBadge; }
    public String getFullName() { return fullName; }
}
