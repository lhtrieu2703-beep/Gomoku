package game.model;

/**
 * Quản lý dữ liệu và luật chơi của Bàn cờ Caro 15x15.
 */
public class Board {
    public static final int SIZE = 15;
    public static final int WIN_COUNT = 5;

    public enum WinRule {
        RULE_A, // Chính xác 5 quân liên tiếp (không tính 6+)
        RULE_B  // Từ 5 quân trở lên (5, 6, 7...)
    }

    private final Piece[][] grid;
    private int moveCount;
    private WinRule winRule = WinRule.RULE_B;

    public Board() {
        this.grid = new Piece[SIZE][SIZE];
        this.reset();
    }

    public void setWinRule(WinRule winRule) {
        this.winRule = winRule;
    }

    public WinRule getWinRule() {
        return winRule;
    }

    /**
     * Khởi tạo lại bàn cờ về trạng thái trống ban đầu.
     */
    public void reset() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = Piece.NONE;
            }
        }
        moveCount = 0;
    }

    /**
     * Kiểm tra tọa độ có nằm trong phạm vi bàn cờ hay không.
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * Kiểm tra một nước đi có hợp lệ không (trong bàn cờ và ô chưa có ai đánh).
     */
    public boolean isValidMove(int row, int col) {
        return isInBounds(row, col) && grid[row][col] == Piece.NONE;
    }

    /**
     * Đặt quân cờ vào vị trí (row, col).
     * @return true nếu đặt thành công, false nếu không hợp lệ.
     */
    public boolean makeMove(int row, int col, Piece piece) {
        if (!isValidMove(row, col) || piece == Piece.NONE) {
            return false;
        }
        grid[row][col] = piece;
        moveCount++;
        return true;
    }

    /**
     * Hoàn tác lại nước đi (rút quân cờ ra).
     * Dùng cho thuật toán tìm kiếm của AI (Minimax).
     */
    public void undoMove(int row, int col) {
        if (isInBounds(row, col) && grid[row][col] != Piece.NONE) {
            grid[row][col] = Piece.NONE;
            moveCount--;
        }
    }

    /**
     * Tạo bản sao độc lập của bàn cờ.
     * Dùng riêng cho luồng tính toán AI để tránh xung đột dữ liệu với luồng giao diện (Race Condition).
     */
    public Board copy() {
        Board clone = new Board();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                clone.grid[r][c] = this.grid[r][c];
            }
        }
        clone.moveCount = this.moveCount;
        clone.winRule = this.winRule;
        return clone;
    }

    /**
     * Lấy quân cờ tại vị trí (row, col).
     */
    public Piece getPiece(int row, int col) {
        if (!isInBounds(row, col)) {
            return Piece.NONE;
        }
        return grid[row][col];
    }

    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Kiểm tra toàn bộ 225 ô đã được đánh kín hay chưa.
     */
    public boolean isFull() {
        return moveCount >= SIZE * SIZE;
    }

    /**
     * Kiểm tra nước đi tại (lastRow, lastCol) có tạo thành thế thắng theo luật hiện tại của bàn cờ hay không.
     */
    public boolean checkWin(int lastRow, int lastCol) {
        return checkWin(lastRow, lastCol, this.winRule);
    }

    /**
     * Kiểm tra nước đi tại (lastRow, lastCol) có tạo thành thế thắng theo RULE cụ thể hay không.
     */
    public boolean checkWin(int lastRow, int lastCol, WinRule rule) {
        if (!isInBounds(lastRow, lastCol)) return false;
        Piece target = grid[lastRow][lastCol];
        if (target == Piece.NONE) return false;
        Piece opp = target.opposite();

        // 4 hướng: Ngang, Dọc, Chéo chính '\', Chéo phụ '/'
        int[][] directions = {
            {0, 1},   // Ngang: Trái - Phải
            {1, 0},   // Dọc: Trên - Dưới
            {1, 1},   // Chéo chính: Tây Bắc - Đông Nam
            {1, -1}   // Chéo phụ: Đông Bắc - Tây Nam
        };

        for (int[] dir : directions) {
            int dr = dir[0];
            int dc = dir[1];

            // 1. Đếm xuôi theo vector (dr, dc)
            int forwardCount = 0;
            int rF = lastRow + dr;
            int cF = lastCol + dc;
            while (isInBounds(rF, cF) && grid[rF][cF] == target) {
                forwardCount++;
                rF += dr;
                cF += dc;
            }
            // Đầu xuôi bị chặn nếu nằm trong bàn cờ và có quân đối thủ
            boolean blockedForward = isInBounds(rF, cF) && grid[rF][cF] == opp;

            // 2. Đếm ngược theo vector (-dr, -dc)
            int backwardCount = 0;
            int rB = lastRow - dr;
            int cB = lastCol - dc;
            while (isInBounds(rB, cB) && grid[rB][cB] == target) {
                backwardCount++;
                rB -= dr;
                cB -= dc;
            }
            // Đầu ngược bị chặn nếu nằm trong bàn cờ và có quân đối thủ
            boolean blockedBackward = isInBounds(rB, cB) && grid[rB][cB] == opp;

            int consecutive = 1 + forwardCount + backwardCount;

            // Kiểm tra điều kiện độ dài theo từng RULE
            boolean lengthValid = (rule == WinRule.RULE_A) ? (consecutive == WIN_COUNT) : (consecutive >= WIN_COUNT);

            if (lengthValid) {
                // Chỉ khi bị chặn CẢ HAI ĐẦU bởi quân đối thủ mới KHÔNG THẮNG
                if (!(blockedForward && blockedBackward)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * In bàn cờ ra console để kiểm tra trực quan.
     */
    public void printBoard() {
        System.out.print("   ");
        for (int c = 0; c < SIZE; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println();

        for (int r = 0; r < SIZE; r++) {
            System.out.printf("%2d ", r);
            for (int c = 0; c < SIZE; c++) {
                System.out.printf(" %s ", grid[r][c].getSymbol());
            }
            System.out.println();
        }
    }
}
