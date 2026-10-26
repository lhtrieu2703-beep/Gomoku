package game.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Hệ thống quản lý ngôn ngữ (Tiếng Việt / English / Português).
 * Các component đăng ký listener sẽ được tự động thông báo khi ngôn ngữ thay đổi.
 *
 * THAY ĐỔI so với phiên bản cũ:
 *  - Thêm Language.PT (Português).
 *  - Thêm set(Language) và get() để chọn ngôn ngữ bất kỳ.
 *  - Thêm removeChangeListener(Runnable) để tránh memory leak.
 *  - Đã gom TẤT CẢ chuỗi hiển thị cho người dùng vào đây (kể cả NetworkDialog, MainFrame).
 *  - toggle() và langBtnLabel()/langBtnTooltip() đã bị XÓA (không còn caller sau khi btnLang → langCombo).
 */
public class Lang {
    public enum Language { VI, EN, PT }

    private static Language current = Language.VI;
    private static final List<Runnable> listeners = new ArrayList<>();

    // =========================================================================
    // Quản lý ngôn ngữ
    // =========================================================================
    public static void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public static void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /** Đặt ngôn ngữ và thông báo cho mọi listener. */
    public static void set(Language lang) {
        current = lang;
        for (Runnable r : new ArrayList<>(listeners)) r.run();
    }

    /** Trả về ngôn ngữ hiện tại. */
    public static Language get() { return current; }

    public static boolean isVI() { return current == Language.VI; }

    // =========================================================================
    // Tên ngôn ngữ hiển thị trong dropdown (hiện bằng chính ngôn ngữ đó)
    // =========================================================================
    public static String langName(Language lang) {
        return switch (lang) {
            case VI -> "Tiếng Việt";
            case EN -> "English";
            case PT -> "Português";
        };
    }

    /** Tooltip cho langCombo */
    public static String langComboTooltip() {
        return switch (current) {
            case VI -> "Đổi ngôn ngữ";
            case EN -> "Change language";
            case PT -> "Mudar idioma";
        };
    }

    // =========================================================================
    // Nhãn điều khiển
    // =========================================================================
    public static String mode() {
        return switch (current) {
            case VI -> "Chế độ:";
            case EN -> "Mode:";
            case PT -> "Modo:";
        };
    }

    public static String difficulty() {
        return switch (current) {
            case VI -> "Độ khó:";
            case EN -> "Difficulty:";
            case PT -> "Dificuldade:";
        };
    }

    public static String newGame() {
        return switch (current) {
            case VI -> "Ván Mới";
            case EN -> "New Game";
            case PT -> "Nova Partida";
        };
    }

    public static String undo() {
        return switch (current) {
            case VI -> "Đi Lại";
            case EN -> "Undo";
            case PT -> "Desfazer";
        };
    }

    public static String sound() {
        return switch (current) {
            case VI -> "Âm thanh";
            case EN -> "Sound";
            case PT -> "Som";
        };
    }

    public static String soundTooltipOn() {
        return switch (current) {
            case VI -> "Âm thanh: Bật (nhấn để tắt)";
            case EN -> "Sound: On (click to mute)";
            case PT -> "Som: Ligado (clique para mutar)";
        };
    }

    public static String soundTooltipOff() {
        return switch (current) {
            case VI -> "Âm thanh: Tắt (nhấn để bật)";
            case EN -> "Sound: Off (click to unmute)";
            case PT -> "Som: Desligado (clique para ativar)";
        };
    }

    // =========================================================================
    // Chế độ chơi
    // =========================================================================
    public static String pvp() {
        return switch (current) {
            case VI -> "Người vs Người";
            case EN -> "Human vs Human";
            case PT -> "Humano vs Humano";
        };
    }

    public static String pvAI() {
        return switch (current) {
            case VI -> "Người vs Máy";
            case EN -> "Human vs AI";
            case PT -> "Humano vs IA";
        };
    }

    public static String online() {
        return switch (current) {
            case VI -> "Chơi Online (LAN)";
            case EN -> "Online (LAN)";
            case PT -> "Online (LAN)";
        };
    }

    // =========================================================================
    // Độ khó
    // =========================================================================
    public static String easy() {
        return switch (current) {
            case VI -> "Dễ";
            case EN -> "Easy";
            case PT -> "Fácil";
        };
    }

    public static String medium() {
        return switch (current) {
            case VI -> "Trung Bình";
            case EN -> "Medium";
            case PT -> "Médio";
        };
    }

    public static String hard() {
        return switch (current) {
            case VI -> "Khó";
            case EN -> "Hard";
            case PT -> "Difícil";
        };
    }

    // =========================================================================
    // Bảng thành tích
    // =========================================================================
    public static String scoreboard() {
        return switch (current) {
            case VI -> "THÀNH TÍCH";
            case EN -> "SCOREBOARD";
            case PT -> "PLACAR";
        };
    }

