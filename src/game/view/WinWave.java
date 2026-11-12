package game.view;

import game.model.Board;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;

/** One finite celebration, owned by the board. Painting never advances time. */
final class WinWave {
    static final long DURATION_NANOS = 3_000_000_000L;
    static final int PARTICLES_PER_STONE = 8;
    private final LongSupplier clock;
    private final Runnable repaint;
    private final Random random;
    private final Timer timer;
    private final List<TitleParticles.Particle> particles = new ArrayList<>();
    private final Ellipse2D.Float shape = new Ellipse2D.Float();
    private List<Board.Cell> line = List.of();
    private boolean active;
    private long startedNanos, lastTickNanos;
    private double elapsedSeconds;

    WinWave(LongSupplier clock, Runnable repaint, Random random) {
        this.clock = clock; this.repaint = repaint; this.random = random;
        timer = new Timer(16, event -> tick()); timer.setCoalesce(true);
    }

    void start(List<Board.Cell> winningLine) {
        requireEdt(); stop();
        if (winningLine.isEmpty()) return;
        line = List.copyOf(winningLine);
        startedNanos = lastTickNanos = clock.getAsLong();
        elapsedSeconds = 0; active = true;
        double side = BoardPanel.OFFSET * 2 + (Board.SIZE - 1) * BoardPanel.CELL_SIZE;
        for (Board.Cell cell : line) for (int i = 0; i < PARTICLES_PER_STONE; i++)
            particles.add(TitleParticles.burst(random,
                    (BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE) / side,
                    (BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE) / side));
        timer.start(); repaint.run();
    }

    void tick() { tick(clock.getAsLong()); }

    /** Same tick used by the timer; injectable time supports tests and phase captures. */
    void tick(long now) {
        requireEdt();
        if (!active) return; // A stopped Swing timer may already have a queued event.
        if (now - startedNanos >= DURATION_NANOS) { stop(); return; }
        double step = Math.max(0, now - lastTickNanos) / 1_000_000_000.0;
        elapsedSeconds = Math.max(0, now - startedNanos) / 1_000_000_000.0;
        lastTickNanos = now;
        particles.removeIf(particle -> !particle.advanceBurst(step));
        repaint.run();
    }

    void stop() {
        requireEdt();
        timer.stop(); active = false; particles.clear(); line = List.of(); elapsedSeconds = 0;
        repaint.run();
    }

    double intensity(int index) {
        if (!active || index < 0 || index >= line.size()) return 0;
        double head = elapsedSeconds / 3.0 * (line.size() + 1) - 1;
        double distance = (head - index) / 0.60;
        double envelope = Math.min(1, elapsedSeconds / 0.10) * Math.min(1, (3 - elapsedSeconds) / 0.25);
        return Math.exp(-0.5 * distance * distance) * Math.max(0, envelope);
    }

    void paint(Graphics2D graphics, int width, int height) {
        if (!active) return;
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < line.size(); i++) {
                Board.Cell cell = line.get(i);
                int cx = BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE;
                int cy = BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE;
                float strength = (float) intensity(i);
                if (strength < 0.01) continue;
                g.setComposite(AlphaComposite.SrcOver.derive(strength));
                g.setPaint(new RadialGradientPaint(cx, cy, 29, new float[]{0, 0.5f, 1},
                        new Color[]{new Color(255, 248, 205, 190), new Color(245, 213, 138, 115), new Color(245, 213, 138, 0)}));
                g.fillOval(cx - 29, cy - 29, 58, 58);
            }
            for (int i = 0; i < particles.size(); i++) {
                TitleParticles.Particle particle = particles.get(i);
                int diameter = particle.diameter();
                shape.setFrame(particle.x() * width - diameter / 2.0, particle.y() * height - diameter / 2.0, diameter, diameter);
                g.setComposite(AlphaComposite.SrcOver.derive(particle.opacity()));
                g.setColor(TitleParticles.color(i)); g.fill(shape);
            }
        } finally { g.dispose(); }
    }

    boolean isActive() { return active; }
    int particleCount() { return particles.size(); }
    List<Board.Cell> line() { return line; }
    double elapsedSeconds() { return elapsedSeconds; }
    long startedNanos() { return startedNanos; }
    Timer timer() { return timer; }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Win animation state requires EDT");
    }
}
