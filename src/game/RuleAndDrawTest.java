package game;

import game.model.Board;
import game.model.Piece;

/**
 * Kiểm thử toàn diện:
 * 1. Luật Chặn Hai Đầu (Blocked-ends rule)
 * 2. Xử lý bàn cờ 15x15 đánh kín 225 ô (Hòa cờ vs Thắng ở nước cuối)
 */
public class RuleAndDrawTest {
    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ LUẬT CHẶN 2 ĐẦU & HÒA CỜ");
        System.out.println("==================================================\n");

        testBlockedEndsRule();
        testBoardFullAndDraw();

        System.out.println("\n==================================================");
        System.out.printf("KẾT QUẢ: ĐÃ VƯỢT QUA %d / %d KIỂM THỬ.\n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println(">>> TẤT CẢ CÁC KIỂM THỬ ĐỀU CHÍNH XÁC 100%! <<<");
        } else {
            System.out.println(">>> CÓ KIỂM THỬ THẤT BẠI! CẦN KIỂM TRA LẠI. <<<");
        }
        System.out.println("==================================================");
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName);
        }
    }

    private static void testBlockedEndsRule() {
        System.out.println("--- PHẦN 1: KIỂM THỬ LUẬT CHẶN HAI ĐẦU ---");

        // 1. Chuỗi 5 quân hở 2 đầu -> Thắng
        Board b1 = new Board();
        for (int c = 2; c <= 6; c++) b1.makeMove(7, c, Piece.X);
        assertTrue("1. Chuỗi 5 quân hở 2 đầu (_XXXXX_) -> THẮNG", b1.checkWin(7, 6));

        // 2. Chuỗi 5 quân bị chặn 1 đầu bởi O -> Thắng
        Board b2 = new Board();
        b2.makeMove(7, 1, Piece.O); // Chặn đầu trái
        for (int c = 2; c <= 6; c++) b2.makeMove(7, c, Piece.X);
        assertTrue("2. Chuỗi 5 quân bị chặn 1 đầu bởi O (OXXXXX_) -> THẮNG", b2.checkWin(7, 6));

        // 3. Chuỗi 5 quân bị chặn CẢ 2 ĐẦU bởi O -> KHÔNG THẮNG
        Board b3 = new Board();
        b3.makeMove(7, 1, Piece.O); // Chặn đầu trái
        for (int c = 2; c <= 6; c++) b3.makeMove(7, c, Piece.X);
        b3.makeMove(7, 7, Piece.O); // Chặn đầu phải
        assertTrue("3. Chuỗi 5 quân bị chặn 2 đầu bởi O (OXXXXXO) -> KHÔNG THẮNG", !b3.checkWin(7, 6));

        // 4. Chuỗi 5 quân sát mép bàn cờ (cột 0 đến 4) và đầu kia hở -> Thắng
        Board b4 = new Board();
        for (int c = 0; c <= 4; c++) b4.makeMove(0, c, Piece.X);
        assertTrue("4. Chuỗi 5 quân sát mép bàn cờ, đầu kia hở (|XXXXX_) -> THẮNG", b4.checkWin(0, 4));

        // 5. Chuỗi 5 quân sát mép bàn cờ và đầu kia bị chặn bởi O -> Vẫn Thắng (vì mép không tính là đối thủ chặn)
        Board b5 = new Board();
        for (int c = 0; c <= 4; c++) b5.makeMove(0, c, Piece.X);
        b5.makeMove(0, 5, Piece.O); // Chặn đầu còn lại
        assertTrue("5. Chuỗi 5 quân sát mép, 1 đầu bị O chặn (|XXXXXO) -> THẮNG", b5.checkWin(0, 4));

        // 6. Kiểm tra trên đường chéo: Bị chặn 2 đầu bởi O -> KHÔNG THẮNG
        Board b6 = new Board();
        b6.makeMove(1, 1, Piece.O); // Chặn đầu trên-trái
        for (int i = 2; i <= 6; i++) b6.makeMove(i, i, Piece.X);
        b6.makeMove(7, 7, Piece.O); // Chặn đầu dưới-phải
        assertTrue("6. Chuỗi chéo bị chặn 2 đầu bởi O -> KHÔNG THẮNG", !b6.checkWin(6, 6));

        // 7. Chuỗi chéo bị chặn 1 đầu bởi O -> THẮNG
        Board b7 = new Board();
        b7.makeMove(1, 1, Piece.O); // Chỉ chặn 1 đầu
        for (int i = 2; i <= 6; i++) b7.makeMove(i, i, Piece.X);
        assertTrue("7. Chuỗi chéo bị chặn 1 đầu bởi O -> THẮNG", b7.checkWin(6, 6));

        // 8. Chuỗi 6 quân bị chặn 2 đầu bởi O -> KHÔNG THẮNG
        Board b8 = new Board();
        b8.makeMove(5, 1, Piece.O);
        for (int c = 2; c <= 7; c++) b8.makeMove(5, c, Piece.X);
        b8.makeMove(5, 8, Piece.O);
        assertTrue("8. Chuỗi 6 quân bị chặn 2 đầu bởi O (OXXXXXXO) -> KHÔNG THẮNG", !b8.checkWin(5, 7));
    }

    private static Piece getDrawPatternPiece(int r, int c) {
        int phase = (r % 2 == 0) ? 0 : 2;
        int pattern = (c + phase) % 4;
        return (pattern == 0 || pattern == 1) ? Piece.X : Piece.O;
    }

    private static void testBoardFullAndDraw() {
        System.out.println("\n--- PHẦN 2: KIỂM THỬ BÀN CỜ ĐÁNH KÍN 225 Ô & HÒA CỜ ---");

        Board b = new Board();

        // Đánh 224 ô đầu tiên theo mẫu đảm bảo không vượt quá 2 quân liên tiếp theo mọi hướng
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (r == Board.SIZE - 1 && c == Board.SIZE - 1) {
                    continue; // Chừa lại ô cuối cùng (14, 14)
                }
                b.makeMove(r, c, getDrawPatternPiece(r, c));
            }
        }

        assertTrue("1. Đã đánh 224 ô -> isFull() phải là false", !b.isFull());
        assertTrue("2. Nước đi tại ô cuối cùng (14, 14) phải hợp lệ", b.isValidMove(14, 14));

        // Kiểm thử Trường hợp HÒA: Đánh nước thứ 225 vào (14, 14) theo mẫu hòa
        Piece lastPiece = getDrawPatternPiece(14, 14);
        b.makeMove(14, 14, lastPiece);

        assertTrue("3. Đánh nước thứ 225 thành công", b.getMoveCount() == 225);
        assertTrue("4. Bàn cờ hiện tại đã đánh kín 225 ô (isFull == true)", b.isFull());
        boolean winAtLast = b.checkWin(14, 14);
        assertTrue("5. Nước thứ 225 không tạo thế thắng (checkWin == false) -> Xử HÒA", !winAtLast);
        assertTrue("6. Không thể đánh thêm bất kỳ ô nào sau khi kín bàn", !b.isValidMove(14, 14));

        // Kiểm thử Trường hợp THẮNG Ở NƯỚC CUỐI CÙNG (không bị nhầm thành hòa):
        // Tạo bàn cờ có 224 ô đã đánh, trong đó hàng 14 có: 4 quân X ở (14, 10..13)
        // Ô cuối cùng (14, 14) sát mép phải được đánh X để tạo thành chuỗi 5 quân X (14, 10..14)
        // Vì đầu phải sát mép bàn cờ nên không bị chặn 2 đầu -> Phải tính THẮNG!
        Board bWinLast = new Board();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (r == 14 && c == 14) continue; // Ô cuối cùng chừa lại
                if (r == 14 && c >= 10 && c <= 13) {
                    bWinLast.makeMove(r, c, Piece.X); // 4 quân X chuẩn bị thắng
                } else {
                    bWinLast.makeMove(r, c, getDrawPatternPiece(r, c));
                }
            }
        }

        assertTrue("7. Bàn cờ trước nước 225 có 224 nước", bWinLast.getMoveCount() == 224);
        bWinLast.makeMove(14, 14, Piece.X); // Nước thứ 225 hoàn thành 5 quân X (14, 10..14)
        assertTrue("8. Nước thứ 225 tạo thành 5 quân X -> checkWin() PHẢI LÀ TRUE", bWinLast.checkWin(14, 14));
        assertTrue("9. Bàn cờ đồng thời đầy 225 ô (isFull == true)", bWinLast.isFull());
    }
}