    public static String wins() {
        return switch (current) {
            case VI -> "Thắng";
            case EN -> "Wins";
            case PT -> "Vitórias";
        };
    }

    public static String losses() {
        return switch (current) {
            case VI -> "Thua";
            case EN -> "Losses";
            case PT -> "Derrotas";
        };
    }

    public static String draws() {
        return switch (current) {
            case VI -> "Hòa";
            case EN -> "Draws";
            case PT -> "Empates";
        };
    }

    // =========================================================================
    // Thông báo lượt chơi
    // =========================================================================
    public static String yourTurn() {
        return switch (current) {
            case VI -> "Lượt của bạn (Quân Đen)";
            case EN -> "Your Turn (Black)";
            case PT -> "Sua vez (Preto)";
        };
    }

    public static String aiThinking() {
        return switch (current) {
            case VI -> "Máy đang suy nghĩ...";
            case EN -> "AI is thinking...";
            case PT -> "IA está pensando...";
        };
    }

    public static String aiTurn() {
        return switch (current) {
            case VI -> "Lượt: Máy (Quân Trắng)";
            case EN -> "Turn: AI (White)";
            case PT -> "Vez: IA (Branco)";
        };
    }

    public static String p1Turn() {
        return switch (current) {
            case VI -> "Lượt: Người chơi 1 (Quân Đen)";
            case EN -> "Turn: Player 1 (Black)";
            case PT -> "Vez: Jogador 1 (Preto)";
        };
    }

    public static String p2Turn() {
        return switch (current) {
            case VI -> "Lượt: Người chơi 2 (Quân Trắng)";
            case EN -> "Turn: Player 2 (White)";
            case PT -> "Vez: Jogador 2 (Branco)";
        };
    }

    // =========================================================================
    // Online – chuỗi trong NetworkDialog
    // =========================================================================
    public static String networkDialogTitle() {
        return switch (current) {
            case VI -> "Kết nối chơi Online (LAN)";
            case EN -> "Online Connection (LAN)";
            case PT -> "Conexão Online (LAN)";
        };
    }

    public static String networkDialogHeader() {
        return switch (current) {
            case VI -> "CHỌN CÁCH THỨC KẾT NỐI";
            case EN -> "CHOOSE CONNECTION MODE";
            case PT -> "ESCOLHA O MODO DE CONEXÃO";
        };
    }

    public static String ipLabel() {
        return switch (current) {
            case VI -> "Địa chỉ IP đối thủ:";
            case EN -> "Opponent IP:";
            case PT -> "IP do adversário:";
        };
    }

    public static String portLabel() {
        return switch (current) {
            case VI -> "Cổng (Port):";
            case EN -> "Port:";
            case PT -> "Porta:";
        };
    }

    public static String myIpLabel() {
        return switch (current) {
            case VI -> "IP máy của bạn: ";
            case EN -> "Your local IP: ";
            case PT -> "Seu IP local: ";
        };
    }

    public static String hostBtn() {
        return switch (current) {
            case VI -> "Tạo Phòng (Host - Quân Đen)";
            case EN -> "Create Room (Host - Black)";
            case PT -> "Criar Sala (Host - Preto)";
        };
    }

    public static String joinBtn() {
        return switch (current) {
            case VI -> "Vào Phòng (Join - Quân Trắng)";
            case EN -> "Join Room (Join - White)";
            case PT -> "Entrar na Sala (Join - Branco)";
        };
    }

    public static String hostingMsg() {
        return switch (current) {
            case VI -> "Đang mở phòng & chờ đối thủ...";
            case EN -> "Hosting & waiting for player...";
            case PT -> "Hospedando & aguardando jogador...";
        };
    }

    /** @param ip địa chỉ IP đang kết nối tới */
    public static String connectingMsg(String ip) {
        return switch (current) {
            case VI -> "Đang kết nối tới " + ip + "...";
            case EN -> "Connecting to " + ip + "...";
            case PT -> "Conectando a " + ip + "...";
        };
    }

    public static String invalidPort() {
        return switch (current) {
            case VI -> "Cổng (Port) không hợp lệ!";
            case EN -> "Invalid port number!";
            case PT -> "Número de porta inválido!";
        };
    }

