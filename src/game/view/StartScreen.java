package game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.util.Random;

/** Title only. Timer lifetime is the visible title card lifetime. */
public final class StartScreen extends BackgroundPanel {
    private static final int FRAME_DELAY_MS = 33;
    private static final Font HINT_FONT = Theme.font(Font.PLAIN, 17);
    private static final Stroke HINT_STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color[] PARTICLE_COLORS = {new Color(0xF5D58A), new Color(0xE8BC61), Theme.BRASS};
    private static final AlphaComposite[] OPACITY = new AlphaComposite[101];

    static {
        for (int i = 0; i < OPACITY.length; i++) {
            OPACITY[i] = AlphaComposite.SrcOver.derive(i / 100f);
        }
    }

    private final Runnable enterMenu;
    private final TitleParticles particles = new TitleParticles(new Random());
    private final Ellipse2D.Float particleShape = new Ellipse2D.Float();
    private final Timer timer;
    private boolean active;
    private long lastFrameNanos;
    private double phase;
    private String hintText;
    private FontRenderContext hintContext;
    private Shape hintOutline;
    private int hintWidth;

    public StartScreen(Runnable enterMenu) {
        this.enterMenu=enterMenu; setLayout(null); setFocusable(true); Logo logo=new Logo(); add(logo);
        timer = new Timer(FRAME_DELAY_MS, e -> animate());
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) refreshAnimationState();
        });
        MouseAdapter click=new MouseAdapter() { public void mousePressed(MouseEvent e) { advance(); } };
        addMouseListener(click); logo.addMouseListener(click);
    }
    public void advance() { enterMenu.run(); }
    public void setActive(boolean active) {
        this.active = active;
        refreshAnimationState();
    }

    void refreshAnimationState() {
        Window window = SwingUtilities.getWindowAncestor(this);
        boolean minimized = window instanceof Frame frame && (frame.getExtendedState() & Frame.ICONIFIED) != 0;
        if (active && isShowing() && !minimized) {
            if (!timer.isRunning()) {
                lastFrameNanos = System.nanoTime();
                timer.start();
            }
        } else {
            timer.stop();
        }
    }

    private void animate() {
        long now = System.nanoTime();
        double elapsed = Math.min((now - lastFrameNanos) / 1_000_000_000.0, 0.1);
        lastFrameNanos = now;
        particles.advance(elapsed);
        phase = (phase + 1.25 * elapsed) % (2 * Math.PI);
        repaint();
    }

    public boolean isHintTimerRunning() { return timer.isRunning(); }
    @Override public void doLayout() { getComponent(0).setBounds(40,getHeight()/4,getWidth()-80,160); }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintParticles(g);
        paintHint(g);
        g.dispose();
    }

    private void paintParticles(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < particles.size(); i++) {
            TitleParticles.Particle particle = particles.get(i);
            int diameter = particle.diameter();
            float x = (float) (particle.x() * width) - diameter / 2f;
            float y = (float) (particle.y() * height) - diameter / 2f;
            particleShape.setFrame(x, y, diameter, diameter);
            g.setComposite(OPACITY[Math.round(particle.opacity() * 100)]);
            g.setColor(PARTICLE_COLORS[i % PARTICLE_COLORS.length]);
            g.fill(particleShape);
        }
    }

    private void paintHint(Graphics2D g) {
        String hint = Lang.titleHint();
        FontRenderContext context = g.getFontRenderContext();
        if (!hint.equals(hintText) || !context.equals(hintContext)) {
            hintOutline = new TextLayout(hint, HINT_FONT, context).getOutline(null);
            hintWidth = g.getFontMetrics(HINT_FONT).stringWidth(hint);
            hintText = hint;
            hintContext = context;
        }
        int x = (getWidth() - hintWidth) / 2;
        int y = getHeight() * 3 / 4;
        g.translate(x, y);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Theme.DARK);
        g.setStroke(HINT_STROKE);
        g.draw(hintOutline);
        float opacity = 0.74f + 0.26f * (float) ((Math.sin(phase) + 1) / 2);
        g.setComposite(OPACITY[Math.round(opacity * 100)]);
        g.setColor(Theme.CREAM);
        g.fill(hintOutline);
    }
}
