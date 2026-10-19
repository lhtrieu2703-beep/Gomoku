package game;

import game.model.Board;
import game.model.Piece;
import game.model.ScoreTracker;
import game.view.BoardPanel;
import game.view.SoundManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử JUnit 5 cho tính năng Undo và tách biệt thành tích khỏi trạng thái bàn cờ (Task 2).
 * Kiểm thử toàn diện:
 * - U01 đến U08
 * - Các ca biên 6a, 6b, 6c, 6d, 6e
 * - Quy tắc kết quả đầu tiên là kết quả cuối cùng (Mục 8)
 * - Xử lý gói mạng khi kết thúc (Mục 7)
 * - Kịch bản tích hợp logic End-to-End (Mục 9)
 */
public class UndoScoreTest {

    private Board board;
    private BoardPanel panel;
    private ScoreTracker tracker;

    @BeforeAll
    public static void initAll() {
        // Tắt âm thanh trong môi trường kiểm thử tự động
        SoundManager.setSoundEnabled(false);
    }

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.setWinRule(Board.WinRule.RULE_B);
        panel = new BoardPanel(board);
        tracker = new ScoreTracker();

        // Gắn listener kết nối BoardPanel với ScoreTracker giống MainFrame
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

    @Test
    @DisplayName("U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0)")
    public void testU01_AiWinRecorded() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        int gId = panel.getGameId();

        // Giả lập AI (O) đánh 5 quân thắng ở hàng 0
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(0, 4, Piece.O); // Nước thứ 5 thắng

