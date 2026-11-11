package game.view;
import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public final class SettingsDialog extends LocalizedDialog {
    private final MainFrame owner; private final JComboBox<Lang.Language> language=new JComboBox<>(Lang.Language.values());
    private final JLabel languageLabel=new JLabel();
    private final JLabel appearanceLabel = new JLabel(), boardLabel = new JLabel(), stoneLabel = new JLabel();
    private final JComboBox<BoardTheme> boardTheme = new JComboBox<>(BoardTheme.values());
    private final JComboBox<PieceStyle> pieceStyle = new JComboBox<>(PieceStyle.values());
    private final JButton sound=new ThemedButton("",ThemedButton.Style.SECONDARY),reset=new ThemedButton("",ThemedButton.Style.SECONDARY),close=new ThemedButton("",ThemedButton.Style.PRIMARY);
    private boolean refreshing;
    public SettingsDialog(MainFrame owner) {
        super(owner);
        this.owner = owner;
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JPanel languageRow = new JPanel(new BorderLayout(20, 0));
        languageRow.add(languageLabel, BorderLayout.WEST);
        languageRow.add(language);
        languageLabel.setLabelFor(language);

        JPanel appearance = new JPanel(new BorderLayout(0, 8));
        appearance.add(appearanceLabel, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout());
        addSelectorRow(fields, 0, boardLabel, boardTheme);
        addSelectorRow(fields, 1, stoneLabel, pieceStyle);
        appearance.add(fields);

        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.gridy = 0;
        row.weightx = 1;
        row.fill = GridBagConstraints.HORIZONTAL;
        for (Component item : new Component[]{languageRow, appearance, sound, reset, close}) {
            row.insets = new Insets(row.gridy == 0 ? 0 : 14, 0, 0, 0);
            content.add(item, row);
            row.gridy++;
        }
        setContentPane(content);
        useLocalizedRenderer(language, Lang::langName);
        useLocalizedRenderer(boardTheme, Lang::boardThemeName);
        useLocalizedRenderer(pieceStyle, Lang::pieceStyleName);
        language.addActionListener(e -> { if(!refreshing) owner.selectLanguage((Lang.Language)language.getSelectedItem()); });
        boardTheme.addActionListener(e -> { if(!refreshing) owner.selectBoardTheme((BoardTheme)boardTheme.getSelectedItem()); });
        pieceStyle.addActionListener(e -> { if(!refreshing) owner.selectPieceStyle((PieceStyle)pieceStyle.getSelectedItem()); });
        sound.setIcon(new SpeakerIcon(20)); sound.addActionListener(e -> { owner.toggleSound(); refreshLanguage(); });
        reset.addActionListener(e -> { if(confirmReset()) owner.resetSessionScores(); });
        close.addActionListener(e -> dispose()); finishLayout(close);
    }

    private static void addSelectorRow(JPanel panel, int y, JLabel label, JComboBox<?> selector) {
        GridBagConstraints cell = new GridBagConstraints();
        cell.gridx = 0;
        cell.gridy = y;
        cell.anchor = GridBagConstraints.LINE_START;
        cell.insets = new Insets(y == 0 ? 0 : 8, 0, 0, 20);
        panel.add(label, cell);
        label.setLabelFor(selector);
        cell.gridx = 1;
        cell.weightx = 1;
        cell.fill = GridBagConstraints.HORIZONTAL;
        cell.insets.right = 0;
        panel.add(selector, cell);
    }

    private static <T> void useLocalizedRenderer(JComboBox<T> selector, Function<T, String> names) {
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        selector.setRenderer((list, value, index, selected, focus) -> {
            JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, selected, focus);
            if (value != null) label.setText(names.apply(value));
            label.setBackground(selected ? Theme.BRASS : Theme.DARK);
            label.setForeground(selected ? Theme.DARK : Theme.CREAM);
            return label;
        });
    }

    private static <T> void sizeSelector(JComboBox<T> selector, Function<T, String> names) {
        FontMetrics metrics = selector.getFontMetrics(selector.getFont());
        int width = 0;
        for (int i = 0; i < selector.getItemCount(); i++) {
            width = Math.max(width, metrics.stringWidth(names.apply(selector.getItemAt(i))));
        }
        selector.setPreferredSize(new Dimension(width + 48, 36));
    }

    protected void refreshLanguage() {
        setTitle(Lang.settings());
        languageLabel.setText(Lang.languageLabel());
        appearanceLabel.setText(Lang.appearance());
        appearanceLabel.setFont(Theme.font(Font.BOLD, 14));
        boardLabel.setText(Lang.boardThemeLabel());
        stoneLabel.setText(Lang.pieceStyleLabel());
        refreshing = true;
        try {
            language.setSelectedItem(Lang.get());
            boardTheme.setSelectedItem(owner.getBoardPanel().getBoardTheme());
            pieceStyle.setSelectedItem(owner.getBoardPanel().getPieceStyle());
        } finally {
            refreshing = false;
        }
        sizeSelector(language, Lang::langName);
        sizeSelector(boardTheme, Lang::boardThemeName);
        sizeSelector(pieceStyle, Lang::pieceStyleName);
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
    public JComboBox<BoardTheme> getBoardThemeCombo() { return boardTheme; }
    public JComboBox<PieceStyle> getPieceStyleCombo() { return pieceStyle; }
    public JButton getResetButton() { return reset; } public JButton getCloseButton() { return close; }
}
