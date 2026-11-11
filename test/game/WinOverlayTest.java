package game;

import game.ai.MinimaxAI;
import game.model.*;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class WinOverlayTest {
    private Lang.Language previous;
    private boolean sound;
    @BeforeEach void remember() { previous = Lang.get(); sound = SoundManager.isSoundEnabled(); }
    @AfterEach void restore() throws Exception {
        SwingUtilities.invokeAndWait(() -> { Lang.set(previous); SoundManager.setSoundEnabled(sound); });
    }
    private void withFrame(Consumer<MainFrame> check) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = Task4StartScreenTest.frame(Lang.Language.VI);
            try {
                frame.updateProfile(new PlayerProfile("Ngọc Ánh", 0));
                frame.setSecondProfile(new PlayerProfile("Trần Minh", 1));
                frame.beginGame(BoardPanel.GameMode.PVP, MinimaxAI.Difficulty.MEDIUM);
                check.accept(frame);
            } finally { frame.dispose(); }
        });
    }
    static void win(BoardPanel board, Piece winner, int row) {
        for (int c = 0; c < 4; c++) {
            board.executeMoveForTest(row, c + 4, winner);
            board.executeMoveForTest(row == 0 ? 14 : 0, c * 2, winner.opposite());
        }
        board.executeMoveForTest(row, 8, winner);
    }
    static void draw(BoardPanel board) {
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            board.executeMoveForTest(r, c, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O);
    }

    @ParameterizedTest @EnumSource(value = Piece.class, names = {"X", "O"})
    void namedWinnerAndHeaderTranslateTogetherWithoutChangingTheRound(Piece winner) throws Exception {
        withFrame(frame -> {
            BoardPanel board = frame.getBoardPanel(); win(board, winner, 3);
            int id = board.getGameId();
            String name = winner == Piece.X ? "Ngọc Ánh" : "Trần Minh";
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language);
                String suffix = switch (language) { case VI -> "CHIẾN THẮNG!"; case EN -> "WINS!"; case PT -> "VENCEU!"; };
                assertEquals(name + " " + suffix, board.getResultOverlay().getFullTitle());
                assertEquals(name + " " + suffix, board.getGameOverMessage());
                assertEquals(name + " " + suffix, frame.getStatusText());
                assertEquals(id, board.getGameId()); assertTrue(board.getResultOverlay().isVisible());
                assertEquals(winner, board.getResultOverlay().getWinner());
                assertTrue(java.util.Arrays.stream(frame.getOwnedWindows()).noneMatch(Window::isVisible));
            }
        });
    }

    @ParameterizedTest @EnumSource(value = Piece.class, names = {"X", "O"})
    void whitespaceNameUsesTheExistingTranslatedDefault(Piece winner) throws Exception {
        withFrame(frame -> {
            frame.updateProfile(new PlayerProfile("   ", 0)); frame.setSecondProfile(new PlayerProfile("\t", 1));
            win(frame.getBoardPanel(), winner, 3);
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language);
                String expected = winner == Piece.X ? Lang.p1Wins() : Lang.p2Wins();
                assertEquals(expected, frame.getStatusText());
                assertEquals(expected, frame.getBoardPanel().getResultOverlay().getFullTitle());
            }
        });
    }

    @ParameterizedTest @EnumSource(Lang.Language.class)
    void fullDrawShowsTranslatedDrawAndScore(Lang.Language language) throws Exception {
        withFrame(frame -> {
            frame.selectLanguage(language); draw(frame.getBoardPanel());
            EndGameOverlay overlay = frame.getBoardPanel().getResultOverlay();
            assertTrue(overlay.isVisible()); assertEquals(Piece.NONE, overlay.getWinner());
            assertEquals(switch (language) { case VI -> "HÒA CỜ"; case EN -> "DRAW"; case PT -> "EMPATE"; }, overlay.getFullTitle());
            assertEquals(overlay.getFullTitle(), frame.getStatusText());
            assertEquals(1, frame.getScoreTracker().getDraws());
            assertEquals(Lang.resultScore(0, 0, 1), overlay.getScoreLabel().getText());
            assertTrue(overlay.getY() > frame.getBoardPanel().getHeight() / 2);
            assertNull(overlay.getTitleLabel().getIcon());
        });
    }

    @ParameterizedTest @CsvSource({"PV_AI, X", "PV_AI, O", "PVP, X", "PVP, O"})
    void bottomUndoWhileOverlayIsVisibleRevokesScoreAndClearsBothResultControls(BoardPanel.GameMode mode, Piece piece) throws Exception {
        withFrame(frame -> {
            frame.beginGame(mode, MinimaxAI.Difficulty.MEDIUM);
            BoardPanel board = frame.getBoardPanel(); win(board, piece, 3);
            assertTrue(board.getResultOverlay().isVisible()); assertTrue(frame.getUndoButton().isEnabled());
            frame.getUndoButton().doClick();
            assertFalse(board.isGameOver()); assertTrue(board.getWinningLine().isEmpty());
            assertFalse(board.getResultOverlay().isVisible()); assertFalse(board.getShowResultButton().isVisible());
            assertEquals(0, frame.getScoreTracker().getHumanWins()); assertEquals(0, frame.getScoreTracker().getAiWins());
        });
    }

    @ParameterizedTest @ValueSource(strings = {"overlay-new", "toolbar-new", "overlay-menu", "toolbar-menu"})
    void newGameAndMenuActionsHideTheOverlayAndRejectLateResults(String action) throws Exception {
        withFrame(frame -> {
            BoardPanel board = frame.getBoardPanel(); win(board, Piece.X, 3); int old = board.getGameId();
            BoardPanel.GameStateListener listener = P2LifecycleTest.field(board, "stateListener");
            JButton button = switch (action) {
                case "overlay-new" -> board.getResultOverlay().getNewGameButton();
                case "toolbar-new" -> frame.getNewGameButton();
                case "overlay-menu" -> board.getResultOverlay().getMenuButton();
                default -> frame.getBackToMenuButton();
            };
            button.doClick();
            assertFalse(board.getResultOverlay().isVisible()); assertFalse(board.getShowResultButton().isVisible());
            assertTrue(board.getWinningLine().isEmpty()); assertFalse(board.isGameOver());
            assertEquals(1, frame.getScoreTracker().getHumanWins());
            assertEquals(action.endsWith("menu") ? MainFrame.Screen.MENU : MainFrame.Screen.GAME, frame.getScreen());
            listener.onGameOver(old, "stale", Piece.O);
            assertFalse(board.getResultOverlay().isVisible()); assertEquals(0, frame.getScoreTracker().getAiWins());
            if (action.endsWith("menu")) frame.startGame();
            win(board, Piece.X, 4);
            listener.onGameOver(old, "stale", Piece.O);
            assertEquals(2, frame.getScoreTracker().getHumanWins()); assertEquals(0, frame.getScoreTracker().getAiWins());
            assertEquals(Piece.X, board.getResultOverlay().getWinner());
        });
    }

    @Test void viewBoardCanRestoreTranslatedResultAndUndoAlsoClearsTheRestoreButton() throws Exception {
        withFrame(frame -> {
            BoardPanel board = frame.getBoardPanel(); win(board, Piece.X, 3);
            board.getResultOverlay().getViewBoardButton().doClick();
            assertFalse(board.getResultOverlay().isVisible()); assertTrue(board.getShowResultButton().isVisible());
            frame.selectLanguage(Lang.Language.PT);
            assertFalse(board.getResultOverlay().isVisible()); assertEquals("Mostrar resultado", board.getShowResultButton().getText());
            board.getShowResultButton().doClick();
            assertTrue(board.getResultOverlay().isVisible()); assertFalse(board.getShowResultButton().isVisible());
            assertEquals("Ngọc Ánh VENCEU!", board.getResultOverlay().getFullTitle());
            board.getResultOverlay().getViewBoardButton().doClick(); frame.getUndoButton().doClick();
            assertFalse(board.getShowResultButton().isVisible()); assertFalse(board.getResultOverlay().isVisible());
        });
    }

    @Test void scoreLineUsesTrackerTotalsAndRefreshesAfterResetWithoutRecounting() throws Exception {
        withFrame(frame -> {
            var scores = frame.getScoreTracker();
            scores.recordResult(-1, Piece.X); scores.recordResult(-2, Piece.O); scores.recordResult(-3, Piece.NONE);
            win(frame.getBoardPanel(), Piece.X, 3);
            EndGameOverlay overlay = frame.getBoardPanel().getResultOverlay();
            assertEquals(2, scores.getHumanWins()); assertEquals(1, scores.getAiWins()); assertEquals(1, scores.getDraws());
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language);
                assertEquals(Lang.resultScore(scores.getHumanWins(), scores.getAiWins(), scores.getDraws()), overlay.getScoreLabel().getText());
            }
            frame.resetSessionScores();
            assertEquals(Lang.resultScore(0, 0, 0), overlay.getScoreLabel().getText());
            frame.selectLanguage(Lang.Language.VI); assertEquals(0, scores.getHumanWins());
        });
    }

    @Test void bridgingNineDiagonalStonesUsesAClearCompactCornerWithCompleteButtonLabels() throws Exception {
        withFrame(frame -> {
            BoardPanel board = frame.getBoardPanel();
            for (int i = 0; i < 9; i++) if (i != 4) board.executeMoveForTest(i + 3, i, Piece.X);
            board.executeMoveForTest(7, 4, Piece.X);
            assertEquals(9, board.getWinningLine().size());
            EndGameOverlay overlay = board.getResultOverlay();
            assertEquals(248, overlay.getWidth());
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language); F4LayoutTest.layout(frame);
                for (JButton button : new JButton[]{overlay.getNewGameButton(), overlay.getMenuButton(), overlay.getViewBoardButton()}) {
                    assertTrue(button.getFontMetrics(button.getFont()).stringWidth(button.getText()) + 28 <= button.getWidth());
                    assertTrue(new Rectangle(overlay.getSize()).contains(button.getBounds()));
                }
                JLabel title = overlay.getTitleLabel();
                assertTrue(title.getFontMetrics(title.getFont()).stringWidth(title.getText()) + 46 <= title.getWidth());
                JLabel score = overlay.getScoreLabel();
                assertTrue(score.getFontMetrics(score.getFont()).stringWidth(score.getText()) <= score.getWidth());
            }
        });
    }

    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void aiAndHumanWinnersUseTheSameNamesAsTheirPlayerCards(MinimaxAI.Difficulty difficulty) throws Exception {
        withFrame(frame -> {
            for (Piece piece : new Piece[]{Piece.X, Piece.O}) for (Lang.Language language : Lang.Language.values()) {
                frame.beginGame(BoardPanel.GameMode.PV_AI, difficulty); frame.selectLanguage(language);
                win(frame.getBoardPanel(), piece, 11);
                String name = piece == Piece.X ? frame.getLeftPlayerCard().getFullName() : frame.getRightPlayerCard().getFullName();
                assertEquals(Lang.namedWinner(name), frame.getStatusText());
                assertEquals(frame.getStatusText(), frame.getBoardPanel().getResultOverlay().getFullTitle());
            }
        });
    }

    static Stream<Arguments> finishes() {
        return Stream.of(BoardTheme.values()).flatMap(theme -> Stream.of(PieceStyle.values())
                .flatMap(style -> Stream.of(Piece.X, Piece.O).map(piece -> Arguments.of(theme, style, piece))));
    }
    @ParameterizedTest @MethodSource("finishes")
    void allFinishesPreserveEveryWinningCellPixelAndFitTranslatedControls(BoardTheme theme, PieceStyle style, Piece piece) throws Exception {
        withFrame(frame -> {
            frame.selectBoardTheme(theme); frame.selectPieceStyle(style);
            frame.updateProfile(new PlayerProfile("WWWWWWWWWWWWWWWW", 0));
            frame.setSecondProfile(new PlayerProfile("WWWWWWWWWWWWWWWW", 1));
            BoardPanel board = frame.getBoardPanel(); win(board, piece, piece == Piece.X ? 2 : 12);
            F4LayoutTest.layout(frame);
            assertEquals(new Dimension(774, 795), frame.getSize());
            EndGameOverlay overlay = board.getResultOverlay();
            assertEquals(style, overlay.getPieceStyle()); assertEquals(piece, overlay.getWinner());
            for (Lang.Language language : Lang.Language.values()) {
                frame.selectLanguage(language); F4LayoutTest.layout(frame);
                JLabel title = overlay.getTitleLabel();
                if (title.getFontMetrics(title.getFont()).stringWidth(overlay.getFullTitle()) + 46 > title.getWidth())
                    assertTrue(title.getText().contains("..."));
                else assertEquals(overlay.getFullTitle(), title.getText());
                assertTrue(title.getFontMetrics(title.getFont()).stringWidth(title.getText()) + 46 <= title.getWidth());
                for (JButton button : new JButton[]{overlay.getNewGameButton(), overlay.getMenuButton(), overlay.getViewBoardButton()}) {
                    assertTrue(overlay.getBounds().contains(new Rectangle(overlay.getX() + button.getX(), overlay.getY() + button.getY(), button.getWidth(), button.getHeight())));
                    assertTrue(button.getFontMetrics(button.getFont()).stringWidth(button.getText()) + 28 <= button.getWidth());
                }
                JTextArea status = P2LifecycleTest.field(frame, "turnText");
                for (String line : status.getText().split("\n")) assertTrue(status.getFontMetrics(status.getFont()).stringWidth(line) <= status.getWidth() - 16);
                BufferedImage shown = paint(board);
                overlay.getViewBoardButton().doClick(); BufferedImage hidden = paint(board);
                for (Board.Cell cell : board.getWinningLine()) {
                    int cx = BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE;
                    int cy = BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE;
                    assertFalse(overlay.getBounds().intersects(new Rectangle(cx - 22, cy - 22, 44, 44)));
                    for (int y = cy - 20; y <= cy + 20; y++) for (int x = cx - 20; x <= cx + 20; x++)
                        assertEquals(hidden.getRGB(x, y), shown.getRGB(x, y), "Win stone/ring was covered");
                }
                board.getShowResultButton().doClick();
            }
        });
    }
    private static BufferedImage paint(BoardPanel board) {
        BufferedImage image = new BufferedImage(board.getWidth(), board.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { board.paint(graphics); } finally { graphics.dispose(); }
        return image;
    }
}
