package game.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý thành tích ván đấu (Thắng / Thua / Hòa), độc lập hoàn toàn khỏi GUI Swing.
 * Mỗi gameId chỉ có một kết quả có hiệu lực; mở lại ván bằng Undo thu hồi kết quả đó.
 */
public class ScoreTracker {
    private int humanWins = 0;
    private int aiWins = 0;
    private int draws = 0;
    private enum Result { HUMAN, AI, DRAW, CLEARED }
    private final Map<Integer, Result> recordedResults = new HashMap<>();

    public ScoreTracker() {}

    /**
     * Ghi nhận kết quả của một ván đấu.
     * @param gameId Định danh ván đấu
     * @param winner Quân cờ thắng (Piece.X, Piece.O hoặc Piece.NONE nếu hòa)
     * @param humanPiece Quân cờ của người chơi (Piece.X trong PvP/PvAI, hoặc myPiece trong Online)
     * @return true nếu kết quả được ghi nhận mới, false nếu gameId này đã được ghi nhận trước đó.
     */
    public boolean recordResult(int gameId, Piece winner, Piece humanPiece) {
        if (recordedResults.containsKey(gameId)) {
            return false;
        }
        recordedResults.put(gameId, winner == Piece.NONE ? Result.DRAW
                : winner == humanPiece ? Result.HUMAN : Result.AI);

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

    /** Remove an effective result so the reopened game can finish again. */
    public boolean revokeResult(int gameId) {
        Result result = recordedResults.remove(gameId);
        if (result == null) return false;
        switch (result) {
            case HUMAN -> humanWins--;
            case AI -> aiWins--;
            case DRAW -> draws--;
            case CLEARED -> { } // resetScores already removed this result from the totals.
        }
        return true;
    }

    /**
     * Chuẩn bị cho ván mới (tuân thủ giao thức yêu cầu).
     */
    public void newGame() {
        // Giữ nguyên lịch sử thành tích các ván trước, không xóa dữ liệu ngoài cơ chế
    }

    public boolean isGameRecorded(int gameId) {
        return recordedResults.containsKey(gameId);
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
        recordedResults.replaceAll((id, result) -> Result.CLEARED);
        // Keep recorded IDs until explicitly revoked; resetting totals alone
        // must not allow an already recorded game to be counted again.
    }
}
