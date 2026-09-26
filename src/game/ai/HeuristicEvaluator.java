package game.ai;

import game.model.Board;
import game.model.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Bộ đánh giá thế cờ Heuristic cho 3 cấp độ:
 * - Hỗ trợ cả đánh giá cơ bản (cho mức Dễ: chỉ nhìn chuỗi liền kề).
 * - Đánh giá nâng cao (cho Trung bình & Khó: Cửa sổ trượt 5-6 ô, nhận diện thế ngắt quãng và bẫy đôi).
 */
public class HeuristicEvaluator {
    // Thang điểm chuẩn Gomoku
    public static final int FIVE = 100_000_000;         // 5 quân: Thắng cuộc
    public static final int OPEN_FOUR = 10_000_000;      // 4 quân hở 2 đầu: Nước sau chắc chắn thắng
    public static final int DOUBLE_THREE = 2_000_000;    // Bẫy 3 đôi (2 đường 3 mở cùng lúc): Thế cờ sát thủ
    public static final int BLOCKED_FOUR = 200_000;      // 4 quân bị chặn 1 đầu / 4 quân có lỗ hổng
    public static final int OPEN_THREE = 120_000;        // 3 quân hở 2 đầu
    public static final int BLOCKED_THREE = 8_000;       // 3 quân chặn 1 đầu
    public static final int OPEN_TWO = 1_500;            // 2 quân hở 2 đầu
    public static final int BLOCKED_TWO = 150;

    /**
     * Đánh giá bàn cờ nâng cao (Dùng cho mức TRUNG BÌNH và KHÓ).
     * Nhận diện thế cờ ngắt quãng, thế hở 2 đầu và bẫy đôi.
     */
    public static int evaluateBoardAdvanced(Board board, Piece aiPiece) {
        Piece humanPiece = aiPiece.opposite();

        int aiScore = evaluatePlayerAdvanced(board, aiPiece);
        int humanScore = evaluatePlayerAdvanced(board, humanPiece);

        // Hệ số phòng thủ 1.3: Ưu tiên bẻ gãy đợt công của người chơi
        return aiScore - (int) (humanScore * 1.3);
    }

    /**
     * Đánh giá bàn cờ cơ bản (Dành riêng cho mức DỄ).
     * Chỉ quét các quân đứng liền kề, "mù" trước các thế ngắt quãng.
     */
    public static int evaluateBoardSimple(Board board, Piece aiPiece) {
        Piece humanPiece = aiPiece.opposite();

        int aiScore = evaluatePlayerSimple(board, aiPiece);
        int humanScore = evaluatePlayerSimple(board, humanPiece);

        return aiScore - humanScore;
    }

    // =========================================================================
    // ĐÁNH GIÁ NÂNG CAO (SLIDING WINDOW 5 & 6 Ô)
    // =========================================================================
    public static int evaluatePlayerAdvanced(Board board, Piece player) {
        int totalScore = 0;
        int openThreeCount = 0;

        // Quét 15 hàng ngang
        for (int r = 0; r < Board.SIZE; r++) {
            Piece[] line = new Piece[Board.SIZE];
            for (int c = 0; c < Board.SIZE; c++) {
                line[c] = board.getPiece(r, c);
            }
            int[] res = evaluateLineAdvanced(line, player);
            totalScore += res[0];
            openThreeCount += res[1];
        }

        // Quét 15 cột dọc
        for (int c = 0; c < Board.SIZE; c++) {
            Piece[] line = new Piece[Board.SIZE];
            for (int r = 0; r < Board.SIZE; r++) {
                line[r] = board.getPiece(r, c);
            }
            int[] res = evaluateLineAdvanced(line, player);
            totalScore += res[0];
            openThreeCount += res[1];
        }

        // Quét các đường chéo chính (\)
        for (int k = 0; k <= 2 * (Board.SIZE - 1); k++) {
            List<Piece> diag = new ArrayList<>();
            for (int r = 0; r < Board.SIZE; r++) {
                int c = k - (Board.SIZE - 1) + r;
                if (c >= 0 && c < Board.SIZE) {
                    diag.add(board.getPiece(r, c));
                }
            }
            if (diag.size() >= 5) {
                int[] res = evaluateLineAdvanced(diag.toArray(new Piece[0]), player);
                totalScore += res[0];
                openThreeCount += res[1];
            }
        }

        // Quét các đường chéo phụ (/)
        for (int k = 0; k <= 2 * (Board.SIZE - 1); k++) {
            List<Piece> diag = new ArrayList<>();
            for (int r = 0; r < Board.SIZE; r++) {
                int c = k - r;
                if (c >= 0 && c < Board.SIZE) {
                    diag.add(board.getPiece(r, c));
                }
            }
            if (diag.size() >= 5) {
                int[] res = evaluateLineAdvanced(diag.toArray(new Piece[0]), player);
                totalScore += res[0];
                openThreeCount += res[1];
            }
        }

        // Thưởng điểm cực lớn cho bẫy 3 đôi (Double-Three Trap: tạo được 2 đường 3 mở cùng lúc)
        if (openThreeCount >= 2) {
            totalScore += DOUBLE_THREE;
        }

        return totalScore;
    }

