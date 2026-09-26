package game.model;

/**
 * Đại diện cho trạng thái của một ô cờ hoặc loại quân cờ.
 */
public enum Piece {
    NONE("-"),
    X("X"),
    O("O");

    private final String symbol;

    Piece(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Lấy quân đối thủ của quân hiện tại.
     * X -> O, O -> X, NONE -> NONE.
     */
    public Piece opposite() {
        if (this == X) return O;
        if (this == O) return X;
        return NONE;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
