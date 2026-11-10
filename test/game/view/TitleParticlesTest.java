package game.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleParticlesTest {
    @Test
    void particlesStayWithinTheNormalizedScreenAfterManyUpdates() {
        TitleParticles particles = new TitleParticles(new Random(41));
        assertEquals(36, particles.size());
        for (int step = 0; step < 3_000; step++) {
            particles.advance(1.0 / 30);
            for (int i = 0; i < particles.size(); i++) {
                TitleParticles.Particle particle = particles.get(i);
                assertTrue(particle.x() >= 0 && particle.x() < 1);
                assertTrue(particle.y() >= 0 && particle.y() <= 1);
                assertTrue(particle.opacity() >= 0 && particle.opacity() <= 1);
                assertTrue(particle.diameter() >= 1 && particle.diameter() <= 4);
            }
        }
    }

    @ParameterizedTest(name = "leaves through the side: {0}")
    @ValueSource(booleans = {false, true})
    void leavingTheScreenRespawnsAtTheBottom(boolean exitsSide) {
        Random edgeStart = new Random(8) {
            private int calls;

            @Override public double nextDouble() {
                return switch (calls++) {
                    case 0 -> exitsSide ? 0.999 : 0.5;
                    case 1 -> exitsSide ? 0.5 : 0.001;
                    case 2 -> 0.99;
                    default -> super.nextDouble();
                };
            }
        };
        TitleParticles particles = new TitleParticles(edgeStart);
        TitleParticles.Particle first = particles.get(0);

        particles.advance(0.1);

        assertEquals(1.0, first.y());
        assertTrue(first.x() >= 0.05 && first.x() <= 0.95);
        assertEquals(0.0f, first.opacity());
        particles.advance(0.1);
        assertTrue(first.y() < 1);
    }

    @Test
    void matchingSeedsProduceIdenticalMotionAndOpacity() {
        TitleParticles first = new TitleParticles(new Random(517));
        TitleParticles second = new TitleParticles(new Random(517));
        for (int step = 0; step < 1_000; step++) {
            double elapsed = (step % 3 + 1) / 60.0;
            first.advance(elapsed);
            second.advance(elapsed);
            for (int i = 0; i < first.size(); i++) {
                TitleParticles.Particle a = first.get(i);
                TitleParticles.Particle b = second.get(i);
                assertEquals(a.x(), b.x());
                assertEquals(a.y(), b.y());
                assertEquals(a.opacity(), b.opacity());
                assertEquals(a.diameter(), b.diameter());
            }
        }
    }

    @Test
    void timerFollowsTitleCardAndParentVisibilityWithoutAWindow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CardLayout layout = new CardLayout();
            JPanel cards = new JPanel(layout);
            StartScreen title = new StartScreen(() -> {});
            cards.add(title, "title");
            cards.add(new JPanel(), "menu");
            title.setActive(true);
            assertFalse(title.isHintTimerRunning());

            cards.addNotify();
            try {
                assertTrue(title.isHintTimerRunning());
                layout.show(cards, "menu");
                assertFalse(title.isHintTimerRunning());
                layout.show(cards, "title");
                assertTrue(title.isHintTimerRunning());
                cards.setVisible(false);
                assertFalse(title.isHintTimerRunning());
                cards.setVisible(true);
                assertTrue(title.isHintTimerRunning());
                title.setActive(false);
                assertFalse(title.isHintTimerRunning());
                title.setActive(true);
                assertTrue(title.isHintTimerRunning());
            } finally {
                cards.removeNotify();
            }
            assertFalse(title.isHintTimerRunning());
        });
    }
}