    /**
     * Trả về mảng 2 phần tử: [Điểm số đường thẳng, Số lượng thế 3 mở]
     */
    private static int[] evaluateLineAdvanced(Piece[] line, Piece player) {
        int score = 0;
        int openThreeCount = 0;
        int n = line.length;
        Piece opp = player.opposite();

        // 1. Quét cửa sổ 6 ô: Bắt các thế hở 2 đầu _XXXX_, _XXX_, _XX_
        for (int i = 0; i <= n - 6; i++) {
            int pCount = 0;
            int oppCount = 0;
            for (int j = 0; j < 6; j++) {
                if (line[i + j] == player) pCount++;
                else if (line[i + j] == opp) oppCount++;
            }

            if (oppCount == 0 && line[i] == Piece.NONE && line[i + 5] == Piece.NONE) {
                if (pCount == 4) {
                    score += OPEN_FOUR;
                } else if (pCount == 3) {
                    score += OPEN_THREE;
                    openThreeCount++;
                } else if (pCount == 2) {
                    score += OPEN_TWO;
                }
            }
        }

        // 2. Quét cửa sổ 5 ô: Bắt các thế 5 liên tiếp hoặc thế bị chặn 1 đầu
        for (int i = 0; i <= n - 5; i++) {
            int pCount = 0;
            int oppCount = 0;
            for (int j = 0; j < 5; j++) {
                if (line[i + j] == player) pCount++;
                else if (line[i + j] == opp) oppCount++;
            }

            if (oppCount == 0) {
                if (pCount == 5) {
                    score += FIVE;
                } else if (pCount == 4) {
                    score += BLOCKED_FOUR;
                } else if (pCount == 3) {
                    score += BLOCKED_THREE;
                } else if (pCount == 2) {
                    score += BLOCKED_TWO;
                }
            }
        }

        return new int[]{score, openThreeCount};
    }

    // =========================================================================
    // ĐÁNH GIÁ CƠ BẢN (CHỈ DÀNH CHO MỨC DỄ)
    // =========================================================================
    private static int evaluatePlayerSimple(Board board, Piece player) {
        int totalScore = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getPiece(r, c) == player) {
                    // Đơn giản chỉ đếm quân liền kề trong bán kính hẹp
                    totalScore += countAdjacentPieces(board, r, c, player) * 100;
                }
            }
        }
        return totalScore;
    }

    private static int countAdjacentPieces(Board board, int r, int c, Piece player) {
        int count = 0;
        int[][] dirs = {{0,1}, {1,0}, {1,1}, {1,-1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (board.isInBounds(nr, nc) && board.getPiece(nr, nc) == player) {
                count++;
            }
        }
        return count;
    }

    /**
     * Chấm điểm tiềm năng nhanh cho một ô đi (Move Ordering).
     */
    public static int evaluateMove(Board board, int r, int c, Piece player, boolean advanced) {
        board.makeMove(r, c, player);
        int myScore = advanced ? evaluatePlayerAdvanced(board, player) : evaluatePlayerSimple(board, player);
        board.undoMove(r, c);

        Piece opp = player.opposite();
        board.makeMove(r, c, opp);
        int oppScore = advanced ? evaluatePlayerAdvanced(board, opp) : evaluatePlayerSimple(board, opp);
        board.undoMove(r, c);

        return myScore + (int)(oppScore * 1.25);
    }
}
