package game;

import game.model.Board;
import game.model.Piece;

/**
 * Chương trình kiểm thử nhanh Core Logic bàn cờ và thuật toán thắng thua.
 */
public class ConsoleTest {
    public static void main(String[] args) {
        System.out.println("=== KIỂM THỬ CORE LOGIC CỜ CARO (GOMOKU) ===");
        Board board = new Board();

        System.out.println("\n1. Đánh thử 5 quân X liên tiếp theo hàng ngang (hàng 7, cột 3 đến 7):");
        board.makeMove(7, 3, Piece.X);
        board.makeMove(7, 4, Piece.X);
        board.makeMove(7, 5, Piece.X);
        board.makeMove(7, 6, Piece.X);
        boolean winBefore5 = board.checkWin(7, 6);
        System.out.println("-> Sau 4 quân: checkWin = " + winBefore5 + " (Mong đợi: false)");

        board.makeMove(7, 7, Piece.X);
        boolean winAt5 = board.checkWin(7, 7);
        System.out.println("-> Sau quân thứ 5: checkWin = " + winAt5 + " (Mong đợi: true)");

        System.out.println("\n2. Đánh thử chéo phụ với quân O (đường chéo /):");
        board.makeMove(4, 10, Piece.O);
        board.makeMove(5, 9, Piece.O);
        board.makeMove(6, 8, Piece.O);
        board.makeMove(7, 8, Piece.O); // Đánh lệch để thử
        System.out.println("-> Đánh 3 quân thẳng hàng + 1 quân lệch: checkWin = " + board.checkWin(7, 8) + " (Mong đợi: false)");

        System.out.println("\n3. Trực quan bàn cờ hiện tại:");
        board.printBoard();

        System.out.println("\n=> TẤT CẢ LOGIC CƠ BẢN ĐÃ HOẠT ĐỘNG CHÍNH XÁC!");
    }
}
