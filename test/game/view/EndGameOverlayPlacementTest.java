package game.view;

import game.model.Board;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EndGameOverlayPlacementTest {
    @ParameterizedTest @CsvSource({"0,1", "1,0", "1,1", "1,-1"})
    void everyPossibleFiveAndOverlineFitsTheFarHalfWithoutCoveringAnyWinningCell(int dr, int dc) {
        for (int length = 5; length <= 15; length++) for (int r = 0; r < 15; r++) for (int c = 0; c < 15; c++) {
            int lastRow = r + (length - 1) * dr, lastCol = c + (length - 1) * dc;
            if (lastRow >= 15 || lastCol < 0 || lastCol >= 15) continue;
            List<Board.Cell> line = new ArrayList<>();
            for (int i = 0; i < length; i++) line.add(new Board.Cell(r + i * dr, c + i * dc));
            Rectangle band = EndGameOverlay.boundsFor(line, new Dimension(596, 596));
            assertTrue(new Rectangle(0, 0, 596, 596).contains(band));
            boolean bottom = (r + lastRow) / 2.0 <= 7;
            assertTrue(bottom ? band.y >= 298 : band.y + band.height <= 298, line.toString());
            for (Board.Cell cell : line) {
                int x = 32 + cell.col() * 38, y = 32 + cell.row() * 38;
                assertFalse(band.intersects(new Rectangle(x - 22, y - 22, 44, 44)), line.toString());
            }
        }
    }

    @Test void drawUsesTheBottomHorizontalBand() {
        assertEquals(new Rectangle(24, 426, 548, 146), EndGameOverlay.boundsFor(List.of(), new Dimension(596, 596)));
    }

    @Test void unicodeNamesAndStatusUseThreeDotsWithoutSplittingASurrogate() {
        var image = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        try {
            var metrics = g.getFontMetrics(Theme.font(Font.BOLD, 23));
            String clipped = TextFit.ellipsis("Ánh 😀😀😀😀😀😀😀😀😀😀", metrics, 130);
            assertTrue(clipped.endsWith("...")); assertTrue(metrics.stringWidth(clipped) <= 130);
            assertFalse(clipped.contains("\uFFFD"));
            for (int i = 0; i < clipped.length(); i++) if (Character.isHighSurrogate(clipped.charAt(i)))
                assertTrue(i + 1 < clipped.length() && Character.isLowSurrogate(clipped.charAt(++i)));
            String status = TextFit.lines("Nguyễn Hoàng WWWWWWWWWWWWWWWW CHIẾN THẮNG!", metrics, 130, 3);
            assertTrue(status.endsWith("...")); assertTrue(status.lines().count() <= 3);
            status.lines().forEach(line -> assertTrue(metrics.stringWidth(line) <= 130));
        } finally { g.dispose(); }
    }
}
