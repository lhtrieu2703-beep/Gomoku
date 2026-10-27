package game.ai;

import game.model.Board;
import game.model.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Bộ đánh giá thế cờ Heuristic cho 3 cấp độ:
 * - Hỗ trợ cơ chế đánh giá cơ bản (cho mức Dễ: chỉ nhìn chuỗi liền kề).
 * - Đánh giá nâng cao (cho Trung bình & Khó: Cửa sổ trượt 5-6 ô, nhận diện thế ngắt quãng và bẫy đôi).
 * - Tuân thủ Luật Chặn Hai Đầu và phân biệt chuẩn RULE A (Đúng 5) vs RULE B (5 trở lên).
 */
public class HeuristicEvaluator {
    public static final int FIVE = 100_000_000;         // 5 quân: Thắng cuộc
    public static final int OPEN_FOUR = 10_000_000;      // 4 quân hở 2 đầu: Nước sau chắc chắn thắng
    public static final int DOUBLE_THREE = 2_000_000;    // Bẫy 3 đôi (2 đường 3 mở cùng lúc): Thế cờ sát thủ
    public static final int BLOCKED_FOUR = 200_000;      // 4 quân bị chặn 1 đầu / 4 quân có lỗ hổng
    public static final int OPEN_THREE = 120_000;        // 3 quân hở 2 đầu
    public static final int BLOCKED_THREE = 8_000;       // 3 quân chặn 1 đầu
    public static final int OPEN_TWO = 1_500;            // 2 quân hở 2 đầu
    public static final int BLOCKED_TWO = 150;
    public static final int MAX_NON_TERMINAL = FIVE / 4;

    public static int evaluateBoardAdvanced(Board board, Piece aiPiece) {
        Piece humanPiece = aiPiece.opposite();

        int aiScore = evaluatePlayerAdvanced(board, aiPiece);
        int humanScore = evaluatePlayerAdvanced(board, humanPiece);

        if (aiScore == FIVE) return FIVE;
        if (humanScore == FIVE) return -FIVE;
        return Math.max(-FIVE + 1, Math.min(FIVE - 1, aiScore - (int) (humanScore * 1.3)));
    }

    public static int evaluateBoardSimple(Board board, Piece aiPiece) {
        Piece humanPiece = aiPiece.opposite();

        int aiScore = evaluatePlayerSimple(board, aiPiece);
        int humanScore = evaluatePlayerSimple(board, humanPiece);

        return aiScore - humanScore;
    }

    public static int evaluatePlayerAdvanced(Board board, Piece player) {
        int totalScore = 0;
        int openThreeCount = 0;
        Board.WinRule rule = board.getWinRule();

        // 1. Quét 15 hàng ngang
        for (int r = 0; r < Board.SIZE; r++) {
            Piece[] line = new Piece[Board.SIZE];
            for (int c = 0; c < Board.SIZE; c++) {
                line[c] = board.getPiece(r, c);
            }
            int[] res = evaluateLineAdvanced(line, player, rule);
            if (res[0] == FIVE) return FIVE;
            totalScore = Math.min(MAX_NON_TERMINAL, totalScore + res[0]);
            openThreeCount += res[1];
        }

        // 2. Quét 15 cột dọc
        for (int c = 0; c < Board.SIZE; c++) {
            Piece[] line = new Piece[Board.SIZE];
            for (int r = 0; r < Board.SIZE; r++) {
                line[r] = board.getPiece(r, c);
            }
            int[] res = evaluateLineAdvanced(line, player, rule);
            if (res[0] == FIVE) return FIVE;
            totalScore = Math.min(MAX_NON_TERMINAL, totalScore + res[0]);
            openThreeCount += res[1];
        }

        // 3. Quét các đường chéo chính (\)
        for (int k = 0; k <= 2 * (Board.SIZE - 1); k++) {
            List<Piece> diag = new ArrayList<>();
            for (int r = 0; r < Board.SIZE; r++) {
                int c = k - (Board.SIZE - 1) + r;
                if (c >= 0 && c < Board.SIZE) {
                    diag.add(board.getPiece(r, c));
                }
            }
            if (diag.size() >= 5) {
                int[] res = evaluateLineAdvanced(diag.toArray(new Piece[0]), player, rule);
                if (res[0] == FIVE) return FIVE;
                totalScore = Math.min(MAX_NON_TERMINAL, totalScore + res[0]);
                openThreeCount += res[1];
            }
        }

        // 4. Quét các đường chéo phụ (/)
        for (int k = 0; k <= 2 * (Board.SIZE - 1); k++) {
            List<Piece> diag = new ArrayList<>();
            for (int r = 0; r < Board.SIZE; r++) {
                int c = k - r;
                if (c >= 0 && c < Board.SIZE) {
                    diag.add(board.getPiece(r, c));
                }
            }
            if (diag.size() >= 5) {
                int[] res = evaluateLineAdvanced(diag.toArray(new Piece[0]), player, rule);
                if (res[0] == FIVE) return FIVE;
                totalScore = Math.min(MAX_NON_TERMINAL, totalScore + res[0]);
                openThreeCount += res[1];
            }
        }

        if (openThreeCount >= 2) {
            totalScore += DOUBLE_THREE;
        }

        return Math.min(MAX_NON_TERMINAL, totalScore);
    }

