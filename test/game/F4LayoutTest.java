package game;

import game.view.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class F4LayoutTest {
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void directStartupBoundsAndRender(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var prefs = new P2LifecycleTest.MemoryPreferences(); prefs.put("language", language.name());
            MainFrame frame = new MainFrame(new AppSettings(prefs));
            try { frame.startGame(); layout(frame); check(frame.getContentPane()); render(frame, language.name()); }
            finally { frame.dispose(); }
        });
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void allTransitionsKeepControlsInsideParents(Lang.Language initial) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var prefs = new P2LifecycleTest.MemoryPreferences(); prefs.put("language", initial.name());
            MainFrame frame = new MainFrame(new AppSettings(prefs));
            try {
                frame.startGame();
                for (Lang.Language next : Lang.Language.values()) { Lang.set(next); layout(frame); check(frame.getContentPane()); }
                if (initial == Lang.Language.VI) {
                    Lang.set(Lang.Language.VI); Lang.set(Lang.Language.PT); layout(frame); check(frame.getContentPane()); render(frame, "VI-to-PT");
                    Lang.set(Lang.Language.VI); Lang.set(Lang.Language.EN); Lang.set(Lang.Language.PT);
                    layout(frame); check(frame.getContentPane()); render(frame, "VI-to-EN-to-PT");
                }
                assertEquals(new Dimension(596, 596), frame.getBoardPanel().getPreferredSize());
            } finally { frame.dispose(); }
        });
    }
    static void layout(Container parent) { parent.doLayout(); for (Component c : parent.getComponents()) if (c instanceof Container child) layout(child); }
    static void check(Container parent) {
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) continue;
            if (c.getWidth() > 0 && c.getHeight() > 0) {
                assertTrue(new Rectangle(0, 0, parent.getWidth(), parent.getHeight()).contains(c.getBounds()),
                        () -> c.getClass().getName() + " outside parent: " + c.getBounds() + " parent=" + parent.getSize());
            }
            if (c instanceof Container child) check(child);
        }
    }
    static void render(MainFrame frame, String name) {
        render(frame, Paths.get("docs", "task4b-results", "D-regression-render", "game"), name);
    }
    static void render(MainFrame frame, Path folder, String name) {
        try {
            Files.createDirectories(folder);
            Container content = frame.getContentPane();
            var scale = frame.getGraphicsConfiguration().getDefaultTransform();
            BufferedImage image = new BufferedImage((int) Math.ceil(content.getWidth() * scale.getScaleX()),
                    (int) Math.ceil(content.getHeight() * scale.getScaleY()), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.scale(scale.getScaleX(), scale.getScaleY());
                if (!frame.isMenuVisible()) {
                    JLabel player = frame.getLeftPlayerCard().getNameLabel();
                    assertTrue(graphics.getFontMetrics(player.getFont()).stringWidth(player.getText()) <= player.getWidth(),
                            "Player name must fit using the same DPI transform as the window");
                }
                content.printAll(graphics);
            } finally { graphics.dispose(); }
            ImageIO.write(image, "png", folder.resolve(name + ".png").toFile());
        } catch (Exception e) { throw new AssertionError(e); }
    }
}
