package game.view;

import game.ai.MinimaxAI;
import game.model.Board;
import game.model.Piece;
import game.model.ScoreTracker;
import game.network.NetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Cửa sổ chính "Gomoku":
 * - Hỗ trợ 2 ngôn ngữ (Tiếng Việt có dấu / English) với nút chuyển ngôn ngữ.
 * - Hỗ trợ 3 chế độ: Người vs Người, Người vs Máy (AI), Chơi Online (qua LAN Socket).
 * - Bảng thành tích (Scoreboard) hiển thị Thắng / Thua / Hòa.
 * - Giao diện hiện đại tối (dark header) + bàn cờ gỗ.
 */
public class MainFrame extends JFrame {
    private final Board board;
    private final BoardPanel boardPanel;

    // === Components điều khiển ===
    private final JLabel lblMode, lblDiff, turnLabel;
    private final JButton btnNewGame, btnUndo, btnLang;
    private final JCheckBox chkSound;
    private final JComboBox<BoardPanel.GameMode> modeCombo;
    private final JComboBox<MinimaxAI.Difficulty> diffCombo;

    // === Scoreboard ===
    private JLabel lblWinVal, lblLossVal, lblDrawVal;
    private JLabel lblWinTxt, lblLossTxt, lblDrawTxt;
    private JLabel lblScoreTitle;

    private final ScoreTracker scoreTracker = new ScoreTracker();
    private int humanWins = 0, aiWins = 0, draws = 0;

    // === Chế độ chơi Online ===
    private NetworkManager networkManager;
    private NetworkDialog networkDialog;

