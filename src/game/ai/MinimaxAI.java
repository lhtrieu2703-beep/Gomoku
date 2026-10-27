package game.ai;

import game.model.Board;
import game.model.Piece;

import java.util.*;
import java.util.concurrent.CancellationException;

/**
 * Thuật toán Minimax + Alpha-Beta Pruning phân cấp đồng đều cho 3 cấp độ:
 * - DỄ (Easy): Tập sự, depth=1, chỉ nhìn quân liền kề, chọn ngẫu nhiên top 3.
 * - TRUNG BÌNH (Medium): Thực chiến, depth=2, nhận diện thế ngắt quãng, chặn Open 4 khẩn cấp, 16 nhánh.
 * - KHÓ (Hard): Kiện tướng, depth=3, nhận diện bẫy đôi (Double-Three), 22 nhánh, tính toán sâu.
 */
public class MinimaxAI {
    public enum Difficulty {
        EASY(1, "Dễ"),
        MEDIUM(2, "Trung bình"),
        HARD(3, "Khó");

        public final int depth;
        public final String label;

        Difficulty(int depth, String label) {
            this.depth = depth;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private volatile Difficulty difficulty;
    private final Piece aiPiece;
    private final Random random = new Random();

    private long nodesEvaluated;
    private long pruningCount;
    private long executionTimeMs;

    public MinimaxAI(Piece aiPiece, Difficulty difficulty) {
        this.aiPiece = aiPiece;
        this.difficulty = difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public long getNodesEvaluated() {
        return nodesEvaluated;
    }

    public long getPruningCount() {
        return pruningCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Tìm nước đi tối ưu nhất theo từng cấp độ khó.
     */
    public Move findBestMove(Board board) {
        checkCancelled();
        final Difficulty level = difficulty;
        long startTime = System.currentTimeMillis();
        nodesEvaluated = 0;
        pruningCount = 0;

        // Nếu bàn cờ trống, đánh vào ô trung tâm (7, 7)
        if (board.getMoveCount() == 0) {
            executionTimeMs = System.currentTimeMillis() - startTime;
            return new Move(Board.SIZE / 2, Board.SIZE / 2, 0);
        }

        Piece humanPiece = aiPiece.opposite();
        boolean isAdvanced = (level != Difficulty.EASY);
        List<Move> candidateMoves = getCandidateMoves(board, aiPiece, isAdvanced);

        if (candidateMoves.isEmpty()) {
            return null;
        }

        // =====================================================================
        // 1. XỬ LÝ RIÊNG CHO MỨC DỄ (EASY)
        // =====================================================================
        if (level == Difficulty.EASY) {
            // Thỉnh thoảng vẫn biết chặn nếu đối thủ sắp thắng 5 quân (70% tỉ lệ chặn)
            for (Move move : candidateMoves) {
                checkCancelled();
                board.makeMove(move.row, move.col, humanPiece);
                boolean oppWin = board.checkWin(move.row, move.col);
                board.undoMove(move.row, move.col);
                if (oppWin && random.nextDouble() < 0.70) {
                    executionTimeMs = System.currentTimeMillis() - startTime;
                    return move;
                }
            }
            // Chọn ngẫu nhiên trong top 3 nước đi tiềm năng cơ bản
            int pick = random.nextInt(Math.min(3, candidateMoves.size()));
            executionTimeMs = System.currentTimeMillis() - startTime;
            return candidateMoves.get(pick);
        }

        // =====================================================================
        // 2. PHẢN XẠ KHẨN CẤP CHO MỨC TRUNG BÌNH & KHÓ
        // =====================================================================

        // A. Nếu AI có nước thắng ngay (5 quân) -> Đánh ngay!
        for (Move move : candidateMoves) {
            checkCancelled();
            board.makeMove(move.row, move.col, aiPiece);
            boolean win = board.checkWin(move.row, move.col);
            board.undoMove(move.row, move.col);
            if (win) {
                executionTimeMs = System.currentTimeMillis() - startTime;
                return move;
            }
        }

        // B. Nếu đối thủ có nước thắng ngay (5 quân) -> BẮT BUỘC CHẶN NGAY!
        for (Move move : candidateMoves) {
            checkCancelled();
            board.makeMove(move.row, move.col, humanPiece);
            boolean oppWin = board.checkWin(move.row, move.col);
            board.undoMove(move.row, move.col);
            if (oppWin) {
                executionTimeMs = System.currentTimeMillis() - startTime;
                return move;
            }
        }

        // C. Chặn thế "4 quân mở 2 đầu" (Open 4) của đối thủ
        Move blockOpen4 = null;
        int maxOppThreat = 0;
        for (Move move : candidateMoves) {
            checkCancelled();
            board.makeMove(move.row, move.col, humanPiece);
            int oppScore = HeuristicEvaluator.evaluatePlayerAdvanced(board, humanPiece);
            board.undoMove(move.row, move.col);

            if (oppScore >= HeuristicEvaluator.OPEN_FOUR) {
                if (oppScore > maxOppThreat) {
                    maxOppThreat = oppScore;
                    blockOpen4 = move;
                }
            }
        }
        if (blockOpen4 != null) {
            executionTimeMs = System.currentTimeMillis() - startTime;
            return blockOpen4;
        }

        // D. Nếu AI có thể tạo ra thế Open 4 -> Đánh ngay để đảm bảo thắng lợi!
        for (Move move : candidateMoves) {
            checkCancelled();
            board.makeMove(move.row, move.col, aiPiece);
            int myScore = HeuristicEvaluator.evaluatePlayerAdvanced(board, aiPiece);
            board.undoMove(move.row, move.col);

            if (myScore >= HeuristicEvaluator.OPEN_FOUR) {
                executionTimeMs = System.currentTimeMillis() - startTime;
                return move;
            }
        }

        // E. (DÀNH RIÊNG CHO MỨC KHÓ): Nhận diện và chặn Bẫy 3 đôi (Double-Three Fork)
        if (level == Difficulty.HARD) {
            // AI tạo bẫy 3 đôi nếu có cơ hội
            for (Move move : candidateMoves) {
                checkCancelled();
                board.makeMove(move.row, move.col, aiPiece);
                int myScore = HeuristicEvaluator.evaluatePlayerAdvanced(board, aiPiece);
                board.undoMove(move.row, move.col);
                if (myScore >= HeuristicEvaluator.DOUBLE_THREE) {
                    executionTimeMs = System.currentTimeMillis() - startTime;
                    return move;
                }
            }
            // AI chặn đối thủ tạo bẫy 3 đôi
            for (Move move : candidateMoves) {
                checkCancelled();
                board.makeMove(move.row, move.col, humanPiece);
                int oppScore = HeuristicEvaluator.evaluatePlayerAdvanced(board, humanPiece);
                board.undoMove(move.row, move.col);
                if (oppScore >= HeuristicEvaluator.DOUBLE_THREE) {
                    executionTimeMs = System.currentTimeMillis() - startTime;
                    return move;
                }
            }
        }

        // =====================================================================
        // 3. THUẬT TOÁN MINIMAX + ALPHA-BETA PRUNING
        // =====================================================================
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Phân cấp số lượng nhánh duyệt:
        // Trung bình: 16 nhánh (đủ bao quát, phản hồi ~30ms)
        // Khó: 22 nhánh (sâu và toàn diện, phản hồi ~150ms)
        int maxCandidates = (level == Difficulty.HARD) ? 22 : 16;
        int branchCount = Math.min(candidateMoves.size(), maxCandidates);

        for (int i = 0; i < branchCount; i++) {
            checkCancelled();
            Move move = candidateMoves.get(i);
            board.makeMove(move.row, move.col, aiPiece);

            int value;
            try {
                value = minimax(board, level.depth - 1, alpha, beta, false, level);
            } finally {
                board.undoMove(move.row, move.col);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }

            alpha = Math.max(alpha, bestValue);
            if (beta <= alpha) {
                pruningCount++;
                break;
            }
        }

        executionTimeMs = System.currentTimeMillis() - startTime;
        return (bestMove != null) ? bestMove : candidateMoves.get(0);
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing, Difficulty level) {
        checkCancelled();
        nodesEvaluated++;

        if (depth == 0 || board.isFull()) {
            return HeuristicEvaluator.evaluateBoardAdvanced(board, aiPiece);
        }

        Piece currentPiece = isMaximizing ? aiPiece : aiPiece.opposite();
        List<Move> candidateMoves = getCandidateMoves(board, currentPiece, true);

        if (candidateMoves.isEmpty()) {
            return HeuristicEvaluator.evaluateBoardAdvanced(board, aiPiece);
        }

        int maxCandidates = (level == Difficulty.HARD) ? 14 : 10;
        int branchCount = Math.min(candidateMoves.size(), maxCandidates);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < branchCount; i++) {
                checkCancelled();
                Move move = candidateMoves.get(i);
                board.makeMove(move.row, move.col, aiPiece);

                int eval;
                try {
                    if (board.checkWin(move.row, move.col)) return HeuristicEvaluator.FIVE;
                    eval = minimax(board, depth - 1, alpha, beta, false, level);
                } finally {
                    board.undoMove(move.row, move.col);
                }

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    pruningCount++;
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            Piece humanPiece = aiPiece.opposite();
            for (int i = 0; i < branchCount; i++) {
                checkCancelled();
                Move move = candidateMoves.get(i);
                board.makeMove(move.row, move.col, humanPiece);

                int eval;
                try {
                    if (board.checkWin(move.row, move.col)) return -HeuristicEvaluator.FIVE;
                    eval = minimax(board, depth - 1, alpha, beta, true, level);
                } finally {
                    board.undoMove(move.row, move.col);
                }

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    pruningCount++;
                    break;
                }
            }
            return minEval;
        }
    }

    private List<Move> getCandidateMoves(Board board, Piece player, boolean advanced) {
        boolean[][] visited = new boolean[Board.SIZE][Board.SIZE];
        List<Move> candidates = new ArrayList<>();

        for (int r = 0; r < Board.SIZE; r++) {
            checkCancelled();
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getPiece(r, c) != Piece.NONE) {
                    for (int dr = -2; dr <= 2; dr++) {
                        for (int dc = -2; dc <= 2; dc++) {
                            int nr = r + dr;
                            int nc = c + dc;
                            if (board.isValidMove(nr, nc) && !visited[nr][nc]) {
                                checkCancelled();
                                visited[nr][nc] = true;
                                int score = HeuristicEvaluator.evaluateMove(board, nr, nc, player, advanced);
                                candidates.add(new Move(nr, nc, score));
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(candidates);
        return candidates;
    }

    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException("AI search cancelled");
    }
}
