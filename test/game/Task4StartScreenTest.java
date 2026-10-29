package game;
import game.model.Piece;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
class Task4StartScreenTest {
    private Lang.Language language; private boolean sound;
    @BeforeEach void remember() { language=Lang.get(); sound=SoundManager.isSoundEnabled(); }
    @AfterEach void restore() throws Exception { SwingUtilities.invokeAndWait(() -> { Lang.set(language); SoundManager.setSoundEnabled(sound); }); }
    static MainFrame frame(Lang.Language language) {
        var node=new MemoryPreferences(); node.put("language",language.name()); node.putBoolean("soundEnabled",false); return new MainFrame(new AppSettings(node));
    }
    @Test void titleMenuSetupAndGameUseTheSameFrame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=frame(Lang.Language.VI);
            try {
                Container cards=f.getStartScreen().getParent(); assertEquals(3,cards.getComponentCount());
                assertEquals(MainFrame.Screen.TITLE,f.getScreen()); assertTrue(f.getStartScreen().isHintTimerRunning());
                Dimension size=f.getSize(); f.getStartScreen().advance(); assertEquals(MainFrame.Screen.MENU,f.getScreen());
                assertFalse(f.getStartScreen().isHintTimerRunning());
                SwingUtilities.invokeLater(() -> { SetupDialog d=(SetupDialog)java.util.Arrays.stream(f.getOwnedWindows()).filter(w -> w instanceof SetupDialog).findFirst().orElseThrow(); d.getStartButton().doClick(); });
                f.getMainMenu().getPvpButton().doClick(); assertEquals(MainFrame.Screen.GAME,f.getScreen());
                assertEquals(BoardPanel.GameMode.PVP,f.getBoardPanel().getGameMode()); assertSame(f,SwingUtilities.getWindowAncestor(f.getBoardPanel()));
                int id=f.getBoardPanel().getGameId(); f.getBackToMenuButton().doClick();
                assertEquals(MainFrame.Screen.MENU,f.getScreen()); assertEquals(BoardPanel.GameMode.PVP,f.getSelectedMode());
                assertEquals(id+1,f.getBoardPanel().getGameId()); assertEquals(size,f.getSize()); assertSame(cards,f.getContentPane());
            } finally { f.dispose(); }
        });
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void emptyProfileUsesTranslatedDefaultWithoutPersistingIt(Lang.Language selected) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=frame(selected); try {
                f.updateProfile(new PlayerProfile("  ",0)); assertEquals("",f.getProfile().name());
                for(var next:Lang.Language.values()) { f.selectLanguage(next); assertEquals(Lang.defaultPlayerName(),f.getPlayerName()); assertEquals(Lang.defaultPlayerName(),f.getMainMenu().getProfileButton().getText()); }
            } finally { f.dispose(); }
        });
    }
    static Stream<Arguments> names() {
        return Stream.of(Arguments.of(null,""),Arguments.of("\u2003\u00a0",""),Arguments.of("  Nguyễn Ánh  ","Nguyễn Ánh"),
            Arguments.of("João Gonçalves","João Gonçalves"),Arguments.of("Conceição","Conceição"),Arguments.of("A\u0301nh","Ánh"),
            Arguments.of("abcdefghijklmnopXYZ","abcdefghijklmnop"),Arguments.of("🎯".repeat(17),"🎯".repeat(16)),Arguments.of("A\nB","A B"),Arguments.of("<html>João","<html>João"));
    }
    @ParameterizedTest @MethodSource("names") void normalizeUnicodeWhitespaceAndLongNames(String input,String expected) { assertEquals(expected,AppSettings.normalizePlayerName(input)); }
    @Test void profileFieldLimitsUnicodeAndNamesRemainPlainText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=frame(Lang.Language.PT); ProfileDialog d=new ProfileDialog(f);
            try { d.getEditor().getNameField().setText("🎯".repeat(17)); assertEquals("🎯".repeat(16),d.getEditor().getNameField().getText());
                d.getEditor().getNameField().setText("<html>João"); d.getDoneButton().doClick();
                assertEquals("<html>João",f.getPlayerName()); assertNull(f.getMainMenu().getProfileButton().getClientProperty("html"));
            } finally { d.dispose(); f.dispose(); }
        });
    }
    @Test void settingsApplyLanguageImmediatelyWithoutChangingProfileDraft() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var node=new MemoryPreferences(); MainFrame f=new MainFrame(new AppSettings(node)); ProfileDialog draft=new ProfileDialog(f); SettingsDialog settings=new SettingsDialog(f);
            try { draft.getEditor().getNameField().setText("Conceição");
                for(var next:Lang.Language.values()) { settings.getLanguageCombo().setSelectedItem(next); assertEquals(next.name(),node.get("language",""));
                    assertEquals(Lang.settings(),settings.getTitle()); assertEquals(Lang.profile(),draft.getTitle());
                    assertEquals(Lang.done(),draft.getDoneButton().getText()); assertEquals("Conceição",draft.getEditor().getNameField().getText()); }
            } finally { settings.dispose(); draft.dispose(); f.dispose(); }
        });
    }
    @Test void profileChangesAndMenuKeepExistingSessionTotals() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=frame(Lang.Language.PT); try {
                f.startGame(); f.getScoreTracker().recordResult(-1,Piece.X); f.getScoreTracker().recordResult(-2,Piece.O); f.getScoreTracker().recordResult(-3,Piece.NONE);
                f.returnToMenu(); f.updateProfile(new PlayerProfile("Alex",7)); f.startGame();
                assertEquals(1,f.getScoreTracker().getHumanWins()); assertEquals(1,f.getScoreTracker().getAiWins()); assertEquals(1,f.getScoreTracker().getDraws());
            } finally { f.dispose(); }
        });
    }
}
