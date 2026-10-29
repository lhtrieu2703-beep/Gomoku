package game.view;

import game.ai.MinimaxAI;
import game.model.*;
import game.network.NetworkManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/** Owns screen state and lifecycle; game rules, search and network protocol stay in their existing owners. */
public class MainFrame extends JFrame {
    public enum Screen { TITLE, MENU, GAME }
    private final Board board=new Board();
    private final BoardPanel boardPanel=new BoardPanel(board);
    private final AppSettings appSettings;
    private final ScoreTracker scoreTracker=new ScoreTracker();
    private final CardLayout screenLayout=new CardLayout();
    private final JPanel screenCards=new JPanel(screenLayout),gameCard=new JPanel(new BorderLayout());
    private final StartScreen startScreen;
    private final MainMenu mainMenu;
    private PlayerProfile profile,secondProfile=new PlayerProfile("",0);
    private BoardPanel.GameMode selectedMode=BoardPanel.GameMode.PV_AI;
    private MinimaxAI.Difficulty selectedDifficulty=MinimaxAI.Difficulty.MEDIUM;
    private Screen screen=Screen.TITLE;
    private volatile long screenEpoch;
    private boolean disposed;
    private NetworkManager networkManager;
    private NetworkDialog networkDialog;
    private JDialog resultDialog;
    private final Runnable languageListener=this::updateLanguage;
    private final KeyEventDispatcher titleKeys=event -> {
        if(!disposed && screen==Screen.TITLE && event.getID()==KeyEvent.KEY_PRESSED
                && event.getSource() instanceof Component component && SwingUtilities.getWindowAncestor(component)==this) {
            showMenu(); return true;
        }
        return false;
    };
    private final JButton btnBackToMenu=new ThemedButton("",ThemedButton.Style.SECONDARY),btnUndo=new ThemedButton("",ThemedButton.Style.SECONDARY),btnNewGame=new ThemedButton("",ThemedButton.Style.PRIMARY);
    private final JButton rulesButton=MainMenu.iconButton(UiIcon.Kind.BOOK),settingsButton=MainMenu.iconButton(UiIcon.Kind.MENU);
    private final PlayerCard leftPlayer=new PlayerCard(),rightPlayer=new PlayerCard();
    private final JTextArea turnText=new JTextArea();
    private final JLabel drawsLabel=new JLabel("",SwingConstants.CENTER);
    private String statusText="";

