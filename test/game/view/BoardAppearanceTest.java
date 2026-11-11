package game.view;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BoardAppearanceTest {
    private static final double SCALE = 1.25;
    private static final int RED_HIGHLIGHT = 0xE53935;

    @Test
    void aNewBoardUsesWarmWoodAndClassicStones() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BoardPanel panel = new BoardPanel(new Board());
            assertEquals(BoardTheme.WARM_WOOD, panel.getBoardTheme());
            assertEquals(PieceStyle.CLASSIC, panel.getPieceStyle());
            assertEquals(Theme.BOARD, panel.getBackground());
        });
    }

    @ParameterizedTest
    @EnumSource(Lang.Language.class)
    void everyAppearanceOptionHasItsLocalizedLabel(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Lang.Language previous = Lang.get();
            try {
                Lang.set(language);
                List<String> expectedBoards = switch (language) {
                    case VI -> List.of("Gỗ ấm", "Đá xám than", "Xanh ngọc đậm");
                    case EN -> List.of("Warm wood", "Charcoal stone", "Dark jade");
                    case PT -> List.of("Madeira quente", "Pedra cinza", "Jade escuro");
                };
                List<String> expectedStones = switch (language) {
                    case VI -> List.of("Cổ điển đen/trắng", "Bóng kính", "Vàng/Bạc");
                    case EN -> List.of("Classic black/white", "Glossy glass", "Gold/Silver");
                    case PT -> List.of("Clássico preto/branco", "Vidro brilhante", "Ouro/Prata");
                };
                assertEquals(expectedBoards, Arrays.stream(BoardTheme.values()).map(Lang::boardThemeName).toList());
                assertEquals(expectedStones, Arrays.stream(PieceStyle.values()).map(Lang::pieceStyleName).toList());
            } finally {
                Lang.set(previous);
            }
        });
    }

    static Stream<Arguments> appearances() {
        return Arrays.stream(BoardTheme.values()).flatMap(theme ->
                Arrays.stream(PieceStyle.values()).map(style -> Arguments.of(theme, style)));
    }

    @ParameterizedTest
    @MethodSource("appearances")
    void paintsDistinctStonesGridStarsAndWinningRingsAt125Percent(BoardTheme theme, PieceStyle style) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            boolean sound = SoundManager.isSoundEnabled();
            SoundManager.setSoundEnabled(false);
            try {
                BoardPanel panel = new BoardPanel(new Board());
                panel.setGameMode(BoardPanel.GameMode.PVP);
                panel.setBoardTheme(theme);
                panel.setPieceStyle(style);
                for (int i = 0; i < 4; i++) {
                    assertTrue(panel.playMove(7, 5 + i));
                    assertTrue(panel.playMove(10, 2 + 2 * i));
                }
                assertTrue(panel.playMove(7, 9));
                assertEquals(Piece.X, panel.getWinner());
                BufferedImage image = render(panel);

                int x = position(7), y = position(7), otherX = position(2), otherY = position(10);
                Color black = sample(image, x, y);
                Color white = sample(image, otherX, otherY);
                assertTrue(distance(black, white) > 85, "X and O centers must be distinguishable");
                assertTrue(distance(black, sample(image, x + 22, y + 12)) > 30, "X must stand out on the board");
                assertTrue(distance(white, sample(image, otherX + 22, otherY + 10)) > 30, "O must stand out on the board");
                assertTrue(hasColorNear(image, x, y - 16, new Color(RED_HIGHLIGHT), 0), "Missing red winning ring");
                assertFalse(hasColorNear(image, otherX, otherY - 16, new Color(RED_HIGHLIGHT), 0));
                Color surface = sample(image, x + 24, y - 18);
                assertTrue(sample(image, x + 4, y - 18).getBlue() > surface.getBlue() + 18,
                        "Translucent white edge must lighten the board beyond the red ring");
                assertTrue(hasColorNear(image, position(4), position(4) + 19, theme.grid(), 20), "Missing grid");
                assertEquals(theme.star(), sample(image, position(3), position(3)));
            } finally {
                SoundManager.setSoundEnabled(sound);
            }
        });
    }

    private static BufferedImage render(BoardPanel panel) {
        panel.setSize(panel.getPreferredSize());
        BufferedImage image = new BufferedImage(
                (int) Math.ceil(panel.getWidth() * SCALE), (int) Math.ceil(panel.getHeight() * SCALE),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(SCALE, SCALE);
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int position(int index) {
        return BoardPanel.OFFSET + index * BoardPanel.CELL_SIZE;
    }

    private static Color sample(BufferedImage image, int x, int y) {
        return new Color(image.getRGB((int) Math.round(x * SCALE), (int) Math.round(y * SCALE)));
    }

    private static boolean hasColorNear(BufferedImage image, int x, int y, Color color, double tolerance) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (distance(sample(image, x + dx, y + dy), color) <= tolerance) return true;
            }
        }
        return false;
    }

    private static double distance(Color first, Color second) {
        return Math.hypot(Math.hypot(first.getRed() - second.getRed(), first.getGreen() - second.getGreen()),
                first.getBlue() - second.getBlue());
    }
}
