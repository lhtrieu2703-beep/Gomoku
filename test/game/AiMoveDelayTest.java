package game;

import game.ai.MinimaxAI;
import game.model.*;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class AiMoveDelayTest {
    private MainFrame frame;
    private BoardPanel panel;
    private Board board;
    private Lang.Language language;
    private boolean sound;

    @BeforeEach void remember() {
        language=Lang.get();sound=SoundManager.isSoundEnabled();SoundManager.setSoundEnabled(false);
    }
    @AfterEach void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            if(frame!=null)frame.dispose();
            if(panel!=null)panel.cancelAiComputation();
            Lang.set(language);SoundManager.setSoundEnabled(sound);
        });
    }
    @ParameterizedTest @CsvSource({"800,0,800","800,100,700","800,799,1","800,800,0","800,1200,0","0,100,0"})
    void waitsOnlyForTheMissingPart(int minimum,long elapsed,int expected) {
        assertEquals(expected,AiMoveDelay.remainingMillis(minimum,elapsed));
    }
    @Test void rejectsNegativeDelayOrElapsedTime() {
        assertThrows(IllegalArgumentException.class,()->AiMoveDelay.remainingMillis(-1,0));
        assertThrows(IllegalArgumentException.class,()->AiMoveDelay.remainingMillis(800,-1));
        assertThrows(IllegalArgumentException.class,()->new BoardPanel(new Board(),-1));
    }
    private Timer pendingTimer() { return P2LifecycleTest.field(panel,"aiMoveTimer"); }
    private void openFrameAndPlay() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            MemoryPreferences prefs=new MemoryPreferences();prefs.put("language","VI");prefs.putBoolean("soundEnabled",false);
            frame=new MainFrame(new AppSettings(prefs),60_000);frame.startGame();panel=frame.getBoardPanel();
            board=P2LifecycleTest.field(frame,"board");assertTrue(panel.playMove(7,7));
        });
    }
    /** Polling runs on EDT; only the JUnit thread waits, with a bounded timeout. */
    private void await(BooleanSupplier condition) throws Exception {
        CompletableFuture<Void> ready=new CompletableFuture<>();Timer[] poll={null};
        SwingUtilities.invokeAndWait(()->{
            poll[0]=new Timer(1,event->{
                try { if(condition.getAsBoolean()) { poll[0].stop();ready.complete(null); } }
                catch(Throwable error) { poll[0].stop();ready.completeExceptionally(error); }
            });poll[0].start();
        });
        try { ready.get(4,TimeUnit.SECONDS); }
        finally { SwingUtilities.invokeAndWait(()->poll[0].stop()); }
    }
    private Timer awaitPending() throws Exception {
        await(()->pendingTimer()!=null);Timer[] timer={null};
        SwingUtilities.invokeAndWait(()->timer[0]=pendingTimer());return timer[0];
    }
    /** Deliberately deliver even stopped/queued callbacks to exercise the production guards. */
    private static void deliver(Timer timer) {
        for(var listener:timer.getActionListeners())listener.actionPerformed(new ActionEvent(timer,ActionEvent.ACTION_PERFORMED,"test-delivery"));
    }
    @ParameterizedTest @EnumSource(MinimaxAI.Difficulty.class)
    void fastSearchKeepsThinkingAndInputLockedUntilTheOriginalDeadline(MinimaxAI.Difficulty difficulty) throws Exception {
        AtomicLong clock=new AtomicLong();
        SwingUtilities.invokeAndWait(()->{
            board=new Board();panel=new BoardPanel(board,AiMoveDelay.DEFAULT_MINIMUM_MS,clock::get);
            panel.setAiDifficulty(difficulty);assertTrue(panel.playMove(7,7));
        });
        Timer timer=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            assertEquals(800,timer.getInitialDelay());assertEquals(1,board.getMoveCount());
            assertTrue(panel.isAiThinking());assertTrue(panel.canUndo());assertFalse(panel.playMove(5,5));
            clock.set(TimeUnit.MILLISECONDS.toNanos(799));deliver(timer);
            assertEquals(1,board.getMoveCount());assertTrue(panel.isAiThinking());assertEquals(1,timer.getInitialDelay());
            clock.set(TimeUnit.MILLISECONDS.toNanos(800));deliver(timer);
            assertEquals(2,board.getMoveCount());assertFalse(panel.isAiThinking());assertEquals(Piece.X,panel.getCurrentTurn());
            assertNull(pendingTimer());deliver(timer);assertEquals(2,board.getMoveCount(),"No duplicate placement");
        });
    }
    @Test void elapsedMinimumBeforeWorkerCompletionAddsNoTimer() throws Exception {
        AtomicLong clock=new AtomicLong();
        SwingUtilities.invokeAndWait(()->{
            board=new Board();panel=new BoardPanel(board,800,clock::get);assertTrue(panel.playMove(7,7));
            // done() cannot run until this EDT event returns.
            clock.set(TimeUnit.MILLISECONDS.toNanos(900));
        });
        await(()->board.getMoveCount()==2);
        SwingUtilities.invokeAndWait(()->{assertNull(pendingTimer());assertFalse(panel.isAiThinking());});
    }
    @Test void zeroDelayInjectionAppliesOnCompletionWithoutATimer() throws Exception {
        SwingUtilities.invokeAndWait(()->{board=new Board();panel=new BoardPanel(board,0);assertTrue(panel.playMove(7,7));});
        await(()->board.getMoveCount()==2);
        SwingUtilities.invokeAndWait(()->{assertNull(pendingTimer());assertFalse(panel.isAiThinking());});
    }
    @Test void undoButtonDuringWaitCancelsQueuedMoveEvenAfterAnotherHumanMove() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            int gameId=panel.getGameId();assertTrue(frame.getUndoButton().isEnabled());
            frame.getUndoButton().doClick(0);
            assertEquals(gameId,panel.getGameId());assertEquals(0,board.getMoveCount());assertFalse(old.isRunning());assertNull(pendingTimer());
            assertTrue(panel.playMove(7,8));deliver(old);
            assertEquals(1,board.getMoveCount());assertEquals(Piece.X,board.getPiece(7,8));
        });
    }
    @Test void newGameButtonCancelsQueuedMoveBeforeStartingAnotherRound() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            int previous=panel.getGameId();BoardDisplayProbe.newGameButton(frame.getContentPane()).doClick(0);
            assertTrue(panel.getGameId()>previous);assertEquals(0,board.getMoveCount());assertFalse(old.isRunning());assertNull(pendingTimer());
            assertTrue(panel.playMove(7,8));deliver(old);assertEquals(1,board.getMoveCount());
        });
    }
    @Test void menuCancelsQueuedMoveEvenAfterReturningToGame() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            frame.getBackToMenuButton().doClick(0);assertEquals(MainFrame.Screen.MENU,frame.getScreen());
            assertNull(pendingTimer());assertFalse(old.isRunning());frame.startGame();
            assertTrue(panel.playMove(7,8));deliver(old);assertEquals(1,board.getMoveCount());
        });
    }
    @Test void changingModeCancelsThePendingAiMove() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            frame.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM);
            assertNull(pendingTimer());assertFalse(old.isRunning());assertTrue(panel.playMove(4,4));
            deliver(old);assertEquals(1,board.getMoveCount());assertFalse(panel.isAiThinking());
        });
    }
    @Test void changingDifficultyRestartsSearchButOldTimerCannotTouchTheNewTimer() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();long[] started={0};int[] id={0};
        SwingUtilities.invokeAndWait(()->{
            started[0]=P2LifecycleTest.field(panel,"aiTurnStartedNanos");id[0]=panel.getGameId();
            panel.setAiDifficulty(MinimaxAI.Difficulty.HARD);assertFalse(old.isRunning());
        });
        Timer replacement=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            assertNotSame(old,replacement);assertEquals(id[0],panel.getGameId());
            assertEquals(started[0],(long)P2LifecycleTest.field(panel,"aiTurnStartedNanos"));
            deliver(old);assertSame(replacement,pendingTimer());assertTrue(replacement.isRunning());
            assertEquals(1,board.getMoveCount());assertTrue(panel.isAiThinking());
        });
    }
    @Test void disposingWindowCancelsThePendingMoveWithoutChangingTheBoard() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            frame.dispose();assertFalse(old.isRunning());assertNull(pendingTimer());assertFalse(panel.isAiThinking());
            deliver(old);assertEquals(1,board.getMoveCount());
        });
    }
    @Test void languageChangeUpdatesThinkingTextInPlaceWithoutRebuildingTheGame() throws Exception {
        openFrameAndPlay();Timer old=awaitPending();
        SwingUtilities.invokeAndWait(()->{
            int id=panel.getGameId();frame.selectLanguage(Lang.Language.EN);
            assertSame(panel,frame.getBoardPanel());assertSame(old,pendingTimer());assertEquals(id,panel.getGameId());
            assertTrue(panel.isAiThinking());assertEquals(Lang.aiThinking(),frame.getStatusText());
        });
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void humanWinOrFullBoardDrawNeverSchedulesAnAiMove(boolean draw) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            board=new Board();panel=new BoardPanel(board,60_000);
            if(draw) {
                for(int r=0;r<15;r++)for(int c=0;c<15;c++)if(r!=0||c!=0)
                    assertTrue(board.makeMove(r,c,(c+(r%2==0?0:2))%4<2?Piece.X:Piece.O));
                assertTrue(panel.playMove(0,0));assertEquals(Piece.NONE,panel.getWinner());
            } else {
                for(int c=3;c<7;c++)assertTrue(board.makeMove(7,c,Piece.X));
                assertTrue(panel.playMove(7,7));assertEquals(Piece.X,panel.getWinner());
            }
            assertTrue(panel.isGameOver());assertFalse(panel.isAiThinking());assertNull(pendingTimer());
            assertNull(P2LifecycleTest.field(panel,"aiWorker"));
        });
    }
    @ParameterizedTest @EnumSource(value=BoardPanel.GameMode.class,names={"PVP","ONLINE"})
    void otherModesDoNotUseTheDelayClockOrScheduleAi(BoardPanel.GameMode mode) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            board=new Board();panel=new BoardPanel(board,60_000,()->{throw new AssertionError("PvAI clock used outside PvAI");});
            if(mode==BoardPanel.GameMode.ONLINE)panel.setupOnlineGame(null,true);else panel.setGameMode(mode);
            assertTrue(panel.playMove(7,7));assertEquals(1,board.getMoveCount());assertEquals(Piece.O,panel.getCurrentTurn());
            assertNull(pendingTimer());assertNull(P2LifecycleTest.field(panel,"aiWorker"));assertFalse(panel.isAiThinking());
        });
    }
}
