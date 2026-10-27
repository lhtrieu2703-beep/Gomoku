import game.ai.*;
import game.model.*;
import game.view.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class AuditBenchmark {
    static Board position(int count) {
        Board board=new Board();Random random=new Random(20261026L+count);
        for(int n=0;n<count;){int r=random.nextInt(15),c=random.nextInt(15);Piece p=n%2==0?Piece.X:Piece.O;
            if(board.makeMove(r,c,p)){if(board.checkWin(r,c))board.undoMove(r,c);else n++;}}
        return board;
    }
    static double percentile(java.util.List<Double> values,double p){Collections.sort(values);return values.get(Math.min(values.size()-1,(int)Math.ceil(values.size()*p)-1));}
    public static void main(String[] args)throws Exception {
        SoundManager.setSoundEnabled(false);
        for(int count:new int[]{1,21,61,121})for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values()){
            Board board=position(count);MinimaxAI ai=new MinimaxAI(Piece.O,level);
            for(int i=0;i<3;i++)ai.findBestMove(board);
            java.util.List<Double> ms=new ArrayList<>();long minNodes=Long.MAX_VALUE,maxNodes=0;
            for(int i=0;i<15;i++){long start=System.nanoTime();Move m=ai.findBestMove(board);ms.add((System.nanoTime()-start)/1e6);
                if(m==null||!board.isValidMove(m.row,m.col)||board.getMoveCount()!=count)throw new AssertionError("invalid/mutated board");
                minNodes=Math.min(minNodes,ai.getNodesEvaluated());maxNodes=Math.max(maxNodes,ai.getNodesEvaluated());}
            System.out.printf(Locale.ROOT,"CORE stones=%d level=%s median=%.2fms p95=%.2fms max=%.2fms nodes=%d..%d%n",count,level.name(),percentile(ms,.5),percentile(ms,.95),Collections.max(ms),minNodes,maxNodes);
        }
        Board board=new Board();BoardPanel[] panel=new BoardPanel[1];
        CountDownLatch[] completion=new CountDownLatch[1];
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new BoardPanel(board);panel[0].setAiDifficulty(MinimaxAI.Difficulty.HARD);
            panel[0].setGameStateListener(new BoardPanel.GameStateListener(){public void onGameOver(String msg,Piece w){if(completion[0]!=null)completion[0].countDown();}
                public void onTurnChanged(Piece p,boolean thinking){if(p==Piece.X&&!thinking&&completion[0]!=null)completion[0].countDown();}
                public void onAiStatsUpdated(long t,long n,long p){} public void onHistoryChanged(boolean b){} });
        });
        java.util.List<Double> delays=Collections.synchronizedList(new ArrayList<>());
        ScheduledExecutorService clock=Executors.newSingleThreadScheduledExecutor();
        clock.scheduleAtFixedRate(()->{long start=System.nanoTime();SwingUtilities.invokeLater(()->delays.add((System.nanoTime()-start)/1e6));},0,5,TimeUnit.MILLISECONDS);
        Random random=new Random(26);java.util.List<Double> turns=new ArrayList<>();
        try{
            for(int i=0;i<10;i++){
                SwingUtilities.invokeAndWait(()->{if(panel[0].isGameOver())panel[0].resetGame();});
                CountDownLatch done=new CountDownLatch(1);long start=System.nanoTime();
                SwingUtilities.invokeAndWait(()->{completion[0]=done;int r,c;do{r=random.nextInt(15);c=random.nextInt(15);}while(!board.isValidMove(r,c));
                    if(!panel[0].playMove(r,c))throw new AssertionError("play rejected");});
                if(!done.await(5,TimeUnit.SECONDS))throw new AssertionError("AI timeout");
                turns.add((System.nanoTime()-start)/1e6);
            }
        }finally{clock.shutdownNow();SwingUtilities.invokeAndWait(()->panel[0].cancelAiComputation());}
        System.out.printf(Locale.ROOT,"UI_AI turns=%d median=%.2fms p95=%.2fms max=%.2fms%n",turns.size(),percentile(turns,.5),percentile(turns,.95),Collections.max(turns));
        System.out.printf(Locale.ROOT,"EDT samples=%d median=%.2fms p95=%.2fms max=%.2fms%n",delays.size(),percentile(delays,.5),percentile(delays,.95),Collections.max(delays));
    }
}
