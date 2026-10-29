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
    public static final Color[] AVATARS = {BRASS, new Color(0xA66E4D), new Color(0x907554),
            new Color(0xC78556), new Color(0x76634E), new Color(0xB9986B),
            new Color(0x9D5443), new Color(0xD1B681)};
    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }
    public static void styleTree(Component component) {
        component.setFont(font(Font.PLAIN, 14));
        component.setForeground(CREAM);
        component.setBackground(DARK);
        if (component instanceof JTextField field) field.setCaretColor(BRASS);
        if (component instanceof java.awt.Container container)
            for (Component child : container.getComponents()) styleTree(child);
    }
}
