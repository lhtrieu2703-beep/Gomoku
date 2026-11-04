package game.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public final class AvatarIcon implements Icon {
    private static final BufferedImage[] IMAGES = new BufferedImage[8];
    static { for (int i=0;i<8;i++) IMAGES[i]=ResourceImages.load("/avatars/avatar_"+i+".png"); }
    private final int index, size;
    private final String name;
    public AvatarIcon(int index, String name, int size) {
        this.index=index>=0&&index<8?index:0; this.name=name; this.size=size;
    }
    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g=(Graphics2D)graphics.create(); g.translate(x,y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.clip(new Ellipse2D.Double(0,0,size,size));
        g.setColor(Theme.AVATARS[index]); g.fillOval(0,0,size,size);
        if(IMAGES[index]!=null) g.drawImage(IMAGES[index],0,0,size,size,null);
        else {
            String initial=name==null||name.isBlank()?"":name.substring(0,name.offsetByCodePoints(0,1)).toUpperCase(java.util.Locale.ROOT);
            g.setFont(Theme.font(Font.BOLD,size/2)); FontMetrics fm=g.getFontMetrics();
            g.setColor(Theme.DARK); g.drawString(initial,(size-fm.stringWidth(initial))/2,(size-fm.getHeight())/2+fm.getAscent());
        }
        g.dispose();
    }
}
