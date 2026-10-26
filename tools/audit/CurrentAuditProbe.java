import game.model.*;
import game.ai.*;
import game.view.*;
import game.network.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.AbstractPreferences;

public class CurrentAuditProbe {
    static class MemoryPreferences extends AbstractPreferences {
        final Map<String,String> values = new HashMap<>();
        MemoryPreferences(Lang.Language lang) { super(null, ""); values.put("soundEnabled", "false"); values.put("language",lang.name()); }
        protected void putSpi(String k,String v){values.put(k,v);} protected String getSpi(String k){return values.get(k);}
        protected void removeSpi(String k){values.remove(k);} protected void removeNodeSpi(){}
        protected String[] keysSpi(){return values.keySet().toArray(new String[0]);} protected String[] childrenNamesSpi(){return new String[0];}
        protected AbstractPreferences childSpi(String n){throw new UnsupportedOperationException();} protected void syncSpi(){} protected void flushSpi(){}
    }
    static class Recorder extends NetworkManager {
        int sent;
        Recorder(){super(null);}
        @Override public void sendMove(int r,int c){sent++;}
    }
    static NetworkManager.NetworkListener listener(BoardPanel panel) {
        return new NetworkManager.NetworkListener() {
            public void onConnected(boolean host){} public void onMoveReceived(int r,int c){panel.applyOpponentMove(r,c);}
            public void onRestartRequested(){panel.resetGame();} public void onOpponentDisconnected(){}
            public void onError(NetworkManager.NetworkError error){System.out.println("AUDIT NETWORK ERROR "+error);}
        };
    }
    public static void main(String[] args) throws Exception {
        SoundManager.setSoundEnabled(false);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeAndWait(() -> {
            Recorder recorder = new Recorder(); Board board = new Board(); BoardPanel panel = new BoardPanel(board);
            panel.setupOnlineGame(recorder,true);
            for(int c=0;c<4;c++){panel.playMove(7,c);panel.applyOpponentMove(0,2*c);}
            panel.setGameStateListener(new BoardPanel.GameStateListener(){
                public void onTurnChanged(Piece p,boolean t){} public void onAiStatsUpdated(long t,long n,long p){}
                public void onHistoryChanged(boolean c){}
                public void onGameOver(String s,Piece p){System.out.println("FINAL_MOVE_SENDS_AT_GAMEOVER="+recorder.sent+" (expected 5 to notify opponent before popup)");}
            });
            panel.playMove(7,4);
            System.out.println("FINAL_MOVE_SENDS_AFTER_CALLBACK="+recorder.sent);
        });
        Board board = new Board(); BoardPanel[] panel=new BoardPanel[1];
        SwingUtilities.invokeAndWait(()->{panel[0]=new BoardPanel(board);panel[0].setupOnlineGame(null,false);});
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        NetworkManager manager = new NetworkManager(listener(panel[0]),queue::add);
        try(ServerSocket server=new ServerSocket(0)){
            server.setSoTimeout(3000);manager.joinGame("127.0.0.1",server.getLocalPort());
            try(Socket peer=server.accept()){
                Runnable connected=queue.poll(3,TimeUnit.SECONDS); if(connected==null)throw new AssertionError("connect timeout");
                SwingUtilities.invokeAndWait(connected);
                new PrintWriter(peer.getOutputStream(),true).println("MOVE 4 4");
                Runnable oldMove=queue.poll(3,TimeUnit.SECONDS);if(oldMove==null)throw new AssertionError("move timeout");
                SwingUtilities.invokeAndWait(()->{panel[0].resetGame();oldMove.run();System.out.println("OLD_MOVE_AFTER_RESTART="+board.getPiece(4,4)+" moveCount="+board.getMoveCount()+" (expected NONE / 0)");});
            }
        }finally{manager.disconnect();}
        Board six=new Board();six.makeMove(7,2,Piece.O);for(int c=3;c<=8;c++)six.makeMove(7,c,Piece.X);six.makeMove(7,9,Piece.O);
        System.out.println("BLOCKED_SIX win="+six.checkWin(7,5)+" heuristic="+HeuristicEvaluator.evaluatePlayerAdvanced(six,Piece.X));
        Board three=new Board();for(int c=6;c<=8;c++)three.makeMove(7,c,Piece.X);
        System.out.println("SINGLE_THREE heuristic="+HeuristicEvaluator.evaluatePlayerAdvanced(three,Piece.X));
        for(Lang.Language lang:Lang.Language.values()) SwingUtilities.invokeAndWait(()->snapshot(lang));
        SwingUtilities.invokeAndWait(()->{
            MainFrame frame=new MainFrame(new AppSettings(new MemoryPreferences(Lang.Language.VI)));
            try{Lang.set(Lang.Language.PT);layout(frame.getContentPane());System.out.println("AFTER_VI_TO_PT "+frame.getSize());checkLayout(frame.getContentPane());save(frame,"bin_test/audit-switch-PT.png");}
            finally{frame.dispose();}
        });
    }
    static void snapshot(Lang.Language lang){
        MainFrame frame=new MainFrame(new AppSettings(new MemoryPreferences(lang)));
        try{layout(frame.getContentPane());System.out.println("UI_"+lang+" frame="+frame.getSize());checkLayout(frame.getContentPane());save(frame,"bin_test/audit-"+lang+".png");}
        finally{frame.dispose();}
    }
    static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container n)layout(n);}
    static void checkLayout(Container c){
        for(Component child:c.getComponents()){
            if(child.isVisible() && child.getWidth()>0 && (child.getX()+child.getWidth()>c.getWidth() || child.getY()+child.getHeight()>c.getHeight()))System.out.println("OUTSIDE_PARENT "+child.getClass().getSimpleName()+" "+child.getBounds()+" parent="+c.getSize());
            if(child instanceof JComboBox<?> combo && combo.getSelectedItem()!=null){
                String text=combo.getSelectedItem().toString();if(combo.getSelectedItem() instanceof MinimaxAI.Difficulty d)text=switch(d){case EASY->Lang.easy();case MEDIUM->Lang.medium();case HARD->Lang.hard();};
                if(combo.getSelectedItem() instanceof Lang.Language l)text=Lang.langName(l);
                int textWidth=combo.getFontMetrics(combo.getFont()).stringWidth(text);int available=combo.getWidth()-25;
                if(textWidth>available)System.out.println("COMBO_TEXT_CLIPPED "+text+" textWidth="+textWidth+" available="+available);
            }
            if(child instanceof Container n)checkLayout(n);
        }
    }
    static void save(MainFrame frame,String path){try{Container content=frame.getContentPane();BufferedImage img=new BufferedImage(content.getWidth(),content.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=img.createGraphics();content.printAll(g);g.dispose();ImageIO.write(img,"png",new File(path));}catch(Exception e){throw new RuntimeException(e);}}
}
