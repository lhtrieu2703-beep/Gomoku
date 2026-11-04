package game.tools;

import game.ai.MinimaxAI;
import game.ai.Move;
import game.model.Board;
import game.model.Piece;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/** Standalone measurements; never discovered by the JUnit scan or normal build.ps1. */
public final class AIArena {
    private static final MinimaxAI.Difficulty E = MinimaxAI.Difficulty.EASY;
    private static final MinimaxAI.Difficulty M = MinimaxAI.Difficulty.MEDIUM;
    private static final MinimaxAI.Difficulty H = MinimaxAI.Difficulty.HARD;
    private static final int MAX_MOVES = Board.SIZE * Board.SIZE;
    private static final String SEED_NOTE = "uncontrolled: production Random has no seed API; src unchanged";
    private static final List<Pair> PAIRS = List.of(new Pair(E,M),new Pair(E,H),new Pair(M,H),
            new Pair(M,M),new Pair(H,H));
    record Pair(MinimaxAI.Difficulty a, MinimaxAI.Difficulty b) {
        String id() { return a.name() + "-" + b.name(); }
    }
    record Turn(int ply, String actor, Piece color, int row, int col, double ms, long nodes, long prunes) {}
    record Game(String id, Pair pair, String first, String outcome, int moves, double totalMs,
                int aMoves, double aTotal, double aMax, int bMoves, double bTotal, double bMax,
                String anomaly, List<Turn> trace, Opening opening) {}
    record Opening(String id,int[][] cells) {}
    record Position(String id, String stage, Board board, Piece turn, String construction) {}
    record Sample(String position, String stage, int stones, Piece turn, MinimaxAI.Difficulty level,
                  String phase, int repetition, double ms, long nodes, long prunes, int row, int col) {}

    private AIArena() {}
    private static String number(double n) { return String.format(Locale.ROOT, "%.6f", n); }
    private static String csv(String s) { return "\"" + s.replace("\"", "\"\"").replace('\n',' ').replace('\r',' ') + "\""; }
    private static BufferedWriter writer(Path output, String filename) throws IOException {
        return Files.newBufferedWriter(output.resolve(filename), StandardCharsets.UTF_8);
    }
    private static void markdown(Path output, String filename, String text) throws IOException {
        Files.writeString(output.resolve(filename), text, StandardCharsets.UTF_8);
    }
    private static Board board() {
        Board b = new Board(); b.setWinRule(Board.WinRule.RULE_B); return b;
    }
    private static String ascii(Board b) {
        StringBuilder s = new StringBuilder("    012345678901234\n");
        for (int r=0;r<Board.SIZE;r++) {
            s.append(String.format(Locale.ROOT,"%02d  ",r));
            for (int c=0;c<Board.SIZE;c++) s.append(switch(b.getPiece(r,c)) {
                case X -> 'X'; case O -> 'O'; case NONE -> '.';
            });
            s.append('\n');
        }
        return s.toString();
    }
    private static void unchanged(Board before, Board after) {
        if (before.getWinRule()!=after.getWinRule() || before.getMoveCount()!=after.getMoveCount())
            throw new IllegalStateException("Search changed count/rule");
        for(int r=0;r<Board.SIZE;r++) for(int c=0;c<Board.SIZE;c++)
            if(before.getPiece(r,c)!=after.getPiece(r,c))
                throw new IllegalStateException("Search changed cell " + r + "," + c);
    }
    private static void noWin(Board b) {
        for(int r=0;r<Board.SIZE;r++) for(int c=0;c<Board.SIZE;c++)
            if(b.checkWin(r,c)) throw new IllegalStateException("Corpus already won at " + r + "," + c);
    }

