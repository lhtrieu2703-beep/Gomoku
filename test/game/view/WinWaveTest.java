package game.view;

import game.ai.MinimaxAI;
import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.prefs.AbstractPreferences;
import static org.junit.jupiter.api.Assertions.*;

class WinWaveTest {
    private Lang.Language previousLanguage;
    private boolean previousSound;

    @BeforeEach void remember() {
        previousLanguage = Lang.get(); previousSound = SoundManager.isSoundEnabled();
        SoundManager.setSoundEnabled(false);
    }
    @AfterEach void restore() throws Exception {
        SwingUtilities.invokeAndWait(() -> { Lang.set(previousLanguage); SoundManager.setSoundEnabled(previousSound); });
    }
    private void board(Consumer<BoardPanel> check) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BoardPanel panel = new BoardPanel(new Board(), 0, () -> 0);
            panel.setGameMode(BoardPanel.GameMode.PVP); panel.setSize(panel.getPreferredSize());
            try { check.accept(panel); } finally { panel.setEnabled(false); }
        });
    }
    private void frame(Consumer<MainFrame> check) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var prefs = new Memory(); prefs.put("language", "VI"); prefs.putBoolean("soundEnabled", false);
            MainFrame frame = new MainFrame(new AppSettings(prefs), 0);
            frame.updateProfile(new PlayerProfile("Ngọc Ánh", 0));
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            try { check.accept(frame); } finally { frame.dispose(); }
        });
    }
    private static void win(BoardPanel panel, Piece piece) {
        for (int c = 0; c < 4; c++) {
            panel.executeMoveForTest(7, c + 4, piece);
            panel.executeMoveForTest(0, c * 2, piece.opposite());
        }
        panel.executeMoveForTest(7, 8, piece);
    }
    private static void stopped(WinWave wave) {
        assertFalse(wave.isActive()); assertFalse(wave.timer().isRunning());
        assertEquals(0, wave.particleCount()); assertTrue(wave.line().isEmpty());
    }

    @ParameterizedTest @CsvSource({"PVP,X", "PVP,O", "PV_AI,X", "PV_AI,O", "ONLINE,X", "ONLINE,O"})
    void aWinningLineStartsTheWaveForEitherSideIncludingAi(BoardPanel.GameMode mode, Piece piece) throws Exception {
        board(panel -> {
            panel.setGameMode(mode); win(panel, piece);
            WinWave wave = panel.winWave();
            assertTrue(wave.isActive()); assertTrue(wave.timer().isRunning());
            assertEquals(panel.getWinningLine(), wave.line());
            assertEquals(5 * WinWave.PARTICLES_PER_STONE, wave.particleCount());
            assertEquals(0, wave.elapsedSeconds());
        });
    }

    @Test void fullBoardDrawNeverStartsAnEffect() throws Exception {
        board(panel -> {
            for (int r = 0; r < 15; r++) for (int c = 0; c < 15; c++)
                panel.executeMoveForTest(r, c, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O);
            assertTrue(panel.isGameOver()); assertEquals(Piece.NONE, panel.getWinner());
            stopped(panel.winWave());
        });
    }

    @Test void ruleBBlockedFiveDoesNotTriggerAnAnimation() throws Exception {
        board(panel -> {
            panel.executeMoveForTest(7, 3, Piece.O); panel.executeMoveForTest(7, 9, Piece.O);
            for (int col = 4; col <= 8; col++) panel.executeMoveForTest(7, col, Piece.X);
            assertFalse(panel.isGameOver()); assertTrue(panel.getWinningLine().isEmpty()); stopped(panel.winWave());
        });
    }

    @ParameterizedTest @ValueSource(ints = {5, 9, 15})
    void waveVisitsEveryStoneInOrderAcrossTheWholeOverline(int length) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicLong clock = new AtomicLong(10_000);
            WinWave wave = new WinWave(clock::get, () -> assertTrue(SwingUtilities.isEventDispatchThread()), new Random(21));
            List<Board.Cell> line = new ArrayList<>();
            for (int i = 0; i < length; i++) line.add(new Board.Cell(7, i));
            try {
                wave.start(line);
                for (int i = 0; i < length; i++) {
                    clock.set(wave.startedNanos() + Math.round(3_000_000_000.0 * (i + 1) / (length + 1)));
                    wave.tick();
                    assertTrue(wave.intensity(i) > 0.70, "Wave should illuminate stone " + i);
                    for (int other = 0; other < length; other++) if (other != i)
                        assertTrue(wave.intensity(i) > wave.intensity(other));
                }
                assertEquals(length, wave.line().size());
            } finally { wave.stop(); }
        });
    }

    @Test void fakeClockStopsAtThreeSecondsWithoutSleepingAndQueuedTicksCannotRestartIt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicLong clock = new AtomicLong(123);
            WinWave wave = new WinWave(clock::get, () -> {}, new Random(5));
            wave.start(List.of(new Board.Cell(0, 0), new Board.Cell(0, 1)));
            long start = wave.startedNanos();
            clock.set(start + WinWave.DURATION_NANOS - 1); wave.tick();
            assertTrue(wave.isActive()); assertTrue(wave.timer().isRunning());
            clock.incrementAndGet(); wave.tick(); stopped(wave);
            wave.timer().getActionListeners()[0].actionPerformed(new ActionEvent(wave.timer(), 0, "queued"));
            stopped(wave);
        });
    }

    @Test void oneBurstUsesTheTitleParticleModelWithoutRespawning() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicLong clock = new AtomicLong();
            WinWave wave = new WinWave(clock::get, () -> {}, new Random(8));
            try {
                wave.start(List.of(new Board.Cell(7, 7)));
                assertEquals(8, wave.particleCount());
                int previous = wave.particleCount();
                for (int millis : new int[]{50, 300, 800, 1400, 2200}) {
                    clock.set(millis * 1_000_000L); wave.tick();
                    assertTrue(wave.particleCount() <= previous); previous = wave.particleCount();
                }
                assertTrue(wave.isActive()); assertEquals(0, wave.particleCount());
                TitleParticles.Particle particle = TitleParticles.burst(new Random(9), 0.5, 0.5);
                assertTrue(particle.advanceBurst(0.1));
                assertTrue(particle.y() < 0.5); assertTrue(particle.opacity() >= 0 && particle.opacity() <= 1);
                assertFalse(particle.advanceBurst(3)); assertEquals(0, particle.opacity());
            } finally { wave.stop(); }
        });
    }

    @ParameterizedTest @ValueSource(strings = {"undo", "new-overlay", "new-toolbar", "menu-overlay", "menu-toolbar", "dispose"})
    void allExitActionsImmediatelyStopAndClearTheEffect(String action) throws Exception {
        frame(frame -> {
            BoardPanel panel = frame.getBoardPanel(); win(panel, Piece.X);
            WinWave wave = panel.winWave();
            assertTrue(wave.isActive()); assertEquals(1, frame.getScoreTracker().getHumanWins());
            switch (action) {
                case "undo" -> frame.getUndoButton().doClick(0);
                case "new-overlay" -> panel.getResultOverlay().getNewGameButton().doClick(0);
                case "new-toolbar" -> frame.getNewGameButton().doClick(0);
                case "menu-overlay" -> panel.getResultOverlay().getMenuButton().doClick(0);
                case "menu-toolbar" -> frame.getBackToMenuButton().doClick(0);
                default -> frame.dispose();
            }
            stopped(wave); assertFalse(panel.getResultOverlay().isVisible());
            assertEquals(action.equals("undo") ? 0 : 1, frame.getScoreTracker().getHumanWins());
            if (!action.equals("dispose")) assertTrue(panel.getWinningLine().isEmpty());
        });
    }

    @Test void repeatedRoundsReuseOneTimerAndLeaveNoParticlesOrRunningTimer() throws Exception {
        board(panel -> {
            WinWave wave = panel.winWave(); var timer = wave.timer();
            for (int i = 0; i < 100; i++) {
                panel.resetGame(); win(panel, i % 2 == 0 ? Piece.X : Piece.O);
                assertSame(timer, wave.timer()); assertTrue(timer.isRunning());
                assertEquals(1, timer.getActionListeners().length);
                assertEquals(40, wave.particleCount());
                if (i % 2 == 0) panel.undoMove(); else panel.resetGame();
                stopped(wave);
                timer.getActionListeners()[0].actionPerformed(new ActionEvent(timer, 0, "old-round"));
                stopped(wave);
            }
        });
    }

    @ParameterizedTest @EnumSource(value = Piece.class, names = {"X", "O"})
    void undoAfterAiGameWinStopsTheEffectAndRevokesTheCorrectScore(Piece piece) throws Exception {
        frame(frame -> {
            frame.beginGame(BoardPanel.GameMode.PV_AI, MinimaxAI.Difficulty.MEDIUM);
            BoardPanel panel = frame.getBoardPanel(); win(panel, piece);
            assertTrue(panel.winWave().isActive());
            assertEquals(piece == Piece.X ? 1 : 0, frame.getScoreTracker().getHumanWins());
            assertEquals(piece == Piece.O ? 1 : 0, frame.getScoreTracker().getAiWins());
            frame.getUndoButton().doClick(0); stopped(panel.winWave());
            assertEquals(0, frame.getScoreTracker().getHumanWins()); assertEquals(0, frame.getScoreTracker().getAiWins());
        });
    }

    @Test void repaintDoesNotAdvanceTheWaveOrCreateOrRestartATimer() throws Exception {
        board(panel -> {
            paint(panel); stopped(panel.winWave()); win(panel, Piece.X);
            WinWave wave = panel.winWave(); var timer = wave.timer();
            wave.tick(wave.startedNanos() + 500_000_000);
            for (int i = 0; i < 10; i++) paint(panel);
            assertSame(timer, wave.timer()); assertEquals(0.5, wave.elapsedSeconds(), 1e-9);
            assertEquals(40, wave.particleCount()); assertEquals(1, timer.getActionListeners().length);
        });
    }

    @Test void removingTheBoardStopsTheAnimation() throws Exception {
        board(panel -> {
            panel.addNotify(); win(panel, Piece.X); assertTrue(panel.winWave().isActive());
            panel.removeNotify(); stopped(panel.winWave());
        });
    }

    @Test void changingLanguageOrViewingTheBoardDoesNotRestartOrStopTheWave() throws Exception {
        frame(frame -> {
            BoardPanel panel = frame.getBoardPanel(); win(panel, Piece.X);
            WinWave wave = panel.winWave(); long start = wave.startedNanos();
            wave.tick(start + 400_000_000); var timer = wave.timer(); int count = wave.particleCount();
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language);
                assertTrue(wave.isActive()); assertSame(timer, wave.timer());
                assertEquals(start, wave.startedNanos()); assertEquals(0.4, wave.elapsedSeconds(), 1e-9);
                assertEquals(count, wave.particleCount()); assertEquals(1, frame.getScoreTracker().getHumanWins());
            }
            panel.getResultOverlay().getViewBoardButton().doClick(0);
            assertTrue(wave.isActive()); assertEquals(start, wave.startedNanos());
            panel.getShowResultButton().doClick(0); assertTrue(wave.isActive());
        });
    }

    @Test void animationNeverChangesOverlayPixelsOrBoundsAndFrameKeepsItsSize() throws Exception {
        frame(frame -> {
            BoardPanel panel = frame.getBoardPanel();
            for (int i = 0; i < 9; i++) if (i != 4) panel.executeMoveForTest(i + 3, i, Piece.X);
            panel.executeMoveForTest(7, 4, Piece.X); layout(frame);
            assertEquals(9, panel.getWinningLine().size()); assertEquals(panel.getWinningLine(), panel.winWave().line());
            Rectangle bounds = panel.getResultOverlay().getBounds(); BufferedImage initial = paint(panel);
            WinWave wave = panel.winWave(); wave.tick(wave.startedNanos() + 1_000_000_000);
            BufferedImage animated = paint(panel);
            assertEquals(bounds, panel.getResultOverlay().getBounds()); assertEquals(new Dimension(774, 795), frame.getSize());
            for (int y = bounds.y + 3; y < bounds.y + bounds.height - 3; y++)
                for (int x = bounds.x + 3; x < bounds.x + bounds.width - 3; x++)
                    assertEquals(initial.getRGB((int)(x * 1.25), (int)(y * 1.25)), animated.getRGB((int)(x * 1.25), (int)(y * 1.25)), "Overlay pixel changed");
        });
    }

    @Test void afterThreeSecondsBoardReturnsToExactlyTheSameStaticPixelsAndRedWhiteRings() throws Exception {
        board(panel -> {
            win(panel, Piece.X); BufferedImage initial = paint(panel); WinWave wave = panel.winWave();
            wave.tick(wave.startedNanos() + 1_500_000_000); BufferedImage lit = paint(panel);
            assertFalse(Arrays.equals(pixels(initial), pixels(lit)), "The wave must visibly change the board");
            wave.tick(wave.startedNanos() + 3_000_000_000L); stopped(wave);
            assertArrayEquals(pixels(initial), pixels(paint(panel)));
            assertEquals(5, panel.getWinningLine().size());
            assertTrue(Arrays.stream(pixels(initial)).filter(rgb -> (rgb & 0xFFFFFF) == 0xE53935).count() > 500);
        });
    }

    @Test void modelRejectsStateUpdatesOffEdt() {
        WinWave wave = new WinWave(() -> 0, () -> {}, new Random(1));
        assertThrows(IllegalStateException.class, () -> wave.start(List.of(new Board.Cell(0, 0))));
        assertThrows(IllegalStateException.class, wave::tick);
        assertThrows(IllegalStateException.class, wave::stop);
    }

    private static void layout(Container root) {
        root.doLayout(); for (Component child : root.getComponents()) if (child instanceof Container container) layout(container);
    }
    private static BufferedImage paint(BoardPanel panel) {
        BufferedImage image = new BufferedImage((int)Math.ceil(panel.getWidth() * 1.25), (int)Math.ceil(panel.getHeight() * 1.25), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { graphics.scale(1.25, 1.25); panel.paint(graphics); } finally { graphics.dispose(); }
        return image;
    }
    private static int[] pixels(BufferedImage image) { return ((DataBufferInt)image.getRaster().getDataBuffer()).getData(); }

    private static final class Memory extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        Memory() { super(null, ""); }
        protected void putSpi(String k, String v) { values.put(k, v); }
        protected String getSpi(String k) { return values.get(k); }
        protected void removeSpi(String k) { values.remove(k); }
        protected void removeNodeSpi() { values.clear(); }
        protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
        protected String[] childrenNamesSpi() { return new String[0]; }
        protected AbstractPreferences childSpi(String n) { throw new UnsupportedOperationException(); }
        protected void syncSpi() {} protected void flushSpi() {}
    }
}
