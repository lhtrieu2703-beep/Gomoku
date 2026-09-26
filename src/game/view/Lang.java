package game.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Hệ thống quản lý ngôn ngữ (Tiếng Việt / English).
 * Các component đăng ký listener sẽ được tự động thông báo khi ngôn ngữ thay đổi.
 */
public class Lang {
    public enum Language { VI, EN }

    private static Language current = Language.VI;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public static void toggle() {
        current = (current == Language.VI) ? Language.EN : Language.VI;
        for (Runnable r : listeners) r.run();
    }

    public static boolean isVI() { return current == Language.VI; }

    // Nút chuyển ngôn ngữ
    public static String langBtnLabel() { return isVI() ? "EN" : "VI"; }
    public static String langBtnTooltip() { return isVI() ? "Switch to English" : "Chuyển sang Tiếng Việt"; }

    // Nhãn điều khiển
    public static String mode()       { return isVI() ? "Chế độ:"  : "Mode:"; }
    public static String difficulty() { return isVI() ? "Độ khó:"  : "Difficulty:"; }
    public static String newGame()    { return isVI() ? "Ván Mới"  : "New Game"; }
    public static String undo()       { return isVI() ? "Đi Lại"   : "Undo"; }
    public static String sound()      { return isVI() ? "Âm thanh" : "Sound"; }

    // Chế độ chơi
    public static String pvp()  { return isVI() ? "Người vs Người" : "Human vs Human"; }
    public static String pvAI() { return isVI() ? "Người vs Máy"   : "Human vs AI"; }

    // Độ khó
    public static String easy()   { return isVI() ? "Dễ"         : "Easy"; }
    public static String medium() { return isVI() ? "Trung Bình"  : "Medium"; }
    public static String hard()   { return isVI() ? "Khó"         : "Hard"; }

    // Bảng thành tích
    public static String scoreboard() { return isVI() ? "THÀNH TÍCH" : "SCOREBOARD"; }
    public static String wins()       { return isVI() ? "Thắng"      : "Wins"; }
    public static String losses()     { return isVI() ? "Thua"       : "Losses"; }
    public static String draws()      { return isVI() ? "Hòa"        : "Draws"; }

    // Thông báo lượt chơi
    public static String yourTurn()    { return isVI() ? "Lượt của bạn (Quân Đen)"       : "Your Turn (Black)"; }
    public static String aiThinking()  { return isVI() ? "Máy đang suy nghĩ..."           : "AI is thinking..."; }
    public static String aiTurn()      { return isVI() ? "Lượt: Máy (Quân Trắng)"         : "Turn: AI (White)"; }
    public static String p1Turn()      { return isVI() ? "Lượt: Người chơi 1 (Quân Đen)"  : "Turn: Player 1 (Black)"; }
    public static String p2Turn()      { return isVI() ? "Lượt: Người chơi 2 (Quân Trắng)": "Turn: Player 2 (White)"; }

    // Kết thúc ván
    public static String youWin()  { return isVI() ? "Bạn (Quân Đen) đã CHIẾN THẮNG!"         : "You (Black) WIN!"; }
    public static String aiWins()  { return isVI() ? "Máy (Quân Trắng) đã CHIẾN THẮNG!"        : "AI (White) WINS!"; }
    public static String p1Wins()  { return isVI() ? "Người chơi 1 (Quân Đen) CHIẾN THẮNG!"    : "Player 1 (Black) WINS!"; }
    public static String p2Wins()  { return isVI() ? "Người chơi 2 (Quân Trắng) CHIẾN THẮNG!"  : "Player 2 (White) WINS!"; }
    public static String drawGame(){ return isVI() ? "Ván đấu HÒA!"                             : "The game is a DRAW!"; }
    public static String gameOverTitle() { return isVI() ? "Kết Thúc Ván Cờ" : "Game Over"; }
}
