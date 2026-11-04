package game.view;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
/** Draft editor, shared by profile and player-two setup. */
public final class ProfileEditor extends JPanel {
    private final JTextField name=new JTextField(22) {
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); if(getText().isEmpty()&&!isFocusOwner()) {
                graphics.setColor(Theme.MUTED); graphics.setFont(getFont());
                graphics.drawString(Lang.namePlaceholder(),8,(getHeight()+getFontMetrics(getFont()).getAscent())/2-3);
            }
        }
    };
    private final JLabel count=new JLabel(); private final JButton clear=MainMenu.iconButton(UiIcon.Kind.CLEAR);
    private final JButton[] avatars=new JButton[8]; private int selected;
    private final JPanel nameSection=new JPanel(new BorderLayout(0,4));
    private final JLabel nameCaption=new JLabel();
    private int blockedAvatar=-1;
    private Runnable changeListener=() -> {};
    public ProfileEditor(PlayerProfile player) {
        super(new BorderLayout(0,12)); setOpaque(false); selected=player.avatarIndex();
        JPanel grid=new JPanel(new GridLayout(2,4,10,10)); grid.setOpaque(false);
        for(int i=0;i<8;i++) { final int index=i; avatars[i]=new ThemedButton("",ThemedButton.Style.SECONDARY); avatars[i].setPreferredSize(new Dimension(76,76));
            avatars[i].addActionListener(e -> { if(index==blockedAvatar) return; selected=index; refreshAvatars(); changeListener.run(); }); grid.add(avatars[i]); }
        JPanel field=new JPanel(new BorderLayout()); field.setOpaque(false); name.setFont(Theme.font(Font.PLAIN,16)); name.setMargin(new Insets(8,8,8,4));
        ((AbstractDocument)name.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb,int offset,String text,AttributeSet attrs) throws BadLocationException { replace(fb,offset,0,text,attrs); }
            public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet attrs) throws BadLocationException {
                String old=fb.getDocument().getText(0,fb.getDocument().getLength()); String candidate=old.substring(0,offset)+(text==null?"":text)+old.substring(offset+length);
                if(candidate.codePointCount(0,candidate.length())>16) candidate=AppSettings.normalizePlayerName(candidate); fb.replace(0,old.length(),candidate,attrs);
            }
        });
        name.setText(player.name()); name.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { name.selectAll(); } });
        name.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); } public void removeUpdate(DocumentEvent e) { update(); } public void changedUpdate(DocumentEvent e) { update(); }
            private void update() { count.setText(Lang.characterCount(name.getText().codePointCount(0,name.getText().length()))); refreshAvatars(); changeListener.run(); }
        });
        clear.addActionListener(e -> { name.setText(""); name.requestFocusInWindow(); });
        name.setBorder(BorderFactory.createEmptyBorder(4,4,4,0)); field.setBorder(BorderFactory.createLineBorder(Theme.MUTED));
        field.add(name); field.add(clear,BorderLayout.EAST);
        nameSection.setOpaque(false); nameSection.add(field); nameSection.add(count,BorderLayout.SOUTH);
        add(grid); add(nameSection,BorderLayout.SOUTH); refreshLanguage();
    }
    public void refreshLanguage() {
        count.setText(Lang.characterCount(name.getText().codePointCount(0,name.getText().length()))); name.setToolTipText(Lang.namePlaceholder());
        name.getAccessibleContext().setAccessibleName(Lang.playerNameLabel()); clear.setToolTipText(Lang.clearName()); refreshAvatars(); name.repaint();
    }
    private void refreshAvatars() {
        String display=new PlayerProfile(name.getText(),selected).displayName();
        for(int i=0;i<8;i++) { Icon icon=new AvatarIcon(i,display,52); boolean locked=TwoPlayerSetupValidation.isAvatarLocked(i,blockedAvatar);
            avatars[i].setEnabled(!locked); avatars[i].setIcon(locked?dimmed(icon):icon);
            avatars[i].setBorder(BorderFactory.createLineBorder(i==selected?Theme.BRASS:Theme.DARK,i==selected?3:1)); avatars[i].setBorderPainted(true);
            avatars[i].getAccessibleContext().setAccessibleName(Lang.avatarLabel(i+1)); }
    }
    private static Icon dimmed(Icon icon) {
        return new Icon() {
            public int getIconWidth() { return icon.getIconWidth(); }
            public int getIconHeight() { return icon.getIconHeight(); }
            public void paintIcon(Component c,Graphics graphics,int x,int y) {
                Graphics2D g=(Graphics2D)graphics.create();
                try { g.setComposite(AlphaComposite.SrcOver.derive(0.28f)); icon.paintIcon(c,g,x,y); }
                finally { g.dispose(); }
            }
        };
    }
    public void setBlockedAvatar(int index) { blockedAvatar=index; refreshAvatars(); }
    public void setChangeListener(Runnable listener) { changeListener=listener; }
    public void setNameCaption(String text) {
        nameCaption.setText(text); nameCaption.setLabelFor(name); nameCaption.setForeground(Theme.CREAM);
        nameCaption.setFont(Theme.font(Font.PLAIN,14)); nameSection.add(nameCaption,BorderLayout.NORTH);
    }
    public PlayerProfile value() { return new PlayerProfile(name.getText(),selected); } public JTextField getNameField() { return name; }
    public JButton getAvatarButton(int index) { return avatars[index]; } public JLabel getCountLabel() { return count; } public JButton getClearButton() { return clear; }
}
