package game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/** Title only. Timer lifetime is the visible title card lifetime. */
public final class StartScreen extends BackgroundPanel {
    private final Runnable enterMenu; private final Timer timer; private float phase;
    public StartScreen(Runnable enterMenu) {
        this.enterMenu=enterMenu; setLayout(null); setFocusable(true); Logo logo=new Logo(); add(logo);
        timer=new Timer(60,e -> { phase+=0.075f; repaint(); });
        MouseAdapter click=new MouseAdapter() { public void mousePressed(MouseEvent e) { advance(); } };
        addMouseListener(click); logo.addMouseListener(click);
    }
    public void advance() { enterMenu.run(); }
    public void setActive(boolean active) { if(active) timer.start(); else timer.stop(); }
    public boolean isHintTimerRunning() { return timer.isRunning(); }
    @Override public void doLayout() { getComponent(0).setBounds(40,getHeight()/4,getWidth()-80,160); }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); Graphics2D g=(Graphics2D)graphics.create(); g.setFont(Theme.font(Font.PLAIN,17));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.SrcOver.derive(0.55f+0.35f*(float)((Math.sin(phase)+1)/2))); g.setColor(Theme.CREAM);
        String hint=Lang.titleHint(); g.drawString(hint,(getWidth()-g.getFontMetrics().stringWidth(hint))/2,getHeight()*3/4); g.dispose();
    }
}
