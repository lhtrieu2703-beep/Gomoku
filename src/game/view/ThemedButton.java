package game.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Fully painted so Windows and Metal use the same button states. */
public class ThemedButton extends JButton {
    public enum Style { PRIMARY, SECONDARY }
    private final Style style;
    public ThemedButton(String text, Style style) {
        super(text); this.style = style; putClientProperty("html.disable", Boolean.TRUE);
        setFont(Theme.font(Font.BOLD, 14));
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
        setRolloverEnabled(true); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "activate");
        getActionMap().put("activate", new AbstractAction() {
            public void actionPerformed(ActionEvent event) { doClick(); }
        });
    }
    public Style getStyle() { return style; }
    public Color fillColor() {
        if (!isEnabled()) return Theme.WOOD_DARK;
        Color base = style == Style.PRIMARY ? Theme.BRASS : Theme.DARK;
        if (getModel().isPressed() && getModel().isArmed()) return base.darker();
        return getModel().isRollover() ? base.brighter() : base;
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fillColor()); g.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, 16, 16);
        g.setColor(isEnabled() ? Theme.BRASS : Theme.MUTED);
        g.drawRoundRect(3, 3, getWidth()-7, getHeight()-7, 16, 16);
        if (hasFocus()) { g.setStroke(new BasicStroke(2)); g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,18,18); }
        Color ink = !isEnabled() ? Theme.MUTED : style == Style.PRIMARY ? Theme.DARK : Theme.CREAM;
        FontMetrics fm = g.getFontMetrics(getFont()); g.setFont(getFont());
        String text = getText() == null ? "" : getText(); Icon icon = getIcon();
        int total = fm.stringWidth(text) + (icon == null ? 0 : icon.getIconWidth() + (text.isEmpty() ? 0 : getIconTextGap()));
        int x = (getWidth()-total)/2;
        if (icon != null) { icon.paintIcon(this,g,x,(getHeight()-icon.getIconHeight())/2); x += icon.getIconWidth()+getIconTextGap(); }
        g.setColor(ink); g.drawString(text,x,(getHeight()-fm.getHeight())/2+fm.getAscent()); g.dispose();
    }
}
