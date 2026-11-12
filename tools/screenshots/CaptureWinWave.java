package game.view;

import game.ai.MinimaxAI;
import game.model.Board;
import game.model.Piece;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.prefs.AbstractPreferences;

/** Native 125% screenshots of deterministic, frozen production animation ticks. */
public final class CaptureWinWave {
    private static final Path OUTPUT = Path.of("docs/screenshots/task5f-win-wave");
    private static final List<Throwable> errors = new CopyOnWriteArrayList<>();
    private static MainFrame frame;
    private static Robot robot;

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { errors.add(error); error.printStackTrace(); });
        try {
            Files.createDirectories(OUTPUT); robot = new Robot();
            edt(() -> {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Memory prefs = new Memory(); prefs.put("language", "VI"); prefs.putBoolean("soundEnabled", false);
                frame = new MainFrame(new AppSettings(prefs), 0);
                frame.updateProfile(new PlayerProfile("Ngọc Ánh", 0)); frame.setSecondProfile(new PlayerProfile("Trần Minh", 1));
                frame.setLocation(24, 16); frame.setVisible(true);
                if (frame.getGraphicsConfiguration().getDefaultTransform().getScaleX() != 1.25 || !frame.getSize().equals(new Dimension(774, 795)))
                    throw new IllegalStateException("Requires real DPI 125% and frame 774x795");
                System.out.println("NATIVE_DPI 1.25 FRAME 774x795 FORCE_SCALE none");
                System.out.println("FIXTURE legal winning lines through executeMoveForTest; pause one production timer and use its actual tick(long) for phase captures");
                begin(Piece.X, 5, 4, 0, 1, 5, false); return null;
            });
            phase(500); capture("01_horizontal_wave_start.png");
            phase(1500); capture("02_horizontal_wave_middle.png");
            phase(2500); capture("03_horizontal_wave_end.png");
            edt(() -> { begin(Piece.X, 5, 4, 0, 1, 5, false); return null; });
            phase(220); capture("04_gold_burst.png");
            phase(3000); capture("05_after_stop_static.png");
            edt(() -> {
                frame.selectBoardTheme(BoardTheme.CHARCOAL_STONE); frame.selectPieceStyle(PieceStyle.GLASS);
                begin(Piece.O, 3, 8, 1, 0, 5, false); return null;
            });
            phase(1500); capture("06_vertical_wave.png");
            edt(() -> {
                frame.selectBoardTheme(BoardTheme.DARK_JADE); frame.selectPieceStyle(PieceStyle.METALLIC);
                begin(Piece.X, 3, 4, 1, 1, 5, false); return null;
            });
            phase(1500); capture("07_diagonal_wave.png");
            edt(() -> {
                frame.selectBoardTheme(BoardTheme.WARM_WOOD); frame.selectPieceStyle(PieceStyle.CLASSIC);
                begin(Piece.X, 3, 0, 1, 1, 9, false); return null;
            });
            phase(1500); capture("08_nine_stone_overline.png");
            edt(() -> {
                frame.selectPieceStyle(PieceStyle.GLASS); begin(Piece.O, 11, 4, 0, 1, 5, true); return null;
            });
            phase(1500); capture("09_ai_winner_wave.png");
            if (!errors.isEmpty()) throw new IllegalStateException("EDT errors: " + errors);
            System.out.println("CAPTURED 9 ERRORS 0");
        } finally { if (frame != null) edt(() -> { frame.dispose(); return null; }); }
        System.exit(0);
    }

    private static void begin(Piece piece, int row, int col, int dr, int dc, int length, boolean ai) {
        frame.beginGame(ai ? BoardPanel.GameMode.PV_AI : BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
        BoardPanel panel = frame.getBoardPanel(); int gap = length / 2;
        for (int i = 0; i < length; i++) if (i != gap) panel.executeMoveForTest(row + i * dr, col + i * dc, piece);
        panel.executeMoveForTest(row + gap * dr, col + gap * dc, piece);
        if (panel.getWinningLine().size() != length || !panel.winWave().isActive()) throw new IllegalStateException("Missing winning effect");
        panel.winWave().timer().stop(); // Freeze only in this capture helper; never in product flows.
    }

    private static void phase(long millis) throws Exception {
        edt(() -> {
            WinWave wave = frame.getBoardPanel().winWave(); wave.tick(wave.startedNanos() + millis * 1_000_000);
            frame.repaint(); return null;
        });
    }

    private static void capture(String name) throws Exception {
        CountDownLatch painted = new CountDownLatch(1);
        edt(() -> {
            frame.toFront(); frame.requestFocus(); frame.repaint();
            javax.swing.Timer settle = new javax.swing.Timer(200, event -> painted.countDown());
            settle.setRepeats(false); settle.start(); return null;
        });
        if (!painted.await(3, TimeUnit.SECONDS)) throw new IllegalStateException("Capture paint timeout");
        robot.mouseMove(0, 0); robot.waitForIdle();
        Rectangle bounds = edt(frame::getBounds);
        if (!edt(frame::isActive)) throw new IllegalStateException("Capture window is occluded");
        var variants = robot.createMultiResolutionScreenCapture(bounds).getResolutionVariants();
        BufferedImage image = (BufferedImage) variants.get(variants.size() - 1);
        edt(() -> {
            BoardPanel panel = frame.getBoardPanel(); Point origin = panel.getLocationOnScreen();
            for (Board.Cell cell : panel.getWinningLine()) {
                int cx = (int)Math.round((origin.x - bounds.x + BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE) * 1.25);
                int cy = (int)Math.round((origin.y - bounds.y + BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE) * 1.25);
                int red = 0;
                for (int y = cy - 25; y <= cy + 25; y++) for (int x = cx - 25; x <= cx + 25; x++) {
                    Color color = new Color(image.getRGB(x, y));
                    if (Math.abs(color.getRed() - 229) <= 6 && Math.abs(color.getGreen() - 57) <= 6 && Math.abs(color.getBlue() - 53) <= 6) red++;
                }
                if (red < 150) throw new IllegalStateException("Red ring covered: " + cell + " pixels=" + red);
            }
            WinWave wave = panel.winWave();
            System.out.println("CAPTURE " + name + " native=" + image.getWidth() + "x" + image.getHeight()
                    + " seconds=" + wave.elapsedSeconds() + " active=" + wave.isActive() + " timer=" + wave.timer().isRunning()
                    + " particles=" + wave.particleCount() + " winningLine=" + panel.getWinningLine()
                    + " overlay=" + panel.getResultOverlay().getBounds() + " frame=" + frame.getSize());
            return null;
        });
        ImageIO.write(image, "png", OUTPUT.resolve(name).toFile());
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action); SwingUtilities.invokeAndWait(task); return task.get();
    }
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
