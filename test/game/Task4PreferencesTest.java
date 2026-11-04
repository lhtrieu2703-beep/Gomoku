package game;

import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import javax.swing.*;
import java.util.*;
import java.util.prefs.Preferences;
import static org.junit.jupiter.api.Assertions.*;

class Task4PreferencesTest {
    private String path;
    private Map<String, String> before;
    private Lang.Language language;
    private boolean sound;
    @BeforeEach void isolate() throws Exception {
        before = PreferencesIsolation.snapshot(); path = "/test/task4/name-" + UUID.randomUUID();
        language = Lang.get(); sound = SoundManager.isSoundEnabled();
    }
    @AfterEach void cleanup() throws Exception {
        try { PreferencesIsolation.cleanup(path, before); }
        finally { SwingUtilities.invokeAndWait(() -> { Lang.set(language); SoundManager.setSoundEnabled(sound); }); }
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void profileAndLanguageRoundTripThroughInMemoryNode(Lang.Language selected) throws Exception {
        Preferences node = PreferencesIsolation.testRoot.node(path);
        node.putBoolean("soundEnabled", false);
        String name = switch (selected) { case VI -> "Đặng Minh"; case EN -> "Alex"; case PT -> "João Gonçalves"; };
        SwingUtilities.invokeAndWait(() -> {
            MainFrame first = new MainFrame(new AppSettings(node));
            try {
                first.selectLanguage(selected);
                first.updateProfile(new PlayerProfile("  " + name + "  ", 7));
                first.startGame(); assertEquals(name, first.getPlayerName());
            } finally { first.dispose(); }
            MainFrame reopened = new MainFrame(new AppSettings(node));
            try {
                assertTrue(reopened.isMenuVisible()); assertEquals(selected, Lang.get());
                assertEquals(name, reopened.getProfile().name()); assertEquals(7, reopened.getProfile().avatarIndex());
                assertEquals(name, reopened.getPlayerName());
                reopened.updateProfile(new PlayerProfile("",0)); reopened.startGame();
                assertEquals(Lang.defaultPlayerName(), reopened.getPlayerName());
                assertNull(node.get("playerName", null));
                assertEquals("", new AppSettings(node).loadPlayerName());
            } finally { reopened.dispose(); }
        });
    }
}
