import game.ai.MinimaxAI;
import game.model.*;
import game.view.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.AbstractPreferences;

/** Deterministic result fixtures, actual native screen pixels at the display's 125% DPI. */
public final class CaptureWinOverlay {
    private static MainFrame frame;
    private static Robot robot;
    private static final Path OUTPUT = Path.of("docs/screenshots/task5e-win-overlay");
    private static final java.util.List<Throwable> errors = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { errors.add(error); error.printStackTrace(); });
        int result = 1;
        try {
            Files.createDirectories(OUTPUT);
            robot = new Robot();
            edt(() -> {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                var prefs = new Memory(); prefs.put("language", "VI"); prefs.putBoolean("soundEnabled", false);
                frame = new MainFrame(new AppSettings(prefs), 0);
                frame.updateProfile(new PlayerProfile("Ngọc Ánh", 0));
                frame.setSecondProfile(new PlayerProfile("Trần Minh", 1));
                frame.setLocation(24, 16); frame.setVisible(true);
                double scale = frame.getGraphicsConfiguration().getDefaultTransform().getScaleX();
                if (scale != 1.25 || !frame.getSize().equals(new Dimension(774, 795)))
                    throw new IllegalStateException("Expected native 125%, 774x795; got " + scale + " " + frame.getSize());
                System.out.println("NATIVE_DPI " + scale + " FRAME " + frame.getSize() + " FORCE_SCALE none");
                System.out.println("FIXTURES executeMoveForTest through production win/draw/score/overlay callbacks; no AI search claim");
                return null;
            });
            edt(() -> {
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                win(Piece.X, 3); return null;
            });
            capture("01_vi_horizontal_win.png");
            edt(() -> {
                frame.selectLanguage(Lang.Language.EN); frame.setSecondProfile(new PlayerProfile("Oliver", 1));
                frame.selectBoardTheme(BoardTheme.CHARCOAL_STONE); frame.selectPieceStyle(PieceStyle.GLASS);
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                win(Piece.O, 11); return null;
            });
            capture("02_en_win.png");
            edt(() -> {
                frame.selectLanguage(Lang.Language.VI);
                frame.selectBoardTheme(BoardTheme.DARK_JADE); frame.selectPieceStyle(PieceStyle.METALLIC);
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                BoardPanel panel = frame.getBoardPanel();
                for (int r = 0; r < 15; r++) for (int c = 0; c < 15; c++)
                    panel.executeMoveForTest(r, c, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O);
                return null;
            });
            capture("03_vi_draw.png");
            edt(() -> {
                frame.selectLanguage(Lang.Language.VI);
                frame.selectBoardTheme(BoardTheme.WARM_WOOD); frame.selectPieceStyle(PieceStyle.GLASS);
                frame.beginGame(BoardPanel.GameMode.PV_AI, MinimaxAI.Difficulty.MEDIUM);
                win(Piece.O, 11); return null;
            });
            capture("04_vi_ai_wins.png");
            edt(() -> {
                frame.selectLanguage(Lang.Language.PT);
                frame.updateProfile(new PlayerProfile("WWWWWWWWWWWWWWWW", 0));
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                win(Piece.X, 3); return null;
            });
            capture("05_pt_long_name.png");
            edt(() -> {
                frame.selectLanguage(Lang.Language.VI);
                frame.getBoardPanel().getResultOverlay().getViewBoardButton().doClick(0); return null;
            });
            capture("06_view_board_restore_button.png");
            edt(() -> {
                frame.updateProfile(new PlayerProfile("Ngọc Ánh", 0));
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                BoardPanel panel = frame.getBoardPanel();
                for (int i = 0; i < 9; i++) if (i != 4) panel.executeMoveForTest(i + 3, i, Piece.X);
                panel.executeMoveForTest(7, 4, Piece.X); return null;
            });
            capture("07_compact_overline.png");
            if (!errors.isEmpty()) throw new IllegalStateException("EDT errors: " + errors);
            result = 0;
        } finally {
            if (frame != null) edt(() -> { frame.dispose(); return null; });
        }
        System.out.println("CAPTURE_EXIT_CODE " + result);
        System.exit(result);
    }

    private static void win(Piece piece, int row) {
        BoardPanel panel = frame.getBoardPanel();
        for (int c = 0; c < 4; c++) {
            panel.executeMoveForTest(row, c + 4, piece);
            panel.executeMoveForTest(row == 0 ? 14 : 0, c * 2, piece.opposite());
        }
        panel.executeMoveForTest(row, 8, piece);
    }

    private static void capture(String name) throws Exception {
        edt(() -> { frame.toFront(); frame.requestFocus(); return null; });
        robot.mouseMove(0, 0); robot.waitForIdle(); Thread.sleep(650);
        Rectangle bounds = edt(frame::getBounds);
        if (!edt(frame::isActive)) throw new IllegalStateException("Capture window is occluded");
        var variants = robot.createMultiResolutionScreenCapture(bounds).getResolutionVariants();
        BufferedImage image = (BufferedImage) variants.get(variants.size() - 1);
        edt(() -> {
            BoardPanel board = frame.getBoardPanel();
            Point origin = board.getLocationOnScreen();
            Rectangle band = board.getResultOverlay().getBounds();
            for (Board.Cell cell : board.getWinningLine()) {
                int x = BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE;
                int y = BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE;
                if (band.intersects(new Rectangle(x - 22, y - 22, 44, 44))) throw new IllegalStateException("Covered winning cell " + cell);
                int cx = (int) Math.round((origin.x - bounds.x + x) * 1.25);
                int cy = (int) Math.round((origin.y - bounds.y + y) * 1.25);
                int red = 0;
                for (int py = cy - 25; py <= cy + 25; py++) for (int px = cx - 25; px <= cx + 25; px++) {
                    Color color = new Color(image.getRGB(px, py));
                    if (Math.abs(color.getRed() - 229) <= 6 && Math.abs(color.getGreen() - 57) <= 6 && Math.abs(color.getBlue() - 53) <= 6) red++;
                }
                if (red < 150) throw new IllegalStateException("Win ring missing from native capture: " + cell + " red=" + red);
                System.out.println("RED_RING " + name + " " + cell + " pixels=" + red);
            }
            System.out.println("CAPTURE " + name + " native=" + image.getWidth() + "x" + image.getHeight()
                    + " frame=" + frame.getSize() + " band=" + band + " visible=" + board.getResultOverlay().isVisible()
                    + " title=" + board.getResultOverlay().getFullTitle());
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
