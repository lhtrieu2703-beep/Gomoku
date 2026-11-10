package game.view;

import game.model.Piece;

import java.awt.Color;

/** Visual finishes for X and O. Their game identities do not depend on the palette. */
public enum PieceStyle {
    CLASSIC(new Palette(Theme.BLACK_HIGHLIGHT, Theme.BLACK_STONE, Theme.BLACK_STONE),
            new Palette(Theme.WHITE_GLEAM, Theme.WHITE_STONE, Theme.WHITE_EDGE)),
    GLASS(new Palette(new Color(0x8CA7B7), new Color(0x273139), new Color(0x12191F)),
            new Palette(new Color(0xFFFFFF), new Color(0xDDEFF7), new Color(0x68879D))),
    METALLIC(new Palette(new Color(0xFFE5A0), new Color(0xCF942B), new Color(0x744819)),
            new Palette(new Color(0xFAFCFF), new Color(0xB4C0CF), new Color(0x526174)));

    private final Palette x, o;

    PieceStyle(Palette x, Palette o) {
        this.x = x;
        this.o = o;
    }

    Palette palette(Piece piece) { return piece == Piece.X ? x : o; }

    record Palette(Color light, Color face, Color edge) {}
}
