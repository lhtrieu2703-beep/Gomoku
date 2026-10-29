package game.view;
import game.ai.MinimaxAI;
import javax.swing.*;
import java.awt.*;
public final class SetupDialog extends LocalizedDialog {
    private final boolean ai; private final MainFrame owner;
    private final JComboBox<MinimaxAI.Difficulty> difficulty=new JComboBox<>(MinimaxAI.Difficulty.values());
    private final JLabel label=new JLabel(); private final ProfileEditor second;
    private final JButton start=new ThemedButton("",ThemedButton.Style.PRIMARY),cancel=new ThemedButton("",ThemedButton.Style.SECONDARY);
    public SetupDialog(MainFrame owner,boolean ai) {
        super(owner); this.owner=owner; this.ai=ai; second=ai?null:new ProfileEditor(owner.getSecondProfile());
        JPanel content=new JPanel(new BorderLayout(0,16)); content.setBorder(BorderFactory.createEmptyBorder(24,26,24,26));
        if(ai) { JPanel form=new JPanel(new BorderLayout(16,0)); form.add(label,BorderLayout.WEST); form.add(difficulty); content.add(form); } else content.add(second);
        JPanel buttons=new JPanel(new GridLayout(1,2,12,0)); buttons.add(cancel); buttons.add(start); content.add(buttons,BorderLayout.SOUTH); setContentPane(content);
        difficulty.setSelectedItem(owner.getSelectedDifficulty()); start.addActionListener(e -> {
            if(ai) owner.beginGame(BoardPanel.GameMode.PV_AI,(MinimaxAI.Difficulty)difficulty.getSelectedItem());
            else { owner.setSecondProfile(second.value()); owner.beginGame(BoardPanel.GameMode.PVP,owner.getSelectedDifficulty()); } dispose();
        }); cancel.addActionListener(e -> dispose()); if(second!=null) second.getNameField().addActionListener(e -> start.doClick()); finishLayout(start);
        difficulty.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof MinimaxAI.Difficulty level) label.setText(Lang.difficultyName(level));
                label.setBackground(selected?Theme.BRASS:Theme.DARK); label.setForeground(selected?Theme.DARK:Theme.CREAM); return label;
            }
        });
    }
    protected void refreshLanguage() { setTitle(ai?Lang.pvAI():Lang.pvp()); label.setText(Lang.difficulty()); start.setText(Lang.startGame()); cancel.setText(Lang.cancel());
        if(second!=null) second.refreshLanguage();
        int width=java.util.Arrays.stream(MinimaxAI.Difficulty.values()).map(Lang::difficultyName).mapToInt(t -> difficulty.getFontMetrics(difficulty.getFont()).stringWidth(t)).max().orElse(100);
        difficulty.setPreferredSize(new Dimension(width+48,36)); difficulty.repaint(); repack(); }
    public JComboBox<MinimaxAI.Difficulty> getDifficultyCombo() { return difficulty; } public ProfileEditor getSecondEditor() { return second; }
    public JButton getStartButton() { return start; } public JButton getCancelButton() { return cancel; }
}
