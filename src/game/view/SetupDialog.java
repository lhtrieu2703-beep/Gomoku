package game.view;
import game.ai.MinimaxAI;
import javax.swing.*;
import java.awt.*;
public final class SetupDialog extends LocalizedDialog {
    private final boolean ai; private final MainFrame owner;
    private final JComboBox<MinimaxAI.Difficulty> difficulty=new JComboBox<>(MinimaxAI.Difficulty.values());
    private final JLabel label=new JLabel(); private final ProfileEditor first,second;
    private final JLabel firstTitle=new JLabel(),secondTitle=new JLabel(),validationMessage=new JLabel(" ");
    private final JButton start=new ThemedButton("",ThemedButton.Style.PRIMARY),cancel=new ThemedButton("",ThemedButton.Style.SECONDARY);
    public SetupDialog(MainFrame owner,boolean ai) {
        super(owner); this.owner=owner; this.ai=ai; first=ai?null:new ProfileEditor(owner.getProfile());
        PlayerProfile other=owner.getSecondProfile();
        second=ai?null:new ProfileEditor(new PlayerProfile(other.name(),TwoPlayerSetupValidation.initialSecondAvatar(owner.getProfile().avatarIndex(),other.avatarIndex())));
        JPanel content=new JPanel(new BorderLayout(0,16)); content.setBorder(BorderFactory.createEmptyBorder(24,26,24,26));
        if(ai) { JPanel form=new JPanel(new BorderLayout(16,0)); form.add(label,BorderLayout.WEST); form.add(difficulty); content.add(form); }
        else {
            JPanel columns=new JPanel(new GridLayout(1,2,20,0)); columns.setOpaque(false);
            columns.add(playerColumn(firstTitle,first)); columns.add(playerColumn(secondTitle,second));
            JPanel form=new JPanel(new BorderLayout(0,12)); form.setOpaque(false); form.add(columns); form.add(validationMessage,BorderLayout.SOUTH); content.add(form);
            first.setChangeListener(this::validatePlayers); second.setChangeListener(this::validatePlayers);
        }
        JPanel buttons=new JPanel(new GridLayout(1,2,12,0)); buttons.add(cancel); buttons.add(start); content.add(buttons,BorderLayout.SOUTH); setContentPane(content);
        difficulty.setSelectedItem(owner.getSelectedDifficulty()); start.addActionListener(e -> {
            if(ai) owner.beginGame(BoardPanel.GameMode.PV_AI,(MinimaxAI.Difficulty)difficulty.getSelectedItem());
            else { if(!validatePlayers().canStart()) return; owner.updateProfile(first.value()); owner.setSecondProfile(second.value()); owner.beginGame(BoardPanel.GameMode.PVP,owner.getSelectedDifficulty()); } dispose();
        }); cancel.addActionListener(e -> dispose());
        if(second!=null) { first.getNameField().addActionListener(e -> start.doClick()); second.getNameField().addActionListener(e -> start.doClick()); }
        finishLayout(start);
        difficulty.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof MinimaxAI.Difficulty level) label.setText(Lang.difficultyName(level));
                label.setBackground(selected?Theme.BRASS:Theme.DARK); label.setForeground(selected?Theme.DARK:Theme.CREAM); return label;
            }
        });
    }
    protected void refreshLanguage() { setTitle(ai?Lang.pvAI():Lang.pvp()); label.setText(Lang.difficulty()); start.setText(Lang.startGame()); cancel.setText(Lang.cancel());
        if(second!=null) {
            firstTitle.setText(Lang.defaultPlayerName()); secondTitle.setText(Lang.defaultPlayerTwo());
            firstTitle.setFont(Theme.font(Font.BOLD,16)); secondTitle.setFont(Theme.font(Font.BOLD,16));
            firstTitle.setBackground(Theme.WOOD_DARK); secondTitle.setBackground(Theme.WOOD_DARK);
            first.setNameCaption(Lang.playerNameLabel()); second.setNameCaption(Lang.playerNameLabel());
            first.refreshLanguage(); second.refreshLanguage(); validationMessage.setForeground(Theme.ERROR); validatePlayers();
        }
        int width=java.util.Arrays.stream(MinimaxAI.Difficulty.values()).map(Lang::difficultyName).mapToInt(t -> difficulty.getFontMetrics(difficulty.getFont()).stringWidth(t)).max().orElse(100);
        difficulty.setPreferredSize(new Dimension(width+48,36)); difficulty.repaint(); repack(); }
    public JComboBox<MinimaxAI.Difficulty> getDifficultyCombo() { return difficulty; } public ProfileEditor getSecondEditor() { return second; }
    private static JPanel playerColumn(JLabel title,ProfileEditor editor) {
        JPanel column=new JPanel(new BorderLayout(0,12)); column.setOpaque(false);
        title.setOpaque(true); title.setBackground(Theme.WOOD_DARK);
        title.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BRASS),BorderFactory.createEmptyBorder(8,12,8,12)));
        column.add(title,BorderLayout.NORTH); column.add(editor); return column;
    }
    private TwoPlayerSetupValidation.Result validatePlayers() {
        PlayerProfile one=first.value(),two=second.value();
        var result=TwoPlayerSetupValidation.validate(one.name(),two.name(),Lang.defaultPlayerName(),Lang.defaultPlayerTwo(),one.avatarIndex(),two.avatarIndex());
        first.setBlockedAvatar(two.avatarIndex()); second.setBlockedAvatar(one.avatarIndex());
        validationMessage.setText(result.duplicateNames()?Lang.duplicatePlayerNames():" ");
        start.setEnabled(result.canStart()); return result;
    }
    public ProfileEditor getFirstEditor() { return first; }
    public JLabel getFirstTitle() { return firstTitle; } public JLabel getSecondTitle() { return secondTitle; }
    public JLabel getValidationMessage() { return validationMessage; }
    public JButton getStartButton() { return start; } public JButton getCancelButton() { return cancel; }
}
