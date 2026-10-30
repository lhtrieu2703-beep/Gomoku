package game;

import game.view.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.prefs.AbstractPreferences;

/** Visible, naturally validated MainFrame + actual modal actions; no recursive doLayout. */
public final class BoardDisplayProbe {
    static final List<Throwable> errors=new CopyOnWriteArrayList<>();
    static MainFrame frame;
    static Path output;
    static boolean pixelFailure;
    static int paintCount(String field) {
        try { return ((java.util.concurrent.atomic.AtomicInteger)Class.forName("game.tools.BoardPaintAgent").getField(field).get(null)).get(); }
        catch(ClassNotFoundException absentOptionalAgent) { return -1; }
        catch(ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }
    static final class Memory extends AbstractPreferences {
        final Map<String,String> values=new HashMap<>();
        Memory() { super(null,""); }
        protected void putSpi(String k,String v) { values.put(k,v); }
        protected String getSpi(String k) { return values.get(k); }
        protected void removeSpi(String k) { values.remove(k); }
        protected void removeNodeSpi() { values.clear(); }
        protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
        protected String[] childrenNamesSpi() { return new String[0]; }
        protected AbstractPreferences childSpi(String n) { throw new UnsupportedOperationException(); }
        protected void syncSpi() {} protected void flushSpi() {}
    }
    static <T extends JDialog> void modal(JButton button,Class<T> type,Consumer<T> action) {
        long started=System.nanoTime();Throwable[] failure={null};
        javax.swing.Timer poll=new javax.swing.Timer(20,null);
        poll.addActionListener(event -> {
            for(Window w:frame.getOwnedWindows()) if(type.isInstance(w)&&w.isVisible()) {
                poll.stop();try { action.accept(type.cast(w)); }
                catch(Throwable ex) { failure[0]=ex;w.dispose(); }
                return;
            }
            if(System.nanoTime()-started>3_000_000_000L) {
                poll.stop();failure[0]=new AssertionError("Modal not shown: "+type.getName());
                for(Window w:frame.getOwnedWindows()) w.dispose();
            }
        });
        poll.start();try { button.doClick(0); } finally { poll.stop(); }
        if(failure[0]!=null) throw new AssertionError(failure[0]);
    }
    static void settle() throws Exception { Thread.sleep(350);SwingUtilities.invokeAndWait(()->{});Toolkit.getDefaultToolkit().sync(); }
    static boolean near(BufferedImage image,int x,int y,Color expected,int tolerance) {
        for(int dy=-2;dy<=2;dy++) for(int dx=-2;dx<=2;dx++) {
            int px=x+dx,py=y+dy;if(px<0||py<0||px>=image.getWidth()||py>=image.getHeight()) continue;
            Color actual=new Color(image.getRGB(px,py));
            if(Math.abs(actual.getRed()-expected.getRed())<=tolerance&&Math.abs(actual.getGreen()-expected.getGreen())<=tolerance&&Math.abs(actual.getBlue()-expected.getBlue())<=tolerance) return true;
        }
        return false;
    }
    static void capture(String name,boolean stone) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                BoardPanel panel=frame.getBoardPanel();Container content=frame.getContentPane();
                var transform=frame.getGraphicsConfiguration().getDefaultTransform();double sx=transform.getScaleX(),sy=transform.getScaleY();
                int before=paintCount("entries"),returned=paintCount("returns");
                System.out.printf("STATE %s frame=%s content=%s board=%s preferred=%s minimum=%s inTree=%s visible=%s showing=%s valid=%s parent=%s paintEntries=%d paintReturns=%d EDT=%s transform=%s%n",
                        name,frame.getSize(),content.getSize(),panel.getSize(),panel.getPreferredSize(),panel.getMinimumSize(),SwingUtilities.isDescendingFrom(panel,content),panel.isVisible(),panel.isShowing(),panel.isValid(),panel.getParent().getBounds(),before,returned,SwingUtilities.isEventDispatchThread(),transform);
                for(Component ancestor=panel;ancestor!=null;ancestor=ancestor.getParent()) System.out.printf("ANCESTOR %s bounds=%s visible=%s valid=%s%n",ancestor.getClass().getName(),ancestor.getBounds(),ancestor.isVisible(),ancestor.isValid());
                BufferedImage image=new BufferedImage((int)Math.ceil(content.getWidth()*sx),(int)Math.ceil(content.getHeight()*sy),BufferedImage.TYPE_INT_RGB);
                Graphics2D g=image.createGraphics();g.scale(sx,sy);try { content.printAll(g); } finally { g.dispose(); }
                Point p=SwingUtilities.convertPoint(panel,0,0,content);
                boolean grid=near(image,(int)Math.round((p.x+108)*sx),(int)Math.round((p.y+108)*sy),Theme.GRID,25);
                boolean black=near(image,(int)Math.round((p.x+306)*sx),(int)Math.round((p.y+298)*sy),Theme.BLACK_STONE,15);
                System.out.printf("PIXELS %s grid=%s blackStone=%s expectedStone=%s renderPaintCalls=%d%n",name,grid,black,stone,before<0?-1:paintCount("entries")-before);
                if(!grid||(stone&&!black)) pixelFailure=true;
                ImageIO.write(image,"png",output.resolve(name+".png").toFile());
                Rectangle frameBounds=frame.getBounds(),work=frame.getGraphicsConfiguration().getBounds();Insets inset=Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
                work=new Rectangle(work.x+inset.left,work.y+inset.top,work.width-inset.left-inset.right,work.height-inset.top-inset.bottom);
                System.out.printf("DISPLAY %s frameBounds=%s workArea=%s frameFits=%s toolbarButton=%s%n",name,frameBounds,work,work.contains(frameBounds),SwingUtilities.convertRectangle(frame.getBackToMenuButton().getParent(),frame.getBackToMenuButton().getBounds(),content));
            } catch(Exception ex) { throw new AssertionError(ex); }
        });
    }
    static void move() throws Exception {
        SwingUtilities.invokeAndWait(()->frame.getBoardPanel().dispatchEvent(new MouseEvent(frame.getBoardPanel(),MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,298,298,1,false)));
        for(int i=0;i<100;i++) { settle();boolean[] thinking={false};SwingUtilities.invokeAndWait(()->thinking[0]=frame.getBoardPanel().isAiThinking());if(!thinking[0])return; }
        throw new AssertionError("AI did not finish");
    }
    static JButton newGameButton(Container root) {
        for(Component c:root.getComponents()) {
            if(c instanceof JButton b&&Lang.newGame().equals(b.getText()))return b;
            if(c instanceof Container child) { JButton b=newGameButton(child);if(b!=null)return b; }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        output=Path.of(args[0]);Files.createDirectories(output);
        Thread.setDefaultUncaughtExceptionHandler((t,e)->{errors.add(e);e.printStackTrace();});
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            protected void dispatchEvent(AWTEvent event) { try { super.dispatchEvent(event); } catch(Throwable ex) { errors.add(ex);ex.printStackTrace(); } }
        });
        System.out.printf("ENV scaleFlag=%s java=%s loadedBoard=%s%n",System.getProperty("sun.java2d.uiScale"),System.getProperty("java.version"),BoardPanel.class.getProtectionDomain().getCodeSource().getLocation());
        try {
            SwingUtilities.invokeAndWait(()->{
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ex) { throw new AssertionError(ex); }
                Memory prefs=new Memory();prefs.put("soundEnabled","false");prefs.put("language","VI");frame=new MainFrame(new AppSettings(prefs));frame.setVisible(true);
            });settle();
            SwingUtilities.invokeAndWait(()->{
                frame.showMenu();
                modal(frame.getMainMenu().getProfileButton(),ProfileDialog.class,d->{d.getEditor().getNameField().setText("messi");d.getDoneButton().doClick(0);});
                modal(frame.getMainMenu().getPvpButton(),SetupDialog.class,d->{d.getSecondEditor().getNameField().setText("ronaldo");d.getStartButton().doClick(0);});
            });settle();capture("pvp-empty",false);move();capture("pvp-stone",true);
            SwingUtilities.invokeAndWait(()->newGameButton(frame.getContentPane()).doClick(0));settle();capture("pvp-new-empty",false);move();capture("pvp-new-stone",true);
            SwingUtilities.invokeAndWait(()->{
                frame.getBackToMenuButton().doClick(0);
                modal(frame.getMainMenu().getAiButton(),SetupDialog.class,d->d.getStartButton().doClick(0));
            });settle();capture("ai-empty",false);move();capture("ai-stone",true);
            SwingUtilities.invokeAndWait(()->newGameButton(frame.getContentPane()).doClick(0));settle();capture("ai-new-empty",false);move();capture("ai-new-stone",true);
            System.out.printf("RESULT pixelFailure=%s errors=%d paintEntries=%d paintReturns=%d%n",pixelFailure,errors.size(),paintCount("entries"),paintCount("returns"));
        } finally { SwingUtilities.invokeAndWait(()->{if(frame!=null)frame.dispose();}); }
        System.exit(errors.isEmpty()&&!pixelFailure?0:1);
    }
}
