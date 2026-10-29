package game.view;
import javax.swing.*;
import java.awt.*;
public final class SettingsDialog extends LocalizedDialog {
    private final MainFrame owner; private final JComboBox<Lang.Language> language=new JComboBox<>(Lang.Language.values());
    private final JLabel languageLabel=new JLabel();
    private final JButton sound=new ThemedButton("",ThemedButton.Style.SECONDARY),reset=new ThemedButton("",ThemedButton.Style.SECONDARY),close=new ThemedButton("",ThemedButton.Style.PRIMARY);
    private boolean refreshing;
    public SettingsDialog(MainFrame owner) {
        super(owner); this.owner=owner; JPanel content=new JPanel(new GridLayout(0,1,0,14)); content.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JPanel row=new JPanel(new BorderLayout(20,0)); row.add(languageLabel,BorderLayout.WEST); row.add(language); content.add(row);
        for(JButton b:new JButton[]{sound,reset,close}) content.add(b); setContentPane(content);
        language.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus); if(value instanceof Lang.Language l) label.setText(Lang.langName(l));
                label.setBackground(selected?Theme.BRASS:Theme.DARK); label.setForeground(selected?Theme.DARK:Theme.CREAM); return label;
            }
        });
        language.addActionListener(e -> { if(!refreshing) owner.selectLanguage((Lang.Language)language.getSelectedItem()); });
        sound.setIcon(new SpeakerIcon(20)); sound.addActionListener(e -> { owner.toggleSound(); refreshLanguage(); });
        reset.addActionListener(e -> { if(confirmReset()) owner.resetSessionScores(); });
        close.addActionListener(e -> dispose()); finishLayout(close);
    }
    protected void refreshLanguage() {
        setTitle(Lang.settings()); languageLabel.setText(Lang.languageLabel()); refreshing=true; language.setSelectedItem(Lang.get()); refreshing=false;
        int width=java.util.Arrays.stream(Lang.Language.values()).map(Lang::langName).mapToInt(t -> language.getFontMetrics(language.getFont()).stringWidth(t)).max().orElse(100);
        language.setPreferredSize(new Dimension(width+48,36));
        sound.setText(Lang.sound()); sound.setToolTipText(SoundManager.isSoundEnabled()?Lang.soundTooltipOn():Lang.soundTooltipOff()); reset.setText(Lang.resetScore()); close.setText(Lang.close()); repack();
    }
    private boolean confirmReset() {
        JButton yes=new ThemedButton(Lang.done(),ThemedButton.Style.PRIMARY),no=new ThemedButton(Lang.cancel(),ThemedButton.Style.SECONDARY);
        JOptionPane pane=new JOptionPane(Lang.confirmResetScore(),JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,null,new Object[]{yes,no},no);
        JDialog dialog=pane.createDialog(this,Lang.resetScore());
        yes.addActionListener(e -> { pane.setValue(JOptionPane.YES_OPTION); dialog.dispose(); });
        no.addActionListener(e -> { pane.setValue(JOptionPane.NO_OPTION); dialog.dispose(); });
        dialog.getRootPane().setDefaultButton(no);
        dialog.getRootPane().registerKeyboardAction(e -> no.doClick(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        Theme.styleTree(pane); dialog.pack(); dialog.setLocationRelativeTo(this);
        try { dialog.setVisible(true); return Integer.valueOf(JOptionPane.YES_OPTION).equals(pane.getValue()); }
        finally { dialog.dispose(); }
    }
    public JComboBox<Lang.Language> getLanguageCombo() { return language; } public JButton getSoundButton() { return sound; }
    public JButton getResetButton() { return reset; } public JButton getCloseButton() { return close; }
}
