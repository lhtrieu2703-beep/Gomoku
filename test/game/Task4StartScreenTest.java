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
    private Lang.Language originalLanguage;
    private boolean originalSound;
    @BeforeEach void remember() { originalLanguage = Lang.get(); originalSound = SoundManager.isSoundEnabled(); }
    @AfterEach void restore() throws Exception {
        SwingUtilities.invokeAndWait(() -> { Lang.set(originalLanguage); SoundManager.setSoundEnabled(originalSound); });
    }
    static MainFrame frame(Lang.Language language) {
        var prefs = new P2LifecycleTest.MemoryPreferences(); prefs.put("language", language.name());
        return new MainFrame(new AppSettings(prefs));
    }

    @Test void startAndMenuSwitchCardsInTheSameFrame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = frame(Lang.Language.VI);
            try {
                Container cards = frame.getStartScreen().getParent();
                assertInstanceOf(CardLayout.class, cards.getLayout()); assertEquals(2, cards.getComponentCount());
                assertTrue(frame.isMenuVisible()); assertTrue(frame.getStartScreen().isVisible());
                assertFalse(frame.getBoardPanel().getParent().isVisible());
                frame.getStartScreen().getNameField().setText("  Nguyễn Ánh  ");
                frame.getStartScreen().getStartButton().doClick();
                assertFalse(frame.isMenuVisible()); assertFalse(frame.getStartScreen().isVisible());
                assertTrue(frame.getBoardPanel().getParent().isVisible());
                assertSame(frame, SwingUtilities.getWindowAncestor(frame.getBoardPanel()));
                assertEquals("Nguyễn Ánh", frame.getPlayerName());
                int gameId = frame.getBoardPanel().getGameId();
                frame.getBackToMenuButton().doClick();
                assertTrue(frame.isMenuVisible()); assertTrue(frame.getStartScreen().isVisible());
                assertEquals(gameId + 1, frame.getBoardPanel().getGameId());
                assertSame(cards, frame.getStartScreen().getParent());
            } finally { frame.dispose(); }
        });
    }

    @ParameterizedTest @EnumSource(Lang.Language.class)
    void emptyNameUsesTranslatedDefaultAndRemainsImplicit(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = frame(language);
            try {
                frame.getStartScreen().getNameField().setText("  "); frame.startGame();
                assertEquals(Lang.defaultPlayerName(), frame.getPlayerName());
                JLabel label = P2LifecycleTest.field(frame, "playerLabel");
                assertTrue(label.getText().contains(Lang.defaultPlayerName()));
                frame.returnToMenu();
                assertEquals("", frame.getStartScreen().getNameField().getText());
                for (Lang.Language next : Lang.Language.values()) {
                    frame.getStartScreen().getLanguageCombo().setSelectedItem(next);
                    assertEquals(next, Lang.get()); assertEquals(Lang.defaultPlayerName(), frame.getPlayerName());
                }
            } finally { frame.dispose(); }
        });
    }

    static Stream<Arguments> names() {
        return Stream.of(
                Arguments.of(null, ""), Arguments.of("\u2003\u00a0", ""), Arguments.of("  Nguyễn Ánh  ", "Nguyễn Ánh"),
                Arguments.of("João Gonçalves", "João Gonçalves"), Arguments.of("Conceição", "Conceição"),
                Arguments.of("A\u0301nh", "Ánh"), Arguments.of("abcdefghijklmnopXYZ", "abcdefghijklmnop"),
                Arguments.of("🎯".repeat(17), "🎯".repeat(16)), Arguments.of("A\nB", "A B"),
                Arguments.of("<html>João", "<html>João"));
    }
    @ParameterizedTest @MethodSource("names")
    void normalizeUnicodeWhitespaceAndLongNames(String input, String expected) {
        assertEquals(expected, AppSettings.normalizePlayerName(input));
        assertTrue(expected.codePointCount(0, expected.length()) <= 16);
    }

    @Test void fieldLimitsUnicodeWithoutSplittingEmojiAndDisplaysNamesAsPlainText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = frame(Lang.Language.PT);
            try {
                JTextField input = frame.getStartScreen().getNameField();
                input.setText("🎯".repeat(17)); assertEquals("🎯".repeat(16), input.getText());
                input.setText("<html>João"); frame.startGame();
                JLabel label = P2LifecycleTest.field(frame, "playerLabel");
                assertTrue(label.getText().contains("<html>João"));
                assertNull(label.getClientProperty("html"));
            } finally { frame.dispose(); }
        });
    }

    @Test void languageChoiceOnMenuPreservesTypedNameAndSynchronizesBothSelectors() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var prefs = new P2LifecycleTest.MemoryPreferences(); MainFrame frame = new MainFrame(new AppSettings(prefs));
            try {
                frame.getStartScreen().getNameField().setText("Conceição");
                for (Lang.Language next : Lang.Language.values()) {
                    frame.getStartScreen().getLanguageCombo().setSelectedItem(next);
                    assertEquals(next.name(), prefs.get("language", ""));
                    assertEquals(next, frame.getLangCombo().getSelectedItem());
                    assertEquals(Lang.startGame(), frame.getStartScreen().getStartButton().getText());
                    assertEquals("Conceição", frame.getStartScreen().getNameField().getText());
                    frame.startGame(); assertEquals("Conceição", frame.getPlayerName()); frame.returnToMenu();
                }
            } finally { frame.dispose(); }
        });
    }

    @Test void achievementIconShowsExistingSessionTotalsAndMenuKeepsThem() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = frame(Lang.Language.PT);
            try {
                frame.getStartScreen().getNameField().setText("João"); frame.startGame();
                frame.getScoreTracker().recordResult(-1, Piece.X);
                frame.getScoreTracker().recordResult(-2, Piece.O);
                frame.getScoreTracker().recordResult(-3, Piece.NONE);
                assertInstanceOf(AchievementIcon.class, frame.getAchievementsButton().getIcon());
                assertEquals(Lang.achievementsTooltip(), frame.getAchievementsButton().getToolTipText());
                frame.getAchievementsButton().doClick();
                JDialog dialog = P2LifecycleTest.field(frame, "achievementsDialog");
                JTextArea text = P2LifecycleTest.field(frame, "achievementsText");
                assertTrue(dialog.isVisible()); assertEquals(Lang.scoreSummary("João", 1, 1, 1), text.getText());
                Lang.set(Lang.Language.EN); assertEquals(Lang.scoreSummary("João", 1, 1, 1), text.getText());
                frame.returnToMenu(); assertFalse(dialog.isDisplayable());
                frame.getStartScreen().getNameField().setText("Alex"); frame.startGame();
                assertEquals(1, frame.getScoreTracker().getHumanWins()); assertEquals(1, frame.getScoreTracker().getAiWins());
                assertEquals(1, frame.getScoreTracker().getDraws());
            } finally { frame.dispose(); }
        });
    }
}
