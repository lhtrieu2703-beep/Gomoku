package game.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Quản lý thành tích ván đấu (Thắng / Thua / Hòa), độc lập hoàn toàn khỏi GUI Swing.
 * Đảm bảo mỗi ván (định danh bởi gameId) chỉ được ghi nhận kết quả ĐẦU TIÊN duy nhất một lần.
 * Undo hay các hành động trong cùng ván không làm thay đổi hay ghi đè thành tích đã ghi.
 */
public class ScoreTracker {
    private int humanWins = 0;
    private int aiWins = 0;
    private int draws = 0;
    private final Set<Integer> recordedGameIds = new HashSet<>();

    public ScoreTracker() {}

    /**
     * Ghi nhận kết quả của một ván đấu.
     * @param gameId Định danh ván đấu
     * @param winner Quân cờ thắng (Piece.X, Piece.O hoặc Piece.NONE nếu hòa)
     * @param humanPiece Quân cờ của người chơi (Piece.X trong PvP/PvAI, hoặc myPiece trong Online)
     * @return true nếu kết quả được ghi nhận mới, false nếu gameId này đã được ghi nhận trước đó.
     */
    public boolean recordResult(int gameId, Piece winner, Piece humanPiece) {
        if (recordedGameIds.contains(gameId)) {
            return false;
        }
        recordedGameIds.add(gameId);

        if (winner == Piece.NONE) {
            draws++;
        } else if (winner == humanPiece) {
            humanWins++;
        } else {
            aiWins++;
        }
        return true;
    }

    /**
     * Ghi nhận kết quả cho người chơi mặc định là Piece.X.
     */
    public boolean recordResult(int gameId, Piece winner) {
        return recordResult(gameId, winner, Piece.X);
    }

    /**
     * Chuẩn bị cho ván mới (tuân thủ giao thức yêu cầu).
     */
    public void newGame() {
        // Giữ nguyên lịch sử thành tích các ván trước, không xóa dữ liệu ngoài cơ chế
    }

    public boolean isGameRecorded(int gameId) {
        return recordedGameIds.contains(gameId);
    }

    public int getHumanWins() {
        return humanWins;
    }

    public int getAiWins() {
        return aiWins;
    }

    public int getDraws() {
        return draws;
    }

    public void resetScores() {
        humanWins = 0;
        aiWins = 0;
        draws = 0;
        recordedGameIds.clear();
    }
}
