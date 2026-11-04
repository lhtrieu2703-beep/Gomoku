package game.view;

import javax.swing.*;
import java.awt.*;

/** Shared palette and typography. Board stones retain their game colours. */
public final class Theme {
    private Theme() {}
    public static final Color BOARD = new Color(0xC89B4B);
    public static final Color DARK = new Color(0x3B2A1E);
    public static final Color CREAM = new Color(0xF5EFE3);
    public static final Color BRASS = new Color(0xD9A441);
    public static final Color WOOD_LIGHT = new Color(0x8B5E34), WOOD_DARK = new Color(0x5A3A22);
    public static final Color MUTED = new Color(0xAD9B82), ERROR = new Color(0xE38D80);
    public static final Color SUCCESS = new Color(0xD5D68B);
    public static final Color BOARD_FACE = new Color(220,170,90), BOARD_EDGE = new Color(195,145,65);
    public static final Color GRID = new Color(140,90,35), STAR = new Color(90,50,10);
    public static final Color BLACK_STONE = new Color(28,28,28), BLACK_HIGHLIGHT = new Color(80,80,80);
    public static final Color WHITE_STONE = new Color(250,250,248), WHITE_EDGE = new Color(160,155,145);
    public static final Color WHITE_GLEAM = new Color(230,230,230,180), SHADOW = new Color(0,0,0,40);
    public static final Color MOVE_MARKER = new Color(230,60,60), MUTE_MARK = new Color(220,50,50);
    public static final Color[] AVATARS = {BRASS, new Color(0xA66E4D), new Color(0x907554),
            new Color(0xC78556), new Color(0x76634E), new Color(0xB9986B),
            new Color(0x9D5443), new Color(0xD1B681)};
    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }
    public static void styleTree(Component component) {
        component.setFont(font(Font.PLAIN, 14));
        component.setForeground(CREAM);
        component.setBackground(DARK);
        if (component instanceof JTextField field) field.setCaretColor(BRASS);
        if (component instanceof JComboBox<?> combo) styleCombo(combo);
        if (component instanceof java.awt.Container container)
            for (Component child : container.getComponents()) styleTree(child);
    }
    private static void styleCombo(JComboBox<?> combo) {
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            protected JButton createArrowButton() {
                return new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH,DARK,DARK,BRASS,DARK);
            }
            public void paintCurrentValueBackground(Graphics g,Rectangle bounds,boolean focused) {
                g.setColor(DARK); g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
            }
        });
        combo.setBorder(BorderFactory.createLineBorder(BRASS));
    }
}