    private static int[] evaluateLineAdvanced(Piece[] line, Piece player, Board.WinRule rule) {
        Piece opponent = player.opposite();
        // Evaluate complete runs, never a five-cell fragment inside an overline.
        for (int i = 0; i < line.length;) {
            if (line[i] != player) { i++; continue; }
            int start = i;
            while (i < line.length && line[i] == player) i++;
            int length = i - start;
            boolean blockedBefore = start > 0 && line[start - 1] == opponent;
            boolean blockedAfter = i < line.length && line[i] == opponent;
            if ((rule == Board.WinRule.RULE_A ? length == 5 : length >= 5)
                    && !(blockedBefore && blockedAfter)) return new int[]{FIVE, 0};
        }
        int score = 0;
        Set<Integer> openThrees = new HashSet<>(), openFours = new HashSet<>(), openTwos = new HashSet<>();
        for (int i = 0; i + 6 <= line.length; i++) {
            if (line[i] != Piece.NONE || line[i + 5] != Piece.NONE) continue;
            int mask = 0, count = 0; boolean blocked = false;
            for (int j = i + 1; j < i + 5; j++) {
                if (line[j] == opponent) { blocked = true; break; }
                if (line[j] == player) { mask |= 1 << j; count++; }
            }
            if (blocked) continue;
            if (count == 4 && openFours.add(mask)) score += OPEN_FOUR;
            if (count == 3 && openThrees.add(mask)) score += OPEN_THREE;
            if (count == 2 && openTwos.add(mask)) score += OPEN_TWO;
        }
        Set<Integer> fours = new HashSet<>(), threes = new HashSet<>(), twos = new HashSet<>();
        for (int i = 0; i + 5 <= line.length; i++) {
            int mask = 0, count = 0; boolean blocked = false;
            for (int j = i; j < i + 5; j++) {
                if (line[j] == opponent) { blocked = true; break; }
                if (line[j] == player) { mask |= 1 << j; count++; }
            }
            if (blocked) continue;
            if (count == 4 && fours.add(mask)) score += BLOCKED_FOUR;
            if (count == 3 && threes.add(mask)) score += BLOCKED_THREE;
            if (count == 2 && twos.add(mask)) score += BLOCKED_TWO;
        }
        return new int[]{Math.min(MAX_NON_TERMINAL, score), openThrees.size()};
    }
    private static int evaluatePlayerSimple(Board board, Piece player) {
        int totalScore = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getPiece(r, c) == player) {
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

    public static int evaluateMove(Board board, int r, int c, Piece player, boolean advanced) {
        board.makeMove(r, c, player);
        int myScore = advanced ? evaluatePlayerAdvanced(board, player) : evaluatePlayerSimple(board, player);
        board.undoMove(r, c);

        Piece opp = player.opposite();
        board.makeMove(r, c, opp);
        int oppScore = advanced ? evaluatePlayerAdvanced(board, opp) : evaluatePlayerSimple(board, opp);
        board.undoMove(r, c);

        if (advanced && myScore == FIVE) return 2 * FIVE;
        if (advanced && oppScore == FIVE) return FIVE;
        return Math.min(FIVE - 1, myScore + (int)(oppScore * 1.25));
    }
}
