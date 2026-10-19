package game;

import game.ai.Move;
import game.model.Board;
import game.model.Piece;
import game.model.ScoreTracker;
import game.view.BoardPanel;
import game.view.SoundManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử JUnit 5 cho Parity của Undo trong chế độ PV_AI.
 * Đảm bảo:
 * - Khi nước cuối là của X (X thắng hoặc nước thứ 225 hòa), Undo chỉ rút 1 nước của X.
 * - Khi nước cuối là của O (AI), Undo rút 2 nước (cả AI và người).
 */
public class UndoParityTest {

    private Board board;
    private BoardPanel panel;
    private ScoreTracker tracker;

    @BeforeAll
    public static void initAll() {
        SoundManager.setSoundEnabled(false);
    }

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        panel = new BoardPanel(board);
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        tracker = new ScoreTracker();

        panel.setGameStateListener(new BoardPanel.GameStateListener() {
            @Override
            public void onTurnChanged(Piece currentTurn, boolean isAiThinking) {}

            @Override
            public void onGameOver(String message, Piece winner) {
                onGameOver(panel.getGameId(), message, winner);
            }

            @Override
            public void onGameOver(int gameId, String message, Piece winner) {
                Piece humanPiece = (panel.getGameMode() == BoardPanel.GameMode.ONLINE) ? panel.getMyPiece() : Piece.X;
                tracker.recordResult(gameId, winner, humanPiece);
            }

            @Override
            public void onAiStatsUpdated(long timeMs, long nodes, long prunings) {}

            @Override
            public void onHistoryChanged(boolean canUndo) {}
        });
    }

    private static Piece getDrawPatternPiece(int r, int c) {
        int phase = (r % 2 == 0) ? 0 : 2;
        int pattern = (c + phase) % 4;
        return (pattern == 0 || pattern == 1) ? Piece.X : Piece.O;
    }

    @Test
    @DisplayName("P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X")
    public void testP1_XWin_UndoOneMove() {
        // Đánh 9 nước: X(7,7) O(0,0) X(7,8) O(0,1) X(7,9) O(0,2) X(7,10) O(0,3) X(7,11)
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(7, 8, Piece.X);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(7, 9, Piece.X);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(7, 10, Piece.X);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(7, 11, Piece.X); // X thắng

        assertTrue(panel.isGameOver(), "Ván cờ phải kết thúc do X thắng");
        assertEquals(1, tracker.getHumanWins(), "Người chơi được ghi nhận 1 trận thắng (1-0)");
        assertEquals(0, tracker.getAiWins());

        // Thực hiện Undo
        panel.undoMove();

        List<Move> history = panel.getMoveHistory();
        assertEquals(8, history.size(), "Kỳ vọng rút 1 nước của X, moveHistory phải còn 8 nước");

        Move lastM = history.get(history.size() - 1);
        assertEquals(0, lastM.row);
        assertEquals(3, lastM.col);
        assertEquals(Piece.O, board.getPiece(lastM.row, lastM.col), "Nước cuối cùng còn lại phải là của O(0,3)");

        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt tiếp theo phải là của X");
        assertFalse(panel.isGameOver(), "isGameOver phải trở về false");
        assertTrue(panel.canUndo(), "Vẫn còn nước trong lịch sử, canUndo phải là true");
        assertEquals(Piece.NONE, board.getPiece(7, 11), "Ô (7,11) của nước thắng phải được xóa sạch");

        // Điểm ScoreTracker không đổi (1-0 cho người)
        assertEquals(1, tracker.getHumanWins(), "Điểm thắng của người chơi không đổi");
        assertEquals(0, tracker.getAiWins());
        assertEquals(0, tracker.getDraws());
    }

    @Test
    @DisplayName("P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi")
    public void testP1b_PlayAgainAfterUndo_ScoreUnchanged() {
        // Đánh 9 nước để X thắng
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(7, 8, Piece.X);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(7, 9, Piece.X);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(7, 10, Piece.X);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(7, 11, Piece.X);

        int initialGameId = panel.getGameId();
        assertEquals(1, tracker.getHumanWins());

        // Undo
        panel.undoMove();
        assertEquals(initialGameId, panel.getGameId(), "Undo không đổi gameId");

        // Người chơi đi lại vào (7,11)
        panel.executeMoveForTest(7, 11, Piece.X);

        assertTrue(panel.isGameOver(), "Ván cờ kết thúc lần 2");
        assertEquals(initialGameId, panel.getGameId(), "gameId không đổi");
        assertEquals(1, tracker.getHumanWins(), "Điểm thắng vẫn là 1, không được tăng trùng");
        assertEquals(0, tracker.getAiWins());
        assertEquals(0, tracker.getDraws());
    }

    @Test
    @DisplayName("P2: X(7,7), undoMove(): size 0, lượt X")
    public void testP2_OnlyOneMove_Undo() {
        panel.executeMoveForTest(7, 7, Piece.X);
        assertEquals(1, panel.getMoveHistory().size());

        panel.undoMove();

        assertEquals(0, panel.getMoveHistory().size(), "Undo nước duy nhất -> history rỗng");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt cờ bắt đầu lại từ Piece.X");
        assertEquals(Piece.NONE, board.getPiece(7, 7));
    }

    @Test
    @DisplayName("P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X")
    public void testP3_XAndO_UndoTwoMoves() {
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        assertEquals(2, panel.getMoveHistory().size());

        panel.undoMove();

        assertEquals(0, panel.getMoveHistory().size(), "AI vừa đi -> Undo rút cả 2 nước");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt cờ trả về Piece.X");
        assertEquals(Piece.NONE, board.getPiece(7, 7));
        assertEquals(Piece.NONE, board.getPiece(0, 0));
    }

    @Test
    @DisplayName("P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X")
    public void testP4_DrawAtMove225_UndoOneMove() {
        // Phân loại các ô theo mẫu hòa không tạo 5 quân liên tiếp
        List<int[]> xList = new ArrayList<>();
        List<int[]> oList = new ArrayList<>();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (getDrawPatternPiece(r, c) == Piece.X) {
                    xList.add(new int[]{r, c});
                } else {
                    oList.add(new int[]{r, c});
                }
            }
        }

        assertEquals(113, xList.size(), "Mẫu hòa chuẩn có 113 quân X");
        assertEquals(112, oList.size(), "Mẫu hòa chuẩn có 112 quân O");

        // Đánh 224 nước đầu luân phiên X và O
        for (int i = 0; i < 112; i++) {
            panel.executeMoveForTest(xList.get(i)[0], xList.get(i)[1], Piece.X);
            panel.executeMoveForTest(oList.get(i)[0], oList.get(i)[1], Piece.O);
        }

        assertEquals(224, panel.getMoveHistory().size());
        assertFalse(panel.isGameOver());

        // Đánh nước thứ 225 là của X
        int[] lastMove = xList.get(112);
        panel.executeMoveForTest(lastMove[0], lastMove[1], Piece.X);

        assertEquals(225, panel.getMoveHistory().size());
        assertTrue(panel.isGameOver(), "Bàn cờ kín 225 ô phải kết thúc ván (Hòa)");
        assertEquals(1, tracker.getDraws(), "Ghi nhận 1 trận hòa");

        // Thực hiện Undo
        panel.undoMove();

        // Kiểm tra sau Undo
        assertEquals(224, panel.getMoveHistory().size(), "Undo chỉ rút đúng 1 nước thứ 225 của X");
        assertFalse(panel.isGameOver(), "Ván cờ chơi tiếp được");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt cờ thuộc về X");
        assertEquals(Piece.NONE, board.getPiece(lastMove[0], lastMove[1]), "Ô thứ 225 phải được xóa");
        assertEquals(1, tracker.getDraws(), "Điểm hòa giữ nguyên");
    }

    @Test
    @DisplayName("P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1")
    public void testP5_AlternatingPieceCountAfterUndo() {
        // Đánh 9 nước
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(7, 8, Piece.X);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(7, 9, Piece.X);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(7, 10, Piece.X);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(7, 11, Piece.X); // X thắng

        panel.undoMove();

        // Sau Undo 1 nước của X: 8 nước còn lại gồm 4 X và 4 O
        int countX = 0, countO = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getPiece(r, c) == Piece.X) countX++;
                if (board.getPiece(r, c) == Piece.O) countO++;
            }
        }

        assertEquals(8, panel.getMoveHistory().size());
        assertEquals(4, countX, "Số quân X còn lại phải là 4");
        assertEquals(4, countO, "Số quân O còn lại phải là 4");
        assertEquals(countO, countX, "Khi lượt tiếp theo là X, số quân X phải bằng số quân O (mỗi bên 4 quân)");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt cờ phải là của X");

        // Khi X đánh tiếp nước thứ 9, số quân X = số quân O + 1
        panel.executeMoveForTest(7, 11, Piece.X);
        int countXAfter = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getPiece(r, c) == Piece.X) countXAfter++;
            }
        }
        assertEquals(countO + 1, countXAfter, "Sau khi X đánh tiếp, số quân X = số quân O + 1");
    }
}
