package game;

import game.view.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class Task4LayoutTest {
    static Stream<Arguments> languagesAndLooks() {
        return Stream.of(Lang.Language.values()).flatMap(language -> Stream.of(false, true).map(nativeLook -> Arguments.of(language, nativeLook)));
    }
    @ParameterizedTest @MethodSource("languagesAndLooks")
    void allThreeCardsFitAllLocalesOnStartupAndAfterTransitions(Lang.Language language, boolean nativeLook) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String look = UIManager.getLookAndFeel().getClass().getName();
            Lang.Language original = Lang.get(); boolean sound = SoundManager.isSoundEnabled();
            MainFrame frame = null;
            try {
                UIManager.setLookAndFeel(nativeLook ? UIManager.getSystemLookAndFeelClassName() : UIManager.getCrossPlatformLookAndFeelClassName());
                frame = Task4StartScreenTest.frame(language);
                check(frame); render(frame, (nativeLook ? "native-" : "") + language.name());
                frame.showMenu(); check(frame);
                frame.updateProfile(new PlayerProfile("Á".repeat(16),0));
                frame.startGame(); check(frame); frame.returnToMenu(); check(frame);
                for (Lang.Language next : Lang.Language.values()) {
                    frame.selectLanguage(next); check(frame);
                    assertEquals("Á".repeat(16), frame.getProfile().name());
                    frame.startGame(); check(frame); frame.returnToMenu(); check(frame);
                }
                assertEquals(new Dimension(596, 596), frame.getBoardPanel().getPreferredSize());
            } catch (Exception e) { throw new AssertionError(e); }
            finally {
                if (frame != null) frame.dispose();
                Lang.set(original); SoundManager.setSoundEnabled(sound);
                try { UIManager.setLookAndFeel(look); } catch (Exception e) { throw new AssertionError(e); }
            }
        });
    }
    private static void check(MainFrame frame) {
        F4LayoutTest.layout(frame); F4LayoutTest.check(frame.getContentPane());
    }
    private static void render(MainFrame frame, String name) {
        F4LayoutTest.render(frame, Paths.get("docs", "task4b-results", "D-regression-render", "start"), name);
    }
}
