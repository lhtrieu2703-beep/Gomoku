package game.view;

import game.model.Board;
import game.model.Piece;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Nonmodal board child: translucent wood, with real keyboard-accessible buttons. */
public final class EndGameOverlay extends JPanel {
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JLabel score = new JLabel("", SwingConstants.CENTER);
    private final JButton newGame = new ThemedButton("", ThemedButton.Style.PRIMARY);
    private final JButton menu = new ThemedButton("", ThemedButton.Style.SECONDARY);
    private final JButton viewBoard = new ThemedButton("", ThemedButton.Style.SECONDARY);
    private String winnerName = "", fullTitle = "";
    private Piece winner = Piece.NONE;
    private PieceStyle style = PieceStyle.CLASSIC;

    EndGameOverlay() {
        setLayout(null); setOpaque(false); setVisible(false);
        title.setForeground(Theme.CREAM); title.putClientProperty("html.disable", true);
        title.setIconTextGap(10);
        score.setForeground(Theme.BRASS); score.setFont(Theme.font(Font.PLAIN, 13));
        add(title); add(score); add(newGame); add(menu); add(viewBoard);
    }

    void update(String name, Piece piece, PieceStyle finish, int firstWins, int secondWins, int draws) {
        winnerName = name; winner = piece; style = finish;
        fullTitle = piece == Piece.NONE ? Lang.drawResult() : Lang.namedWinner(name);
        title.setToolTipText(fullTitle); title.getAccessibleContext().setAccessibleName(fullTitle);
        title.setIcon(piece == Piece.NONE ? null : new WinnerIcon(piece, finish));
        score.setText(Lang.resultScore(firstWins, secondWins, draws));
        newGame.setText(Lang.newGame()); menu.setText(Lang.backToMenu()); viewBoard.setText(Lang.viewBoard());
        getAccessibleContext().setAccessibleName(fullTitle);
        doLayout(); repaint();
    }

    @Override public void doLayout() {
        boolean compact = getWidth() < 400;
        title.setFont(Theme.font(Font.BOLD, compact ? 18 : 23));
        title.setBounds(12, compact ? 6 : 12, Math.max(0, getWidth() - 24), 38);
        FontMetrics metrics = title.getFontMetrics(title.getFont());
        int available = title.getWidth() - (title.getIcon() == null ? 0 : 46);
        String suffix = " " + Lang.victory();
        title.setText(winner == Piece.NONE ? fullTitle
                : TextFit.ellipsis(winnerName, metrics, available - metrics.stringWidth(suffix)) + suffix);
        score.setBounds(12, compact ? 42 : 52, Math.max(0, getWidth() - 24), 22);
        JButton[] buttons = {newGame, menu, viewBoard};
        if (compact) {
            for (int i = 0; i < buttons.length; i++) buttons[i].setBounds(12, 66 + i * 32, getWidth() - 24, 30);
        } else {
            int total = 0;
            for (JButton button : buttons) total += button.getPreferredSize().width;
            int x = (getWidth() - total - 16) / 2;
            for (JButton button : buttons) {
                int width = button.getPreferredSize().width;
                button.setBounds(x, 88, width, 40); x += width + 8;
            }
        }
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(91, 60, 35, 225), 0, getHeight(), new Color(42, 29, 21, 235)));
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 22, 22);
            g.setColor(Theme.BRASS); g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
        } finally { g.dispose(); }
    }

    /** Protect the entire grid cell plus the outer red/white win rings. */
    static boolean clearsLine(Rectangle bounds, List<Board.Cell> line) {
        int radius = BoardPanel.CELL_SIZE / 2 + 3;
        for (Board.Cell cell : line) {
            int x = BoardPanel.OFFSET + cell.col() * BoardPanel.CELL_SIZE;
            int y = BoardPanel.OFFSET + cell.row() * BoardPanel.CELL_SIZE;
            if (bounds.intersects(new Rectangle(x - radius, y - radius, radius * 2, radius * 2))) return false;
        }
        return true;
    }

    static Rectangle boundsFor(List<Board.Cell> line, Dimension board) {
        double middle = line.stream().mapToInt(Board.Cell::row).average().orElse(0);
        boolean bottom = line.isEmpty() || middle <= (Board.SIZE - 1) / 2.0;
        Rectangle band = new Rectangle(24, bottom ? board.height - 170 : 24, board.width - 48, 146);
        if (clearsLine(band, line)) return band;
        // A vertical/diagonal overline can span both halves. Keep its rings visible
        // by stacking the actions in the free corner of the preferred half.
        for (int x : new int[]{24, board.width - 272}) {
            Rectangle corner = new Rectangle(x, bottom ? board.height - 198 : 24, 248, 174);
            if (clearsLine(corner, line)) return corner;
        }
        throw new IllegalStateException("No clear corner for a straight winning line");
    }

    public String getFullTitle() { return fullTitle; }
    public JLabel getTitleLabel() { return title; }
    public JLabel getScoreLabel() { return score; }
    public JButton getNewGameButton() { return newGame; }
    public JButton getMenuButton() { return menu; }
    public JButton getViewBoardButton() { return viewBoard; }
    public Piece getWinner() { return winner; }
    public PieceStyle getPieceStyle() { return style; }

    private record WinnerIcon(Piece piece, PieceStyle style) implements Icon {
        public int getIconWidth() { return 36; }
        public int getIconHeight() { return 36; }
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                StonePainter.paint(g, x + 18, y + 18, 15, piece, style);
            } finally { g.dispose(); }
        }
    }
}
