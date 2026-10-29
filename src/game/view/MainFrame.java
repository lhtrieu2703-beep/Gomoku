package game.view;

import game.ai.MinimaxAI;
import game.model.Board;
import game.model.Piece;
import game.model.ScoreTracker;
import game.network.NetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cửa sổ chính "Gomoku":
 * - Hỗ trợ 3 ngôn ngữ (Tiếng Việt / English / Português) với JComboBox chọn ngôn ngữ.
 * - Hỗ trợ 3 chế độ: Người vs Người, Người vs Máy (AI), Chơi Online (qua LAN Socket).
 * - Bảng thành tích (Scoreboard) hiển thị Thắng / Thua / Hòa.
 * - Giao diện hiện đại tối (dark header) + bàn cờ gỗ.
 * - Nút âm thanh dạng icon Java2D (loa bật/tắt).
 * - Lưu cài đặt ngôn ngữ & âm thanh qua AppSettings (java.util.prefs.Preferences).
 */
public class MainFrame extends JFrame {
    private final Board board;
    private final BoardPanel boardPanel;
    private static final String MENU_CARD = "menu", GAME_CARD = "game";
    private final CardLayout screenLayout = new CardLayout();
    private final JPanel screenCards = new JPanel(screenLayout);
    private final JPanel gameCard = new JPanel(new BorderLayout());
    private final StartScreen startScreen;
    private final JButton btnBackToMenu, btnAchievements;
    private final JLabel playerLabel;
    private String playerName;
    private boolean menuVisible = true;
    private volatile long screenEpoch;
    private JDialog achievementsDialog;
    private JTextArea achievementsText;

    // === Components điều khiển ===
    private final JLabel lblMode, lblDiff, turnLabel;
    private final JButton btnNewGame, btnUndo;
    // Nút âm thanh icon (thay JCheckBox cũ)
    private JButton btnSound;
    // Combo chọn ngôn ngữ (thay btnLang cũ)
    private JComboBox<Lang.Language> langCombo;
    private boolean langComboUpdating = false; // tránh listener loop

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
    private boolean modeComboUpdating;
    private boolean disposed;
    private final Runnable languageListener = this::updateLanguage;
    private JDialog resultDialog;

    // === Cài đặt ===
    private final AppSettings appSettings;

    public MainFrame() {
        this(new AppSettings());
    }

    /** Constructor có thể inject AppSettings — dùng cho test. */
    public MainFrame(AppSettings settings) {
        this.appSettings = settings;
        this.board = new Board();
        this.boardPanel = new BoardPanel(board);
        this.boardPanel.setEnabled(false);

        // Load cài đặt TRƯỚC khi dựng UI
        Lang.set(appSettings.loadLang());
        SoundManager.setSoundEnabled(appSettings.loadSound());
        playerName = appSettings.loadPlayerName();
        startScreen = new StartScreen(playerName, this::startGame, this::selectLanguage);

        setTitle(Lang.startTitle());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(0, 0));

        // Khởi tạo NetworkManager
        initNetworkManager();

        // =====================================================================
        // 1. HEADER TỐI
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(Theme.DARK);
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
        diffCombo.setRenderer(makeDiffRenderer());
        styleCombo(diffCombo);

        btnNewGame = makeDarkBtn(Lang.newGame(), Theme.BRASS, Color.WHITE);
        btnUndo    = makeDarkBtn(Lang.undo(),    Theme.BRASS, Color.WHITE);
        btnUndo.setEnabled(false);

        // Nút âm thanh — icon Java2D
        btnSound = makeSoundButton();

        // Combo ngôn ngữ
        langCombo = makeLangCombo();
        btnBackToMenu = makeDarkBtn(Lang.backToMenu(), Theme.DARK, Color.WHITE);
        btnAchievements = makeDarkBtn("", Theme.DARK, Color.WHITE);
        btnAchievements.setIcon(new AchievementIcon(20));
        btnAchievements.setToolTipText(Lang.achievementsTooltip());
        btnAchievements.getAccessibleContext().setAccessibleName(Lang.achievementsTooltip());

        ctrlRow.add(lblMode); ctrlRow.add(modeCombo);
        ctrlRow.add(Box.createHorizontalStrut(6));
        ctrlRow.add(lblDiff); ctrlRow.add(diffCombo);
        ctrlRow.add(Box.createHorizontalStrut(10));
        ctrlRow.add(btnNewGame); ctrlRow.add(btnUndo);
        ctrlRow.add(Box.createHorizontalStrut(6));
        ctrlRow.add(btnSound);

        JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightRow.setOpaque(false);
        rightRow.add(btnAchievements);
        rightRow.add(Box.createHorizontalStrut(8));
        rightRow.add(langCombo);

        JPanel navigation = new JPanel(new BorderLayout(12, 0));
        navigation.setOpaque(false);
        navigation.add(btnBackToMenu, BorderLayout.WEST);
        navigation.add(rightRow, BorderLayout.EAST);
        header.add(navigation, BorderLayout.NORTH);
        header.add(ctrlRow, BorderLayout.CENTER);

        // =====================================================================
        // 2. BẢNG THÀNH TÍCH
        // =====================================================================
        JPanel scoreBar = buildScoreBar();

        // =====================================================================
        // 3. THANH LƯỢT CHƠI
        // =====================================================================
        JPanel turnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        turnBar.setBackground(Theme.CREAM);
        turnBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BRASS));

        turnLabel = new JLabel(Lang.yourTurn());
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        turnLabel.setForeground(Theme.DARK);
        playerLabel = new JLabel();
        playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        playerLabel.putClientProperty("html.disable", Boolean.TRUE);
        turnBar.add(playerLabel);
        turnBar.add(Box.createHorizontalStrut(16));
        turnBar.add(turnLabel);

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(scoreBar, BorderLayout.CENTER);
        north.add(turnBar, BorderLayout.SOUTH);

        // =====================================================================
        // 4. ĐĂNG KÝ SỰ KIỆN
        // =====================================================================
        modeCombo.addActionListener(e -> {
            if (modeComboUpdating || menuVisible || disposed) return;
            BoardPanel.GameMode sel = (BoardPanel.GameMode) modeCombo.getSelectedItem();
            diffCombo.setEnabled(sel == BoardPanel.GameMode.PV_AI);

            if (sel == BoardPanel.GameMode.ONLINE) {
                btnUndo.setEnabled(false);
                openNetworkDialog();
            } else {
                closeResultDialog();
                networkManager.disconnect();
                boardPanel.setGameMode(sel);
            }
        });

        diffCombo.addActionListener(e ->
            boardPanel.setAiDifficulty((MinimaxAI.Difficulty) diffCombo.getSelectedItem()));

        btnNewGame.addActionListener(e -> {
            if (menuVisible || disposed) return;
            if (boardPanel.getGameMode() == BoardPanel.GameMode.ONLINE) {
                if (networkManager.requestRestart(boardPanel.getNetworkRoundId()) && !networkManager.isHost())
                    boardPanel.setRestartWaiting(true);
                return;
            }
            closeResultDialog();
            scoreTracker.newGame();
            boardPanel.resetGame();
        });

        btnUndo.addActionListener(e -> { if (!menuVisible && !disposed) boardPanel.undoMove(); });
        btnBackToMenu.addActionListener(e -> returnToMenu());
        btnAchievements.addActionListener(e -> showScoreSummary());

        // Listener nút âm thanh
        btnSound.addActionListener(e -> {
            SoundManager.setSoundEnabled(!SoundManager.isSoundEnabled());
            appSettings.saveSound(SoundManager.isSoundEnabled());
            updateSoundButton();
        });

        // Listener langCombo
        langCombo.addActionListener(e -> {
            if (langComboUpdating) return;
            Lang.Language sel = (Lang.Language) langCombo.getSelectedItem();
            if (sel != null) selectLanguage(sel);
        });

        boardPanel.setGameStateListener(new BoardPanel.GameStateListener() {
            @Override
            public void onTurnChanged(Piece currentTurn, boolean isAiThinking) {
                refreshGameStatus();
            }

            @Override
            public void onGameOver(String message, Piece winner) {
                onGameOver(boardPanel.getGameId(), message, winner);
            }

            @Override
            public void onGameOver(int gameId, String message, Piece winner) {
                if (disposed || menuVisible || gameId != boardPanel.getGameId() || !boardPanel.isGameOver()) return;
                BoardPanel.GameMode mode = boardPanel.getGameMode();
                Piece humanPiece = (mode == BoardPanel.GameMode.ONLINE) ? boardPanel.getMyPiece() : Piece.X;
                boolean isNewRecord = scoreTracker.recordResult(gameId, winner, humanPiece);
                if (isNewRecord) {
                    humanWins = scoreTracker.getHumanWins();
                    aiWins    = scoreTracker.getAiWins();
                    draws     = scoreTracker.getDraws();
                    refreshScores();
                }

                refreshGameStatus();

                if (!isNewRecord) return; // a gameId presents its result at most once
                closeResultDialog();
                JDialog dialog = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE)
                        .createDialog(MainFrame.this, Lang.gameOverTitle());
                resultDialog = dialog;
                try { dialog.setVisible(true); }
                finally {
                    if (resultDialog == dialog) resultDialog = null;
                    dialog.dispose();
                }
            }

            @Override
            public void onAiStatsUpdated(long t, long n, long p) {}

            @Override
            public void onHistoryChanged(boolean canUndo) {
                btnUndo.setEnabled(!menuVisible && !disposed && canUndo);
            }
        });

        // Lang listener — cập nhật toàn bộ UI khi ngôn ngữ thay đổi
        Lang.addChangeListener(languageListener);

        // =====================================================================
        // 5. LẮP RÁP
        // =====================================================================
        gameCard.add(north, BorderLayout.NORTH);
        gameCard.add(boardPanel, BorderLayout.CENTER);
        screenCards.add(startScreen, MENU_CARD);
        screenCards.add(gameCard, GAME_CARD);
        screenCards.setPreferredSize(new Dimension(760, 758));
        add(screenCards, BorderLayout.CENTER);
        screenLayout.show(screenCards, MENU_CARD);
        getRootPane().setDefaultButton(startScreen.getStartButton());
        refreshGameStatus();

        resizeTranslatedControls();
        setLocationRelativeTo(null);
    }

    private void selectLanguage(Lang.Language language) {
        if (language != Lang.get()) Lang.set(language);
        appSettings.saveLang(language);
    }

    public void startGame() {
        if (disposed || !menuVisible) return;
        playerName = AppSettings.normalizePlayerName(startScreen.getNameField().getText());
        appSettings.savePlayerName(playerName);
        startScreen.getNameField().setText(playerName);
        menuVisible = false;
        boardPanel.setEnabled(true);
        screenLayout.show(screenCards, GAME_CARD);
        getRootPane().setDefaultButton(null);
        refreshScores();
        refreshGameStatus();
        resizeTranslatedControls();
        boardPanel.requestFocusInWindow();
    }

    /** Invalidate callbacks before closing modal loops or resetting game state. */
    public void returnToMenu() {
        if (disposed || menuVisible) return;
        screenEpoch++;
        menuVisible = true;
        boardPanel.setEnabled(false);
        networkManager.disconnect();
        closeOwnedDialogs();
        modeComboUpdating = true;
        try {
            boardPanel.resetForMenu();
            modeCombo.setSelectedItem(BoardPanel.GameMode.PV_AI);
            diffCombo.setSelectedItem(MinimaxAI.Difficulty.MEDIUM);
            boardPanel.setAiDifficulty(MinimaxAI.Difficulty.MEDIUM);
            diffCombo.setEnabled(true);
            btnUndo.setEnabled(false);
        } finally { modeComboUpdating = false; }
        scoreTracker.newGame(); // Task 2: retain totals for this application session.
        startScreen.getNameField().setText(playerName);
        screenLayout.show(screenCards, MENU_CARD);
        getRootPane().setDefaultButton(startScreen.getStartButton());
        refreshGameStatus();
        resizeTranslatedControls();
        startScreen.getNameField().requestFocusInWindow();
    }

    private void closeOwnedDialogs() {
        closeResultDialog();
        NetworkDialog pending = networkDialog;
        networkDialog = null;
        if (pending != null) pending.dispose();
        for (Window window : getOwnedWindows()) if (window instanceof Dialog) window.dispose();
        achievementsDialog = null;
        achievementsText = null;
    }

    private void showScoreSummary() {
        if (disposed || menuVisible) return;
        if (achievementsDialog != null && achievementsDialog.isDisplayable()) {
            refreshAchievementSummary();
            achievementsDialog.toFront();
            return;
        }
        achievementsDialog = new JDialog(this, Lang.scoreboard(), false);
        achievementsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        achievementsDialog.setResizable(false);
        achievementsText = new JTextArea();
        achievementsText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        achievementsText.setEditable(false);
        achievementsText.setFocusable(false);
        achievementsText.setBorder(new EmptyBorder(20, 24, 20, 24));
        achievementsDialog.add(achievementsText);
        refreshAchievementSummary();
        achievementsDialog.setLocationRelativeTo(this);
        achievementsDialog.setVisible(true);
    }

    private void refreshAchievementSummary() {
        if (achievementsDialog == null || achievementsText == null) return;
        achievementsDialog.setTitle(Lang.scoreboard());
        achievementsText.setText(Lang.scoreSummary(getPlayerName(), scoreTracker.getHumanWins(),
                scoreTracker.getAiWins(), scoreTracker.getDraws()));
        achievementsDialog.pack();
    }

    // =========================================================================
    // TẠO NÚT ÂM THANH
    // =========================================================================
    private JButton makeSoundButton() {
        SpeakerIcon icon = new SpeakerIcon(18);
        JButton btn = new ThemedButton("", ThemedButton.Style.SECONDARY);
        btn.setIcon(icon);
        btn.setOpaque(true);
        btn.setBackground(Theme.DARK);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.setToolTipText(SoundManager.isSoundEnabled() ? Lang.soundTooltipOn() : Lang.soundTooltipOff());
        return btn;
    }

    private void updateSoundButton() {
        boolean on = SoundManager.isSoundEnabled();
        btnSound.setToolTipText(on ? Lang.soundTooltipOn() : Lang.soundTooltipOff());
        btnSound.repaint();
    }

    // =========================================================================
    // TẠO COMBO NGÔN NGỮ
    // =========================================================================
    private JComboBox<Lang.Language> makeLangCombo() {
        JComboBox<Lang.Language> cb = new JComboBox<>(Lang.Language.values());
        cb.setSelectedItem(Lang.get());
        cb.setFocusable(false);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setToolTipText(Lang.langComboTooltip());
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Lang.Language lang) {
                    lbl.setText(Lang.langName(lang));
                }
                // Luôn đặt màu rõ ràng
                lbl.setOpaque(true);
                if (isSelected) {
                    lbl.setBackground(Theme.BRASS);
                    lbl.setForeground(Theme.DARK);
                } else {
                    lbl.setBackground(Theme.DARK);
                    lbl.setForeground(Color.WHITE);
                }
                return lbl;
            }
        });
        applyDarkComboUI(cb);
        return cb;
    }

    // =========================================================================
    // STYLE COMBO (sửa để luôn tự vẽ màu — tránh Windows L&F override field)
    // =========================================================================
    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setFocusable(false);
        // Không gọi setBackground/setForeground trực tiếp — để renderer lo
        applyDarkComboUI(c);
    }

    /**
     * Thay WindowsComboBoxUI bằng BasicComboBoxUI tùy biến:
     * vẽ nền ô display + nút mũi tên đúng màu tối, chữ trắng.
     */
    private void applyDarkComboUI(JComboBox<?> c) {
        final Color bgColor  = Theme.DARK;
        final Color fgColor  = Color.WHITE;
        final Color selBg    = Theme.BRASS;
        final Color arrowBg  = Theme.DARK;
        final Color borderC  = Theme.DARK;

        c.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH);
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                btn.setBackground(arrowBg);
                btn.setForeground(fgColor);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(true);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(bgColor);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Lấy item hiển thị
                Object item = comboBox.getSelectedItem();
                if (item == null) return;
                @SuppressWarnings("rawtypes")
                ListCellRenderer renderer = comboBox.getRenderer();
                Component rc = renderer.getListCellRendererComponent(
                        listBox, item, -1, false, false);
                // Đảm bảo màu
                rc.setBackground(bgColor);
                rc.setForeground(fgColor);
                if (rc instanceof JComponent jc) jc.setOpaque(true);

                currentValuePane.paintComponent(g, rc, comboBox,
                        bounds.x, bounds.y, bounds.width, bounds.height, true);
            }

            @Override
            protected void installDefaults() {
                super.installDefaults();
                comboBox.setBackground(bgColor);
                comboBox.setForeground(fgColor);
                comboBox.setBorder(BorderFactory.createLineBorder(borderC));
            }
        });

        // Renderer mặc định cho item trong popup
        if (!(c.getRenderer() instanceof DefaultListCellRenderer r &&
              r.getClass().getEnclosingClass() == MainFrame.class)) {
            // Chỉ đặt renderer mặc định nếu chưa có renderer tùy biến
            if (true) {
                c.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value,
                            int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel lbl = (JLabel) super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);
                        lbl.setOpaque(true);
                        if (isSelected) {
                            lbl.setBackground(selBg);
                            lbl.setForeground(Theme.DARK);
                        } else {
                            lbl.setBackground(bgColor);
                            lbl.setForeground(fgColor);
                        }
                        return lbl;
                    }
                });
            }
        }
    }

    // =========================================================================
    // RENDERER CHO diffCombo
    // =========================================================================
    private ListCellRenderer<Object> makeDiffRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof MinimaxAI.Difficulty d) {
                    lbl.setText(switch (d) {
                        case EASY   -> Lang.easy();
                        case MEDIUM -> Lang.medium();
                        case HARD   -> Lang.hard();
                    });
                }
                lbl.setOpaque(true);
                if (isSelected) {
                    lbl.setBackground(Theme.BRASS);
                    lbl.setForeground(Theme.DARK);
                } else {
                    lbl.setBackground(Theme.DARK);
                    lbl.setForeground(Color.WHITE);
                }
                return lbl;
            }
        };
    }

    // =========================================================================
    // NETWORK
    // =========================================================================
    private void initNetworkManager() {
        this.networkManager = new NetworkManager(new NetworkManager.NetworkListener() {
            @Override
            public void onConnected(boolean isHost) {
                onConnected(isHost, networkManager.getRoundId());
            }

            @Override
            public void onConnected(boolean isHost, String roundId) {
                if (!disposed && !menuVisible) {
                    if (networkDialog != null) networkDialog.onConnectionSuccess();
                    boardPanel.setupOnlineGame(networkManager, isHost, roundId);

                    String msg = isHost ? Lang.onlineConnectedHost() : Lang.onlineConnectedClient();
                    JOptionPane.showMessageDialog(MainFrame.this, msg,
                            Lang.connectedTitle(), JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void onMoveReceived(int row, int col) {
                if (!disposed && !menuVisible) boardPanel.applyOpponentMove(row, col);
            }

            @Override
            public void onMoveReceived(String roundId, int ply, int row, int col) {
                if (!disposed && !menuVisible) boardPanel.applyOpponentMove(roundId, ply, row, col);
            }

            @Override
            public void onRestartRequested() {
                // Restart decisions are handled by requestRestart / onRoundStarted.
            }

            @Override
            public void onRoundStarted(String base, String next) {
                if (!disposed && !menuVisible && java.util.Objects.equals(base, boardPanel.getNetworkRoundId())) {
                    closeResultDialog();
                    scoreTracker.newGame();
                    boardPanel.startOnlineRound(base, next);
                }
            }

            @Override
            public void onRestartWaiting(String base) {
                if (!disposed && !menuVisible && java.util.Objects.equals(base, boardPanel.getNetworkRoundId()))
                    boardPanel.setRestartWaiting(true);
            }

            @Override
            public void onOpponentDisconnected() {
                if (!disposed && !menuVisible) {
                    closeResultDialog();
                    boardPanel.setRestartWaiting(false);
                    modeCombo.setSelectedItem(BoardPanel.GameMode.PV_AI);
                    JOptionPane.showMessageDialog(MainFrame.this,
                            Lang.opponentDisconnected(), Lang.disconnectedTitle(),
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            @Override
            public void onError(NetworkManager.NetworkError error) {
                if (!disposed && !menuVisible && networkDialog != null) networkDialog.onConnectionFailed(Lang.networkError(error));
            }
        }, callback -> {
            long epoch = screenEpoch;
            SwingUtilities.invokeLater(() -> {
                if (!disposed && !menuVisible && epoch == screenEpoch) callback.run();
            });
        });
    }

    private void openNetworkDialog() {
        long epoch = screenEpoch;
        BoardPanel.GameMode previousMode = boardPanel.getGameMode();
        NetworkDialog dialog = new NetworkDialog(this, networkManager, () -> {});
        networkDialog = dialog;
        dialog.setVisible(true);
        if (networkDialog == dialog) networkDialog = null;
        if (!disposed && !menuVisible && epoch == screenEpoch && !dialog.wasConnected()) {
            modeComboUpdating = true;
            try {
                modeCombo.setSelectedItem(previousMode);
                diffCombo.setEnabled(previousMode == BoardPanel.GameMode.PV_AI);
                btnUndo.setEnabled(boardPanel.canUndo());
            } finally {
                modeComboUpdating = false;
            }
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        screenEpoch++;
        if (boardPanel != null) boardPanel.setEnabled(false);
        closeOwnedDialogs();
        Lang.removeChangeListener(languageListener);
        if (networkManager != null) networkManager.disconnect();
        if (boardPanel != null) boardPanel.cancelAiComputation();
        super.dispose();
    }

    private void closeResultDialog() {
        if (resultDialog != null) { JDialog old = resultDialog; resultDialog = null; old.dispose(); }
    }

    // =========================================================================
    // CẬP NHẬT NGÔN NGỮ
    // =========================================================================
    private void updateLanguage() {
        setTitle(Lang.startTitle());
        startScreen.updateLanguage();
        lblMode.setText(Lang.mode());
        lblDiff.setText(Lang.difficulty());
        btnNewGame.setText(Lang.newGame());
        btnUndo.setText(Lang.undo());
        btnBackToMenu.setText(Lang.backToMenu());
        btnAchievements.setToolTipText(Lang.achievementsTooltip());
        btnAchievements.getAccessibleContext().setAccessibleName(Lang.achievementsTooltip());
        refreshAchievementSummary();

        // Cập nhật langCombo mà không kích hoạt listener
        langComboUpdating = true;
        langCombo.setSelectedItem(Lang.get());
        langCombo.setToolTipText(Lang.langComboTooltip());
        langComboUpdating = false;

        // Cập nhật tooltip âm thanh
        updateSoundButton();

        // Repaint combo để toString() cập nhật
        modeCombo.repaint();
        diffCombo.repaint();

        lblScoreTitle.setText(Lang.scoreboard());
        lblWinTxt.setText(Lang.wins());
        lblLossTxt.setText(Lang.losses());
        lblDrawTxt.setText(Lang.draws());

        refreshGameStatus();
        revalidate();
        resizeTranslatedControls();
        repaint();
    }

    /** Recompute text widths; BasicComboBoxUI caches sizes across locale changes. */
    private void resizeTranslatedControls() {
        resizeCombo(modeCombo, java.util.Arrays.stream(BoardPanel.GameMode.values()).map(Object::toString).toArray(String[]::new));
        resizeCombo(diffCombo, new String[]{Lang.easy(), Lang.medium(), Lang.hard()});
        resizeCombo(langCombo, java.util.Arrays.stream(Lang.Language.values()).map(Lang::langName).toArray(String[]::new));
        invalidateLayoutTree(getContentPane());
        invalidate();
        pack();
    }

    private void invalidateLayoutTree(Container container) {
        for (Component child : container.getComponents())
            if (child instanceof Container nested) invalidateLayoutTree(nested);
        container.invalidate();
    }

    private void resizeCombo(JComboBox<?> combo, String[] texts) {
        FontMetrics metrics = combo.getFontMetrics(combo.getFont());
        int width = java.util.Arrays.stream(texts).mapToInt(metrics::stringWidth).max().orElse(0);
        combo.setPreferredSize(new Dimension(width + 40, 28));
    }

    // =========================================================================
    // SCOREBOARD
    // =========================================================================
    private JPanel buildScoreBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 7));
        bar.setBackground(Theme.DARK);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.BRASS));

        lblScoreTitle = new JLabel(Lang.scoreboard());
        lblScoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblScoreTitle.setForeground(Theme.BRASS);
        bar.add(lblScoreTitle);

        bar.add(makeSep());

        lblWinTxt = makeScoreLabel(Lang.wins(),   Theme.BRASS, false);
        lblWinVal = makeScoreLabel("0",            Theme.BRASS, true);
        bar.add(lblWinTxt); bar.add(lblWinVal);

        bar.add(makeSep());

        lblLossTxt = makeScoreLabel(Lang.losses(), Theme.ERROR, false);
        lblLossVal = makeScoreLabel("0",            Theme.ERROR, true);
        bar.add(lblLossTxt); bar.add(lblLossVal);

        bar.add(makeSep());

        lblDrawTxt = makeScoreLabel(Lang.draws(),  Theme.BRASS, false);
        lblDrawVal = makeScoreLabel("0",            Theme.BRASS, true);
        bar.add(lblDrawTxt); bar.add(lblDrawVal);

        return bar;
    }

    private void refreshScores() {
        humanWins = scoreTracker.getHumanWins();
        aiWins = scoreTracker.getAiWins();
        draws = scoreTracker.getDraws();
        lblWinVal.setText(String.valueOf(humanWins));
        lblLossVal.setText(String.valueOf(aiWins));
        lblDrawVal.setText(String.valueOf(draws));
        refreshAchievementSummary();
    }

    private void setTurnLabel(String text, Color color) {
        turnLabel.setText(text);
        turnLabel.setForeground(color);
    }

    /** Refresh translated status without changing the board, turn or AI worker. */
    private void refreshGameStatus() {
        playerLabel.setText(Lang.playerNameLabel() + ": " + getPlayerName());
        BoardPanel.GameMode mode = boardPanel.getGameMode();
        Piece turn = boardPanel.getCurrentTurn();
        if (boardPanel.isRestartWaiting()) {
            setTurnLabel(Lang.restartWaiting(), Theme.ERROR);
        } else if (boardPanel.isGameOver()) {
            Piece winner = boardPanel.getWinner();
            Piece local = mode == BoardPanel.GameMode.ONLINE ? boardPanel.getMyPiece() : Piece.X;
            Color color = winner == Piece.NONE ? Theme.MUTED
                    : winner == local ? Theme.MUTED : Theme.ERROR;
            setTurnLabel(boardPanel.getGameOverMessage(), color);
        } else if (mode == BoardPanel.GameMode.ONLINE) {
            boolean localTurn = turn == boardPanel.getMyPiece();
            setTurnLabel(localTurn ? Lang.onlineYourTurn() : Lang.onlineOpponentTurn(),
                    localTurn ? Theme.MUTED : Theme.ERROR);
        } else if (boardPanel.isAiThinking()) {
            setTurnLabel(Lang.aiThinking(), Theme.ERROR);
        } else if (mode == BoardPanel.GameMode.PV_AI) {
            setTurnLabel(turn == Piece.X ? Lang.yourTurn() : Lang.aiTurn(),
                    turn == Piece.X ? Theme.BRASS : Theme.ERROR);
        } else {
            setTurnLabel(turn == Piece.X ? Lang.p1Turn() : Lang.p2Turn(),
                    turn == Piece.X ? Theme.BRASS : Theme.ERROR);
        }
    }

    // =========================================================================
    // FACTORY HELPERS
    // =========================================================================
    private JLabel makeHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(Theme.BRASS);
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
        sep.setForeground(Theme.DARK);
        return sep;
    }

    private JButton makeDarkBtn(String text, Color bg, Color fg) {
        JButton b = new ThemedButton(text, bg.equals(Theme.BRASS) ? ThemedButton.Style.PRIMARY : ThemedButton.Style.SECONDARY);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    // =========================================================================
    // GETTERS (cho test)
    // =========================================================================
    public ScoreTracker getScoreTracker() { return scoreTracker; }
    public BoardPanel getBoardPanel()     { return boardPanel; }
    public StartScreen getStartScreen() { return startScreen; }
    public boolean isMenuVisible() { return menuVisible; }
    public String getPlayerName() { return playerName.isEmpty() ? Lang.defaultPlayerName() : playerName; }
    public JButton getBackToMenuButton() { return btnBackToMenu; }
    public JButton getAchievementsButton() { return btnAchievements; }

    /** Trả về modeCombo để test kiểm tra renderer/màu. */
    public JComboBox<BoardPanel.GameMode> getModeCombo() { return modeCombo; }

    /** Trả về diffCombo để test kiểm tra renderer/màu. */
    public JComboBox<MinimaxAI.Difficulty> getDiffCombo() { return diffCombo; }

    /** Trả về btnSound để test kiểm tra tooltip/trạng thái. */
    public JButton getBtnSound() { return btnSound; }

    /** Trả về langCombo để test kiểm tra selected item. */
    public JComboBox<Lang.Language> getLangCombo() { return langCombo; }

    /** Trả về lblMode để test kiểm tra updateLanguage. */
    public JLabel getLblMode() { return lblMode; }
}
