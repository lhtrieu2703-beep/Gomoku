package game.view;

import java.util.Random;
import java.awt.Color;

/** Title-only particle state in normalized screen coordinates. */
final class TitleParticles {
    private static final int COUNT = 36;
    private static final double FULL_TURN = 2 * Math.PI;
    private static final Color[] COLORS = {new Color(0xF5D58A), new Color(0xE8BC61), Theme.BRASS};

    static Color color(int index) { return COLORS[index % COLORS.length]; }
    static Particle burst(Random random, double x, double y) { return new Particle(random, x, y); }

    private final Random random;
    private final Particle[] particles = new Particle[COUNT];

    TitleParticles(Random random) {
        this.random = random;
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(random);
        }
    }

    int size() { return particles.length; }
    Particle get(int index) { return particles[index]; }

    void advance(double seconds) {
        for (Particle particle : particles) {
            particle.x += particle.drift * seconds;
            particle.y -= particle.rise * seconds;
            if (particle.y < 0 || particle.x < 0 || particle.x >= 1) {
                particle.x = 0.05 + random.nextDouble() * 0.90;
                particle.y = 1;
            }
            particle.phase = (particle.phase + particle.twinkle * seconds) % FULL_TURN;
            particle.updateOpacity();
        }
    }

    static final class Particle {
        private double x, y, phase;
        private final double drift, rise, twinkle;
        private final int diameter;
        private float opacity;
        private double age, lifetime, burstVx, burstVy;

        private Particle(Random random) {
            x = random.nextDouble();
            y = random.nextDouble();
            drift = (random.nextDouble() - 0.5) * 0.028;
            rise = 0.018 + random.nextDouble() * 0.022;
            phase = random.nextDouble() * FULL_TURN;
            twinkle = 1.2 + random.nextDouble() * 1.8;
            diameter = random.nextInt(4) + 1;
            updateOpacity();
        }

        /** Finite one-shot motion; title construction/advance/respawn stay unchanged. */
        private Particle(Random random, double originX, double originY) {
            this(random);
            x = originX; y = originY;
            burstVx = (random.nextDouble() - 0.5) * 0.36;
            burstVy = -(0.10 + random.nextDouble() * 0.17);
            lifetime = 1.35 + random.nextDouble() * 0.80;
            opacity = 0;
        }

        boolean advanceBurst(double seconds) {
            age += seconds;
            if (age >= lifetime) { opacity = 0; return false; }
            double gravity = 0.18;
            x += burstVx * seconds;
            y += burstVy * seconds + gravity * seconds * seconds / 2;
            burstVy += gravity * seconds;
            phase = (phase + twinkle * seconds) % FULL_TURN;
            updateOpacity();
            opacity = (float) Math.max(0, opacity * Math.min(1, age / 0.07) * (1 - age / lifetime));
            return true;
        }

        private void updateOpacity() {
            double edgeFade = Math.min(1, Math.min(y, 1 - y) * 10);
            double pulse = (Math.sin(phase) + 1) / 2;
            opacity = (float) (edgeFade * (0.18 + 0.58 * pulse));
        }

        double x() { return x; }
        double y() { return y; }
        int diameter() { return diameter; }
        float opacity() { return opacity; }
    }
}
