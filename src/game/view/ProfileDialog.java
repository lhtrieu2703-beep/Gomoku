package game.view;
import javax.swing.*;
import java.awt.*;
public final class ProfileDialog extends LocalizedDialog {
    private final ProfileEditor editor; private final JButton done=new ThemedButton("",ThemedButton.Style.PRIMARY);
    public ProfileDialog(MainFrame owner) {
        super(owner); editor=new ProfileEditor(owner.getProfile()); JPanel content=new JPanel(new BorderLayout(0,16));
        content.setBorder(BorderFactory.createEmptyBorder(22,24,22,24)); content.add(editor); content.add(done,BorderLayout.SOUTH); setContentPane(content);
        done.addActionListener(e -> { owner.updateProfile(editor.value()); dispose(); }); editor.getNameField().addActionListener(e -> done.doClick()); finishLayout(done);
    }
    protected void refreshLanguage() { setTitle(Lang.profile()); done.setText(Lang.done()); editor.refreshLanguage(); repack(); }
    public ProfileEditor getEditor() { return editor; } public JButton getDoneButton() { return done; }
}
