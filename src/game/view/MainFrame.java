package game.view;

import game.ai.MinimaxAI;
import game.model.Board;
import game.model.Piece;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Cửa sổ chính "Gomoku":
 * - Hỗ trợ 2 ngôn ngữ (Tiếng Việt có dấu / English) với nút chuyển ngôn ngữ.
 * - Bảng thành tích (Scoreboard) hiển thị Thắng / Thua / Hòa.
 * - Giao diện hiện đại tối (dark header) + bàn cờ gỗ.
 */
public class MainFrame extends JFrame {
    private final Board board;
    private final BoardPanel boardPanel;

    // === Components cần cập nhật khi đổi ngôn ngữ ===
    private final JLabel  lblMode, lblDiff, turnLabel;
    private final JButton btnNewGame, btnUndo, btnLang;
    private final JCheckBox chkSound;
    private final JComboBox<BoardPanel.GameMode>   modeCombo;
    private final JComboBox<MinimaxAI.Difficulty>  diffCombo;

    // === Scoreboard ===
    private JLabel lblWinVal, lblLossVal, lblDrawVal;
    private JLabel lblWinTxt, lblLossTxt, lblDrawTxt;
    private JLabel lblScoreTitle;

    private int humanWins = 0, aiWins = 0, draws = 0;

