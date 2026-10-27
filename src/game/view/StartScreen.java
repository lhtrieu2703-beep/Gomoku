package game.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.util.function.Consumer;

/** The menu card. All persistence and navigation are owned by MainFrame. */
public final class StartScreen extends JPanel {
    private final JLabel title = new JLabel();
    private final JLabel subtitle = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel hint = new JLabel();
    private final JLabel languageLabel = new JLabel();
    private final JTextField nameField = new JTextField(20);
    private final JButton startButton = new JButton();
    private final JComboBox<Lang.Language> languageCombo = new JComboBox<>(Lang.Language.values());
    private boolean updatingLanguage;

    public StartScreen(String savedName, Runnable onStart, Consumer<Lang.Language> onLanguage) {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(760, 700));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 241, 232));
        form.setBorder(new EmptyBorder(30, 38, 32, 38));
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(27, 64, 50));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(66, 75, 70));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(38, 47, 42));
        nameLabel.setLabelFor(nameField);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(83, 90, 85));
        languageLabel.setLabelFor(languageCombo);
        languageLabel.setForeground(new Color(38, 47, 42));
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        nameField.setMargin(new Insets(9, 10, 9, 10));
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException { replace(fb, offset, 0, text, attr); }
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String old = fb.getDocument().getText(0, fb.getDocument().getLength());
                String replacement = text == null ? "" : text;
                String candidate = old.substring(0, offset) + replacement + old.substring(offset + length);
                if (candidate.codePointCount(0, candidate.length()) > AppSettings.MAX_PLAYER_NAME_LENGTH)
                    fb.replace(0, old.length(), AppSettings.normalizePlayerName(candidate), attrs);
                else fb.replace(offset, length, replacement, attrs);
            }
        });
        nameField.setText(savedName);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(36, 115, 72));
        startButton.setOpaque(true);
        startButton.setBorderPainted(false);
        startButton.setBorder(new EmptyBorder(11, 24, 11, 24));
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        languageCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        languageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Lang.Language language) label.setText(Lang.langName(language));
                return label;
            }
        });
        addRow(form, title, 0, 0, 0);
        addRow(form, subtitle, 1, 6, 25);
        addRow(form, nameLabel, 2, 0, 6);
        addRow(form, nameField, 3, 0, 5);
        addRow(form, hint, 4, 0, 20);
        JPanel languageRow = new JPanel(new BorderLayout(16, 0));
        languageRow.setOpaque(false);
        languageRow.add(languageLabel, BorderLayout.WEST);
        languageRow.add(languageCombo, BorderLayout.CENTER);
        addRow(form, languageRow, 5, 0, 24);
        addRow(form, startButton, 6, 0, 0);
        add(form);
        updateLanguage();
        startButton.addActionListener(e -> onStart.run());
        nameField.addActionListener(e -> onStart.run());
        languageCombo.addActionListener(e -> {
            if (!updatingLanguage && languageCombo.getSelectedItem() instanceof Lang.Language language)
                onLanguage.accept(language);
        });
    }

    private static void addRow(JPanel form, Component component, int row, int top, int bottom) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0; constraints.gridy = row; constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(top, 0, bottom, 0);
        form.add(component, constraints);
    }

    public void updateLanguage() {
        title.setText(Lang.startTitle());
        subtitle.setText(Lang.startSubtitle());
        nameLabel.setText(Lang.playerNameLabel());
        hint.setText(Lang.playerNameHint());
        languageLabel.setText(Lang.languageLabel());
        startButton.setText(Lang.startGame());
        nameField.setToolTipText(Lang.playerNameHint());
        nameField.getAccessibleContext().setAccessibleName(Lang.playerNameLabel());
        updatingLanguage = true;
        try { languageCombo.setSelectedItem(Lang.get()); }
        finally { updatingLanguage = false; }
        languageCombo.setToolTipText(Lang.langComboTooltip());
        revalidate(); repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(22, 40, 42), getWidth(), getHeight(), new Color(43, 78, 59)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cell = 40, originX = getWidth() - 9 * cell, originY = getHeight() - 8 * cell;
            g.setColor(new Color(223, 190, 118, 36));
            for (int i = 0; i < 10; i++) {
                g.drawLine(originX + i * cell, originY, originX + i * cell, getHeight());
                g.drawLine(originX, originY + i * cell, getWidth(), originY + i * cell);
            }
            for (int i = 0; i < 5; i++) {
                g.setColor(i % 2 == 0 ? new Color(10, 19, 19, 120) : new Color(242, 231, 203, 90));
                g.fillOval(originX + i * cell - 13, getHeight() - 52, 26, 26);
            }
        } finally { g.dispose(); }
    }

    public JTextField getNameField() { return nameField; }
    public JButton getStartButton() { return startButton; }
    public JComboBox<Lang.Language> getLanguageCombo() { return languageCombo; }
}
