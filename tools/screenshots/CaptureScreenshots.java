import game.ai.MinimaxAI;
import game.ai.Move;
import game.model.Piece;
import game.network.NetworkManager;
import game.view.AppSettings;
import game.view.BoardPanel;
import game.view.Lang;
import game.view.MainFrame;
import game.view.NetworkDialog;
import game.view.RulesDialog;
import game.view.SettingsDialog;
import game.view.SetupDialog;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.AbstractPreferences;

/** A separate visible JVM. Every PNG comes directly from Robot's native screen pixels. */
public final class CaptureScreenshots {
    private static final String[] NAMES = {
            "01_title.png", "02_menu.png", "03_setup_dialog.png", "04_board_playing.png",
            "05_settings.png", "06_rules.png", "07_lan_dialog.png"
    };
    private static final List<String> rows = new ArrayList<>();
    private static final List<Throwable> uncaught = new CopyOnWriteArrayList<>();
    private static MainFrame frame;
    private static Robot robot;
    private static Path output;
    private static Path evidence;
    private static Dimension initialFrameSize;

    private CaptureScreenshots() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: CaptureScreenshots OUTPUT_DIRECTORY EVIDENCE_DIRECTORY");
        }
        Path allowedImages = Path.of("docs", "screenshots").toAbsolutePath().normalize();
        Path allowedEvidence = Path.of("tools", "screenshots").toAbsolutePath().normalize();
        output = Path.of(args[0]).toAbsolutePath().normalize();
        evidence = Path.of(args[1]).toAbsolutePath().normalize();
        if (!output.startsWith(allowedImages) || !evidence.startsWith(allowedEvidence)) {
            throw new IllegalArgumentException("Outputs must stay inside the two authorized directories");
        }
        for (String name : NAMES) {
            if (Files.exists(output.resolve(name))) {
                throw new IllegalStateException("Refusing to overwrite an existing screenshot: " + name);
            }
        }
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Visible desktop required");
        if (System.getProperty("sun.java2d.uiScale") != null) {
            throw new IllegalStateException("Remove the forced sun.java2d.uiScale; this tool uses current display scaling");
        }
        Files.createDirectories(output);
        Files.createDirectories(evidence);
        // CREATE_NEW is also an execution guard: this evidence directory cannot be run twice.
        Files.writeString(evidence.resolve("capture.started"),
                OffsetDateTime.now(ZoneOffset.ofHours(7)) + "\n",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE_NEW);
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            uncaught.add(error);
            error.printStackTrace(System.err);
        });
        Thread watchdog = new Thread(() -> {
            try { Thread.sleep(60_000); }
            catch (InterruptedException done) { return; }
            System.err.println("CAPTURE_TIMEOUT: no second execution will be attempted");
            System.exit(3);
        }, "screenshot-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();

        Point originalPointer = MouseInfo.getPointerInfo().getLocation();
        int exitCode = 0;
        try {
            System.out.println("START_ASIA_SAIGON " + OffsetDateTime.now(ZoneOffset.ofHours(7)));
            System.out.println("JAVA " + System.getProperty("java.version"));
            System.out.println("CAPTURE_SOURCE java.awt.Robot.createMultiResolutionScreenCapture; native pixels; only lossless removal of invisible Windows resize margins");
            System.out.println("FORCED_SCALE none");
            robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
            robot.setAutoDelay(30);
            edt(() -> {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                MemoryPreferences memory = new MemoryPreferences();
                AppSettings settings = new AppSettings(memory);
                settings.saveLang(Lang.Language.VI);
                settings.saveSound(false);
                frame = new MainFrame(settings); // Production size and production 800-ms AI delay.
                initialFrameSize = frame.getSize();
                if (Toolkit.getDefaultToolkit().isAlwaysOnTopSupported()) frame.setAlwaysOnTop(true);
                frame.setVisible(true);
                keepWithinWorkArea(frame); // Reposition only, never resize or repack.
                if (Lang.get() != Lang.Language.VI || !frame.getProfile().name().isEmpty()) {
                    throw new IllegalStateException("Expected Vietnamese and the anonymous default profile");
                }
                logDisplay();
                return null;
            });
            capture("01_title.png", frame, "TITLE; hồ sơ mặc định; tiếng Việt");
            edt(() -> { frame.getStartScreen().advance(); return null; });
            requireScreen(MainFrame.Screen.MENU);
            capture("02_menu.png", frame, "MENU; Người chơi 1; tiếng Việt");

            SetupDialog setup = null;
            try {
                setup = openModal(() -> frame.getMainMenu().getAiButton().doClick(0), SetupDialog.class);
                capture("03_setup_dialog.png", setup, "SetupDialog chơi với máy; độ khó mặc định Trung Bình");
            } catch (Exception error) {
                unavailable("03_setup_dialog.png", error);
            }
            if (setup != null) {
                SetupDialog selected = setup;
                edt(() -> { selected.getStartButton().doClick(0); return null; });
            } else {
                closeDialogs();
                edt(() -> { frame.beginGame(BoardPanel.GameMode.PV_AI, MinimaxAI.Difficulty.MEDIUM); return null; });
            }

            try {
                requireScreen(MainFrame.Screen.GAME);
                playFourHumanTurns();
                String note = edt(() -> {
                    if (!frame.getSize().equals(initialFrameSize)) {
                        throw new IllegalStateException("Native frame size changed; tool does not resize it");
                    }
                    BoardPanel panel = frame.getBoardPanel();
                    if (!panel.getVisibleRect().contains(new Rectangle(16, 16, 564, 564))) {
                        throw new IllegalStateException("Default-size board is clipped at the current display scale");
                    }
                    for (var button : new javax.swing.JButton[]{frame.getBackToMenuButton(), frame.getUndoButton()}) {
                        if (!button.isShowing() || !button.getVisibleRect().equals(new Rectangle(button.getSize()))) {
                            throw new IllegalStateException("A toolbar control is clipped");
                        }
                    }
                    if (!frame.getUndoButton().isEnabled()) throw new IllegalStateException("Undo should be enabled after eight stones");
                    return "PV_AI / MEDIUM; 8 quân qua playMove và AI thật; tỉ số 0–0, hòa 0; Về menu / Ván Mới / Đi Lại";
                });
                capture("04_board_playing.png", frame, note);
            } catch (Exception error) {
                unavailable("04_board_playing.png", error);
            }

            try {
                SettingsDialog settings = openModal(() -> frame.getSettingsButton().doClick(0), SettingsDialog.class);
                capture("05_settings.png", settings, "SettingsDialog; Tiếng Việt; âm thanh tắt trong phiên RAM");
                edt(() -> { settings.getCloseButton().doClick(0); return null; });
            } catch (Exception error) {
                unavailable("05_settings.png", error);
                closeDialogs();
            }
            try {
                RulesDialog rules = openModal(() -> frame.getRulesButton().doClick(0), RulesDialog.class);
                capture("06_rules.png", rules, "RulesDialog; nội dung và ví dụ RULE_B của ứng dụng");
                edt(() -> { rules.getCloseButton().doClick(0); return null; });
            } catch (Exception error) {
                unavailable("06_rules.png", error);
                closeDialogs();
            }
            try {
                // The standalone JVM's hosts resolver invokes the existing getLocalIP fallback.
                // Never edit a JLabel or redact pixels to disguise a real machine address.
                if (!NetworkManager.getLocalIP().equals("127.0.0.1")) {
                    throw new IllegalStateException("LAN screenshot refused: resolver did not produce the privacy-safe loopback fallback");
                }
                edt(() -> { frame.returnToMenu(); return null; });
                NetworkDialog network = openModal(() -> frame.getMainMenu().getLanButton().doClick(0), NetworkDialog.class);
                capture("07_lan_dialog.png", network, "NetworkDialog; 127.0.0.1 qua fallback có sẵn; chưa Host/Join, không mở socket");
                edt(() -> { network.dispatchEvent(new WindowEvent(network, WindowEvent.WINDOW_CLOSING)); return null; });
            } catch (Exception error) {
                unavailable("07_lan_dialog.png", error);
                closeDialogs();
            }
        } catch (Throwable error) {
            error.printStackTrace(System.err);
            for (String name : NAMES) {
                if (!hasResult(name)) unavailable(name, error);
            }
            exitCode = 1;
        } finally {
            if (frame != null) edt(() -> { frame.dispose(); return null; });
            if (robot != null) robot.mouseMove(originalPointer.x, originalPointer.y);
            watchdog.interrupt();
            Files.writeString(evidence.resolve("captures.tsv"),
                    "file\tstatus\twidth_px\theight_px\tlogical_bounds\tscale_x\tscale_y\tnote\n"
                            + String.join("\n", rows) + "\n", StandardCharsets.UTF_8);
            System.out.println("END_ASIA_SAIGON " + OffsetDateTime.now(ZoneOffset.ofHours(7)));
        }
        long captured = rows.stream().filter(row -> row.contains("\tCAPTURED\t")).count();
        if (captured != NAMES.length || !uncaught.isEmpty()) exitCode = 1;
        System.out.printf("RESULT captured=%d unavailable=%d uncaught=%d%n", captured, NAMES.length - captured, uncaught.size());
        System.exit(exitCode);
    }

    private static void playFourHumanTurns() throws Exception {
        int[][] candidates = {{7, 7}, {8, 7}, {6, 8}, {8, 9}, {6, 6}, {9, 8}, {5, 9}, {9, 5}, {5, 5}};
        for (int turn = 0; turn < 4; turn++) {
            edt(() -> {
                BoardPanel panel = frame.getBoardPanel();
                if (panel.getGameMode() != BoardPanel.GameMode.PV_AI || panel.getCurrentTurn() != Piece.X
                        || panel.isAiThinking() || panel.isGameOver()) {
                    throw new IllegalStateException("Expected an active human turn in PV_AI");
                }
                for (int[] cell : candidates) {
                    if (panel.playMove(cell[0], cell[1])) return null;
                }
                throw new IllegalStateException("No prepared human candidate is legal");
            });
            int expected = (turn + 1) * 2;
            long deadline = System.nanoTime() + 10_000_000_000L;
            boolean ready = false;
            while (System.nanoTime() < deadline) {
                ready = edt(() -> frame.getBoardPanel().getMoveHistory().size() == expected
                        && !frame.getBoardPanel().isAiThinking()
                        && frame.getBoardPanel().getCurrentTurn() == Piece.X);
                if (ready) break;
                Thread.sleep(40); // Main thread only; the EDT and real AI remain unblocked.
            }
            if (!ready) throw new IllegalStateException("AI did not complete turn " + (turn + 1));
        }
        edt(() -> {
            List<Move> history = frame.getBoardPanel().getMoveHistory();
            for (int i = 0; i < history.size(); i++) {
                Move move = history.get(i);
                System.out.printf("MOVE ply=%d piece=%s row=%d col=%d%n", i + 1, i % 2 == 0 ? "X" : "O", move.row, move.col);
            }
            return null;
        });
    }

    private static <T extends JDialog> T openModal(Runnable action, Class<T> type) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeLater(() -> {
            try { action.run(); }
            catch (Throwable error) { failure.set(error); }
        });
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (failure.get() != null) throw new IllegalStateException("Opening " + type.getSimpleName(), failure.get());
            T dialog = edt(() -> {
                for (Window window : frame.getOwnedWindows()) {
                    if (type.isInstance(window) && window.isShowing()) return type.cast(window);
                }
                return null;
            });
            if (dialog != null) return dialog;
            Thread.sleep(30);
        }
        throw new IllegalStateException("Modal did not appear: " + type.getSimpleName());
    }

    private static void capture(String name, Window window, String note) throws Exception {
        edt(() -> {
            if (!window.isShowing()) throw new IllegalStateException("Window is not showing");
            keepWithinWorkArea(window);
            if (Toolkit.getDefaultToolkit().isAlwaysOnTopSupported()) window.setAlwaysOnTop(true);
            window.toFront();
            window.requestFocus();
            window.repaint();
            return null;
        });
        Rectangle bounds = edt(window::getBounds);
        GraphicsConfiguration configuration = edt(window::getGraphicsConfiguration);
        if (!workArea(configuration).contains(bounds)) {
            throw new IllegalStateException("Default window cannot fit the current work area without resizing");
        }
        // Move the pointer out of the captured area so hover previews/tooltips are absent.
        Rectangle screen = configuration.getBounds();
        Point pointer = new Point(screen.x + 2, screen.y + 2);
        if (bounds.contains(pointer)) pointer = new Point(screen.x + screen.width - 2, screen.y + screen.height - 2);
        robot.waitForIdle();
        if (!edt(window::isActive)) {
            Insets decoration = edt(window::getInsets);
            robot.mouseMove(bounds.x + bounds.width / 2, bounds.y + Math.max(8, decoration.top / 2));
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
        robot.mouseMove(pointer.x, pointer.y);
        robot.waitForIdle();
        Thread.sleep(650); // Includes normal Windows activation animation and compositor repaint.
        edt(() -> { Toolkit.getDefaultToolkit().sync(); return null; });
        robot.waitForIdle();
        if (!edt(window::isActive)) throw new IllegalStateException("Target window did not become active; refusing a possibly occluded capture");
        if (!bounds.equals(edt(window::getBounds))) throw new IllegalStateException("Window moved while waiting for paint");
        if (edt(Lang::get) != Lang.Language.VI) throw new IllegalStateException("Language changed unexpectedly");

        MultiResolutionImage screenCapture = robot.createMultiResolutionScreenCapture(bounds);
        List<Image> variants = screenCapture.getResolutionVariants();
        // JDK Robot documents variant 1 as native pixels on HiDPI, variant 0 at 100%.
        BufferedImage pixels = (BufferedImage) variants.get(variants.size() - 1);
        AffineTransform transform = configuration.getDefaultTransform();
        // Win32's outer Window bounds include invisible side/bottom resize margins.
        // Keep the title bar and the complete visible client area; copy existing pixels only.
        if (System.getProperty("os.name").startsWith("Windows")) {
            Insets decoration = edt(window::getInsets);
            int left = (int) Math.floor(decoration.left * transform.getScaleX());
            int right = (int) Math.floor(decoration.right * transform.getScaleX());
            int bottom = (int) Math.floor(decoration.bottom * transform.getScaleY());
            System.out.printf("VISIBLE_CROP %s raw=%dx%d left=%d top=0 right=%d bottom=%d insets=%s%n",
                    name, pixels.getWidth(), pixels.getHeight(), left, right, bottom, decoration);
            pixels = pixels.getSubimage(left, 0, pixels.getWidth() - left - right, pixels.getHeight() - bottom);
            note += "; cắt lossless viền resize ngoài Windows, giữ thanh tiêu đề";
        }
        if (!ImageIO.write(pixels, "png", output.resolve(name).toFile())) throw new IllegalStateException("PNG writer unavailable");
        String row = String.join("\t", name, "CAPTURED", Integer.toString(pixels.getWidth()),
                Integer.toString(pixels.getHeight()), bounds.toString(),
                Double.toString(transform.getScaleX()), Double.toString(transform.getScaleY()), note);
        rows.add(row);
        System.out.println(row);
    }

    private static void requireScreen(MainFrame.Screen expected) throws Exception {
        if (edt(frame::getScreen) != expected) throw new IllegalStateException("Expected screen " + expected);
    }

    private static Rectangle workArea(GraphicsConfiguration configuration) {
        Rectangle screen = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        return new Rectangle(screen.x + insets.left, screen.y + insets.top,
                screen.width - insets.left - insets.right, screen.height - insets.top - insets.bottom);
    }

    private static void keepWithinWorkArea(Window window) {
        Rectangle work = workArea(window.getGraphicsConfiguration());
        if (window.getWidth() <= work.width && window.getHeight() <= work.height) {
            int x = Math.max(work.x, Math.min(window.getX(), work.x + work.width - window.getWidth()));
            int y = Math.max(work.y, Math.min(window.getY(), work.y + work.height - window.getHeight()));
            window.setLocation(x, y);
        }
    }

    private static void logDisplay() throws Exception {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        System.out.println("LOOK_AND_FEEL " + UIManager.getLookAndFeel().getClass().getName());
        System.out.println("SCALE " + gc.getDefaultTransform());
        System.out.println("SCREEN_LOGICAL " + gc.getBounds());
        System.out.println("DISPLAY_MODE " + gc.getDevice().getDisplayMode().getWidth() + "x" + gc.getDevice().getDisplayMode().getHeight());
        System.out.println("TOOLKIT_DPI " + Toolkit.getDefaultToolkit().getScreenResolution());
        System.out.println("WORK_AREA " + workArea(gc));
        System.out.println("FRAME_DEFAULT " + initialFrameSize + " content=" + frame.getContentPane().getSize());
        System.out.println("PRIVACY RAM Preferences; anonymous player; JVM-only hosts resolver; no personal Preferences read/write");
    }

    private static void closeDialogs() throws Exception {
        edt(() -> {
            for (Window window : frame.getOwnedWindows()) if (window instanceof JDialog) window.dispose();
            return null;
        });
    }

    private static boolean hasResult(String name) {
        return rows.stream().anyMatch(row -> row.startsWith(name + "\t"));
    }

    private static void unavailable(String name, Throwable error) {
        if (hasResult(name)) return;
        String reason = error.toString().replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
        String row = name + "\tKHÔNG CHỤP ĐƯỢC\t0\t0\t\t\t\t" + reason;
        rows.add(row);
        System.err.println(row);
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) return action.call();
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }

    /** No Preferences.userRoot/userNodeForPackage call; all settings stay in this JVM. */
    private static final class MemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        MemoryPreferences() { super(null, ""); }
        @Override protected void putSpi(String key, String value) { values.put(key, value); }
        @Override protected String getSpi(String key) { return values.get(key); }
        @Override protected void removeSpi(String key) { values.remove(key); }
        @Override protected void removeNodeSpi() { values.clear(); }
        @Override protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
        @Override protected String[] childrenNamesSpi() { return new String[0]; }
        @Override protected AbstractPreferences childSpi(String name) { throw new UnsupportedOperationException(); }
        @Override protected void syncSpi() {}
        @Override protected void flushSpi() {}
    }
}
