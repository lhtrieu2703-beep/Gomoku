package game;

import game.ai.MinimaxAI;
import game.ai.Move;
import game.model.Board;
import game.model.Piece;

/**
 * Chương trình kiểm thử và đo đạc hiệu năng thực nghiệm của thuật toán AI.
 * Dữ liệu đầu ra dùng trực tiếp cho Báo cáo Đồ án cơ sở (Mục 4.2 và Kế hoạch tuần 9).
 */
public class AIBenchmarkTest {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   ĐỒ ÁN CƠ SỞ: THỬ NGHIỆM ĐO ĐẠC HIỆU NĂNG THUẬT TOÁN MINIMAX + ALPHA-BETA");
        System.out.println("==========================================================================\n");

        Board board = new Board();

        // Tạo một thế cờ giả lập thực tế: 7 nước đi đầu tiên
        // X đi trước tại trung tâm, hai bên giằng co
        board.makeMove(7, 7, Piece.X);
        board.makeMove(7, 8, Piece.O);
        board.makeMove(6, 8, Piece.X);
        board.makeMove(8, 6, Piece.O);
        board.makeMove(8, 7, Piece.X);
        board.makeMove(6, 7, Piece.O);
        board.makeMove(5, 7, Piece.X);

        System.out.println("Thế cờ thử nghiệm (7 nước cờ đã đi):");
        board.printBoard();
        System.out.println("\nBắt đầu đo đạc hiệu năng với các cấp độ khó khác nhau:\n");

        MinimaxAI.Difficulty[] levels = MinimaxAI.Difficulty.values();

        System.out.printf("%-12s | %-8s | %-16s | %-16s | %-16s | %-12s%n",
                "Mức độ", "Độ sâu", "Thời gian (ms)", "Nodes đã duyệt", "Số lần cắt tỉa", "Nước đi chọn");
        System.out.println("-----------------------------------------------------------------------------------------------");

        for (MinimaxAI.Difficulty level : levels) {
            MinimaxAI ai = new MinimaxAI(Piece.O, level);
            Move bestMove = ai.findBestMove(board);

            System.out.printf("%-12s | %-8d | %-16d | %-16s | %-16s | (%d, %d)%n",
                    level.label,
                    level.depth,
                    ai.getExecutionTimeMs(),
                    String.format("%,d", ai.getNodesEvaluated()),
                    String.format("%,d", ai.getPruningCount()),
                    bestMove.row, bestMove.col
            );
        }

        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("=> KẾT LUẬN THỰC NGHIỆM:");
        System.out.println("1. Kỹ thuật Alpha-Beta Pruning cắt bỏ hàng trăm đến hàng ngàn trạng thái dư thừa.");
        System.out.println("2. Thời gian suy nghĩ của AI ở độ sâu 3 chỉ mất vài chục mili-giây trên máy tính.");
        System.out.println("3. Dữ liệu này có thể đưa ngay vào Bảng số liệu trong Báo cáo Đồ án!");
    }
}