    public MainFrame() {
        this.board = new Board();
        this.boardPanel = new BoardPanel(board);

        setTitle("Gomoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(0, 0));

        // Khởi tạo NetworkManager với các listener sự kiện
        initNetworkManager();

        // Đảm bảo ngắt socket khi tắt ứng dụng
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (networkManager != null) {
                    networkManager.disconnect();
                }
            }
        });

        // =====================================================================
        // 1. HEADER TỐI (Điều khiển chính)
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(34, 36, 40));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));

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
        // 3. THANH LƯỢT CHƠI
        // =====================================================================
        JPanel turnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        turnBar.setBackground(new Color(245, 241, 232));
        turnBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 175, 120)));

        turnLabel = new JLabel(Lang.yourTurn());
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        turnLabel.setForeground(new Color(30, 30, 30));
        turnBar.add(turnLabel);

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

            if (sel == BoardPanel.GameMode.ONLINE) {
                btnUndo.setEnabled(false);
                openNetworkDialog();
            } else {
                if (networkManager != null && networkManager.isConnected()) {
                    networkManager.disconnect();
                }
                boardPanel.setGameMode(sel);
            }
        });

        diffCombo.addActionListener(e ->
            boardPanel.setAiDifficulty((MinimaxAI.Difficulty) diffCombo.getSelectedItem()));

        btnNewGame.addActionListener(e -> {
            if (boardPanel.getGameMode() == BoardPanel.GameMode.ONLINE) {
                if (networkManager != null && networkManager.isConnected()) {
                    networkManager.sendRestart();
                }
            }
            scoreTracker.newGame();
            boardPanel.resetGame();
        });

        btnUndo.addActionListener(e -> boardPanel.undoMove());
        chkSound.addActionListener(e -> SoundManager.setSoundEnabled(chkSound.isSelected()));

        btnLang.addActionListener(e -> {
            Lang.toggle();
            updateLanguage();
        });

        boardPanel.setGameStateListener(new BoardPanel.GameStateListener() {
            @Override
            public void onTurnChanged(Piece currentTurn, boolean isAiThinking) {
                BoardPanel.GameMode mode = boardPanel.getGameMode();
                if (mode == BoardPanel.GameMode.ONLINE) {
                    Piece myP = boardPanel.getMyPiece();
                    if (currentTurn == myP) {
                        setTurnLabel(Lang.onlineYourTurn(), new Color(20, 120, 50));
                    } else {
                        setTurnLabel(Lang.onlineOpponentTurn(), new Color(200, 100, 20));
                    }
                } else if (isAiThinking) {
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
                onGameOver(boardPanel.getGameId(), message, winner);
            }

            @Override
            public void onGameOver(int gameId, String message, Piece winner) {
                BoardPanel.GameMode mode = boardPanel.getGameMode();
                Piece humanPiece = (mode == BoardPanel.GameMode.ONLINE) ? boardPanel.getMyPiece() : Piece.X;
                boolean isNewRecord = scoreTracker.recordResult(gameId, winner, humanPiece);
                if (isNewRecord) {
                    humanWins = scoreTracker.getHumanWins();
                    aiWins = scoreTracker.getAiWins();
                    draws = scoreTracker.getDraws();
                    refreshScores();
                }

                if (winner == Piece.NONE) {
                    setTurnLabel(message, new Color(120, 80, 20));
                } else if (winner == humanPiece) {
                    setTurnLabel(message, new Color(30, 130, 50));
                } else {
                    setTurnLabel(message, new Color(160, 30, 30));
                }

                JOptionPane.showMessageDialog(MainFrame.this, message, Lang.gameOverTitle(), JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onAiStatsUpdated(long t, long n, long p) {}

            @Override
            public void onHistoryChanged(boolean canUndo) {
                btnUndo.setEnabled(canUndo);
            }
        });

        Lang.addChangeListener(this::updateLanguage);

        // =====================================================================
        // 5. LẮP RÁP
        // =====================================================================
        add(north, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void initNetworkManager() {
        this.networkManager = new NetworkManager(new NetworkManager.NetworkListener() {
            @Override
            public void onConnected(boolean isHost) {
                SwingUtilities.invokeLater(() -> {
                    if (networkDialog != null) networkDialog.onConnectionSuccess();
                    boardPanel.setupOnlineGame(networkManager, isHost);

                    String msg = isHost ? Lang.onlineConnectedHost() : Lang.onlineConnectedClient();
                    JOptionPane.showMessageDialog(MainFrame.this, msg,
                            Lang.isVI() ? "Kết nối thành công" : "Connected Successfully",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onMoveReceived(int row, int col) {
                boardPanel.applyOpponentMove(row, col);
            }

            @Override
            public void onRestartRequested() {
                SwingUtilities.invokeLater(() -> {
                    scoreTracker.newGame();
                    boardPanel.resetGame();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            Lang.isVI() ? "Đối thủ đã bắt đầu ván mới!" : "Opponent restarted the game!",
                            Lang.isVI() ? "Ván mới" : "New Game",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onOpponentDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            Lang.opponentDisconnected(),
                            Lang.isVI() ? "Mất kết nối" : "Disconnected",
                            JOptionPane.WARNING_MESSAGE);
                    modeCombo.setSelectedItem(BoardPanel.GameMode.PV_AI);
                });
            }

            @Override
            public void onError(String message) {
                if (networkDialog != null) {
                    networkDialog.onConnectionFailed(message);
                }
            }
        });
    }

    private void openNetworkDialog() {
        networkDialog = new NetworkDialog(this, networkManager, () -> {
            // Khi kết nối thành công, không cần làm gì thêm vì callback onConnected đã lo
        });
        networkDialog.setVisible(true);
    }

    private void updateLanguage() {
        setTitle("Gomoku");
        lblMode.setText(Lang.mode());
        lblDiff.setText(Lang.difficulty());
        btnNewGame.setText(Lang.newGame());
        btnUndo.setText(Lang.undo());
        chkSound.setText(Lang.sound());
        btnLang.setText(Lang.langBtnLabel());
        btnLang.setToolTipText(Lang.langBtnTooltip());

        modeCombo.repaint();
        diffCombo.repaint();

        lblScoreTitle.setText(Lang.scoreboard());
        lblWinTxt.setText(Lang.wins());
        lblLossTxt.setText(Lang.losses());
        lblDrawTxt.setText(Lang.draws());

        boardPanel.resetGame();
        revalidate();
        repaint();
    }

    private JPanel buildScoreBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 7));
        bar.setBackground(new Color(44, 47, 55));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 75, 165)));

        lblScoreTitle = new JLabel(Lang.scoreboard());
        lblScoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblScoreTitle.setForeground(new Color(180, 155, 230));
        bar.add(lblScoreTitle);

        bar.add(makeSep());

        lblWinTxt = makeScoreLabel(Lang.wins(), new Color(100, 200, 120), false);
        lblWinVal = makeScoreLabel("0", new Color(100, 200, 120), true);
        bar.add(lblWinTxt); bar.add(lblWinVal);

        bar.add(makeSep());

        lblLossTxt = makeScoreLabel(Lang.losses(), new Color(220, 100, 100), false);
        lblLossVal = makeScoreLabel("0", new Color(220, 100, 100), true);
        bar.add(lblLossTxt); bar.add(lblLossVal);

        bar.add(makeSep());

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

    public ScoreTracker getScoreTracker() {
        return scoreTracker;
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
}
