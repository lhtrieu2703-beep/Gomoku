package game;
import game.ai.MinimaxAI;
import game.model.*;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import static org.junit.jupiter.api.Assertions.*;

class Task4bGameplayTest {
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(language);
            try {
                SetupDialog dialog=new SetupDialog(f,true);
                for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values()) {
                    JLabel rendered=(JLabel)dialog.getDifficultyCombo().getRenderer().getListCellRendererComponent(new JList<>(),level,0,false,false);
                    assertEquals(Lang.difficultyName(level),rendered.getText());
                    f.beginGame(BoardPanel.GameMode.PV_AI,level); assertEquals(Lang.computerName(level),f.getRightPlayerCard().getFullName());
                    if(language==Lang.Language.PT) for(char vi:LangPurityTest.VI_ONLY_CHARS.toCharArray()) assertFalse(Lang.computerName(level).indexOf(vi)>=0);
                }
                dialog.dispose();
            } finally { f.dispose(); }
        });
    }
    @ParameterizedTest @EnumSource(Piece.class)
    void realGameResultsUpdateBothCardsAndDrawCount(Piece winner) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(Lang.Language.PT);
            try {
                f.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM); BoardPanel panel=f.getBoardPanel();
                SwingUtilities.invokeLater(() -> { JDialog result=P2LifecycleTest.field(f,"resultDialog"); assertNotNull(result); result.dispose(); });
                if(winner==Piece.NONE) {
                    for(int r=0;r<15;r++) for(int c=0;c<15;c++) panel.executeMoveForTest(r,c,(c+(r%2==0?0:2))%4<2?Piece.X:Piece.O);
                } else for(int c=0;c<5;c++) panel.executeMoveForTest(7,c,winner);
                assertTrue(panel.isGameOver());
                assertEquals(Lang.wins()+": "+(winner==Piece.X?1:0),f.getLeftPlayerCard().getWinsLabel().getText());
                assertEquals(Lang.wins()+": "+(winner==Piece.O?1:0),f.getRightPlayerCard().getWinsLabel().getText());
                assertEquals(Lang.draws()+": "+(winner==Piece.NONE?1:0),f.getDrawsLabel().getText());
                assertEquals(winner==Piece.X,f.getLeftPlayerCard().isHighlighted()); assertEquals(winner==Piece.O,f.getRightPlayerCard().isHighlighted());
            } finally { f.dispose(); }
        });
    }
    @Test void scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var node=new MemoryPreferences(); MainFrame f=new MainFrame(new AppSettings(node));
            try {
                f.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM); f.getScoreTracker().recordResult(-1,Piece.X);
                f.updateProfile(new PlayerProfile("João",7)); f.setSecondProfile(new PlayerProfile("Ánh",3));
                assertEquals(1,f.getScoreTracker().getHumanWins()); f.returnToMenu(); f.startGame(); assertEquals(1,f.getScoreTracker().getHumanWins());
                f.beginGame(BoardPanel.GameMode.PV_AI,MinimaxAI.Difficulty.MEDIUM); assertEquals(0,f.getScoreTracker().getHumanWins());
                f.getScoreTracker().recordResult(-2,Piece.O); f.beginGame(BoardPanel.GameMode.PV_AI,MinimaxAI.Difficulty.HARD); assertEquals(0,f.getScoreTracker().getAiWins());
                assertNull(node.get("humanWins",null)); assertNull(node.get("aiWins",null)); assertNull(node.get("draws",null));
                assertEquals(java.util.Set.of("playerName","avatarIndex"),java.util.Set.of(node.keys()));
            } catch(Exception e) { throw new AssertionError(e); } finally { f.dispose(); }
        });
    }
    @Test void resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(Lang.Language.EN);
            try {
                f.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM); f.getBoardPanel().playMove(7,7);
                int gameId=f.getBoardPanel().getGameId(); f.getScoreTracker().recordResult(-1,Piece.X); SettingsDialog settings=new SettingsDialog(f);
                SwingUtilities.invokeLater(() -> {
                    JDialog confirm=(JDialog)java.util.Arrays.stream(settings.getOwnedWindows()).filter(Window::isVisible).findFirst().orElseThrow();
                    JOptionPane pane=find(confirm,JOptionPane.class); pane.setValue(JOptionPane.YES_OPTION);
                });
                settings.getResetButton().doClick(); assertEquals(0,f.getScoreTracker().getHumanWins());
                assertEquals(gameId,f.getBoardPanel().getGameId()); assertEquals(1,f.getBoardPanel().getMoveHistory().size());
                assertEquals(Piece.O,f.getBoardPanel().getCurrentTurn()); assertTrue(f.getBoardPanel().canUndo()); settings.dispose();
            } finally { f.dispose(); }
        });
    }
    @Test void resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain() {
        ScoreTracker scores=new ScoreTracker(); assertTrue(scores.recordResult(7,Piece.X)); scores.resetScores();
        assertEquals(0,scores.getHumanWins()); assertFalse(scores.recordResult(7,Piece.O)); assertEquals(0,scores.getAiWins());
        assertTrue(scores.recordResult(8,Piece.O)); assertEquals(1,scores.getAiWins());
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void boardIsCentredAndMouseUsesUnchangedCoordinates(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(language);
            try {
                f.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM); F4LayoutTest.layout(f);
                BoardPanel panel=f.getBoardPanel(); Point origin=SwingUtilities.convertPoint(panel,0,0,f.getContentPane());
                assertEquals(new Dimension(596,596),panel.getSize());
                int left=origin.x+32,right=f.getContentPane().getWidth()-(origin.x+32+14*38);
                assertTrue(Math.abs(left-right)<=1,"left="+left+" right="+right);
                assertTrue(f.getLeftPlayerCard().isHighlighted()); assertFalse(f.getRightPlayerCard().isHighlighted());
                panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,32+7*38,32+7*38,1,false));
                assertEquals(1,panel.getMoveHistory().size()); Board board=P2LifecycleTest.field(f,"board"); assertEquals(Piece.X,board.getPiece(7,7));
                assertFalse(f.getLeftPlayerCard().isHighlighted()); assertTrue(f.getRightPlayerCard().isHighlighted());
                f.updateProfile(new PlayerProfile("WWWWWWWWWWWWWWWW",0)); F4LayoutTest.layout(f);
                assertTrue(f.getLeftPlayerCard().getNameLabel().getText().endsWith("..."));
                assertEquals("WWWWWWWWWWWWWWWW",f.getLeftPlayerCard().getNameLabel().getToolTipText());
                for(String obsolete:new String[]{"btnAchievements","lblScoreTitle","modeCombo","diffCombo","langCombo","turnLabel","scoresLabel"})
                    assertThrows(NoSuchFieldException.class,() -> MainFrame.class.getDeclaredField(obsolete));
                assertNull(find(f.getContentPane(),JComboBox.class));
            } finally { f.dispose(); }
        });
    }
    static <T> T find(Container container,Class<T> type) {
        if(type.isInstance(container)) return type.cast(container);
        for(Component component:container.getComponents()) {
            if(type.isInstance(component)) return type.cast(component);
            if(component instanceof Container child) { T found=find(child,type); if(found!=null) return found; }
        } return null;
    }
}
