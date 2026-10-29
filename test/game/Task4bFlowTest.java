package game;
import game.ai.MinimaxAI;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static org.junit.jupiter.api.Assertions.*;
class Task4bFlowTest {
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(language);
            try {
                f.showMenu();
                for(boolean game:new boolean[]{false,true}) {
                    if(game) f.startGame();
                    SwingUtilities.invokeLater(() -> {
                        RulesDialog d=(RulesDialog)java.util.Arrays.stream(f.getOwnedWindows()).filter(w -> w instanceof RulesDialog&&w.isVisible()).findFirst().orElseThrow();
                        for(int i=1;i<=6;i++) assertTrue(d.getBody().getText().contains(Lang.rulesLine(i)));
                        assertEquals(Lang.rulesTitle(),d.getTitle()); d.getCloseButton().doClick();
                    });
                    if(game) f.getRulesButton().doClick(); else f.getMainMenu().getRulesButton().doClick();
                }
            } finally { f.dispose(); }
        });
    }
    @Test void titleAcceptsMouseAndAnyKeyAndStopsItsTimer() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for(boolean key:new boolean[]{false,true}) {
                MainFrame f=Task4StartScreenTest.frame(Lang.Language.EN);
                try {
                    if(key) KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(new KeyEvent(f.getStartScreen(),KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,KeyEvent.VK_F8,KeyEvent.CHAR_UNDEFINED));
                    else f.getStartScreen().dispatchEvent(new MouseEvent(f.getStartScreen(),MouseEvent.MOUSE_PRESSED,System.currentTimeMillis(),0,10,10,1,false));
                    assertEquals(MainFrame.Screen.MENU,f.getScreen()); assertFalse(f.getStartScreen().isHintTimerRunning());
                } finally { f.dispose(); }
            }
        });
    }
    static void escape(JDialog dialog) {
        JRootPane root=dialog.getRootPane(); KeyStroke key=KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
        Object action=root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(key); assertNotNull(action);
        root.getActionMap().get(action).actionPerformed(new ActionEvent(root,ActionEvent.ACTION_PERFORMED,"escape"));
    }
    @Test void enterCommitsProfileAndEscapeCancelsEveryDialogDraft() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(Lang.Language.EN);
            try {
                ProfileDialog draft=new ProfileDialog(f); draft.getEditor().getNameField().setText("Discard"); draft.getEditor().getAvatarButton(7).doClick();
                escape(draft); assertEquals(new PlayerProfile("",0),f.getProfile()); assertFalse(draft.isDisplayable());
                ProfileDialog commit=new ProfileDialog(f); commit.getEditor().getNameField().setText(" João "); commit.getEditor().getAvatarButton(3).doClick();
                commit.getRootPane().getDefaultButton().doClick(); assertEquals(new PlayerProfile("João",3),f.getProfile());
                SetupDialog setup=new SetupDialog(f,true); setup.getDifficultyCombo().setSelectedItem(MinimaxAI.Difficulty.HARD); escape(setup);
                assertEquals(MinimaxAI.Difficulty.MEDIUM,f.getSelectedDifficulty());
                for(LocalizedDialog d:new LocalizedDialog[]{new SetupDialog(f,false),new SettingsDialog(f),new RulesDialog(f)}) { escape(d); assertFalse(d.isDisplayable()); }
                for(LocalizedDialog d:new LocalizedDialog[]{new SettingsDialog(f),new RulesDialog(f)}) { d.getRootPane().getDefaultButton().doClick(); assertFalse(d.isDisplayable()); }
            } finally { f.dispose(); }
        });
    }
    @Test void setupRemembersDifficultyAndPlayerTwoOnlyAfterStart() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(Lang.Language.VI);
            try {
                f.showMenu(); SetupDialog ai=new SetupDialog(f,true); assertEquals(MinimaxAI.Difficulty.MEDIUM,ai.getDifficultyCombo().getSelectedItem());
                ai.getDifficultyCombo().setSelectedItem(MinimaxAI.Difficulty.HARD); ai.getStartButton().doClick(); f.returnToMenu();
                assertEquals(BoardPanel.GameMode.PV_AI,f.getSelectedMode()); assertEquals(MinimaxAI.Difficulty.HARD,f.getSelectedDifficulty());
                SetupDialog reopened=new SetupDialog(f,true); assertEquals(MinimaxAI.Difficulty.HARD,reopened.getDifficultyCombo().getSelectedItem()); reopened.dispose();
                SetupDialog pvp=new SetupDialog(f,false); pvp.getSecondEditor().getNameField().setText("Conceição"); pvp.getSecondEditor().getAvatarButton(4).doClick();
                pvp.getStartButton().doClick(); f.returnToMenu(); assertEquals(BoardPanel.GameMode.PVP,f.getSelectedMode());
                assertEquals(new PlayerProfile("Conceição",4),f.getSecondProfile()); assertEquals(MinimaxAI.Difficulty.HARD,f.getSelectedDifficulty());
            } finally { f.dispose(); }
        });
    }
    @ParameterizedTest @MethodSource("game.Task4LayoutTest#languagesAndLooks")
    void allCardsAndDialogsFitBothLooksWithStableFrameSize(Lang.Language language,boolean nativeLook) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String previous=UIManager.getLookAndFeel().getClass().getName(); MainFrame f=null;
            try {
                UIManager.setLookAndFeel(nativeLook?UIManager.getSystemLookAndFeelClassName():UIManager.getCrossPlatformLookAndFeelClassName());
                f=Task4StartScreenTest.frame(language); Dimension outer=f.getSize();
                F4LayoutTest.layout(f); F4LayoutTest.check(f.getContentPane()); f.showMenu(); F4LayoutTest.layout(f); F4LayoutTest.check(f.getContentPane());
                assertEquals(outer,f.getSize()); f.startGame(); F4LayoutTest.layout(f); F4LayoutTest.check(f.getContentPane()); assertEquals(outer,f.getSize());
                assertEquals(new Dimension(760,758),f.getContentPane().getSize()); assertFalse(f.isResizable());
                for(LocalizedDialog dialog:new LocalizedDialog[]{new ProfileDialog(f),new SettingsDialog(f),new RulesDialog(f),new SetupDialog(f,true),new SetupDialog(f,false)}) {
                    F4LayoutTest.layout(dialog); F4LayoutTest.check(dialog.getContentPane()); dialog.dispose();
                }
            } catch(Exception e) { throw new AssertionError(e); }
            finally { if(f!=null) f.dispose(); try { UIManager.setLookAndFeel(previous); } catch(Exception e) { throw new AssertionError(e); } }
        });
    }
}
