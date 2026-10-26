package game.view;

import game.ai.MinimaxAI;
import game.ai.Move;
import game.model.Board;
import game.model.Piece;
import game.network.NetworkManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bàn cờ Gomoku kiểu cổ điển:
 * - Quân cờ đặt tại GIAO ĐIỂM của các đường kẻ (chuẩn Gomoku quốc tế).
 * - Quân Đen (Piece.X) và Quân Trắng (Piece.O) dạng hình tròn đặc.
 * - Bàn cờ màu gỗ ấm, viền đổ bóng.
 * - Hover preview khi rê chuột.
 * - Hỗ trợ 3 chế độ: PvP (cùng máy), PvAI, Online (qua LAN Socket).
 */
public class BoardPanel extends JPanel {
    public static final int CELL_SIZE = 38;
    public static final int OFFSET = 32;

    // Màu bàn cờ gỗ
    private static final Color BOARD_COLOR      = new Color(220, 170, 90);
    private static final Color BOARD_DARK       = new Color(195, 145, 65);
    private static final Color LINE_COLOR       = new Color(140, 90, 35);
    private static final Color STAR_COLOR       = new Color(90, 50, 10);

    // Màu quân cờ
    private static final Color BLACK_STONE      = new Color(28, 28, 28);
    private static final Color BLACK_HIGHLIGHT  = new Color(80, 80, 80);
    private static final Color WHITE_STONE      = new Color(250, 250, 248);
    private static final Color WHITE_BORDER     = new Color(160, 155, 145);
    private static final Color LAST_MARKER      = new Color(230, 60, 60);

    private static final int STONE_RADIUS = CELL_SIZE / 2 - 3;

    // =========================================================================
    public enum GameMode {
        PVP {
            @Override public String toString() { return Lang.pvp(); }
        },
        PV_AI {
            @Override public String toString() { return Lang.pvAI(); }
        },
        ONLINE {
            @Override public String toString() { return Lang.online(); }
        }
    }

    // =========================================================================
    private final Board board;
    private Piece currentTurn = Piece.X;
    private boolean isGameOver = false;
    private Piece winner = Piece.NONE;
    private boolean isAiThinking = false;
    private int lastMoveRow = -1, lastMoveCol = -1;
    private int hoverRow = -1, hoverCol = -1;

    private volatile int gameId = 1; // also captured when a network callback is queued
    private SwingWorker<Move, Void> aiWorker = null;
    private int aiExecutionToken = 0;

    private final List<Move> moveHistory = new ArrayList<>();
    private GameMode gameMode = GameMode.PV_AI;
    private final MinimaxAI ai;

    // Quản lý chế độ Online
    private NetworkManager networkManager;
    private Piece myPiece = Piece.X; // Host là X (Đen), Client là O (Trắng)

    // =========================================================================
    public interface GameStateListener {
        void onTurnChanged(Piece currentTurn, boolean isAiThinking);
        void onGameOver(String message, Piece winner);
        default void onGameOver(int gameId, String message, Piece winner) {
            onGameOver(message, winner);
        }
        void onAiStatsUpdated(long timeMs, long nodes, long prunings);
        void onHistoryChanged(boolean canUndo);
    }

    private GameStateListener stateListener;

