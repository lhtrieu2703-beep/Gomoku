package game.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

/** Palettes for the playing surface and the area around it. */
public enum BoardTheme {
    WARM_WOOD(Theme.BOARD, Theme.BOARD_FACE, Theme.BOARD_FACE, Theme.BOARD_EDGE, Theme.GRID, Theme.STAR),
    CHARCOAL_STONE(new Color(0x353E47), new Color(0x6B7783), new Color(0x505D6A),
            new Color(0x313B45), new Color(0xBCCAD2), new Color(0xF0F4F7)),
    DARK_JADE(new Color(0x1B443B), new Color(0x3D7465), new Color(0x2C5C50),
            new Color(0x173F36), new Color(0xACD2C1), new Color(0xE1F2E9));

    private final Color surround, face, shade, edge, grid, star;

    BoardTheme(Color surround, Color face, Color shade, Color edge, Color grid, Color star) {
        this.surround = surround;
        this.face = face;
        this.shade = shade;
        this.edge = edge;
        this.grid = grid;
        this.star = star;
    }

    Color surround() { return surround; }
    Color edge() { return edge; }
    Color grid() { return grid; }
    Color star() { return star; }

    Paint surface(int top, int height) {
        return face.equals(shade) ? face : new GradientPaint(0, top, face, 0, top + height, shade);
    }
}
