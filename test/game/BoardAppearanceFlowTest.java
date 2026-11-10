package game;

import game.ai.MinimaxAI;
import game.model.Piece;
import game.view.AppSettings;
import game.view.BoardPanel;
import game.view.BoardTheme;
import game.view.Lang;
import game.view.MainFrame;
import game.view.PieceBadgeIcon;
import game.view.PieceStyle;
import game.view.PlayerCard;
import game.view.SettingsDialog;
import game.view.SoundManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BoardAppearanceFlowTest {
    @Test
    void changingEitherSelectorKeepsMovesTurnAndScore() throws Exception {
        SwingUtilities.invokeAndWait(() -> withFrame(frame -> {
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            BoardPanel panel = frame.getBoardPanel();
            frame.getScoreTracker().recordResult(-1, Piece.X, Piece.X);
            frame.getScoreTracker().recordResult(-2, Piece.O, Piece.X);
            frame.getScoreTracker().recordResult(-3, Piece.NONE, Piece.X);
            assertTrue(panel.playMove(7, 7));
            var history = List.copyOf(panel.getMoveHistory());
            int gameId = panel.getGameId();

            SettingsDialog settings = new SettingsDialog(frame);
            try {
                assertEquals(BoardTheme.WARM_WOOD, settings.getBoardThemeCombo().getSelectedItem());
                assertEquals(PieceStyle.CLASSIC, settings.getPieceStyleCombo().getSelectedItem());
                settings.getBoardThemeCombo().setSelectedItem(BoardTheme.DARK_JADE);
                assertEquals(BoardTheme.DARK_JADE, panel.getBoardTheme());
                assertEquals(panel.getBackground(), panel.getParent().getBackground());
                settings.getPieceStyleCombo().setSelectedItem(PieceStyle.METALLIC);
                assertEquals(PieceStyle.METALLIC, panel.getPieceStyle());
                assertBadge(frame.getLeftPlayerCard(), Piece.X, PieceStyle.METALLIC);
                assertBadge(frame.getRightPlayerCard(), Piece.O, PieceStyle.METALLIC);
            } finally {
                settings.dispose();
            }
            assertEquals(gameId, panel.getGameId());
            assertEquals(history, panel.getMoveHistory());
            assertEquals(Piece.O, panel.getCurrentTurn());
            assertFalse(panel.isGameOver());
            assertFalse(panel.playMove(7, 7), "The original stone must remain on the board");
            assertEquals(1, frame.getScoreTracker().getHumanWins());
            assertEquals(1, frame.getScoreTracker().getAiWins());
            assertEquals(1, frame.getScoreTracker().getDraws());
            assertTrue(frame.getRightPlayerCard().isHighlighted());
            assertEquals(Lang.wins() + ": 1", frame.getLeftPlayerCard().getWinsLabel().getText());
        }));
    }

    @Test
    void appearancePersistsAcrossMenuAndNewGameWithinTheSession() throws Exception {
        SwingUtilities.invokeAndWait(() -> withFrame(frame -> {
            frame.getStartScreen().advance();
            frame.selectBoardTheme(BoardTheme.CHARCOAL_STONE);
            frame.selectPieceStyle(PieceStyle.GLASS);
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            assertAppearance(frame, BoardTheme.CHARCOAL_STONE, PieceStyle.GLASS);

            JButton newGame = P2LifecycleTest.field(frame, "btnNewGame");
            newGame.doClick();
            assertAppearance(frame, BoardTheme.CHARCOAL_STONE, PieceStyle.GLASS);
            frame.returnToMenu();
            frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
            assertAppearance(frame, BoardTheme.CHARCOAL_STONE, PieceStyle.GLASS);

            SettingsDialog reopened = new SettingsDialog(frame);
            try {
                assertEquals(BoardTheme.CHARCOAL_STONE, reopened.getBoardThemeCombo().getSelectedItem());
                assertEquals(PieceStyle.GLASS, reopened.getPieceStyleCombo().getSelectedItem());
            } finally {
                reopened.dispose();
            }
        }));
    }

    @Test
    void aNewFrameUsingTheSamePreferencesStartsWithTheDefaults() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var memory = new MemoryPreferences();
            var settings = new AppSettings(memory);
            settings.saveSound(false);
            Lang.Language language = Lang.get();
            boolean sound = SoundManager.isSoundEnabled();
            MainFrame first = new MainFrame(settings, 0);
            try {
                first.selectBoardTheme(BoardTheme.DARK_JADE);
                first.selectPieceStyle(PieceStyle.METALLIC);
            } finally {
                first.dispose();
            }
            MainFrame second = new MainFrame(settings, 0);
            try {
                assertAppearance(second, BoardTheme.WARM_WOOD, PieceStyle.CLASSIC);
                assertEquals(List.of("soundEnabled"), List.of(memory.keys()));
            } catch (Exception error) {
                throw new AssertionError(error);
            } finally {
                second.dispose();
                Lang.set(language);
                SoundManager.setSoundEnabled(sound);
            }
        });
    }

    @ParameterizedTest
    @EnumSource(Lang.Language.class)
    void allAppearanceOptionsFitInTheLocalizedDialog(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> withFrame(frame -> {
            SettingsDialog settings = new SettingsDialog(frame);
            try {
                settings.getLanguageCombo().setSelectedItem(language);
                F4LayoutTest.layout(settings.getContentPane());
                F4LayoutTest.check(settings.getContentPane());
                Task4bPresentationTest.textFits(settings.getContentPane());
                assertOptionsFit(settings.getBoardThemeCombo());
                assertOptionsFit(settings.getPieceStyleCombo());
            } finally {
                settings.dispose();
            }
        }));
    }

    private static <T> void assertOptionsFit(JComboBox<T> combo) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            JLabel label = (JLabel) combo.getRenderer()
                    .getListCellRendererComponent(new JList<T>(), combo.getItemAt(i), i, false, false);
            assertTrue(label.getFontMetrics(label.getFont()).stringWidth(label.getText()) <= combo.getWidth() - 30,
                    "Appearance option is clipped: " + label.getText());
        }
    }

    private static void assertBadge(PlayerCard card, Piece piece, PieceStyle style) {
        JLabel label = card.getPieceBadge();
        PieceBadgeIcon icon = (PieceBadgeIcon) label.getIcon();
        assertEquals(piece, icon.getPiece());
        assertEquals(style, icon.getStyle());
        assertEquals(Lang.pieceName(piece, style), label.getToolTipText());
        assertEquals(label.getToolTipText(), label.getAccessibleContext().getAccessibleName());
    }

    private static void assertAppearance(MainFrame frame, BoardTheme board, PieceStyle stones) {
        assertEquals(board, frame.getBoardPanel().getBoardTheme());
        assertEquals(stones, frame.getBoardPanel().getPieceStyle());
        assertBadge(frame.getLeftPlayerCard(), Piece.X, stones);
        assertBadge(frame.getRightPlayerCard(), Piece.O, stones);
    }

    private static void withFrame(Consumer<MainFrame> test) {
        Lang.Language language = Lang.get();
        boolean sound = SoundManager.isSoundEnabled();
        MainFrame frame = Task4StartScreenTest.frame(Lang.Language.VI);
        try {
            test.accept(frame);
        } finally {
            frame.dispose();
            Lang.set(language);
            SoundManager.setSoundEnabled(sound);
        }
    }
}