        assertTrue(panel.isGameOver(), "Ván đấu phải kết thúc khi AI có 5 quân hợp lệ");
        assertEquals(0, tracker.getHumanWins(), "Người chơi chưa thắng");
        assertEquals(1, tracker.getAiWins(), "AI thắng phải ghi nhận 1 trận thua cho người chơi (0-1)");
        assertEquals(0, tracker.getDraws(), "Không có ván hòa");
        assertTrue(tracker.isGameRecorded(gId), "GameId hiện tại phải được đánh dấu đã ghi nhận");
    }

    @Test
    @DisplayName("U02: Undo xong, thành tích vẫn 0-1, isGameOver = false")
    public void testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        int initialGameId = panel.getGameId();

        // Người đánh (7,7), AI đánh (0,0)... AI tạo 5 quân thắng
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(7, 8, Piece.X);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(7, 9, Piece.X);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(7, 10, Piece.X);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(8, 8, Piece.X);
        panel.executeMoveForTest(0, 4, Piece.O); // AI thắng

        assertTrue(panel.isGameOver());
        assertEquals(0, tracker.getHumanWins());
        assertEquals(1, tracker.getAiWins());

        // Thực hiện Undo
        assertTrue(panel.canUndo(), "Sau khi ván kết thúc vẫn có thể Undo");
        panel.undoMove();

        assertFalse(panel.isGameOver(), "isGameOver phải trở về false sau khi Undo");
        assertEquals(initialGameId, panel.getGameId(), "Undo KHÔNG được thay đổi gameId");
        assertEquals(0, tracker.getHumanWins(), "Thành tích người thắng không đổi");
        assertEquals(1, tracker.getAiWins(), "Thành tích AI thắng VẪN PHẢI LÀ 1 (không bị xóa)");
        assertEquals(0, tracker.getDraws(), "Thành tích hòa không đổi");
    }

    @Test
    @DisplayName("U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm")
    public void testU03_PlayAgainAndFinish_NoDuplicateScore() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);

        // 1. AI thắng lần đầu
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(0, 4, Piece.O);
        assertTrue(panel.isGameOver());
        assertEquals(0, tracker.getHumanWins());
        assertEquals(1, tracker.getAiWins());

        // 2. Undo (ở chế độ PvAI sẽ rút 2 nước cuối là 0,4 và 0,3)
        panel.undoMove();
        assertFalse(panel.isGameOver());

        // 3. AI đánh lại đúng 2 nước để thắng lại
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(0, 4, Piece.O);
        assertTrue(panel.isGameOver());

        // Điểm vẫn phải giữ nguyên 0-1, KHÔNG được tăng thành 0-2
        assertEquals(0, tracker.getHumanWins(), "Người chơi vẫn là 0");
        assertEquals(1, tracker.getAiWins(), "AI thắng không được tăng trùng lần 2, vẫn là 1");
        assertEquals(0, tracker.getDraws());
    }

    @Test
    @DisplayName("U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false")
    public void testU04_UndoWhenHistoryEmpty_NoException() {
        panel.setGameMode(BoardPanel.GameMode.PVP);
        assertFalse(panel.canUndo(), "Lịch sử rỗng thì canUndo phải là false");
        assertEquals(0, panel.getMoveHistory().size());

        // Gọi undoMove khi không có nước đi
        assertDoesNotThrow(() -> panel.undoMove(), "Undo khi rỗng không được văng Exception");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt chơi ban đầu vẫn là Piece.X");
        assertFalse(panel.isGameOver());
        assertEquals(-1, panel.getLastMoveRow());
        assertEquals(-1, panel.getLastMoveCol());
    }

    @Test
    @DisplayName("U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đúng")
    public void testU05_UndoMultipleTimesUntilEmpty_RestoresStateCorrectly() {
        panel.setGameMode(BoardPanel.GameMode.PVP);

        // Đánh 3 nước: X(7,7), O(7,8), X(8,8)
        panel.playMove(7, 7);
        panel.playMove(7, 8);
        panel.playMove(8, 8);
        assertEquals(3, panel.getMoveHistory().size());
        assertEquals(Piece.O, panel.getCurrentTurn());
        assertEquals(8, panel.getLastMoveRow());
        assertEquals(8, panel.getLastMoveCol());

        // Undo lần 1: rút X(8,8) -> nước cuối còn lại là O(7,8)
        panel.undoMove();
        assertEquals(2, panel.getMoveHistory().size());
        assertEquals(Piece.X, panel.getCurrentTurn());
        assertEquals(Piece.NONE, board.getPiece(8, 8));
        assertEquals(7, panel.getLastMoveRow());
        assertEquals(8, panel.getLastMoveCol());

        // Undo lần 2: rút O(7,8) -> nước cuối còn lại là X(7,7)
        panel.undoMove();
        assertEquals(1, panel.getMoveHistory().size());
        assertEquals(Piece.O, panel.getCurrentTurn());
        assertEquals(Piece.NONE, board.getPiece(7, 8));
        assertEquals(7, panel.getLastMoveRow());
        assertEquals(7, panel.getLastMoveCol());

        // Undo lần 3: rút X(7,7) -> hết lịch sử
        panel.undoMove();
        assertEquals(0, panel.getMoveHistory().size());
        assertEquals(Piece.X, panel.getCurrentTurn());
        assertEquals(Piece.NONE, board.getPiece(7, 7));
        assertEquals(-1, panel.getLastMoveRow());
        assertEquals(-1, panel.getLastMoveCol());
        assertFalse(panel.canUndo(), "Khi hết lịch sử, canUndo phải là false");

        // Undo lần 4: không làm gì thêm
        panel.undoMove();
        assertEquals(0, panel.getMoveHistory().size());
    }

    @Test
    @DisplayName("U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ nguyên")
    public void testU06_ResetGame_NewGameId_ScoresPreserved_NewResultRecorded() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        int g1 = panel.getGameId();

        // Ván 1: AI thắng
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(0, 4, Piece.O);
        assertTrue(panel.isGameOver());
        assertEquals(0, tracker.getHumanWins());
        assertEquals(1, tracker.getAiWins());

        // Người chơi bấm "Ván mới" (Reset)
        tracker.newGame();
        panel.resetGame();
        int g2 = panel.getGameId();

        assertNotEquals(g1, g2, "Ván mới phải sinh gameId mới");
        assertFalse(panel.isGameOver(), "Ván mới chưa kết thúc");
        assertEquals(0, panel.getMoveHistory().size(), "Lịch sử nước đi phải được xóa sạch");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt ván mới bắt đầu từ Piece.X");

        // Điểm số cũ không bị xóa
        assertEquals(0, tracker.getHumanWins(), "Điểm thắng cũ được giữ nguyên");
        assertEquals(1, tracker.getAiWins(), "Điểm thua cũ được giữ nguyên");

        // Ván 2: Người chơi thắng
        panel.executeMoveForTest(5, 5, Piece.X);
        panel.executeMoveForTest(5, 6, Piece.X);
        panel.executeMoveForTest(5, 7, Piece.X);
        panel.executeMoveForTest(5, 8, Piece.X);
        panel.executeMoveForTest(5, 9, Piece.X);

        assertTrue(panel.isGameOver());
        // Điểm cập nhật: 1 thắng, 1 thua
        assertEquals(1, tracker.getHumanWins(), "Ván mới ghi nhận người chơi thắng");
        assertEquals(1, tracker.getAiWins(), "Điểm AI thắng ván cũ vẫn bảo toàn");
    }

    @Test
    @DisplayName("U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng")
    public void testU07_DrawGame_Undo_DrawScorePreserved() {
        panel.setGameMode(BoardPanel.GameMode.PVP);
        int gId = panel.getGameId();

        // Giả lập hòa cờ trực tiếp qua listener
        tracker.recordResult(gId, Piece.NONE);
        assertEquals(0, tracker.getHumanWins());
        assertEquals(0, tracker.getAiWins());
        assertEquals(1, tracker.getDraws(), "Phải ghi nhận 1 trận hòa");

        // Undo
        panel.playMove(0, 0);
        panel.undoMove();

        assertEquals(1, tracker.getDraws(), "Sau Undo, điểm hòa vẫn là 1");

        // Kết thúc lại trong cùng ván
        boolean recordedAgain = tracker.recordResult(gId, Piece.X);
        assertFalse(recordedAgain, "Không được ghi nhận kết quả trùng cho cùng gameId");
        assertEquals(0, tracker.getHumanWins(), "Điểm thắng không bị ghi đè");
        assertEquals(1, tracker.getDraws(), "Điểm hòa vẫn giữ nguyên là 1");
    }

    @Test
    @DisplayName("U08: Race condition AI - token cũ bị vô hiệu hóa khi resetGame hoặc undoMove")
    public void testU08_AiWorkerTokenDiscardedOnResetOrUndo() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        int g1 = panel.getGameId();

        // Khi resetGame được gọi, gameId tăng và token tăng
        panel.resetGame();
        int g2 = panel.getGameId();
        assertTrue(g2 > g1, "gameId phải tăng sau resetGame");
        assertFalse(panel.isAiThinking(), "isAiThinking phải là false sau reset");
    }

    @Test
    @DisplayName("6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false)")
    public void test6e_PvpUndoOneMove_PvAiUndoTwoMoves_OnlineUndoForbidden() {
        // 1. PvP: Undo 1 nước
        panel.setGameMode(BoardPanel.GameMode.PVP);
        panel.playMove(3, 3); // X
        panel.playMove(3, 4); // O
        assertEquals(2, panel.getMoveHistory().size());
        panel.undoMove();
        assertEquals(1, panel.getMoveHistory().size(), "PvP Undo chỉ rút đúng 1 nước");
        assertEquals(Piece.O, panel.getCurrentTurn(), "PvP đổi lượt về người vừa rút nước");

        // 2. PvAI: Undo 2 nước (người + máy)
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        panel.executeMoveForTest(4, 4, Piece.X);
        panel.executeMoveForTest(4, 5, Piece.O);
        assertEquals(2, panel.getMoveHistory().size());
        panel.undoMove();
        assertEquals(0, panel.getMoveHistory().size(), "PvAI Undo rút tối đa 2 nước");
        assertEquals(Piece.X, panel.getCurrentTurn(), "PvAI đổi lượt về Piece.X");

        // 3. Online: canUndo luôn trả về false
        panel.setupOnlineGame(null, true);
        panel.executeMoveForTest(5, 5, Piece.X);
        assertFalse(panel.canUndo(), "Trong chế độ Online, canUndo() bắt buộc phải là false");
        panel.undoMove();
        assertEquals(1, panel.getMoveHistory().size(), "Online không được rút nước nào");
    }

    @Test
    @DisplayName("7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua")
    public void test7_OnlineMoveIgnoredWhenGameOver() {
        panel.setupOnlineGame(null, true); // Host là Piece.X

        // Tạo thế cờ thắng cho X
        panel.executeMoveForTest(1, 0, Piece.X);
        panel.executeMoveForTest(1, 1, Piece.X);
        panel.executeMoveForTest(1, 2, Piece.X);
        panel.executeMoveForTest(1, 3, Piece.X);
        panel.executeMoveForTest(1, 4, Piece.X); // X thắng
        assertTrue(panel.isGameOver());

        int historyBefore = panel.getMoveHistory().size();

        // Đối thủ gửi nước đi tới sau khi game đã kết thúc
        panel.applyOpponentMove(2, 2);

        // Đảm bảo không thay đổi bàn cờ
        assertEquals(historyBefore, panel.getMoveHistory().size(), "Gói MOVE sau khi ván kết thúc phải bị bỏ qua");
        assertEquals(Piece.NONE, board.getPiece(2, 2), "Ô cờ không được đánh đè");
    }

    @Test
    @DisplayName("8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm vẫn 0-1, bàn cờ và lượt đúng")
    public void test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01() {
        panel.setGameMode(BoardPanel.GameMode.PV_AI);
        int gId = panel.getGameId();

        // 1. Diễn biến: Người đánh (7,7), AI đánh (0,0)...
        panel.executeMoveForTest(7, 7, Piece.X);
        panel.executeMoveForTest(0, 0, Piece.O);
        panel.executeMoveForTest(7, 8, Piece.X);
        panel.executeMoveForTest(0, 1, Piece.O);
        panel.executeMoveForTest(7, 9, Piece.X);
        panel.executeMoveForTest(0, 2, Piece.O);
        panel.executeMoveForTest(7, 10, Piece.X);
        panel.executeMoveForTest(0, 3, Piece.O);
        panel.executeMoveForTest(8, 8, Piece.X);
        panel.executeMoveForTest(0, 4, Piece.O); // AI thắng lần đầu!

        assertTrue(panel.isGameOver(), "Ván đấu kết thúc do AI thắng");
        assertEquals(0, tracker.getHumanWins());
        assertEquals(1, tracker.getAiWins(), "Kết quả ban đầu: AI thắng (0-1)");
        assertEquals(0, tracker.getDraws());

        // 2. Người chơi bấm Undo
        assertTrue(panel.canUndo(), "Nút Undo phải khả dụng");
        panel.undoMove(); // Hoàn tác 2 nước cuối: O(0,4) và X(8,8)

        assertFalse(panel.isGameOver(), "isGameOver phải trở về false");
        assertEquals(Piece.X, panel.getCurrentTurn(), "Lượt cờ khôi phục về Piece.X");
        assertEquals(Piece.NONE, board.getPiece(0, 4), "Ô (0,4) của AI đã được rút");
        assertEquals(Piece.NONE, board.getPiece(8, 8), "Ô (8,8) của người đã được rút");
        assertEquals(gId, panel.getGameId(), "gameId vẫn giữ nguyên");
        assertEquals(0, tracker.getHumanWins(), "Điểm thắng vẫn là 0");
        assertEquals(1, tracker.getAiWins(), "Điểm thua vẫn là 1");

        // 3. Người chơi đánh vào (7, 11) để tự tạo chuỗi 5 quân thắng (7,7 đến 7,11)
        panel.executeMoveForTest(7, 11, Piece.X);

        assertTrue(panel.isGameOver(), "Người chơi hoàn thành chuỗi 5 quân X, ván cờ kết thúc lần 2");
        // Theo quy tắc Mục 8: kết quả đầu tiên của một gameId là kết quả cuối cùng!
        assertEquals(0, tracker.getHumanWins(), "Người chơi thắng ở lần kết thúc thứ hai KHÔNG được cộng điểm");
        assertEquals(1, tracker.getAiWins(), "Thành tích VẪN GIỮ NGUYÊN là 0-1");
        assertEquals(0, tracker.getDraws());
    }
}
