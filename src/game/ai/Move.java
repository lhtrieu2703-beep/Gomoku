package game.ai;

/**
 * Đại diện cho một nước đi trên bàn cờ kèm điểm số lượng giá.
 */
public class Move implements Comparable<Move> {
    public final int row;
    public final int col;
    public int score;

    public Move(int row, int col) {
        this(row, col, 0);
    }

    public Move(int row, int col, int score) {
        this.row = row;
        this.col = col;
        this.score = score;
    }

    /**
     * Sắp xếp các nước đi giảm dần theo điểm số để tối ưu cắt tỉa Alpha-Beta (Move Ordering).
     */
    @Override
    public int compareTo(Move other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d) [Score: %d]", row, col, score);
    }
}
