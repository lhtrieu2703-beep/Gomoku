package game.view;
import javax.swing.*;
import java.awt.event.*;
public abstract class LocalizedDialog extends JDialog {
    private final Runnable languageListener=this::refreshLanguage;
    protected LocalizedDialog(MainFrame owner) {
        super(owner,true); setDefaultCloseOperation(DISPOSE_ON_CLOSE); setResizable(false); getContentPane().setBackground(Theme.DARK);
        getRootPane().registerKeyboardAction(e -> dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        Lang.addChangeListener(languageListener);
    }
    protected abstract void refreshLanguage();
    protected final void finishLayout(JButton defaultButton) {
        Theme.styleTree(getContentPane()); refreshLanguage(); getRootPane().setDefaultButton(defaultButton); pack(); setLocationRelativeTo(getOwner());
    }
    protected final void repack() { pack(); revalidate(); repaint(); }
    @Override public void dispose() { Lang.removeChangeListener(languageListener); super.dispose(); }
}
