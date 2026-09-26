package game.model;

/**
 * Quản lý dữ liệu và luật chơi của Bàn cờ Caro 15x15.
 */
public class Board {
    public static final int SIZE = 15;
    public static final int WIN_COUNT = 5;

    private final Piece[][] grid;
    private int moveCount;

    public Board() {
        this.grid = new Piece[SIZE][SIZE];
        this.reset();
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

    public boolean isFull() {
        return moveCount >= SIZE * SIZE;
    }

    /**
     * Kiểm tra nước đi tại (lastRow, lastCol) có tạo ra 5 quân liên tiếp không.
     * Thuật toán kiểm tra 4 hướng đi qua điểm vừa đánh:
     * 1. Hướng ngang (dr = 0, dc = 1)
     * 2. Hướng dọc (dr = 1, dc = 0)
     * 3. Hướng chéo chính '\' (dr = 1, dc = 1)
     * 4. Hướng chéo phụ '/' (dr = 1, dc = -1)
     */
    public boolean checkWin(int lastRow, int lastCol) {
        if (!isInBounds(lastRow, lastCol)) return false;
        Piece target = grid[lastRow][lastCol];
        if (target == Piece.NONE) return false;

        // Mảng 4 hướng tương ứng [dr, dc]
        int[][] directions = {
            {0, 1},   // Ngang: Trái - Phải
            {1, 0},   // Dọc: Trên - Dưới
            {1, 1},   // Chéo chính: Tây Bắc - Đông Nam
            {1, -1}   // Chéo phụ: Đông Bắc - Tây Nam
        };

        for (int[] dir : directions) {
            int dr = dir[0];
            int dc = dir[1];
            int consecutive = 1; // Tính luôn quân cờ vừa đánh tại (lastRow, lastCol)

            // Đếm xuôi theo chiều vector (dr, dc)
            consecutive += countConsecutive(lastRow, lastCol, dr, dc, target);

            // Đếm ngược theo chiều ngược lại (-dr, -dc)
            consecutive += countConsecutive(lastRow, lastCol, -dr, -dc, target);

            // Nếu đạt từ 5 quân liên tiếp trở lên -> Thắng
            if (consecutive >= WIN_COUNT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hàm phụ trợ: đếm số quân liên tiếp cùng loại từ (row, col) theo bước nhảy (dr, dc).
     */
    private int countConsecutive(int row, int col, int dr, int dc, Piece target) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;

        while (isInBounds(r, c) && grid[r][c] == target) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
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