    private static Game play(String id, Pair pair, boolean aFirst, Opening opening) {
        Board b=board();
        Piece aColor=aFirst?Piece.X:Piece.O;
        // Exclusive ownership: no Board or engine is ever shared across games/tasks.
        MinimaxAI a=new MinimaxAI(aColor,pair.a), other=new MinimaxAI(aColor.opposite(),pair.b);
        List<Turn> trace=new ArrayList<>();
        int aMoves=0,bMoves=0; double aTotal=0,bTotal=0,aMax=0,bMax=0;
        String outcome="ABNORMAL",anomaly="";
        long start=System.nanoTime();
        for(int i=0;i<opening.cells.length;i++) {
            int[] cell=opening.cells[i];Piece color=i%2==0?Piece.X:Piece.O;
            if(!b.makeMove(cell[0],cell[1],color)||b.checkWin(cell[0],cell[1])) throw new IllegalStateException("Invalid opening");
            trace.add(new Turn(i+1,color==aColor?"A":"B",color,cell[0],cell[1],0,0,0));
        }
        for(int ply=opening.cells.length+1;ply<=MAX_MOVES;ply++) {
            Piece color=ply%2==1?Piece.X:Piece.O;
            boolean actorA=color==aColor;
            Board before=b.copy(); Move m;
            long t=System.nanoTime();
            try {
                m=(actorA?a:other).findBestMove(b);
            } catch(RuntimeException ex) {
                anomaly="SEARCH_EXCEPTION " + ex; break;
            }
            double ms=(System.nanoTime()-t)/1e6;
            if(actorA) { aMoves++;aTotal+=ms;aMax=Math.max(aMax,ms); }
            else { bMoves++;bTotal+=ms;bMax=Math.max(bMax,ms); }
            try { unchanged(before,b); }
            catch(IllegalStateException ex) { anomaly=ex.getMessage(); break; }
            if(m==null || !b.isValidMove(m.row,m.col)) {
                anomaly="INVALID_MOVE ply="+ply+" move="+m; break;
            }
            MinimaxAI current=actorA?a:other;
            trace.add(new Turn(ply,actorA?"A":"B",color,m.row,m.col,ms,
                    current.getNodesEvaluated(),current.getPruningCount()));
            if(!b.makeMove(m.row,m.col,color)) { anomaly="APPLY_REJECTED";break; }
            // Identical terminal order to the game: win before full-board draw.
            if(b.checkWin(m.row,m.col)) { outcome=actorA?"A_WIN":"B_WIN";break; }
            if(b.isFull()) { outcome="DRAW";break; }
        }
        if(outcome.equals("ABNORMAL") && anomaly.isEmpty()) anomaly="MOVE_LIMIT_WITHOUT_TERMINAL";
        return new Game(id,pair,aFirst?"A":"B",outcome,b.getMoveCount(),(System.nanoTime()-start)/1e6,
                aMoves,aTotal,aMax,bMoves,bTotal,bMax,anomaly,trace,opening);
    }
    private static void gameHeader(BufferedWriter w) throws IOException {
        w.write("game_id,pair,actor_a,actor_b,first_actor,x_actor,o_actor,result,moves,total_ms,a_moves,a_mean_ms,a_max_ms,b_moves,b_mean_ms,b_max_ms,anomaly,easy_seed,opening_id,opening_sequence,sequence\n");
    }
    private static void gameRow(BufferedWriter w,Game g) throws IOException {
        w.write(String.join(",",g.id,g.pair.id(),g.pair.a.name(),g.pair.b.name(),g.first,g.first,
                g.first.equals("A")?"B":"A",g.outcome,Integer.toString(g.moves),number(g.totalMs),
                Integer.toString(g.aMoves),number(g.aMoves==0?0:g.aTotal/g.aMoves),number(g.aMax),
                Integer.toString(g.bMoves),number(g.bMoves==0?0:g.bTotal/g.bMoves),number(g.bMax),
                csv(g.anomaly),csv(SEED_NOTE),g.opening.id,csv(cells(g.opening.cells)),csv(g.trace.stream().map(t->t.row+":"+t.col).collect(java.util.stream.Collectors.joining(";"))))+"\n");
        w.flush();
    }

    private static String cells(int[][] cells) {
        return Arrays.stream(cells).map(c->c[0]+":"+c[1]).collect(java.util.stream.Collectors.joining(";"));
    }
    private static String canonical(int[][] cells) {
        List<String> variants=new ArrayList<>();
        for(int flip:new int[]{-1,1})for(int rotation=0;rotation<4;rotation++) {
            List<String> stones=new ArrayList<>();
            for(int i=0;i<cells.length;i++) {
                int x=(cells[i][0]-7)*flip,y=cells[i][1]-7;
                for(int j=0;j<rotation;j++){int tmp=x;x=-y;y=tmp;}
                stones.add((i%2==0?"X":"O")+":"+(x+7)+":"+(y+7));
            }
            Collections.sort(stones);variants.add(String.join(";",stones));
        }
        return Collections.min(variants);
    }
    private static List<Opening> openings(int count) {
        Random random=new Random(20261031L);List<Opening> out=new ArrayList<>();java.util.Set<String> seen=new java.util.HashSet<>();
        for(int attempts=0;out.size()<count&&attempts<100000;attempts++) {
            int n=out.size()%2==0?3:4;int[][] cells=new int[n][2];cells[0]=new int[]{7,7};Board b=board();b.makeMove(7,7,Piece.X);
            for(int i=1;i<n;i++){int r,c;do{r=5+random.nextInt(5);c=5+random.nextInt(5);}while(!b.isValidMove(r,c));cells[i]=new int[]{r,c};b.makeMove(r,c,i%2==0?Piece.X:Piece.O);}
            if(seen.add(canonical(cells)))out.add(new Opening(String.format(Locale.ROOT,"O%02d",out.size()+1),cells));
        }
        if(out.size()!=count)throw new IllegalStateException("Opening generation exhausted");return out;
    }