    // =========================================================================
    public BoardPanel(Board board) {
        this.board = board;
        this.ai    = new MinimaxAI(Piece.O, MinimaxAI.Difficulty.MEDIUM);

        int panelSize = OFFSET * 2 + (Board.SIZE - 1) * CELL_SIZE;
        setPreferredSize(new Dimension(panelSize, panelSize));
        setBackground(new Color(200, 155, 75));

        // Click chuột
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e)  { clearHover(); }
        });

        // Hover chuột
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isGameOver || isAiThinking) { clearHover(); return; }
                // Ở chế độ Online, nếu chưa đến lượt của mình thì không cho hover
                if (gameMode == GameMode.ONLINE && currentTurn != myPiece) { clearHover(); return; }

                int col = Math.round((float)(e.getX() - OFFSET) / CELL_SIZE);
                int row = Math.round((float)(e.getY() - OFFSET) / CELL_SIZE);
                if (board.isInBounds(row, col) && board.getPiece(row, col) == Piece.NONE) {
                    if (hoverRow != row || hoverCol != col) {
                        hoverRow = row; hoverCol = col; repaint();
                    }
                } else { clearHover(); }
            }
        });
    }

    private void clearHover() {
        if (hoverRow != -1 || hoverCol != -1) { hoverRow = -1; hoverCol = -1; repaint(); }
    }

    // =========================================================================
    public void setGameStateListener(GameStateListener l) {
        this.stateListener = l;
        if (l != null) { l.onTurnChanged(currentTurn, false); l.onHistoryChanged(false); }
    }

    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
        resetGame();
    }

    public GameMode getGameMode() { return gameMode; }

    public void setAiDifficulty(MinimaxAI.Difficulty d) { ai.setDifficulty(d); }

    public boolean canUndo() {
        // Trong chế độ Online không cho phép Undo 1 phía
        if (gameMode == GameMode.ONLINE) return false;
        return !isAiThinking && !moveHistory.isEmpty();
    }

    public void setupOnlineGame(NetworkManager nm, boolean isHost) {
        this.networkManager = nm;
        this.myPiece = isHost ? Piece.X : Piece.O;
        this.gameMode = GameMode.ONLINE;
        resetGame();
    }

    public Piece getMyPiece() { return myPiece; }
    public Piece getCurrentTurn() { return currentTurn; }
    public int getGameId() { return gameId; }
    public boolean isGameOver() { return isGameOver; }
    public Piece getWinner() { return winner; }
    public String getGameOverMessage() {
        if (!isGameOver) return null;
        return winner == Piece.NONE ? Lang.drawGame() : buildWinMessage(winner);
    }
    public boolean isAiThinking() { return isAiThinking; }
    public int getLastMoveRow() { return lastMoveRow; }
    public int getLastMoveCol() { return lastMoveCol; }
    public List<Move> getMoveHistory() { return Collections.unmodifiableList(moveHistory); }

    // =========================================================================
    public void resetGame() {
        gameId++;
        aiExecutionToken++;
        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
            aiWorker = null;
        }
        board.reset();
        moveHistory.clear();
        currentTurn = Piece.X;
        isGameOver = isAiThinking = false;
        winner = Piece.NONE;
        lastMoveRow = lastMoveCol = hoverRow = hoverCol = -1;
        setCursor(Cursor.getDefaultCursor());
        if (stateListener != null) {
            stateListener.onTurnChanged(currentTurn, false);
            stateListener.onHistoryChanged(false);
        }
        repaint();
    }

    // =========================================================================
    public void undoMove() {
        if (!canUndo()) return;

        aiExecutionToken++;
        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
            aiWorker = null;
        }
        isAiThinking = false;
        setCursor(Cursor.getDefaultCursor());

        if (gameMode == GameMode.PVP) {
            Move m = moveHistory.remove(moveHistory.size() - 1);
            Piece removedPiece = board.getPiece(m.row, m.col);
            board.undoMove(m.row, m.col);
            currentTurn = removedPiece;
            isGameOver = false;
        } else if (gameMode == GameMode.PV_AI) {
            isGameOver = false;
            if (!moveHistory.isEmpty()) {
                Move last = moveHistory.get(moveHistory.size() - 1);
                Piece lastPiece = board.getPiece(last.row, last.col);
                int desired = (lastPiece == Piece.O) ? 2 : 1;
                int toUndo = Math.min(desired, moveHistory.size());
                for (int i = 0; i < toUndo; i++) {
                    Move m = moveHistory.remove(moveHistory.size() - 1);
                    board.undoMove(m.row, m.col);
                }
            }
            currentTurn = Piece.X;
        }
        winner = Piece.NONE;

        if (!moveHistory.isEmpty()) {
            Move prev = moveHistory.get(moveHistory.size() - 1);
            lastMoveRow = prev.row; lastMoveCol = prev.col;
        } else { lastMoveRow = lastMoveCol = -1; }

        hoverRow = hoverCol = -1;

        SoundManager.playPlacePieceSound();
        if (stateListener != null) {
            stateListener.onTurnChanged(currentTurn, false);
            stateListener.onHistoryChanged(canUndo());
        }
        repaint();
    }

    // =========================================================================
    public boolean playMove(int row, int col) {
        if (isGameOver || isAiThinking) return false;

        // Ở chế độ Online: Không được đánh nếu chưa đến lượt của mình
        if (gameMode == GameMode.ONLINE && currentTurn != myPiece) return false;

        if (!board.isValidMove(row, col)) return false;

        executeMove(row, col, currentTurn);

        // Gửi nước đi sang máy đối thủ nếu đang chơi Online
        if (gameMode == GameMode.ONLINE && networkManager != null) {
            networkManager.sendMove(row, col);
        }

        // Kích hoạt AI nếu chơi với máy
        if (!isGameOver && gameMode == GameMode.PV_AI && currentTurn == Piece.O) {
            triggerAI();
        }
        return true;
    }

    private void handleClick(int px, int py) {
        int col = Math.round((float)(px - OFFSET) / CELL_SIZE);
        int row = Math.round((float)(py - OFFSET) / CELL_SIZE);
        playMove(row, col);
    }

    /**
     * Nhận nước đi từ mạng do đối thủ gửi tới.
     */
    public void applyOpponentMove(int row, int col) {
        final int receivedGameId = gameId;
        Runnable apply = () -> {
            if (receivedGameId != gameId || gameMode != GameMode.ONLINE
                    || isGameOver || currentTurn != myPiece.opposite()) {
                // Reject callbacks queued for an old game, wrong mode or local turn.
                return;
            }
            if (board.isValidMove(row, col)) {
                executeMove(row, col, myPiece.opposite());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) apply.run();
        else SwingUtilities.invokeLater(apply);
    }

    /** Stop background work when its owning window is disposed. */
    public void cancelAiComputation() {
        aiExecutionToken++;
        if (aiWorker != null) aiWorker.cancel(true);
        aiWorker = null;
        isAiThinking = false;
        setCursor(Cursor.getDefaultCursor());
    }

    private void executeMove(int row, int col, Piece piece) {
        board.makeMove(row, col, piece);
        moveHistory.add(new Move(row, col));
        lastMoveRow = row; lastMoveCol = col;
        hoverRow = hoverCol = -1;

        SoundManager.playPlacePieceSound();

        if (board.checkWin(row, col)) {
            isGameOver = true;
            winner = piece;
            SoundManager.playVictorySound();
            repaint();
            if (stateListener != null) {
                String msg = buildWinMessage(piece);
                stateListener.onGameOver(gameId, msg, piece);
                stateListener.onHistoryChanged(canUndo());
            }
            return;
        }
        if (board.isFull()) {
            isGameOver = true;
            winner = Piece.NONE;
            repaint();
            if (stateListener != null) {
                stateListener.onGameOver(gameId, Lang.drawGame(), Piece.NONE);
                stateListener.onHistoryChanged(canUndo());
            }
            return;
        }

        currentTurn = currentTurn.opposite();
        if (stateListener != null) {
            stateListener.onTurnChanged(currentTurn, false);
            stateListener.onHistoryChanged(canUndo());
        }
        repaint();
    }

    /**
     * Hỗ trợ kiểm thử trực tiếp nước cờ mà không cần click GUI.
     */
    public void executeMoveForTest(int row, int col, Piece piece) {
        executeMove(row, col, piece);
    }

    private String buildWinMessage(Piece winner) {
        if (gameMode == GameMode.ONLINE) {
            return (winner == myPiece) ? Lang.youWin() : Lang.youLose();
        } else if (gameMode == GameMode.PV_AI) {
            return (winner == Piece.X) ? Lang.youWin() : Lang.aiWins();
        } else {
            return (winner == Piece.X) ? Lang.p1Wins() : Lang.p2Wins();
        }
    }

    private void triggerAI() {
        isAiThinking = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (stateListener != null) { stateListener.onTurnChanged(currentTurn, true); stateListener.onHistoryChanged(false); }

        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
        }

        final int token = ++aiExecutionToken;
        final int capturedGameId = this.gameId;
        final int capturedMoveCount = this.moveHistory.size();
        final Board snap = board.copy();
        final MinimaxAI searchAi = new MinimaxAI(Piece.O, ai.getDifficulty());

        aiWorker = new SwingWorker<Move, Void>() {
            @Override protected Move doInBackground() { return searchAi.findBestMove(snap); }
            @Override protected void done() {
                try {
                    if (isCancelled() || token != aiExecutionToken || capturedGameId != gameId || capturedMoveCount != moveHistory.size()) {
                        return;
                    }
                    Move best = get();
                    isAiThinking = false;
                    setCursor(Cursor.getDefaultCursor());
                    if (stateListener != null)
                        stateListener.onAiStatsUpdated(searchAi.getExecutionTimeMs(), searchAi.getNodesEvaluated(), searchAi.getPruningCount());
                    if (best != null && !isGameOver) executeMove(best.row, best.col, Piece.O);
                } catch (Exception ex) {
                    if (token == aiExecutionToken) {
                        isAiThinking = false;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        };
        aiWorker.execute();
    }

    // =========================================================================
    // PAINTING
    // =========================================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2);
        drawGrid(g2);
        drawStarPoints(g2);
        drawHover(g2);
        drawStones(g2);
    }

    private void drawBoard(Graphics2D g2) {
        int boardPx  = (Board.SIZE - 1) * CELL_SIZE;
        int x0 = OFFSET - 8, y0 = OFFSET - 8;
        int bw = boardPx + 16, bh = boardPx + 16;

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x0 + 4, y0 + 4, bw, bh, 10, 10);

        g2.setColor(BOARD_COLOR);
        g2.fillRoundRect(x0, y0, bw, bh, 10, 10);

        g2.setColor(BOARD_DARK);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x0, y0, bw, bh, 10, 10);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(1.2f));
        int end = (Board.SIZE - 1) * CELL_SIZE;

        for (int i = 0; i < Board.SIZE; i++) {
            int x = OFFSET + i * CELL_SIZE;
            int y = OFFSET + i * CELL_SIZE;
            g2.drawLine(x, OFFSET, x, OFFSET + end);
            g2.drawLine(OFFSET, y, OFFSET + end, y);
        }

        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRect(OFFSET, OFFSET, end, end);
    }

    private void drawStarPoints(Graphics2D g2) {
        g2.setColor(STAR_COLOR);
        int[] pts = {3, 7, 11};
        for (int r : pts) {
            for (int c : pts) {
                int cx = OFFSET + c * CELL_SIZE;
                int cy = OFFSET + r * CELL_SIZE;
                g2.fillOval(cx - 4, cy - 4, 8, 8);
            }
        }
    }

    private void drawHover(Graphics2D g2) {
        if (hoverRow < 0 || hoverCol < 0 || isGameOver || isAiThinking) return;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        drawSingleStone(g2, hoverRow, hoverCol, currentTurn);
        g2.setComposite(old);
    }

    private void drawStones(Graphics2D g2) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                if (p != Piece.NONE) {
                    drawSingleStone(g2, r, c, p);
                    if (r == lastMoveRow && c == lastMoveCol) {
                        int cx = OFFSET + c * CELL_SIZE;
                        int cy = OFFSET + r * CELL_SIZE;
                        g2.setColor(LAST_MARKER);
                        g2.fillOval(cx - 4, cy - 4, 8, 8);
                    }
                }
            }
        }
    }

    private void drawSingleStone(Graphics2D g2, int row, int col, Piece piece) {
        int cx = OFFSET + col * CELL_SIZE;
        int cy = OFFSET + row * CELL_SIZE;
        int r  = STONE_RADIUS;

        if (piece == Piece.X) {
            g2.setColor(BLACK_STONE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(BLACK_HIGHLIGHT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(cx - r + 3, cy - r + 3, r, r, 45, 120);
        } else {
            g2.setColor(WHITE_STONE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(WHITE_BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(new Color(230, 230, 230, 180));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawArc(cx - r + 4, cy - r + 4, r, r, 50, 100);
        }
    }
}
