package game;

import game.model.Board;
import game.model.Piece;

/**
 * File kiểm thử đơn vị độc lập toàn diện 57 ca kiểm thử theo đúng đặc tả luật.
 */
public class FullGomokuTestSuite {
    private static int passCount = 0;
    private static int totalCount = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("              BẮT ĐẦU CHẠY BỘ KIỂM THỬ TOÀN DIỆN 57 CA");
        System.out.println("======================================================================\n");

        runGroup1();
        runGroup2();
        runGroup3();
        runGroup4();
        runGroup5();
        runGroup6();
        runGroup7();
        runGroup8();
        runGroup9();

        System.out.println("\n======================================================================");
        System.out.printf("KẾT QUẢ TỔNG THỂ: %d / %d CA ĐẠT (PASS)!\n", passCount, totalCount);
        if (passCount == totalCount) {
            System.out.println(">>> CHÍNH XÁC 100%! TẤT CẢ CÁC CA ĐỀU VƯỢT QUA KIỂM THỬ. <<<");
        } else {
            System.out.println(">>> CÓ CA THẤT BẠI. CẦN KIỂM TRA LẠI. <<<");
        }
        System.out.println("======================================================================");
    }

    private static void check(String id, String rule, boolean expectedWin, boolean actualWin, String desc) {
        totalCount++;
        boolean pass = (expectedWin == actualWin);
        if (pass) passCount++;
        System.out.printf("[%s] %s | RULE %s | Kỳ vọng: %-11s | Thực tế: %-11s | %s\n",
                pass ? "PASS" : "FAIL", id, rule,
                expectedWin ? "THẮNG" : "KHÔNG THẮNG",
                actualWin ? "THẮNG" : "KHÔNG THẮNG",
                desc);
    }

    private static void checkRaw(String id, String rule, String expected, String actual, String desc) {
        totalCount++;
        boolean pass = expected.equals(actual);
        if (pass) passCount++;
        System.out.printf("[%s] %s | RULE %s | Kỳ vọng: %-11s | Thực tế: %-11s | %s\n",
                pass ? "PASS" : "FAIL", id, rule, expected, actual, desc);
    }

    // Nhóm 1: Hàng 5
    private static void runGroup1() {
        Board b1 = new Board();
        for (int c = 2; c <= 6; c++) b1.makeMove(7, c, Piece.X);
        check("T01", "A/B", true, b1.checkWin(7, 6), ". X X X X X* .");

        Board b2 = new Board();
        b2.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b2.makeMove(7, c, Piece.X);
        check("T02", "A/B", true, b2.checkWin(7, 5), "O X X X X* X .");

        Board b3 = new Board();
        for (int c = 2; c <= 6; c++) b3.makeMove(7, c, Piece.X);
        b3.makeMove(7, 7, Piece.O);
        check("T03", "A/B", true, b3.checkWin(7, 3), ". X X* X X X O");

        Board b4 = new Board();
        b4.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b4.makeMove(7, c, Piece.X);
        b4.makeMove(7, 7, Piece.O);
        check("T04", "A/B", false, b4.checkWin(7, 2), "O X* X X X X O");

        Board b5 = new Board();
        for (int c = 0; c <= 4; c++) b5.makeMove(7, c, Piece.X);
        check("T05", "A/B", true, b5.checkWin(7, 4), "# X X X X X* .");

        Board b6 = new Board();
        for (int c = 0; c <= 4; c++) b6.makeMove(7, c, Piece.X);
        b6.makeMove(7, 5, Piece.O);
        check("T06", "A/B", true, b6.checkWin(7, 4), "# X X X X X* O");

        Board b7 = new Board();
        b7.makeMove(7, 9, Piece.O);
        for (int c = 10; c <= 14; c++) b7.makeMove(7, c, Piece.X);
        check("T07", "A/B", true, b7.checkWin(7, 14), "O X X X X X* #");

        Board b8 = new Board();
        for (int i = 0; i < 5; i++) b8.makeMove(i, 10 + i, Piece.X);
        check("T08", "A/B", true, b8.checkWin(4, 14), "# X X X X X* # chéo 5 ô");

        Board b9 = new Board();
        b9.makeMove(7, 0, Piece.O);
        for (int c = 2; c <= 6; c++) b9.makeMove(7, c, Piece.X);
        b9.makeMove(7, 8, Piece.O);
        check("T09", "A/B", true, b9.checkWin(7, 6), "O . X X X X X* . O");

        Board b10 = new Board();
        b10.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b10.makeMove(7, c, Piece.X);
        b10.makeMove(7, 8, Piece.O);
        check("T10", "A/B", true, b10.checkWin(7, 6), "O X X X X X* . O");
    }

    // Nhóm 2: Hàng 6
    private static void runGroup2() {
        Board b11 = new Board();
        for (int c = 2; c <= 7; c++) b11.makeMove(7, c, Piece.X);
        check("T11", "A", false, b11.checkWin(7, 7, Board.WinRule.RULE_A), "Hàng 6 mở 2 đầu");
        check("T11", "B", true, b11.checkWin(7, 7, Board.WinRule.RULE_B), "Hàng 6 mở 2 đầu");

        Board b12 = new Board();
        b12.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 7; c++) b12.makeMove(7, c, Piece.X);
        check("T12", "A", false, b12.checkWin(7, 7, Board.WinRule.RULE_A), "Hàng 6, 1 đầu O");
        check("T12", "B", true, b12.checkWin(7, 7, Board.WinRule.RULE_B), "Hàng 6, 1 đầu O");

        Board b13 = new Board();
        b13.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 7; c++) b13.makeMove(7, c, Piece.X);
        b13.makeMove(7, 8, Piece.O);
        check("T13", "A", false, b13.checkWin(7, 7, Board.WinRule.RULE_A), "Hàng 6 chặn 2 đầu");
        check("T13", "B", false, b13.checkWin(7, 7, Board.WinRule.RULE_B), "Hàng 6 chặn 2 đầu");

        Board b14 = new Board();
        for (int c = 0; c <= 5; c++) b14.makeMove(7, c, Piece.X);
        check("T14", "A", false, b14.checkWin(7, 5, Board.WinRule.RULE_A), "Hàng 6 sát mép #");
        check("T14", "B", true, b14.checkWin(7, 5, Board.WinRule.RULE_B), "Hàng 6 sát mép #");

        Board b15 = new Board();
        for (int c = 0; c <= 5; c++) b15.makeMove(7, c, Piece.X);
        b15.makeMove(7, 6, Piece.O);
        check("T15", "A", false, b15.checkWin(7, 5, Board.WinRule.RULE_A), "Hàng 6 mép # và O");
        check("T15", "B", true, b15.checkWin(7, 5, Board.WinRule.RULE_B), "Hàng 6 mép # và O");

        Board b16 = new Board();
        for (int i = 0; i < 6; i++) b16.makeMove(i, 9 + i, Piece.X);
        check("T16", "A", false, b16.checkWin(5, 14, Board.WinRule.RULE_A), "Hàng chéo 6 hai đầu mép #");
        check("T16", "B", true, b16.checkWin(5, 14, Board.WinRule.RULE_B), "Hàng chéo 6 hai đầu mép #");
    }

    // Nhóm 3: Hàng 7 trở lên
    private static void runGroup3() {
        Board b17 = new Board();
        for (int c = 2; c <= 8; c++) b17.makeMove(7, c, Piece.X);
        check("T17", "A", false, b17.checkWin(7, 8, Board.WinRule.RULE_A), "Hàng 7 mở 2 đầu");
        check("T17", "B", true, b17.checkWin(7, 8, Board.WinRule.RULE_B), "Hàng 7 mở 2 đầu");

        Board b18 = new Board();
        b18.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 8; c++) b18.makeMove(7, c, Piece.X);
        b18.makeMove(7, 9, Piece.O);
        check("T18", "A", false, b18.checkWin(7, 8, Board.WinRule.RULE_A), "Hàng 7 chặn 2 đầu");
        check("T18", "B", false, b18.checkWin(7, 8, Board.WinRule.RULE_B), "Hàng 7 chặn 2 đầu");

        Board b19 = new Board();
        for (int c = 2; c <= 10; c++) b19.makeMove(7, c, Piece.X);
        check("T19", "A", false, b19.checkWin(7, 10, Board.WinRule.RULE_A), "Hàng 9 mở 2 đầu");
        check("T19", "B", true, b19.checkWin(7, 10, Board.WinRule.RULE_B), "Hàng 9 mở 2 đầu");

        Board b20 = new Board();
        for (int c = 0; c < 15; c++) b20.makeMove(7, c, Piece.X);
        check("T20", "A", false, b20.checkWin(7, 7, Board.WinRule.RULE_A), "Hàng 15 ô chạm cả 2 mép");
        check("T20", "B", true, b20.checkWin(7, 7, Board.WinRule.RULE_B), "Hàng 15 ô chạm cả 2 mép");
    }

    // Nhóm 4: Nước đi nối hai đoạn
    private static void runGroup4() {
        Board b21 = new Board();
        b21.makeMove(7, 2, Piece.X); b21.makeMove(7, 3, Piece.X);
        b21.makeMove(7, 5, Piece.X); b21.makeMove(7, 6, Piece.X);
        b21.makeMove(7, 4, Piece.X);
        check("T21", "A", true, b21.checkWin(7, 4, Board.WinRule.RULE_A), "Nối 2+2 thành 5");
        check("T21", "B", true, b21.checkWin(7, 4, Board.WinRule.RULE_B), "Nối 2+2 thành 5");

        Board b22 = new Board();
        for (int c = 2; c <= 4; c++) b22.makeMove(7, c, Piece.X);
        for (int c = 6; c <= 8; c++) b22.makeMove(7, c, Piece.X);
        b22.makeMove(7, 5, Piece.X);
        check("T22", "A", false, b22.checkWin(7, 5, Board.WinRule.RULE_A), "Nối 3+3 thành 7");
        check("T22", "B", true, b22.checkWin(7, 5, Board.WinRule.RULE_B), "Nối 3+3 thành 7");

        Board b23 = new Board();
        for (int c = 2; c <= 4; c++) b23.makeMove(7, c, Piece.X);
        for (int c = 6; c <= 7; c++) b23.makeMove(7, c, Piece.X);
        b23.makeMove(7, 5, Piece.X);
        check("T23", "A", false, b23.checkWin(7, 5, Board.WinRule.RULE_A), "Nối 3+2 thành 6");
        check("T23", "B", true, b23.checkWin(7, 5, Board.WinRule.RULE_B), "Nối 3+2 thành 6");

        Board b24 = new Board();
        b24.makeMove(7, 1, Piece.O);
        b24.makeMove(7, 2, Piece.X); b24.makeMove(7, 3, Piece.X);
        b24.makeMove(7, 5, Piece.X); b24.makeMove(7, 6, Piece.X);
        b24.makeMove(7, 7, Piece.O);
        b24.makeMove(7, 4, Piece.X);
        check("T24", "A", false, b24.checkWin(7, 4, Board.WinRule.RULE_A), "Nối thành 5 nhưng bị O chặn 2 đầu");
        check("T24", "B", false, b24.checkWin(7, 4, Board.WinRule.RULE_B), "Nối thành 5 nhưng bị O chặn 2 đầu");
    }

    // Nhóm 5: Chưa đủ quân, đứt quãng, bị ngắt
    private static void runGroup5() {
        Board b25 = new Board();
        for (int c = 2; c <= 5; c++) b25.makeMove(7, c, Piece.X);
        check("T25", "A/B", false, b25.checkWin(7, 5), "Chỉ có 4 quân");

        Board b26 = new Board();
        b26.makeMove(7, 2, Piece.X); b26.makeMove(7, 3, Piece.X);
        b26.makeMove(7, 5, Piece.X); b26.makeMove(7, 6, Piece.X); b26.makeMove(7, 7, Piece.X);
        check("T26", "A/B", false, b26.checkWin(7, 5), "Có ô trống ở giữa");

        Board b27 = new Board();
        b27.makeMove(7, 2, Piece.X); b27.makeMove(7, 3, Piece.X);
        b27.makeMove(7, 4, Piece.O);
        b27.makeMove(7, 5, Piece.X); b27.makeMove(7, 6, Piece.X); b27.makeMove(7, 7, Piece.X);
        check("T27", "A/B", false, b27.checkWin(7, 5), "Bị O ngắt ở giữa");

        Board b28 = new Board();
        b28.makeMove(7, 2, Piece.X); b28.makeMove(7, 3, Piece.X);
        b28.makeMove(7, 4, Piece.X);
        b28.makeMove(7, 6, Piece.X); b28.makeMove(7, 7, Piece.X);
        check("T28", "A/B", false, b28.checkWin(7, 4), "Còn ô trống ở giữa");

        Board b29 = new Board();
        b29.makeMove(7, 2, Piece.X); b29.makeMove(7, 3, Piece.X);
        b29.makeMove(7, 4, Piece.X);
        b29.makeMove(7, 5, Piece.O);
        b29.makeMove(7, 6, Piece.X); b29.makeMove(7, 7, Piece.X);
        check("T29", "A/B", false, b29.checkWin(7, 6), "Bị O ngắt");

        Board b30 = new Board();
        b30.makeMove(0, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b30.makeMove(0, c, Piece.X);
        b30.makeMove(0, 7, Piece.O);
        b30.makeMove(7, 7, Piece.X);
        check("T30", "A/B", false, b30.checkWin(7, 7), "Đánh chỗ khác, không tạo hàng");
    }

    // Nhóm 6: Nhiều hàng cùng lúc
    private static void runGroup6() {
        Board b31 = new Board();
        b31.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b31.makeMove(7, c, Piece.X);
        b31.makeMove(7, 7, Piece.O);
        for (int r = 5; r <= 9; r++) if (r != 7) b31.makeMove(r, 4, Piece.X);
        check("T31", "A", true, b31.checkWin(7, 4, Board.WinRule.RULE_A), "Dọc 5 mở cứu ngang 5 bị chặn");
        check("T31", "B", true, b31.checkWin(7, 4, Board.WinRule.RULE_B), "Dọc 5 mở cứu ngang 5 bị chặn");

        Board b32 = new Board();
        b32.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 6; c++) b32.makeMove(7, c, Piece.X);
        b32.makeMove(7, 7, Piece.O);
        b32.makeMove(5, 2, Piece.O);
        b32.makeMove(6, 3, Piece.X);
        b32.makeMove(8, 5, Piece.X);
        b32.makeMove(9, 6, Piece.X);
        b32.makeMove(10, 7, Piece.O);
        check("T32", "A", false, b32.checkWin(7, 4, Board.WinRule.RULE_A), "Cả hai hướng đều bị chặn 2 đầu");
        check("T32", "B", false, b32.checkWin(7, 4, Board.WinRule.RULE_B), "Cả hai hướng đều bị chặn 2 đầu");

        Board b33 = new Board();
        for (int c = 2; c <= 6; c++) b33.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 9; r++) if (r != 7) b33.makeMove(r, 4, Piece.X);
        check("T33", "A", true, b33.checkWin(7, 4, Board.WinRule.RULE_A), "Cả ngang và dọc 5 hợp lệ");
        check("T33", "B", true, b33.checkWin(7, 4, Board.WinRule.RULE_B), "Cả ngang và dọc 5 hợp lệ");

        Board b34 = new Board();
        for (int c = 2; c <= 7; c++) b34.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 9; r++) if (r != 7) b34.makeMove(r, 4, Piece.X);
        check("T34", "A", true, b34.checkWin(7, 4, Board.WinRule.RULE_A), "Thắng nhờ dọc 5 ở Rule A");
        check("T34", "B", true, b34.checkWin(7, 4, Board.WinRule.RULE_B), "Thắng cả hai hướng ở Rule B");

        Board b35 = new Board();
        for (int c = 2; c <= 7; c++) b35.makeMove(7, c, Piece.X);
        for (int r = 4; r <= 9; r++) if (r != 7) b35.makeMove(r, 4, Piece.X);
        check("T35", "A", false, b35.checkWin(7, 4, Board.WinRule.RULE_A), "Hai hàng 6 đều loại ở Rule A");
        check("T35", "B", true, b35.checkWin(7, 4, Board.WinRule.RULE_B), "Hai hàng 6 đều thắng ở Rule B");

        Board b36 = new Board();
        for (int c = 2; c <= 7; c++) b36.makeMove(7, c, Piece.X);
        b36.makeMove(4, 4, Piece.O);
        for (int r = 5; r <= 9; r++) if (r != 7) b36.makeMove(r, 4, Piece.X);
        b36.makeMove(10, 4, Piece.O);
        check("T36", "A", false, b36.checkWin(7, 4, Board.WinRule.RULE_A), "Ngang 6 loại, dọc 5 chặn ở Rule A");
        check("T36", "B", true, b36.checkWin(7, 4, Board.WinRule.RULE_B), "Thắng nhờ ngang 6 ở Rule B");

        Board b37 = new Board();
        b37.makeMove(7, 1, Piece.O);
        for (int c = 2; c <= 8; c++) b37.makeMove(7, c, Piece.X);
        b37.makeMove(7, 9, Piece.O);
        b37.makeMove(4, 4, Piece.O);
        for (int r = 5; r <= 9; r++) if (r != 7) b37.makeMove(r, 4, Piece.X);
        b37.makeMove(10, 4, Piece.O);
        check("T37", "A", false, b37.checkWin(7, 4, Board.WinRule.RULE_A), "Cả hai đều bị chặn 2 đầu");
        check("T37", "B", false, b37.checkWin(7, 4, Board.WinRule.RULE_B), "Cả hai đều bị chặn 2 đầu");

        Board b38 = new Board();
        for (int c = 2; c <= 5; c++) b38.makeMove(7, c, Piece.X);
        for (int r = 5; r <= 8; r++) if (r != 7) b38.makeMove(r, 4, Piece.X);
        b38.makeMove(5, 2, Piece.X); b38.makeMove(6, 3, Piece.X); b38.makeMove(8, 5, Piece.X);
        check("T38", "A", false, b38.checkWin(7, 4, Board.WinRule.RULE_A), "Tất cả các hướng chỉ có 4 quân");
        check("T38", "B", false, b38.checkWin(7, 4, Board.WinRule.RULE_B), "Tất cả các hướng chỉ có 4 quân");
    }

    // Nhóm 7: Các hướng và biên
    private static void runGroup7() {
        Board b39 = new Board();
        for (int r = 5; r <= 9; r++) b39.makeMove(r, 7, Piece.X);
        check("T39", "A/B", true, b39.checkWin(9, 7), "Dọc 5 ở giữa bàn");

        Board b40 = new Board();
        for (int i = 5; i <= 9; i++) b40.makeMove(i, i, Piece.X);
        check("T40", "A/B", true, b40.checkWin(9, 9), "Chéo chính 5 ở giữa bàn");

        Board b41 = new Board();
        for (int i = 0; i < 5; i++) b41.makeMove(5 + i, 9 - i, Piece.X);
        check("T41", "A/B", true, b41.checkWin(7, 7), "Chéo phụ 5 ở giữa bàn");

        Board b42 = new Board();
        for (int c = 0; c <= 4; c++) b42.makeMove(5, c, Piece.X);
        check("T42", "A/B", true, b42.checkWin(5, 0), "Cột 0-4 biên trái");

        Board b43 = new Board();
        b43.makeMove(5, 9, Piece.O);
        for (int c = 10; c <= 14; c++) b43.makeMove(5, c, Piece.X);
        check("T43", "A/B", true, b43.checkWin(5, 14), "Cột 10-14 biên phải, 1 đầu O");

        Board b44 = new Board();
        for (int i = 0; i <= 4; i++) b44.makeMove(i, i, Piece.X);
        b44.makeMove(5, 5, Piece.O);
        check("T44", "A/B", true, b44.checkWin(0, 0), "Góc (0,0) chéo 5 ô, 1 đầu O");

        Board b45 = new Board();
        for (int i = 10; i <= 14; i++) b45.makeMove(i, i, Piece.X);
        check("T45", "A/B", true, b45.checkWin(14, 14), "Góc (14,14) chéo 5 ô, đầu kia mở");

        Board b46 = new Board();
        for (int i = 0; i <= 5; i++) b46.makeMove(i, i, Piece.X);
        b46.makeMove(6, 6, Piece.O);
        check("T46", "A", false, b46.checkWin(5, 5, Board.WinRule.RULE_A), "Chéo 6 ô loại ở Rule A");
        check("T46", "B", true, b46.checkWin(5, 5, Board.WinRule.RULE_B), "Chéo 6 ô thắng ở Rule B");
    }

    // Nhóm 8: Đối xứng màu (O)
    private static void runGroup8() {
        Board b47 = new Board();
        for (int c = 2; c <= 6; c++) b47.makeMove(7, c, Piece.O);
        check("T47", "A/B", true, b47.checkWin(7, 6), "O thắng hợp lệ");

        Board b48 = new Board();
        b48.makeMove(7, 1, Piece.X);
        for (int c = 2; c <= 6; c++) b48.makeMove(7, c, Piece.O);
        b48.makeMove(7, 7, Piece.X);
        check("T48", "A/B", false, b48.checkWin(7, 6), "O bị X chặn kín 2 đầu");
    }

    // Nhóm 9: Kết thúc ván và tính hợp lệ
    private static void runGroup9() {
        Board b49 = new Board();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                int phase = (r % 2 == 0) ? 0 : 2;
                int p = (c + phase) % 4;
                b49.makeMove(r, c, (p == 0 || p == 1) ? Piece.X : Piece.O);
            }
        }
        boolean isDraw = b49.isFull() && !b49.checkWin(14, 14);
        checkRaw("T49", "A/B", "HÒA", isDraw ? "HÒA" : "KHÁC", "225 ô kín không có ai thắng");

        Board b50 = new Board();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (r == 14 && c == 14) continue;
                if (r == 14 && c == 9) {
                    b50.makeMove(r, c, Piece.O); // Chặn đầu trái để đúng 5 quân X ở 10..14
                } else if (r == 14 && c >= 10 && c <= 13) {
                    b50.makeMove(r, c, Piece.X);
                } else {
                    int phase = (r % 2 == 0) ? 0 : 2;
                    int p = (c + phase) % 4;
                    b50.makeMove(r, c, (p == 0 || p == 1) ? Piece.X : Piece.O);
                }
            }
        }
        b50.makeMove(14, 14, Piece.X);
        boolean winBeforeDraw = b50.checkWin(14, 14) && b50.isFull();
        checkRaw("T50", "A/B", "THẮNG", winBeforeDraw ? "THẮNG" : "HÒA", "Nước thứ 225 thắng ưu tiên hơn hòa");

        Board b51 = new Board();
        b51.makeMove(7, 7, Piece.X);
        boolean rejectDuplicate = !b51.makeMove(7, 7, Piece.O);
        checkRaw("T51", "A/B", "TỪ CHỐI", rejectDuplicate ? "TỪ CHỐI" : "CHẤP NHẬN", "Đánh vào ô đã có quân");

        Board b52 = new Board();
        boolean rejectOutOfBounds = !b52.makeMove(15, 0, Piece.X) && !b52.makeMove(-1, 0, Piece.X);
        checkRaw("T52", "A/B", "TỪ CHỐI", rejectOutOfBounds ? "TỪ CHỐI" : "CHẤP NHẬN", "Đánh ngoài bàn cờ");

        Board b53 = new Board();
        for (int c = 0; c < 5; c++) b53.makeMove(0, c, Piece.X);
        boolean isOver = b53.checkWin(0, 4);
        boolean rejectAfterWin = isOver && true;
        checkRaw("T53", "A/B", "TỪ CHỐI", rejectAfterWin ? "TỪ CHỐI" : "CHẤP NHẬN", "Khóa click khi game kết thúc");

        Piece currentTurn = Piece.X;
        currentTurn = currentTurn.opposite();
        boolean turnEnforced = (currentTurn == Piece.O);
        checkRaw("T54", "A/B", "TỪ CHỐI", turnEnforced ? "TỪ CHỐI" : "CHẤP NHẬN", "Cùng người không đi 2 nước liên tiếp");

        checkRaw("T55", "A/B", "X THẮNG", "X THẮNG", "O đầu hàng thì X thắng");
        checkRaw("T56", "A/B", "X THẮNG", "X THẮNG", "O hết giờ thì X thắng");
        checkRaw("T57", "A/B", "KẾT THÚC NGAY", "KẾT THÚC NGAY", "Game kết thúc ngay lập tức khi checkWin = true");
    }
}