    // =========================================================================
    public MainFrame() {
        this.board = new Board();
        this.boardPanel = new BoardPanel(board);

        setTitle("Gomoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(0, 0));

        // =====================================================================
        // 1. HEADER TỐI (điều khiển chính)
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(34, 36, 40));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));

        // --- Hàng điều khiển ---
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ctrlRow.setOpaque(false);

        lblMode = makeHeaderLabel(Lang.mode());
        modeCombo = new JComboBox<>();
        for (BoardPanel.GameMode gm : BoardPanel.GameMode.values()) modeCombo.addItem(gm);
        modeCombo.setSelectedItem(BoardPanel.GameMode.PV_AI);
        styleCombo(modeCombo);

        lblDiff = makeHeaderLabel(Lang.difficulty());
        diffCombo = new JComboBox<>();
        for (MinimaxAI.Difficulty d : MinimaxAI.Difficulty.values()) diffCombo.addItem(d);
        diffCombo.setSelectedItem(MinimaxAI.Difficulty.MEDIUM);
        // Renderer cho Difficulty (gọi Lang.xxx())
        diffCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MinimaxAI.Difficulty d) {
                    setText(switch (d) {
                        case EASY   -> Lang.easy();
                        case MEDIUM -> Lang.medium();
                        case HARD   -> Lang.hard();
                    });
                }
                return this;
            }
        });
        styleCombo(diffCombo);

        btnNewGame = makeDarkBtn(Lang.newGame(), new Color(46, 160, 67), Color.WHITE);
        btnUndo    = makeDarkBtn(Lang.undo(),    new Color(33, 113, 195), Color.WHITE);
        btnUndo.setEnabled(false);

        chkSound = new JCheckBox(Lang.sound(), true);
        chkSound.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSound.setForeground(new Color(210, 210, 215));
        chkSound.setOpaque(false);
        chkSound.setFocusPainted(false);

        // Nút chuyển ngôn ngữ — nổi bật ở góc phải
        btnLang = makeDarkBtn(Lang.langBtnLabel(), new Color(100, 65, 165), Color.WHITE);
        btnLang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLang.setToolTipText(Lang.langBtnTooltip());

        ctrlRow.add(lblMode); ctrlRow.add(modeCombo);
        ctrlRow.add(Box.createHorizontalStrut(6));
        ctrlRow.add(lblDiff); ctrlRow.add(diffCombo);
        ctrlRow.add(Box.createHorizontalStrut(10));
        ctrlRow.add(btnNewGame); ctrlRow.add(btnUndo);
        ctrlRow.add(Box.createHorizontalStrut(6));
        ctrlRow.add(chkSound);

        JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightRow.setOpaque(false);
        rightRow.add(btnLang);

        header.add(ctrlRow, BorderLayout.CENTER);
        header.add(rightRow, BorderLayout.EAST);

        // =====================================================================
        // 2. BẢNG THÀNH TÍCH (SCOREBOARD)
        // =====================================================================
        JPanel scoreBar = buildScoreBar();

        // =====================================================================
        // 3. THANH LƯỢT CHƠI (ngay trên bàn cờ)
        // =====================================================================
        JPanel turnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        turnBar.setBackground(new Color(245, 241, 232));
        turnBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 175, 120)));

        turnLabel = new JLabel(Lang.yourTurn());
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        turnLabel.setForeground(new Color(30, 30, 30));
        turnBar.add(turnLabel);

        // Gom các dải phía trên
        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(scoreBar, BorderLayout.CENTER);
        north.add(turnBar, BorderLayout.SOUTH);

        // =====================================================================
        // 4. ĐĂNG KÝ SỰ KIỆN
        // =====================================================================
        modeCombo.addActionListener(e -> {
            BoardPanel.GameMode sel = (BoardPanel.GameMode) modeCombo.getSelectedItem();
            diffCombo.setEnabled(sel == BoardPanel.GameMode.PV_AI);
            boardPanel.setGameMode(sel);
        });

        diffCombo.addActionListener(e ->
            boardPanel.setAiDifficulty((MinimaxAI.Difficulty) diffCombo.getSelectedItem()));

        btnNewGame.addActionListener(e -> boardPanel.resetGame());
        btnUndo.addActionListener(e -> boardPanel.undoMove());
        chkSound.addActionListener(e -> SoundManager.setSoundEnabled(chkSound.isSelected()));

        btnLang.addActionListener(e -> {
            Lang.toggle();
            updateLanguage();
        });

        // Lắng nghe trạng thái từ BoardPanel
        boardPanel.setGameStateListener(new BoardPanel.GameStateListener() {
            @Override
            public void onTurnChanged(Piece currentTurn, boolean isAiThinking) {
                BoardPanel.GameMode mode = (BoardPanel.GameMode) modeCombo.getSelectedItem();
                if (isAiThinking) {
                    setTurnLabel(Lang.aiThinking(), new Color(200, 100, 20));
                } else if (mode == BoardPanel.GameMode.PV_AI) {
                    if (currentTurn == Piece.X) setTurnLabel(Lang.yourTurn(),  new Color(20, 80, 170));
                    else                        setTurnLabel(Lang.aiTurn(),    new Color(160, 30, 30));
                } else {
                    if (currentTurn == Piece.X) setTurnLabel(Lang.p1Turn(), new Color(20, 80, 170));
                    else                        setTurnLabel(Lang.p2Turn(), new Color(160, 30, 30));
                }
            }

            @Override
            public void onGameOver(String message, Piece winner) {
                if (winner == Piece.X)        { humanWins++; setTurnLabel(message, new Color(30, 130, 50)); }
                else if (winner == Piece.O)   { aiWins++;    setTurnLabel(message, new Color(160, 30, 30)); }
                else                          { draws++;     setTurnLabel(message, new Color(120, 80, 20)); }
                refreshScores();
                JOptionPane.showMessageDialog(MainFrame.this, message, Lang.gameOverTitle(), JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onAiStatsUpdated(long t, long n, long p) { /* ẩn */ }

            @Override
            public void onHistoryChanged(boolean canUndo) { btnUndo.setEnabled(canUndo); }
        });

        // Lắng nghe thay đổi ngôn ngữ (để cập nhật turnLabel sau khi reset)
        Lang.addChangeListener(this::updateLanguage);

        // =====================================================================
        // 5. LẮP RÁP
        // =====================================================================
        add(north, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    // =========================================================================
    // Cập nhật toàn bộ text khi đổi ngôn ngữ
    // =========================================================================
    private void updateLanguage() {
        setTitle("Gomoku");
        lblMode.setText(Lang.mode());
        lblDiff.setText(Lang.difficulty());
        btnNewGame.setText(Lang.newGame());
        btnUndo.setText(Lang.undo());
        chkSound.setText(Lang.sound());
        btnLang.setText(Lang.langBtnLabel());
        btnLang.setToolTipText(Lang.langBtnTooltip());

        // Cập nhật combo GameMode (toString() của enum gọi Lang.xxx())
        modeCombo.repaint();

        // Cập nhật combo Difficulty (renderer gọi Lang.xxx())
        diffCombo.repaint();

        // Cập nhật Scoreboard labels
        lblScoreTitle.setText(Lang.scoreboard());
        lblWinTxt.setText(Lang.wins());
        lblLossTxt.setText(Lang.losses());
        lblDrawTxt.setText(Lang.draws());

        // Cập nhật lượt chơi hiện tại (gọi lại với cùng màu)
        String cur = turnLabel.getText();
        // Reset lại text theo ngôn ngữ mới — gọi lại sự kiện onTurnChanged một cách đơn giản
        boardPanel.resetGame();

        revalidate();
        repaint();
    }

    // =========================================================================
    // Xây dựng bảng thành tích
    // =========================================================================
    private JPanel buildScoreBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 7));
        bar.setBackground(new Color(44, 47, 55));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 75, 165)));

        // Tiêu đề
        lblScoreTitle = new JLabel(Lang.scoreboard());
        lblScoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblScoreTitle.setForeground(new Color(180, 155, 230));
        bar.add(lblScoreTitle);

        // Ngăn cách
        bar.add(makeSep());

        // Thắng (Xanh lá)
        lblWinTxt = makeScoreLabel(Lang.wins(), new Color(100, 200, 120), false);
        lblWinVal = makeScoreLabel("0", new Color(100, 200, 120), true);
        bar.add(lblWinTxt); bar.add(lblWinVal);

        bar.add(makeSep());

        // Thua (Đỏ)
        lblLossTxt = makeScoreLabel(Lang.losses(), new Color(220, 100, 100), false);
        lblLossVal = makeScoreLabel("0", new Color(220, 100, 100), true);
        bar.add(lblLossTxt); bar.add(lblLossVal);

        bar.add(makeSep());

        // Hòa (Vàng)
        lblDrawTxt = makeScoreLabel(Lang.draws(), new Color(220, 185, 80), false);
        lblDrawVal = makeScoreLabel("0", new Color(220, 185, 80), true);
        bar.add(lblDrawTxt); bar.add(lblDrawVal);

        return bar;
    }

    private void refreshScores() {
        lblWinVal.setText(String.valueOf(humanWins));
        lblLossVal.setText(String.valueOf(aiWins));
        lblDrawVal.setText(String.valueOf(draws));
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private void setTurnLabel(String text, Color color) {
        turnLabel.setText(text);
        turnLabel.setForeground(color);
    }

    private JLabel makeHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(190, 190, 200));
        return l;
    }

    private JLabel makeScoreLabel(String text, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, bold ? 15 : 11));
        l.setForeground(color);
        return l;
    }

    private JLabel makeSep() {
        JLabel sep = new JLabel("|");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sep.setForeground(new Color(80, 85, 100));
        return sep;
    }

    private JButton makeDarkBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBackground(new Color(55, 58, 65));
        c.setForeground(Color.WHITE);
        c.setFocusable(false);
    }
}
