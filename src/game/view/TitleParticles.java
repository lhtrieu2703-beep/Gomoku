package game.view;

import java.util.Random;

/** Title-only particle state in normalized screen coordinates. */
final class TitleParticles {
    private static final int COUNT = 36;
    private static final double FULL_TURN = 2 * Math.PI;

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
