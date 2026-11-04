package game;

import game.view.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class TwoPlayerSetupDialogTest {
    interface Scenario { void run(MainFrame frame,SetupDialog dialog); }
    static void withDialog(Lang.Language language,Scenario scenario) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            Lang.Language before=Lang.get(); boolean sound=SoundManager.isSoundEnabled();
            MainFrame f=null; SetupDialog d=null;
            try { f=Task4StartScreenTest.frame(language); d=new SetupDialog(f,false); scenario.run(f,d); }
            finally { if(d!=null)d.dispose(); if(f!=null)f.dispose(); Lang.set(before); SoundManager.setSoundEnabled(sound); }
        });
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void bothPlayersHaveSymmetricHeadersAndNameLabels(Lang.Language language) throws Exception {
        withDialog(language,(f,d)->{
            F4LayoutTest.layout(d);
            assertEquals(Lang.defaultPlayerName(),d.getFirstTitle().getText());
            assertEquals(Lang.defaultPlayerTwo(),d.getSecondTitle().getText());
            assertEquals(d.getFirstEditor().getSize(),d.getSecondEditor().getSize());
            assertEquals(d.getFirstTitle().getSize(),d.getSecondTitle().getSize());
            for(ProfileEditor editor:new ProfileEditor[]{d.getFirstEditor(),d.getSecondEditor()}) {
                JLabel caption=findCaption(editor,editor.getNameField());
                assertNotNull(caption); assertEquals(Lang.playerNameLabel(),caption.getText());
            }
            assertTrue(d.getStartButton().isEnabled());
        });
    }
    static JLabel findCaption(Container root,JTextField field) {
        for(Component c:root.getComponents()) {
            if(c instanceof JLabel label&&label.getLabelFor()==field)return label;
            if(c instanceof Container child) { JLabel found=findCaption(child,field);if(found!=null)return found; }
        }
        return null;
    }
    @Test void duplicateWarningUpdatesImmediatelyAndBlocksButtonAndEnter() throws Exception {
        withDialog(Lang.Language.VI,(f,d)->{
            JTextField first=d.getFirstEditor().getNameField(),second=d.getSecondEditor().getNameField();
            first.setText(" Messi ");second.setText("mEsSi");
            assertFalse(d.getStartButton().isEnabled()); assertEquals(Lang.duplicatePlayerNames(),d.getValidationMessage().getText());
            d.getRootPane().getDefaultButton().doClick();first.postActionEvent();second.postActionEvent();
            assertEquals(MainFrame.Screen.TITLE,f.getScreen());
            first.setText("Người chơi 2");second.setText("");assertFalse(d.getStartButton().isEnabled());
            first.setText("  ");second.setText("  ");assertTrue(d.getStartButton().isEnabled());
            first.setText("Messi");second.setText("Ronaldo");assertTrue(d.getStartButton().isEnabled());
            assertEquals(" ",d.getValidationMessage().getText());
        });
    }
    @Test void avatarLocksFollowBothChoicesAndLockedIconsAreDimmed() throws Exception {
        withDialog(Lang.Language.VI,(f,d)->{
            ProfileEditor first=d.getFirstEditor(),second=d.getSecondEditor();
            assertEquals(0,first.value().avatarIndex());assertEquals(1,second.value().avatarIndex());
            assertFalse(first.getAvatarButton(1).isEnabled());assertFalse(second.getAvatarButton(0).isEnabled());
            first.getAvatarButton(3).doClick();assertFalse(second.getAvatarButton(3).isEnabled());assertTrue(second.getAvatarButton(0).isEnabled());
            second.getAvatarButton(3).doClick();assertEquals(1,second.value().avatarIndex());
            second.getAvatarButton(4).doClick();assertFalse(first.getAvatarButton(4).isEnabled());assertTrue(first.getAvatarButton(1).isEnabled());
            first.getAvatarButton(4).doClick();assertEquals(3,first.value().avatarIndex());
            assertTrue(iconAlpha(first.getAvatarButton(4))<iconAlpha(second.getAvatarButton(4)),"Locked avatar must be visibly faded");
        });
    }
    static int iconAlpha(JButton button) {
        BufferedImage image=new BufferedImage(52,52,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try { button.getIcon().paintIcon(button,g,0,0); } finally { g.dispose(); }
        return image.getRGB(26,5)>>>24;
    }
    @Test void startCommitsBothDraftsAndCancelDiscardsChanges() throws Exception {
        withDialog(Lang.Language.VI,(f,d)->{
            d.getFirstEditor().getNameField().setText(" Messi ");d.getFirstEditor().getAvatarButton(5).doClick();
            d.getSecondEditor().getNameField().setText(" Ronaldo ");d.getSecondEditor().getAvatarButton(6).doClick();
            d.getStartButton().doClick();assertEquals(new PlayerProfile("Messi",5),f.getProfile());assertEquals(new PlayerProfile("Ronaldo",6),f.getSecondProfile());
            assertEquals(MainFrame.Screen.GAME,f.getScreen());
            SetupDialog cancelled=new SetupDialog(f,false);
            try { cancelled.getFirstEditor().getNameField().setText("Discard");cancelled.getSecondEditor().getAvatarButton(2).doClick();cancelled.getCancelButton().doClick(); }
            finally { cancelled.dispose(); }
            assertEquals(new PlayerProfile("Messi",5),f.getProfile());assertEquals(new PlayerProfile("Ronaldo",6),f.getSecondProfile());
        });
    }
}