    public static String networkError(game.network.NetworkManager.NetworkError error) {
        String endpoint = error.address() + ":" + error.port();
        return switch (error.type()) {
            case HOST -> switch (current) {
                case VI -> "Không thể tạo phòng tại cổng " + error.port() + ". Hãy thử cổng khác.";
                case EN -> "Cannot host on port " + error.port() + ". Try another port.";
                case PT -> "Não foi possível criar a sala na porta " + error.port() + ". Tente outra porta.";
            };
            case JOIN -> switch (current) {
                case VI -> "Không thể kết nối tới " + endpoint + ". Kiểm tra địa chỉ và phòng đang mở.";
                case EN -> "Cannot connect to " + endpoint + ". Check the address and that the room is open.";
                case PT -> "Não foi possível conectar a " + endpoint + ". Verifique o endereço e se a sala está aberta.";
            };
            case CHANNEL -> switch (current) {
                case VI -> "Không thể thiết lập kết nối. Hãy thử lại.";
                case EN -> "Cannot set up the connection. Please try again.";
                case PT -> "Não foi possível estabelecer a conexão. Tente novamente.";
            };
        };
    }

    // =========================================================================
    // Online – thông báo trong MainFrame
    // =========================================================================
    public static String onlineWaiting() {
        return switch (current) {
            case VI -> "Đang chờ đối thủ kết nối...";
            case EN -> "Waiting for opponent...";
            case PT -> "Aguardando adversário...";
        };
    }

    public static String onlineConnectedHost() {
        return switch (current) {
            case VI -> "Đã kết nối! Bạn: Quân Đen (Đi trước)";
            case EN -> "Connected! You: Black (First)";
            case PT -> "Conectado! Você: Preto (Primeiro)";
        };
    }

    public static String onlineConnectedClient() {
        return switch (current) {
            case VI -> "Đã kết nối! Bạn: Quân Trắng (Đi sau)";
            case EN -> "Connected! You: White (Second)";
            case PT -> "Conectado! Você: Branco (Segundo)";
        };
    }

    public static String connectedTitle() {
        return switch (current) {
            case VI -> "Kết nối thành công";
            case EN -> "Connected Successfully";
            case PT -> "Conexão bem-sucedida";
        };
    }

    public static String onlineYourTurn() {
        return switch (current) {
            case VI -> "Lượt của bạn! Hãy đi cờ.";
            case EN -> "Your turn! Make a move.";
            case PT -> "Sua vez! Faça um movimento.";
        };
    }

    public static String onlineOpponentTurn() {
        return switch (current) {
            case VI -> "Lượt đối thủ đang suy nghĩ...";
            case EN -> "Opponent's turn...";
            case PT -> "Vez do adversário...";
        };
    }

    public static String opponentDisconnected() {
        return switch (current) {
            case VI -> "Đối thủ đã ngắt kết nối!";
            case EN -> "Opponent disconnected!";
            case PT -> "Adversário desconectou!";
        };
    }

    public static String restartedMsg() {
        return switch (current) {
            case VI -> "Đối thủ đã bắt đầu ván mới!";
            case EN -> "Opponent restarted the game!";
            case PT -> "O adversário iniciou uma nova partida!";
        };
    }

    public static String newGameTitle() {
        return switch (current) {
            case VI -> "Ván mới";
            case EN -> "New Game";
            case PT -> "Nova Partida";
        };
    }

    public static String disconnectedTitle() {
        return switch (current) {
            case VI -> "Mất kết nối";
            case EN -> "Disconnected";
            case PT -> "Desconectado";
        };
    }

    // =========================================================================
    // Kết thúc ván
    // =========================================================================
    public static String youWin() {
        return switch (current) {
            case VI -> "Bạn đã CHIẾN THẮNG!";
            case EN -> "You WIN!";
            case PT -> "Você VENCEU!";
        };
    }

    public static String youLose() {
        return switch (current) {
            case VI -> "Bạn đã THUA cuộc!";
            case EN -> "You LOSE!";
            case PT -> "Você PERDEU!";
        };
    }

    public static String aiWins() {
        return switch (current) {
            case VI -> "Máy (Quân Trắng) đã CHIẾN THẮNG!";
            case EN -> "AI (White) WINS!";
            case PT -> "IA (Branco) VENCEU!";
        };
    }

    public static String p1Wins() {
        return switch (current) {
            case VI -> "Người chơi 1 (Quân Đen) CHIẾN THẮNG!";
            case EN -> "Player 1 (Black) WINS!";
            case PT -> "Jogador 1 (Preto) VENCEU!";
        };
    }

    public static String p2Wins() {
        return switch (current) {
            case VI -> "Người chơi 2 (Quân Trắng) CHIẾN THẮNG!";
            case EN -> "Player 2 (White) WINS!";
            case PT -> "Jogador 2 (Branco) VENCEU!";
        };
    }

    public static String drawGame() {
        return switch (current) {
            case VI -> "Ván đấu HÒA!";
            case EN -> "The game is a DRAW!";
            case PT -> "A partida EMPATOU!";
        };
    }

    public static String gameOverTitle() {
        return switch (current) {
            case VI -> "Kết Thúc Ván Cờ";
            case EN -> "Game Over";
            case PT -> "Fim da Partida";
        };
    }
}