    private static List<Position> corpus() {
        List<Position> positions=new ArrayList<>();
        int[][][] openings={{{7,7},{7,8},{6,8}},{{0,0},{0,1},{1,1},{1,2}},{{7,7},{6,6},{8,8},{6,8},{8,6}}};
        for(int i=0;i<openings.length;i++) {
            Board b=board(); for(int n=0;n<openings[i].length;n++) b.makeMove(openings[i][n][0],openings[i][n][1],n%2==0?Piece.X:Piece.O);
            positions.add(new Position("opening-"+(i+1),"opening",b,b.getMoveCount()%2==0?Piece.X:Piece.O,"explicit alternating sequence: "+Arrays.deepToString(openings[i])));
        }
        for(int count:new int[]{10,14,18,20}) {
            Board b=board(); long seed=2026103000L+count; Random random=new Random(seed);
            int attempts=0;
            while(b.getMoveCount()<count) {
                if(++attempts>100000) throw new IllegalStateException("Corpus generation exhausted");
                // Central cluster, selected by a fixed construction seed, never by AI output.
                int r=3+random.nextInt(9),c=3+random.nextInt(9);
                if(b.makeMove(r,c,b.getMoveCount()%2==0?Piece.X:Piece.O) && b.checkWin(r,c)) b.undoMove(r,c);
            }
            positions.add(new Position("middle-"+count,"middle",b,Piece.X,"central 9x9 alternating legal nonwinning placements; seed="+seed));
        }
        for(int count:new int[]{40,60,100}) {
            Board b=board(); List<int[]> xs=new ArrayList<>(),os=new ArrayList<>();long seed=2026103100L+count;
            for(int r=0;r<Board.SIZE;r++) for(int c=0;c<Board.SIZE;c++)
                ((c+(r%2==0?0:2))%4<2?xs:os).add(new int[]{r,c});
            Collections.shuffle(xs,new Random(seed));Collections.shuffle(os,new Random(seed+1));
            for(int i=0;i<count;i++) { int[] cell=(i%2==0?xs:os).get(i/2); b.makeMove(cell[0],cell[1],i%2==0?Piece.X:Piece.O); }
            positions.add(new Position("late-"+count,"late",b,Piece.X,"balanced shuffled subset of XXOO/OOXX draw pattern; seeds="+seed+","+(seed+1)));
        }
        for(Position p:positions) noWin(p.board);
        return positions;
    }
    private static void benchmark(Path output,int warmups,int measurements) throws IOException {
        List<Position> positions=corpus();
        StringBuilder corpusText=new StringBuilder("# Fixed benchmark corpus\n\nCoordinates: zero-based row/column. X starts; side to move follows parity. RULE_B. No position contains a pre-existing win.\n\n"+
                "Construction is fixed independently of AI answers. Late 40/60/100 are stone-count categories, not claims of realistic endgame difficulty. No positions are filtered by runtime, nodes, or engine choice.\n");
        for(Position p:positions) corpusText.append("\n## ").append(p.id).append("; stones=").append(p.board.getMoveCount()).append("; turn=").append(p.turn.name()).append("\n\n")
                .append(p.construction).append("\n\n```text\n").append(ascii(p.board)).append("```\n");
        markdown(output,"benchmark-positions.md",corpusText.toString());
        try(BufferedWriter w=writer(output,"benchmark.csv")) {
            w.write("position,stage,stones,side_to_move,level,depth,phase,repetition,elapsed_ms,minimax_nodes,alpha_beta_prunes,row,col,easy_seed\n");
            for(Position p:positions) for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values()) {
                MinimaxAI ai=new MinimaxAI(p.turn,level);
                for(int repetition=0;repetition<warmups+measurements;repetition++) {
                    Board before=p.board.copy();long start=System.nanoTime();Move m=ai.findBestMove(p.board);
                    double ms=(System.nanoTime()-start)/1e6;unchanged(before,p.board);
                    if(m==null || !p.board.isValidMove(m.row,m.col)) throw new IllegalStateException("Illegal benchmark result "+p.id+" "+level);
                    Sample sample=new Sample(p.id,p.stage,p.board.getMoveCount(),p.turn,level,repetition<warmups?"warmup":"measured",
                            repetition<warmups?repetition+1:repetition-warmups+1,ms,ai.getNodesEvaluated(),ai.getPruningCount(),m.row,m.col);
                    w.write(String.join(",",sample.position,sample.stage,Integer.toString(sample.stones),sample.turn.name(),sample.level.name(),Integer.toString(level.depth),sample.phase,Integer.toString(sample.repetition),
                            number(ms),Long.toString(sample.nodes),Long.toString(sample.prunes),Integer.toString(m.row),Integer.toString(m.col),csv(SEED_NOTE))+"\n");
                }
                w.flush();System.out.println("BENCHMARK "+p.id+" "+level.name()+" completed "+warmups+" warmup + "+measurements+" measured");
            }
        }
    }

    private static void arena(Path output,int count,int threads) throws Exception {
        List<Opening> openingSet=openings(count);var executor=Executors.newFixedThreadPool(threads);
        var completed=new ExecutorCompletionService<Game>(executor);
        try(BufferedWriter w=writer(output,"arena-games.csv")) {
            gameHeader(w);
            for(Pair pair:PAIRS)for(Opening opening:openingSet)for(int side=0;side<2;side++) {
                final boolean first=side==0;String id=pair.id()+"-"+opening.id+"-"+(first?"A":"B");
                completed.submit(()->play(id,pair,first,opening));
            }
            int total=count*2*PAIRS.size();
            for(int i=1;i<=total;i++) {
                Game g=completed.take().get();gameRow(w,g);
                System.out.printf(Locale.ROOT,"ARENA %d/%d %s %s %d moves %.3fs%n",i,total,g.id,g.outcome,g.moves,g.totalMs/1000);
                if(!g.anomaly.isEmpty())throw new IllegalStateException(g.id+": "+g.anomaly);
            }
        }finally{executor.shutdownNow();}
    }
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);long start=System.nanoTime();
        Path output=null;int count=16,threads=2,warmups=5,measurements=20;String revision="unspecified";
        for(int i=0;i<args.length;i++)switch(args[i]) {
            case "--output-base64" -> output=Path.of(new String(Base64.getDecoder().decode(args[++i]),StandardCharsets.UTF_8)).toAbsolutePath();
            case "--openings" -> count=Integer.parseInt(args[++i]);
            case "--threads" -> threads=Integer.parseInt(args[++i]);
            case "--warmups" -> warmups=Integer.parseInt(args[++i]);
            case "--measurements" -> measurements=Integer.parseInt(args[++i]);
            case "--revision" -> revision=args[++i];
            default -> throw new IllegalArgumentException(args[i]);
        }
        if(output==null||count<1||count>64||threads<1||warmups<1||measurements<2)throw new IllegalArgumentException("Invalid run arguments");
        Files.createDirectories(output);
        markdown(output,"environment.md","# Environment v2\n\n- Timestamp: "+ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))+"\n- Base revision: "+revision+
            "\n- Java: "+System.getProperty("java.runtime.version")+" "+System.getProperty("java.vendor")+
            "\n- CPU: "+System.getenv("ARENA_CPU_NAME")+"\n- CPU identifier: "+System.getenv("PROCESSOR_IDENTIFIER")+
            "\n- Logical processors: "+Runtime.getRuntime().availableProcessors()+"\n- OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+
            "\n- Opening seed: 20261031; openings: "+count+"; two color assignments each; five pairs.\n- Arena workers: "+threads+" (exclusive Board/engine ownership); benchmark sequential before arena.\n- Warmups per position/level: "+warmups+"; measured: "+measurements+
            "\n- EASY seed: "+SEED_NOTE+". Only openings/corpus are exactly reproducible.\n- Calls direct MinimaxAI.findBestMove: no BoardPanel, AiMoveDelay, Swing, audio, or LAN.\n");
        benchmark(output,warmups,measurements);arena(output,count,threads);
        Files.writeString(output.resolve("environment.md"),"\n- Java measurement wall time seconds: "+number((System.nanoTime()-start)/1e9)+"\n",StandardCharsets.UTF_8,java.nio.file.StandardOpenOption.APPEND);
    }
}
