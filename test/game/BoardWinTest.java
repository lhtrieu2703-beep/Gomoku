package game;

import game.model.Board;
import game.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử JUnit 5 cho luật Gomoku RULE B (từ 5 quân trở lên, mép không tính là chặn).
 * Gồm các ca kiểm thử sanity cho hàm dựng bàn cờ setupPattern và các ca T01 đến T52.
 * Các ca T53 đến T57 thuộc tầng GameController / Lifecycle (khóa sau khi thắng, luân phiên lượt,
 * đầu hàng, hết giờ) không tồn tại trong model Board của dự án nên được xác định là KHÔNG ÁP DỤNG.
 */
public class BoardWinTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.setWinRule(Board.WinRule.RULE_B); // Thiết lập RULE B: >= 5 quân, không bị chặn 2 đầu
    }

    // =========================================================================
    // HÀM TIỆN ÍCH DỰNG BÀN CỜ TỪ CHUỖI
    // =========================================================================

    public enum Direction {
        HORIZONTAL, // Ngang (dr=0, dc=1)
        VERTICAL,   // Dọc (dr=1, dc=0)
        DIAG_MAIN,  // Chéo chính ↘ (dr=1, dc=1)
        DIAG_ANTI   // Chéo phụ ↙ (dr=1, dc=-1)
    }

    public static class PlacedMove {
        public final int row;
        public final int col;
        public final Piece piece;

        public PlacedMove(int row, int col, Piece piece) {
            this.row = row;
            this.col = col;
            this.piece = piece;
        }
    }

    /**
     * Dựng bàn cờ từ chuỗi quy ước.
     * Ký hiệu chuỗi:
     * - 'X': quân X
     * - 'O': quân O
     * - '.': ô trống trên bàn cờ
     * - '#': mép bàn cờ (ngoài biên). Đầu chuỗi có '#' -> đặt sát biên đầu. Cuối chuỗi có '#' -> kết thúc sát biên cuối.
     * - '*' đi kèm quân (ví dụ "X*" hoặc "O*"): đánh dấu quân vừa đặt.
     *
     * @param pattern Chuỗi cách nhau bởi dấu cách, ví dụ: "O X X X X* X ." hoặc "# X X X X X* ."
     * @param dir Hướng đặt chuỗi (mặc định HORIZONTAL)
     * @param invertColors nếu true: đảo ngược X thành O, O thành X
     * @return Tọa độ quân vừa đặt (row, col, piece)
     */
    public PlacedMove setupPattern(String pattern, Direction dir, boolean invertColors) {
        String[] tokens = pattern.trim().split("\\s+");

        boolean startsWithEdge = tokens[0].equals("#");
        boolean endsWithEdge = tokens[tokens.length - 1].equals("#");

        int tokenStart = startsWithEdge ? 1 : 0;
        int tokenEnd = endsWithEdge ? tokens.length - 2 : tokens.length - 1;
        int numBoardCells = tokenEnd - tokenStart + 1;

        int dr = 0, dc = 0;
        switch (dir) {
            case HORIZONTAL -> { dr = 0; dc = 1; }
            case VERTICAL   -> { dr = 1; dc = 0; }
            case DIAG_MAIN  -> { dr = 1; dc = 1; }
            case DIAG_ANTI  -> { dr = 1; dc = -1; }
        }

        int startR, startC;

        if (dir == Direction.HORIZONTAL) {
            startR = 7; // Giữa bàn
            if (startsWithEdge) {
                startC = 0; // Sát mép trái
            } else if (endsWithEdge) {
                startC = Board.SIZE - numBoardCells; // Sát mép phải
            } else {
                startC = (Board.SIZE - numBoardCells) / 2; // Canh giữa
            }
        } else if (dir == Direction.VERTICAL) {
            startC = 7;
            if (startsWithEdge) {
                startR = 0; // Sát mép trên
            } else if (endsWithEdge) {
                startR = Board.SIZE - numBoardCells; // Sát mép dưới
            } else {
                startR = (Board.SIZE - numBoardCells) / 2;
            }
        } else if (dir == Direction.DIAG_MAIN) {
            if (startsWithEdge && endsWithEdge) {
                // Đường chéo ngắn ở góc, ví dụ 5 ô: (0, 10) đến (4, 14)
                startR = 0;
                startC = Board.SIZE - numBoardCells;
            } else if (startsWithEdge) {
                startR = 0;
                startC = 2;
            } else if (endsWithEdge) {
                startR = Board.SIZE - numBoardCells;
                startC = Board.SIZE - numBoardCells;
            } else {
                startR = (Board.SIZE - numBoardCells) / 2;
                startC = (Board.SIZE - numBoardCells) / 2;
            }
        } else {
            if (startsWithEdge) {
                startR = 0;
                startC = Board.SIZE - 1;
            } else {
                startR = (Board.SIZE - numBoardCells) / 2;
                startC = (Board.SIZE + numBoardCells) / 2;
            }
        }

        int starR = -1, starC = -1;
        Piece starPiece = Piece.NONE;

        // Đặt các quân nền lên bàn cờ, ghi nhận tọa độ quân *
        for (int i = 0; i < numBoardCells; i++) {
            String token = tokens[tokenStart + i];
            int r = startR + i * dr;
            int c = startC + i * dc;

            if (token.contains("*")) {
                starR = r;
                starC = c;
                if (token.startsWith("X")) {
                    starPiece = invertColors ? Piece.O : Piece.X;
                } else if (token.startsWith("O")) {
                    starPiece = invertColors ? Piece.X : Piece.O;
                }
            } else if (token.equals("X")) {
                board.makeMove(r, c, invertColors ? Piece.O : Piece.X);
            } else if (token.equals("O")) {
                board.makeMove(r, c, invertColors ? Piece.X : Piece.O);
            }
        }

        // Quân vừa đặt (*) luôn được đặt sau cùng
        if (starR != -1 && starPiece != Piece.NONE) {
            board.makeMove(starR, starC, starPiece);
        }

        return new PlacedMove(starR, starC, starPiece);
    }

    public PlacedMove setupPattern(String pattern) {
        return setupPattern(pattern, Direction.HORIZONTAL, false);
    }

    // =========================================================================
    // TEST KIỂM TRA CHÍNH HÀM DỰNG BÀN CỜ (SANITY TESTS)
    // =========================================================================

    @Test
    @DisplayName("Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô")
    public void testSanitySetupPatternNormal() {
        PlacedMove m = setupPattern("O X X X X* X O");
        // startC = (15 - 7) / 2 = 4, startR = 7
        assertEquals(7, m.row);
        assertEquals(8, m.col);
        assertEquals(Piece.X, m.piece);

        // Đầu trái (c=4) phải là O
        assertEquals(Piece.O, board.getPiece(7, 4), "Đầu trái phải là quân O");
        // Các quân giữa (c=5..9) phải là X
        for (int c = 5; c <= 9; c++) {
            assertEquals(Piece.X, board.getPiece(7, c), "Ô giữa tại cột " + c + " phải là quân X");
        }
        // Đầu phải (c=10) phải là O
        assertEquals(Piece.O, board.getPiece(7, 10), "Đầu phải phải là quân O");
    }

    @Test
    @DisplayName("Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X")
    public void testSanitySetupPatternAllO() {
        PlacedMove m = setupPattern(". O O O O O* .");
        // startC = (15 - 7) / 2 = 4, startR = 7
        assertEquals(Piece.O, m.piece);
        // c=4 là '.', c=5..9 là O, c=10 là '.'
        assertEquals(Piece.NONE, board.getPiece(7, 4), "Ô đầu phải trống");
        for (int c = 5; c <= 9; c++) {
            assertEquals(Piece.O, board.getPiece(7, c), "Ô tại cột " + c + " phải là quân O");
        }
        assertEquals(Piece.NONE, board.getPiece(7, 10), "Ô cuối phải trống");

        // Đảm bảo không có quân X nào trên toàn bàn cờ
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                assertNotEquals(Piece.X, board.getPiece(r, c), "Không được có quân X nào trên bàn");
            }
        }
    }

    @Test
    @DisplayName("Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O")
    public void testSanitySetupPatternInverted() {
        // Mẫu "O X X X X* X O" với invertColors = true -> hai đầu O thành X, các quân giữa X thành O
        PlacedMove m = setupPattern("O X X X X* X O", Direction.HORIZONTAL, true);
        assertEquals(Piece.O, m.piece, "Quân vừa đặt X* sau khi đảo màu phải là O");

        // Đầu trái và phải vốn là O, sau đảo màu phải là X
        assertEquals(Piece.X, board.getPiece(7, 4), "Đầu trái O sau khi đảo màu phải là quân X");
        assertEquals(Piece.X, board.getPiece(7, 10), "Đầu phải O sau khi đảo màu phải là quân X");

        // Các quân giữa vốn là X, sau đảo màu phải là O
        for (int c = 5; c <= 9; c++) {
            assertEquals(Piece.O, board.getPiece(7, c), "Ô giữa tại cột " + c + " sau đảo màu phải là quân O");
        }
    }

    // =========================================================================
    // NHÓM 1: HÀNG 5 (T01 - T10)
    // =========================================================================

    @Test
    @DisplayName("T01: . X X X X X* . -> THẮNG")
    public void testT01() {
        PlacedMove m = setupPattern(". X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 mở 2 đầu phải THẮNG");
    }

    @Test
    @DisplayName("T02: O X X X X* X . -> THẮNG")
    public void testT02() {
        PlacedMove m = setupPattern("O X X X X* X .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 bị chặn 1 đầu trái bởi O phải THẮNG");
    }

    @Test
    @DisplayName("T03: . X X* X X X O -> THẮNG")
    public void testT03() {
        PlacedMove m = setupPattern(". X X* X X X O");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 bị chặn 1 đầu phải bởi O phải THẮNG");
    }

    @Test
    @DisplayName("T04: O X* X X X X O -> KHÔNG THẮNG")
    public void testT04() {
        PlacedMove m = setupPattern("O X* X X X X O");
        assertFalse(board.checkWin(m.row, m.col), "Hàng 5 bị O chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T05: # X X X X X* . -> THẮNG")
    public void testT05() {
        PlacedMove m = setupPattern("# X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 sát mép #, đầu kia mở phải THẮNG");
    }

    @Test
    @DisplayName("T06: # X X X X X* O -> THẮNG")
    public void testT06() {
        PlacedMove m = setupPattern("# X X X X X* O");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 sát mép #, 1 đầu bị O chặn phải THẮNG");
    }

    @Test
    @DisplayName("T07: O X X X X X* # -> THẮNG")
    public void testT07() {
        PlacedMove m = setupPattern("O X X X X X* #");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 5 bị O chặn 1 đầu, đầu kia sát mép # phải THẮNG");
    }

    @Test
    @DisplayName("T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG")
    public void testT08() {
        PlacedMove m = setupPattern("# X X X X X* #", Direction.DIAG_MAIN, false);
        assertTrue(board.checkWin(m.row, m.col), "Hàng chéo 5 ô chạm 2 mép # không bị chặn phải THẮNG");
    }

    @Test
    @DisplayName("T09: O . X X X X X* . O (O không liền kề) -> THẮNG")
    public void testT09() {
        PlacedMove m = setupPattern("O . X X X X X* . O");
        assertTrue(board.checkWin(m.row, m.col), "O cách đầu 1 ô trống, không tính là chặn phải THẮNG");
    }

    @Test
    @DisplayName("T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG")
    public void testT10() {
        PlacedMove m = setupPattern("O X X X X X* . O");
        assertTrue(board.checkWin(m.row, m.col), "Chỉ 1 đầu bị chặn liền kề, đầu kia là ô trống phải THẮNG");
    }

    // =========================================================================
    // NHÓM 2: HÀNG 6 (T11 - T16)
    // =========================================================================

    @Test
    @DisplayName("T11: . X X X X X X* . -> THẮNG")
    public void testT11() {
        PlacedMove m = setupPattern(". X X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 6 mở 2 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T12: O X X X X X X* . -> THẮNG")
    public void testT12() {
        PlacedMove m = setupPattern("O X X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 6 bị O chặn 1 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T13: O X X X X X X* O -> KHÔNG THẮNG")
    public void testT13() {
        PlacedMove m = setupPattern("O X X X X X X* O");
        assertFalse(board.checkWin(m.row, m.col), "Hàng 6 bị O chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T14: # X X X X X X* . -> THẮNG")
    public void testT14() {
        PlacedMove m = setupPattern("# X X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 6 sát mép # ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T15: # X X X X X X* O -> THẮNG")
    public void testT15() {
        PlacedMove m = setupPattern("# X X X X X X* O");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 6 sát mép # và 1 đầu O ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG")
    public void testT16() {
        PlacedMove m = setupPattern("# X X X X X X* #", Direction.DIAG_MAIN, false);
        assertTrue(board.checkWin(m.row, m.col), "Hàng chéo 6 ô chạm 2 mép # ở RULE B phải THẮNG");
    }

    // =========================================================================
    // NHÓM 3: HÀNG 7 TRỞ LÊN (T17 - T20)
    // =========================================================================

    @Test
    @DisplayName("T17: . X X X X X X X* . -> THẮNG")
    public void testT17() {
        PlacedMove m = setupPattern(". X X X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 7 mở 2 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T18: O X X X X X X X* O -> KHÔNG THẮNG")
    public void testT18() {
        PlacedMove m = setupPattern("O X X X X X X X* O");
        assertFalse(board.checkWin(m.row, m.col), "Hàng 7 bị O chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T19: Hàng 9 quân mở 2 đầu -> THẮNG")
    public void testT19() {
        PlacedMove m = setupPattern(". X X X X X X X X X* .");
        assertTrue(board.checkWin(m.row, m.col), "Hàng 9 quân mở 2 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG")
    public void testT20() {
        for (int c = 0; c < 15; c++) {
            if (c != 7) board.makeMove(7, c, Piece.X);
        }
        board.makeMove(7, 7, Piece.X);
        assertTrue(board.checkWin(7, 7), "Hàng 15 ô chạm 2 mép bàn cờ ở RULE B phải THẮNG");
    }

    // =========================================================================
    // NHÓM 4: NƯỚC ĐI NỐI HAI ĐOẠN (T21 - T24)
    // =========================================================================

    @Test
    @DisplayName("T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG")
    public void testT21() {
        board.makeMove(7, 2, Piece.X); board.makeMove(7, 3, Piece.X);
        board.makeMove(7, 5, Piece.X); board.makeMove(7, 6, Piece.X);
        board.makeMove(7, 4, Piece.X); // Nối
        assertTrue(board.checkWin(7, 4), "Nối 2+2 thành 5 mở 2 đầu phải THẮNG");
    }

    @Test
    @DisplayName("T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG")
    public void testT22() {
        for (int c = 2; c <= 4; c++) board.makeMove(7, c, Piece.X);
        for (int c = 6; c <= 8; c++) board.makeMove(7, c, Piece.X);
        board.makeMove(7, 5, Piece.X); // Nối
        assertTrue(board.checkWin(7, 5), "Nối 3+3 thành 7 mở 2 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG")
    public void testT23() {
        for (int c = 2; c <= 4; c++) board.makeMove(7, c, Piece.X);
        for (int c = 6; c <= 7; c++) board.makeMove(7, c, Piece.X);
        board.makeMove(7, 5, Piece.X); // Nối
        assertTrue(board.checkWin(7, 5), "Nối 3+2 thành 6 mở 2 đầu ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG")
    public void testT24() {
        board.makeMove(7, 1, Piece.O);
        board.makeMove(7, 2, Piece.X); board.makeMove(7, 3, Piece.X);
        board.makeMove(7, 5, Piece.X); board.makeMove(7, 6, Piece.X);
        board.makeMove(7, 7, Piece.O);
        board.makeMove(7, 4, Piece.X); // Nối
        assertFalse(board.checkWin(7, 4), "Nối thành 5 nhưng bị O chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    // =========================================================================
    // NHÓM 5: CHƯA ĐỦ QUÂN, ĐỨT QUÃNG, BỊ NGẮT (T25 - T30)
    // =========================================================================

    @Test
    @DisplayName("T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG")
    public void testT25() {
        PlacedMove m = setupPattern(". X X X X* .");
        assertFalse(board.checkWin(m.row, m.col), "Chỉ 4 quân KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG")
    public void testT26() {
        PlacedMove m = setupPattern(". X X . X* X X .");
        assertFalse(board.checkWin(m.row, m.col), "Bị ô trống ngắt quãng KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG")
    public void testT27() {
        PlacedMove m = setupPattern(". X X O X* X X .");
        assertFalse(board.checkWin(m.row, m.col), "Bị quân O ngắt ở giữa KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG")
    public void testT28() {
        PlacedMove m = setupPattern(". X X X* . X X .");
        assertFalse(board.checkWin(m.row, m.col), "Còn ô trống ở giữa KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T29: . X X X O X* X . -> KHÔNG THẮNG")
    public void testT29() {
        PlacedMove m = setupPattern(". X X X O X* X .");
        assertFalse(board.checkWin(m.row, m.col), "Bị quân O ngắt KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG")
    public void testT30() {
        board.makeMove(0, 1, Piece.O);
        for (int c = 2; c <= 6; c++) board.makeMove(0, c, Piece.X);
        board.makeMove(0, 7, Piece.O);

        board.makeMove(7, 7, Piece.X);
        assertFalse(board.checkWin(7, 7), "Đặt chỗ khác không tạo hàng mới KHÔNG ĐƯỢC THẮNG");
    }

    // =========================================================================
    // NHÓM 6: MỘT NƯỚC ĐI TẠO NHIỀU HÀNG (T31 - T38)
    // =========================================================================

    @Test
    @DisplayName("T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG")
    public void testT31() {
        board.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        board.makeMove(7, 7, Piece.O);

        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);

        board.makeMove(7, 4, Piece.X);
        assertTrue(board.checkWin(7, 4), "Hàng dọc 5 mở hợp lệ phải THẮNG dù hàng ngang bị chặn");
    }

    @Test
    @DisplayName("T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG")
    public void testT32() {
        board.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        board.makeMove(7, 7, Piece.O);

        board.makeMove(4, 1, Piece.O);
        board.makeMove(5, 2, Piece.X); board.makeMove(6, 3, Piece.X);
        board.makeMove(8, 5, Piece.X); board.makeMove(9, 6, Piece.X);
        board.makeMove(10, 7, Piece.O);

        board.makeMove(7, 4, Piece.X);
        assertFalse(board.checkWin(7, 4), "Cả hai hướng đều bị chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG")
    public void testT33() {
        for (int c = 2; c <= 6; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(7, 4, Piece.X);
        assertTrue(board.checkWin(7, 4), "Cả ngang và dọc đều hợp lệ phải THẮNG");
    }

    @Test
    @DisplayName("T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG")
    public void testT34() {
        for (int c = 2; c <= 7; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(7, 4, Piece.X);
        assertTrue(board.checkWin(7, 4), "Cả ngang 6 và dọc 5 đều hợp lệ ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG")
    public void testT35() {
        for (int c = 2; c <= 7; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        for (int r = 4; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(7, 4, Piece.X);
        assertTrue(board.checkWin(7, 4), "Cả hai hàng 6 đều mở ở RULE B phải THẮNG");
    }

    @Test
    @DisplayName("T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang)")
    public void testT36() {
        for (int c = 2; c <= 7; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        board.makeMove(4, 4, Piece.O);
        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(10, 4, Piece.O);
        board.makeMove(7, 4, Piece.X);
        assertTrue(board.checkWin(7, 4), "Thắng nhờ hàng ngang 6 mở ở RULE B dù dọc bị chặn");
    }

    @Test
    @DisplayName("T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG")
    public void testT37() {
        board.makeMove(7, 0, Piece.O);
        for (int c = 1; c <= 7; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        board.makeMove(7, 8, Piece.O);

        board.makeMove(4, 4, Piece.O);
        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(10, 4, Piece.O);

        board.makeMove(7, 4, Piece.X);
        assertFalse(board.checkWin(7, 4), "Cả ngang 7 và dọc 5 đều bị chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    @Test
    @DisplayName("T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG")
    public void testT38() {
        for (int c = 2; c <= 5; c++) if (c != 4) board.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 8; r++) if (r != 7) board.makeMove(r, 4, Piece.X);
        board.makeMove(5, 2, Piece.X); board.makeMove(6, 3, Piece.X); board.makeMove(8, 5, Piece.X);
        board.makeMove(7, 4, Piece.X);
        assertFalse(board.checkWin(7, 4), "Tất cả các hướng chỉ có 4 quân KHÔNG ĐƯỢC THẮNG");
    }

    // =========================================================================
    // NHÓM 7: CÁC HƯỚNG VÀ BIÊN BÀN CỜ (T39 - T46)
    // =========================================================================

    @Test
    @DisplayName("T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG")
    public void testT39() {
        for (int r = 5; r <= 9; r++) if (r != 7) board.makeMove(r, 7, Piece.X);
        board.makeMove(7, 7, Piece.X);
        assertTrue(board.checkWin(7, 7), "Hàng dọc 5 mở 2 đầu phải THẮNG");
    }

    @Test
    @DisplayName("T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG")
    public void testT40() {
        for (int i = 5; i <= 9; i++) if (i != 7) board.makeMove(i, i, Piece.X);
        board.makeMove(7, 7, Piece.X);
        assertTrue(board.checkWin(7, 7), "Chéo chính 5 mở 2 đầu phải THẮNG");
    }

    @Test
    @DisplayName("T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG")
    public void testT41() {
        for (int i = 0; i < 5; i++) {
            int r = 5 + i;
            int c = 9 - i;
            if (r != 7) board.makeMove(r, c, Piece.X);
        }
        board.makeMove(7, 7, Piece.X);
        assertTrue(board.checkWin(7, 7), "Chéo phụ 5 mở 2 đầu phải THẮNG");
    }

    @Test
    @DisplayName("T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG")
    public void testT42() {
        for (int c = 1; c <= 4; c++) board.makeMove(5, c, Piece.X);
        board.makeMove(5, 0, Piece.X);
        assertTrue(board.checkWin(5, 0), "Cột 0-4 biên trái không lỗi chỉ số mảng và phải THẮNG");
    }

    @Test
    @DisplayName("T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG")
    public void testT43() {
        board.makeMove(5, 9, Piece.O);
        for (int c = 10; c <= 13; c++) board.makeMove(5, c, Piece.X);
        board.makeMove(5, 14, Piece.X);
        assertTrue(board.checkWin(5, 14), "Cột 10-14 mép phải, 1 đầu O phải THẮNG");
    }

    @Test
    @DisplayName("T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG")
    public void testT44() {
        for (int i = 1; i <= 4; i++) board.makeMove(i, i, Piece.X);
        board.makeMove(5, 5, Piece.O);
        board.makeMove(0, 0, Piece.X);
        assertTrue(board.checkWin(0, 0), "Góc (0,0) chéo 5 ô, 1 đầu O phải THẮNG");
    }

    @Test
    @DisplayName("T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG")
    public void testT45() {
        for (int i = 10; i <= 13; i++) board.makeMove(i, i, Piece.X);
        board.makeMove(14, 14, Piece.X);
        assertTrue(board.checkWin(14, 14), "Góc (14,14) chéo 5 ô, đầu kia mở phải THẮNG");
    }

    @Test
    @DisplayName("T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG")
    public void testT46() {
        for (int i = 0; i <= 4; i++) board.makeMove(i, i, Piece.X);
        board.makeMove(6, 6, Piece.O);
        board.makeMove(5, 5, Piece.X);
        assertTrue(board.checkWin(5, 5), "Góc (0,0) chéo 6 ô ở RULE B phải THẮNG");
    }

    // =========================================================================
    // NHÓM 8: ĐỐI XỨNG MÀU (NGƯỜI VỪA ĐI LÀ O) (T47 - T48)
    // =========================================================================

    @Test
    @DisplayName("T47: . O O O O O* . -> O THẮNG")
    public void testT47() {
        PlacedMove m = setupPattern(". O O O O O* .");
        assertTrue(board.checkWin(m.row, m.col), "Người vừa đi là O tạo hàng 5 mở phải THẮNG");
    }

    @Test
    @DisplayName("T48: X O O O O O* X -> KHÔNG THẮNG")
    public void testT48() {
        PlacedMove m = setupPattern("X O O O O O* X");
        assertFalse(board.checkWin(m.row, m.col), "O bị X chặn kín 2 đầu KHÔNG ĐƯỢC THẮNG");
    }

    // =========================================================================
    // NHÓM 9: KẾT THÚC VÁN VÀ NƯỚC ĐI KHÔNG HỢP LỆ (T49 - T52)
    // (Các ca T53 đến T57 không áp dụng do dự án chưa có lớp controller quản lý)
    // =========================================================================

    @Test
    @DisplayName("T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA")
    public void testT49() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (r == 14 && c == 14) continue;
                int phase = (r % 2 == 0) ? 0 : 2;
                int p = (c + phase) % 4;
                board.makeMove(r, c, (p == 0 || p == 1) ? Piece.X : Piece.O);
            }
        }
        int phase = (14 % 2 == 0) ? 0 : 2;
        int p = (14 + phase) % 4;
        board.makeMove(14, 14, (p == 0 || p == 1) ? Piece.X : Piece.O);

        assertTrue(board.isFull(), "Bàn cờ phải đầy 225 ô");
        assertFalse(board.checkWin(14, 14), "Nước cuối không thắng");
    }

    @Test
    @DisplayName("T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa)")
    public void testT50() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (r == 14 && c == 14) continue;
                if (r == 14 && c == 9) {
                    board.makeMove(r, c, Piece.O); // Chặn đầu trái để chuỗi X đúng 5 quân ở 10..14
                } else if (r == 14 && c >= 10 && c <= 13) {
                    board.makeMove(r, c, Piece.X);
                } else {
                    int phase = (r % 2 == 0) ? 0 : 2;
                    int p = (c + phase) % 4;
                    board.makeMove(r, c, (p == 0 || p == 1) ? Piece.X : Piece.O);
                }
            }
        }
        board.makeMove(14, 14, Piece.X);

        assertTrue(board.isFull(), "Bàn cờ đầy 225 ô");
        assertTrue(board.checkWin(14, 14), "Nước thứ 225 tạo hàng hợp lệ phải THẮNG");
    }

    @Test
    @DisplayName("T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên")
    public void testT51() {
        board.makeMove(7, 7, Piece.X);
        int movesBefore = board.getMoveCount();

        boolean allowed = board.makeMove(7, 7, Piece.O);
        assertFalse(allowed, "Không được phép đặt vào ô đã có quân");
        assertEquals(movesBefore, board.getMoveCount(), "Số nước đi không được tăng");
        assertEquals(Piece.X, board.getPiece(7, 7), "Quân cờ cũ tại ô phải được giữ nguyên");
    }

    @Test
    @DisplayName("T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ")
    public void testT52() {
        assertFalse(board.isValidMove(15, 0), "Tọa độ hàng 15 phải không hợp lệ");
        assertFalse(board.isValidMove(-1, 0), "Tọa độ âm phải không hợp lệ");
        assertFalse(board.isValidMove(0, 15), "Tọa độ cột 15 phải không hợp lệ");
        assertDoesNotThrow(() -> board.makeMove(15, 0, Piece.X), "Gọi makeMove ngoài bàn cờ không được văng Exception");
    }
}