    public MainFrame() { this(new AppSettings()); }
    public MainFrame(AppSettings settings) {
        appSettings=settings; Lang.set(settings.loadLang()); SoundManager.setSoundEnabled(settings.loadSound()); profile=settings.loadProfile();
        setTitle(Lang.startTitle()); setDefaultCloseOperation(DISPOSE_ON_CLOSE); setResizable(false);
        startScreen=new StartScreen(this::showMenu);
        mainMenu=new MainMenu(this::showProfile,() -> showSetup(true),() -> showSetup(false),this::openNetworkDialog,this::showRules,this::showSettings);
        boardPanel.setEnabled(false); initNetworkManager();
        JPanel header=new JPanel(new GridBagLayout()); header.setBackground(Theme.DARK);
        header.setPreferredSize(new Dimension(760,108)); header.setBorder(BorderFactory.createEmptyBorder(6,16,6,16));
        turnText.setEditable(false); turnText.setFocusable(false); turnText.setLineWrap(true); turnText.setWrapStyleWord(true);
        turnText.setBackground(Theme.DARK); turnText.setForeground(Theme.CREAM); turnText.setFont(Theme.font(Font.PLAIN,13));
        turnText.setBorder(BorderFactory.createEmptyBorder(2,8,2,8));
        drawsLabel.setForeground(Theme.BRASS); drawsLabel.setFont(Theme.font(Font.BOLD,13));
        JPanel middle=new JPanel(new BorderLayout(0,2)); middle.setOpaque(false); middle.add(turnText); middle.add(drawsLabel,BorderLayout.SOUTH);
        GridBagConstraints cell=new GridBagConstraints(); cell.fill=GridBagConstraints.BOTH; cell.weighty=1;
        cell.gridx=0; cell.weightx=0; cell.ipadx=0; leftPlayer.setPreferredSize(new Dimension(230,96)); header.add(leftPlayer,cell);
        cell.gridx=1; cell.weightx=1; header.add(middle,cell);
        cell.gridx=2; cell.weightx=0; rightPlayer.setPreferredSize(new Dimension(230,96)); header.add(rightPlayer,cell);
        gameCard.setBackground(Theme.DARK); gameCard.add(header,BorderLayout.NORTH);
        // BoardPanel paints and hit-tests the same fixed 596x596 coordinates.
        // Keep that preferred size and centre the panel instead of stretching it to 760px.
        JPanel boardArea=new JPanel(new GridBagLayout()); boardArea.setBackground(Theme.BOARD); boardArea.add(boardPanel);
        gameCard.add(boardArea,BorderLayout.CENTER);
        JPanel toolbar=new JPanel(new BorderLayout()); toolbar.setBorder(BorderFactory.createEmptyBorder(6,16,6,16)); toolbar.setBackground(Theme.DARK);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); actions.setOpaque(false);
        for(JButton button:new JButton[]{btnBackToMenu,btnUndo,btnNewGame}) actions.add(button);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false); right.add(rulesButton); right.add(settingsButton);
        toolbar.add(actions); toolbar.add(right,BorderLayout.EAST); gameCard.add(toolbar,BorderLayout.SOUTH);
        btnBackToMenu.addActionListener(e -> returnToMenu()); btnUndo.addActionListener(e -> { if(isGameActive()) boardPanel.undoMove(); });
        btnNewGame.addActionListener(e -> newGame()); rulesButton.addActionListener(e -> showRules()); settingsButton.addActionListener(e -> showSettings());
        boardPanel.setGameStateListener(new BoardPanel.GameStateListener() {
            public void onTurnChanged(Piece turn,boolean thinking) { refreshGameStatus(); }
            public void onGameOver(String message,Piece winner) { onGameOver(boardPanel.getGameId(),message,winner); }
            public void onGameOver(int gameId,String message,Piece winner) {
                if(!isGameActive() || gameId!=boardPanel.getGameId() || !boardPanel.isGameOver()) return;
                Piece local=boardPanel.getGameMode()==BoardPanel.GameMode.ONLINE?boardPanel.getMyPiece():Piece.X;
                boolean first=scoreTracker.recordResult(gameId,winner,local); refreshGameStatus();
                if(!first) return;
                closeResultDialog(); JDialog dialog=new JOptionPane(message,JOptionPane.INFORMATION_MESSAGE).createDialog(MainFrame.this,Lang.gameOverTitle());
                resultDialog=dialog;
                try { dialog.setVisible(true); } finally { if(resultDialog==dialog) resultDialog=null; dialog.dispose(); }
            }
            public void onAiStatsUpdated(long time,long nodes,long prunes) {}
            public void onHistoryChanged(boolean undo) { btnUndo.setEnabled(isGameActive()&&undo); }
        });
        screenCards.setPreferredSize(new Dimension(760,758)); screenCards.add(startScreen,Screen.TITLE.name());
        screenCards.add(mainMenu,Screen.MENU.name()); screenCards.add(gameCard,Screen.GAME.name()); setContentPane(screenCards);
        Lang.addChangeListener(languageListener); KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(titleKeys);
        updateLanguage(); showScreen(Screen.TITLE); pack(); setLocationRelativeTo(null);
    }
    private boolean isGameActive() { return !disposed&&screen==Screen.GAME; }
    private void showScreen(Screen next) {
        screen=next; startScreen.setActive(next==Screen.TITLE&&!disposed); screenLayout.show(screenCards,next.name());
        getRootPane().setDefaultButton(null); revalidate(); repaint();
    }
    public void showMenu() { if(!disposed&&screen==Screen.TITLE) showScreen(Screen.MENU); }
    public void startGame() { if(!disposed&&!isGameActive()) beginGame(selectedMode,selectedDifficulty); }
    public void beginGame(BoardPanel.GameMode mode,MinimaxAI.Difficulty difficulty) {
        if(disposed) return;
        closeResultDialog();
        if(mode!=selectedMode || difficulty!=selectedDifficulty) scoreTracker.resetScores();
        selectedMode=mode; selectedDifficulty=difficulty;
        boardPanel.setGameMode(mode); boardPanel.setAiDifficulty(difficulty); boardPanel.setEnabled(true);
        showScreen(Screen.GAME); refreshGameStatus(); boardPanel.requestFocusInWindow();
    }
    public void returnToMenu() {
        if(!isGameActive()) return;
        screenEpoch++; selectedMode=boardPanel.getGameMode(); boardPanel.setEnabled(false);
        // Change the screen guard before closing dialogs: disposal can resume nested event loops.
        showScreen(Screen.MENU); networkManager.disconnect(); closeOwnedDialogs(); boardPanel.resetForMenu();
        btnUndo.setEnabled(false); refreshGameStatus();
    }
    private void newGame() {
        if(!isGameActive()) return;
        if(boardPanel.getGameMode()==BoardPanel.GameMode.ONLINE) {
            if(networkManager.requestRestart(boardPanel.getNetworkRoundId())&&!networkManager.isHost()) boardPanel.setRestartWaiting(true);
        } else { closeResultDialog(); scoreTracker.newGame(); boardPanel.resetGame(); }
    }
    public void updateProfile(PlayerProfile value) { profile=value; appSettings.saveProfile(value); updateLanguage(); }
    public void setSecondProfile(PlayerProfile value) { secondProfile=value; refreshGameStatus(); }
    public void selectLanguage(Lang.Language value) { if(value!=Lang.get()) Lang.set(value); appSettings.saveLang(value); }
    public void toggleSound() { SoundManager.setSoundEnabled(!SoundManager.isSoundEnabled()); appSettings.saveSound(SoundManager.isSoundEnabled()); }
    public void resetSessionScores() { scoreTracker.resetScores(); refreshGameStatus(); }
    public void showProfile() { if(!disposed) new ProfileDialog(this).setVisible(true); }
    public void showSetup(boolean ai) { if(!disposed) new SetupDialog(this,ai).setVisible(true); }
    public void showSettings() { if(!disposed) new SettingsDialog(this).setVisible(true); }
    public void showRules() { if(!disposed) new RulesDialog(this).setVisible(true); }
    public void openNetworkDialog() {
        if(disposed||networkDialog!=null) return;
        NetworkDialog dialog=new NetworkDialog(this,networkManager,() -> {}); networkDialog=dialog;
        try { dialog.setVisible(true); } finally { if(networkDialog==dialog) networkDialog=null; }
    }
    private void initNetworkManager() {
        networkManager=new NetworkManager(new NetworkManager.NetworkListener() {
            public void onConnected(boolean host) { onConnected(host,networkManager.getRoundId()); }
            public void onConnected(boolean host,String round) {
                if(disposed || (!isGameActive()&&networkDialog==null)) return;
                if(networkDialog!=null) networkDialog.onConnectionSuccess();
                beginGame(BoardPanel.GameMode.ONLINE,selectedDifficulty); boardPanel.setupOnlineGame(networkManager,host,round);
                JOptionPane.showMessageDialog(MainFrame.this,host?Lang.onlineConnectedHost():Lang.onlineConnectedClient(),Lang.connectedTitle(),JOptionPane.INFORMATION_MESSAGE);
            }
            public void onMoveReceived(int row,int col) { if(isGameActive()) boardPanel.applyOpponentMove(row,col); }
            public void onMoveReceived(String round,int ply,int row,int col) { if(isGameActive()) boardPanel.applyOpponentMove(round,ply,row,col); }
            public void onRestartRequested() {}
            public void onRoundStarted(String base,String next) {
                if(isGameActive()&&Objects.equals(base,boardPanel.getNetworkRoundId())) { closeResultDialog(); scoreTracker.newGame(); boardPanel.startOnlineRound(base,next); }
            }
            public void onRestartWaiting(String base) { if(isGameActive()&&Objects.equals(base,boardPanel.getNetworkRoundId())) boardPanel.setRestartWaiting(true); }
            public void onOpponentDisconnected() {
                if(isGameActive()) { closeResultDialog(); boardPanel.setRestartWaiting(false); returnToMenu();
                    JOptionPane.showMessageDialog(MainFrame.this,Lang.opponentDisconnected(),Lang.disconnectedTitle(),JOptionPane.WARNING_MESSAGE); }
            }
            public void onError(NetworkManager.NetworkError error) { if(!disposed&&networkDialog!=null) networkDialog.onConnectionFailed(Lang.networkError(error)); }
        },callback -> {
            long epoch=screenEpoch;
            SwingUtilities.invokeLater(() -> { if(!disposed&&epoch==screenEpoch&&(isGameActive()||networkDialog!=null)) callback.run(); });
        });
    }
    private void closeResultDialog() { if(resultDialog!=null) { JDialog old=resultDialog; resultDialog=null; old.dispose(); } }
    private void closeOwnedDialogs() {
        closeResultDialog(); NetworkDialog old=networkDialog; networkDialog=null; if(old!=null) old.dispose();
        for(Window window:getOwnedWindows()) if(window instanceof Dialog) window.dispose();
    }
    private void updateLanguage() {
        if(disposed) return;
        setTitle(Lang.startTitle()); mainMenu.updateLanguage(profile);
        btnBackToMenu.setText(Lang.backToMenu()); btnUndo.setText(Lang.undo()); btnNewGame.setText(Lang.newGame());
        rulesButton.setToolTipText(Lang.rulesTitle()); settingsButton.setToolTipText(Lang.settings());
        refreshGameStatus(); revalidate(); repaint();
    }
    private void refreshGameStatus() {
        Piece turn=boardPanel.getCurrentTurn(); BoardPanel.GameMode mode=boardPanel.getGameMode();
        String status=boardPanel.isRestartWaiting()?Lang.restartWaiting():boardPanel.isGameOver()?boardPanel.getGameOverMessage():
            mode==BoardPanel.GameMode.ONLINE?(turn==boardPanel.getMyPiece()?Lang.onlineYourTurn():Lang.onlineOpponentTurn()):
            boardPanel.isAiThinking()?Lang.aiThinking():mode==BoardPanel.GameMode.PV_AI?(turn==Piece.X?Lang.yourTurn():Lang.aiTurn()):(turn==Piece.X?Lang.p1Turn():Lang.p2Turn());
        if(!boardPanel.isGameOver()&&!boardPanel.isRestartWaiting()&&!boardPanel.isAiThinking()&&mode==BoardPanel.GameMode.PVP)
            status=Lang.namedTurn(turn==Piece.X?profile.displayName():secondProfile.displayName(true),turn);
        statusText=status; turnText.setText(status); drawsLabel.setText(Lang.draws()+": "+scoreTracker.getDraws());
        Piece local=mode==BoardPanel.GameMode.ONLINE?boardPanel.getMyPiece():Piece.X;
        Piece active=boardPanel.isGameOver()?boardPanel.getWinner():boardPanel.getCurrentTurn();
        String opponent=mode==BoardPanel.GameMode.PV_AI?Lang.computerName(selectedDifficulty):mode==BoardPanel.GameMode.ONLINE?Lang.opponentName():secondProfile.displayName(true);
        int opponentAvatar=mode==BoardPanel.GameMode.PV_AI?6:mode==BoardPanel.GameMode.ONLINE?0:secondProfile.avatarIndex();
        leftPlayer.update(profile.displayName(),profile.avatarIndex(),scoreTracker.getHumanWins(),active!=Piece.NONE&&active==local);
        rightPlayer.update(opponent,opponentAvatar,scoreTracker.getAiWins(),active!=Piece.NONE&&active!=local);
    }
    @Override public void dispose() {
        disposed=true; screenEpoch++; if(startScreen!=null) startScreen.setActive(false); boardPanel.setEnabled(false);
        closeOwnedDialogs(); Lang.removeChangeListener(languageListener); KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(titleKeys);
        if(networkManager!=null) networkManager.disconnect(); boardPanel.cancelAiComputation(); super.dispose();
    }
    public Screen getScreen() { return screen; } public MainMenu getMainMenu() { return mainMenu; }
    public ScoreTracker getScoreTracker() { return scoreTracker; } public BoardPanel getBoardPanel() { return boardPanel; }
    public StartScreen getStartScreen() { return startScreen; } public boolean isMenuVisible() { return screen!=Screen.GAME; }
    public PlayerProfile getProfile() { return profile; } public PlayerProfile getSecondProfile() { return secondProfile; }
    public String getPlayerName() { return profile.displayName(); } public BoardPanel.GameMode getSelectedMode() { return selectedMode; }
    public MinimaxAI.Difficulty getSelectedDifficulty() { return selectedDifficulty; }
    public JButton getBackToMenuButton() { return btnBackToMenu; } public JButton getUndoButton() { return btnUndo; }
    public JButton getRulesButton() { return rulesButton; } public JButton getSettingsButton() { return settingsButton; }
    public String getStatusText() { return statusText; }
    public PlayerCard getLeftPlayerCard() { return leftPlayer; } public PlayerCard getRightPlayerCard() { return rightPlayer; }
    public JLabel getDrawsLabel() { return drawsLabel; }
}
