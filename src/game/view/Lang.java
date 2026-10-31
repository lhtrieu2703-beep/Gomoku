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
    public static final String BRAND = "GOMOKU";
    private static String text(String vi,String en,String pt) { return switch(current) { case VI -> vi; case EN -> en; case PT -> pt; }; }
    public static String titleHint() { return text("Nhấn để bắt đầu","Click or press any key to start","Clique ou pressione uma tecla para começar"); }
    public static String profile() { return text("Hồ sơ","Profile","Perfil"); }
    public static String namePlaceholder() { return text("Nhập tên của bạn","Enter your name","Digite seu nome"); }
    public static String done() { return text("Xong","Done","Concluir"); }
    public static String cancel() { return text("Hủy","Cancel","Cancelar"); }
    public static String settings() { return text("Cài đặt","Settings","Configurações"); }
    public static String duplicatePlayerNames() { return text("Tên hai người chơi phải khác nhau.","Player names must be different.","Os nomes dos jogadores devem ser diferentes."); }
    public static String resetScore() { return text("Đặt lại tỉ số","Reset score","Zerar placar"); }
    public static String confirmResetScore() { return text("Đặt lại tỉ số về 0?","Reset the score to zero?","Zerar o placar?"); }
    public static String rulesTitle() { return text("Luật chơi","Rules","Regras"); }
    public static String close() { return text("Đóng","Close","Fechar"); }
    public static String clearName() { return text("Xóa tên","Clear name","Limpar nome"); }
    public static String avatarLabel(int index) { return text("Ảnh đại diện ","Avatar ","Imagem de perfil ")+index; }
    public static String characterCount(int count) { return count+"/"+AppSettings.MAX_PLAYER_NAME_LENGTH; }
    public static String difficultyName(game.ai.MinimaxAI.Difficulty difficulty) { return switch(difficulty) { case EASY -> easy(); case MEDIUM -> medium(); case HARD -> hard(); }; }
    public static String computerName(game.ai.MinimaxAI.Difficulty difficulty) { return text("Máy","Computer","Computador")+" ("+difficultyName(difficulty)+")"; }
    public static String opponentName() { return text("Đối thủ","Opponent","Adversário"); }
    public static String pieceName(game.model.Piece piece) { return piece==game.model.Piece.X?text("Quân Đen","Black","Preto"):text("Quân Trắng","White","Branco"); }
    public static String ruleExample(boolean win) { return win?text("Thắng","Win","Vitória"):text("Không thắng (bị chặn hai đầu)","No win (both ends blocked)","Sem vitória (as duas extremidades bloqueadas)"); }
    public static String rules(String key) {
        return switch(key) {
            case "rules.l1" -> rulesLine(1); case "rules.l2" -> rulesLine(2); case "rules.l3" -> rulesLine(3);
            case "rules.l4" -> rulesLine(4); case "rules.l5" -> rulesLine(5); case "rules.l6" -> rulesLine(6);
            case "rules.winExample" -> ruleExample(true); case "rules.noWinExample" -> ruleExample(false);
            default -> throw new IllegalArgumentException("Unknown rule key: "+key);
        };
    }
    public static String rulesLine(int index) {
        String[][] lines={
            {"Bàn cờ 15×15. Hai người luân phiên đặt quân vào ô trống; Quân Đen đi trước.","15×15 board. Players take turns placing a stone on an empty cell; Black goes first.","Tabuleiro 15×15. Os jogadores alternam colocando uma peça numa casa vazia; as Pretas começam."},
            {"Thắng khi có từ 5 quân cùng màu liên tiếp theo hàng ngang, hàng dọc hoặc đường chéo. Dãy 6 quân trở lên cũng thắng.","You win with 5 or more stones of your colour in a row (horizontal, vertical or diagonal). Rows of 6 or more also win.","Vence quem formar 5 ou mais peças da sua cor em linha (horizontal, vertical ou diagonal). Linhas de 6 ou mais também vencem."},
            {"Nếu dãy bị quân đối thủ chặn sát cả hai đầu thì KHÔNG tính thắng. Chặn một đầu, hoặc một đầu sát mép bàn, vẫn thắng.","If the opponent blocks BOTH ends of the row, it does NOT count as a win. One blocked end, or one end at the board edge, still wins.","Se o adversário bloquear AMBAS as extremidades da linha, NÃO conta como vitória. Com uma extremidade bloqueada, ou uma extremidade na borda do tabuleiro, ainda vence."},
            {"Bàn đầy mà chưa ai thắng thì hòa.","If the board is full and nobody has won, the game is a draw.","Se o tabuleiro ficar cheio sem vencedor, o jogo termina empatado."},
            {"Không có luật cấm tay (3-3, 4-4) và không giới hạn thời gian mỗi nước.","There are no forbidden moves (3-3, 4-4) and no time limit per move.","Não há jogadas proibidas (3-3, 4-4) nem limite de tempo por jogada."},
            {"Đi lại: chế độ 2 người rút 1 nước; khi chơi với máy sẽ rút cả nước của máy. Chế độ LAN không có đi lại.","Undo: in 2-player mode it takes back 1 move; against the computer it also takes back the computer's move. Undo is not available in LAN mode.","Desfazer: no modo de 2 jogadores desfaz 1 jogada; contra o computador desfaz também a jogada do computador. Não está disponível no modo LAN."}
        };
        if(index<1||index>6) throw new IllegalArgumentException("rule index");
        return lines[index-1][current.ordinal()];
    }
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

    public static String startTitle() {
        return switch (current) {
            case VI -> "Cờ Gomoku";
            case EN -> "Gomoku Game";
            case PT -> "Jogo Gomoku";
        };
    }

    public static String startSubtitle() {
        return switch (current) {
            case VI -> "Nhập tên để bắt đầu ván cờ.";
            case EN -> "Enter your name to start playing.";
            case PT -> "Digite seu nome para começar a jogar.";
        };
    }

    public static String playerNameLabel() {
        return switch (current) {
            case VI -> "Tên người chơi";
            case EN -> "Player name";
            case PT -> "Nome do jogador";
        };
    }

    public static String playerNameHint() {
        return switch (current) {
            case VI -> "Tối đa 16 ký tự";
            case EN -> "Up to 16 characters";
            case PT -> "Até 16 caracteres";
        };
    }

    public static String defaultPlayerName() {
        return switch (current) {
            case VI -> "Người chơi 1";
            case EN -> "Player 1";
            case PT -> "Jogador 1";
        };
    }

    public static String defaultPlayerTwo() {
        return switch (current) {
            case VI -> "Người chơi 2";
            case EN -> "Player 2";
            case PT -> "Jogador 2";
        };
    }

    public static String startGame() {
        return switch (current) {
            case VI -> "Bắt đầu";
            case EN -> "Start";
            case PT -> "Começar";
        };
    }

    public static String languageLabel() {
        return switch (current) {
            case VI -> "Ngôn ngữ";
            case EN -> "Language";
            case PT -> "Idioma";
        };
    }

    public static String backToMenu() {
        return switch (current) {
            case VI -> "Về menu";
            case EN -> "Back to menu";
            case PT -> "Voltar ao menu";
        };
    }

    public static String achievementsTooltip() {
        return switch (current) {
            case VI -> "Thành tích";
            case EN -> "Score summary";
            case PT -> "Resultados";
        };
    }

    public static String scoreSummary(String name, int winCount, int lossCount, int drawCount) {
        return playerNameLabel() + ": " + name + "\n\n" + wins() + ": " + winCount
                + "\n" + losses() + ": " + lossCount + "\n" + draws() + ": " + drawCount;
    }

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
            case VI -> "2 người cùng máy";
            case EN -> "2 Players, Same Computer";
            case PT -> "2 jogadores no mesmo computador";
        };
    }

    public static String pvAI() {
        return switch (current) {
            case VI -> "Chơi với máy";
            case EN -> "Play vs Computer";
            case PT -> "Jogar contra o computador";
        };
    }

    public static String online() {
        return switch (current) {
            case VI -> "Chơi LAN";
            case EN -> "Play LAN";
            case PT -> "Jogar em rede (LAN)";
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
            case START_TIMEOUT -> switch (current) {
                case VI -> "Hết thời gian chờ bắt đầu ván. Hãy kết nối lại.";
                case EN -> "Timed out waiting for the game to start. Please reconnect.";
                case PT -> "O tempo de espera para iniciar a partida terminou. Conecte-se novamente.";
            };
            case VERSION -> switch (current) {
                case VI -> "Phiên bản kết nối không tương thích. Hai máy cần cùng phiên bản game.";
                case EN -> "Incompatible protocol version. Both players need the same game version.";
                case PT -> "Versão de protocolo incompatível. Os dois jogadores precisam da mesma versão do jogo.";
            };
        };
    }

    public static String restartWaiting() {
        return switch (current) {
            case VI -> "Đang chờ chủ phòng bắt đầu ván mới...";
            case EN -> "Waiting for the host to start a new game...";
            case PT -> "Aguardando o anfitrião iniciar uma nova partida...";
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
