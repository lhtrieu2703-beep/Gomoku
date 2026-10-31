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
import java.util.Comparator;
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
            new Pair(E,E),new Pair(M,M),new Pair(H,H));
    record Pair(MinimaxAI.Difficulty a, MinimaxAI.Difficulty b) {
        String id() { return a.name() + "-" + b.name(); }
    }
    record Turn(int ply, String actor, Piece color, int row, int col, double ms, long nodes, long prunes) {}
    record Game(String id, Pair pair, String first, String outcome, int moves, double totalMs,
                int aMoves, double aTotal, double aMax, int bMoves, double bTotal, double bMax,
                String anomaly, List<Turn> trace) {}
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

    private static Game play(String id, Pair pair, boolean aFirst) {
        Board b=board();
        Piece aColor=aFirst?Piece.X:Piece.O;
        // Exclusive ownership: no Board or engine is ever shared across games/tasks.
        MinimaxAI a=new MinimaxAI(aColor,pair.a), other=new MinimaxAI(aColor.opposite(),pair.b);
        List<Turn> trace=new ArrayList<>();
        int aMoves=0,bMoves=0; double aTotal=0,bTotal=0,aMax=0,bMax=0;
        String outcome="ABNORMAL",anomaly="";
        long start=System.nanoTime();
        for(int ply=1;ply<=MAX_MOVES;ply++) {
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
                aMoves,aTotal,aMax,bMoves,bTotal,bMax,anomaly,trace);
    }
    private static void gameHeader(BufferedWriter w) throws IOException {
        w.write("game_id,pair,actor_a,actor_b,first_actor,x_actor,o_actor,result,moves,total_ms,a_moves,a_mean_ms,a_max_ms,b_moves,b_mean_ms,b_max_ms,anomaly,easy_seed\n");
    }
    private static void gameRow(BufferedWriter w,Game g) throws IOException {
        w.write(String.join(",",g.id,g.pair.id(),g.pair.a.name(),g.pair.b.name(),g.first,g.first,
                g.first.equals("A")?"B":"A",g.outcome,Integer.toString(g.moves),number(g.totalMs),
                Integer.toString(g.aMoves),number(g.aMoves==0?0:g.aTotal/g.aMoves),number(g.aMax),
                Integer.toString(g.bMoves),number(g.bMoves==0?0:g.bTotal/g.bMoves),number(g.bMax),
                csv(g.anomaly),csv(SEED_NOTE))+"\n");
        w.flush();
    }

    private static int pilot(Path output,int requested) throws IOException {
        List<Game> games=new ArrayList<>();
        List<Pair> selected=List.of(new Pair(H,H),new Pair(E,H),new Pair(M,H));
        try(BufferedWriter w=writer(output,"pilot-games.csv")) {
            gameHeader(w);
            for(int i=0;i<3;i++) {
                Game g=play("pilot-"+(i+1),selected.get(i),i%2==0); games.add(g);gameRow(w,g);
                System.out.printf(Locale.ROOT,"PILOT %s %s %d moves %.3fs anomaly=%s%n",g.pair.id(),g.outcome,g.moves,g.totalMs/1000,g.anomaly);
                if(!g.anomaly.isEmpty()) throw new IllegalStateException("Pilot anomaly: "+g.anomaly);
            }
        }
        double maxMs=games.stream().mapToDouble(Game::totalMs).max().orElseThrow();
        double estimate=maxMs*PAIRS.size()*requested/1000;
        int actual=estimate>7200?Math.min(requested,30):requested;
        StringBuilder text=new StringBuilder("# Three-game pilot\n\n| Pair | Result | Moves | Seconds |\n|---|---|---:|---:|\n");
        for(Game g:games) text.append("| ").append(g.pair.id()).append(" | ").append(g.outcome).append(" | ").append(g.moves).append(" | ").append(number(g.totalMs/1000)).append(" |\n");
        text.append("\nConservative estimate = slowest measured pilot game × 6 pairs × ").append(requested)
                .append(" games = ").append(number(estimate)).append(" seconds (").append(number(estimate/60)).append(" minutes), ignoring any parallel speedup.\n")
                .append("Selected games per pair: ").append(actual).append(". Pilot games are excluded from arena standings.\n")
                .append("This is an estimate from three games, not an upper bound or search timeout. EASY seed: ").append(SEED_NOTE).append(".\n");
        markdown(output,"pilot-summary.md",text.toString());
        System.out.printf(Locale.ROOT,"ESTIMATE %.2f minutes; selected=%d games/pair%n",estimate/60,actual);
        return actual;
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
    private static double median(double[] values) {
        double[] sorted=values.clone();Arrays.sort(sorted);int n=sorted.length;
        return n%2==1?sorted[n/2]:(sorted[n/2-1]+sorted[n/2])/2;
    }
    private static double sd(double[] values) {
        double mean=Arrays.stream(values).average().orElseThrow();double sum=0;
        for(double value:values) sum+=(value-mean)*(value-mean);
        return Math.sqrt(sum/values.length); // Population SD, denominator n (not n-1).
    }
    private static String stats(double[] values) { return number(median(values))+" ± "+number(sd(values)); }
    private static void statsRow(StringBuilder text,String prefix,List<Sample> samples) {
        text.append("| ").append(prefix).append(" | ").append(samples.size()).append(" | ")
                .append(stats(samples.stream().mapToDouble(Sample::ms).toArray())).append(" | ")
                .append(stats(samples.stream().mapToDouble(Sample::nodes).toArray())).append(" | ")
                .append(stats(samples.stream().mapToDouble(Sample::prunes).toArray())).append(" |\n");
    }
    private static void benchmark(Path output) throws IOException {
        List<Position> positions=corpus();List<Sample> samples=new ArrayList<>();
        StringBuilder corpusText=new StringBuilder("# Fixed benchmark corpus\n\nCoordinates: zero-based row/column. X starts; side to move follows parity. RULE_B. No position contains a pre-existing win.\n\n"+
                "Construction is fixed independently of AI answers. Late 40/60/100 are stone-count categories, not claims of realistic endgame difficulty. No positions are filtered by runtime, nodes, or engine choice.\n");
        for(Position p:positions) corpusText.append("\n## ").append(p.id).append("; stones=").append(p.board.getMoveCount()).append("; turn=").append(p.turn.name()).append("\n\n")
                .append(p.construction).append("\n\n```text\n").append(ascii(p.board)).append("```\n");
        markdown(output,"benchmark-positions.md",corpusText.toString());
        try(BufferedWriter w=writer(output,"benchmark.csv")) {
            w.write("position,stage,stones,side_to_move,level,phase,repetition,elapsed_ms,minimax_nodes,alpha_beta_prunes,row,col,easy_seed\n");
            for(Position p:positions) for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values()) {
                MinimaxAI ai=new MinimaxAI(p.turn,level);
                for(int repetition=0;repetition<7;repetition++) {
                    Board before=p.board.copy();long start=System.nanoTime();Move m=ai.findBestMove(p.board);
                    double ms=(System.nanoTime()-start)/1e6;unchanged(before,p.board);
                    if(m==null || !p.board.isValidMove(m.row,m.col)) throw new IllegalStateException("Illegal benchmark result "+p.id+" "+level);
                    Sample sample=new Sample(p.id,p.stage,p.board.getMoveCount(),p.turn,level,repetition<2?"warmup":"measured",
                            repetition<2?repetition+1:repetition-1,ms,ai.getNodesEvaluated(),ai.getPruningCount(),m.row,m.col);
                    if(repetition>=2) samples.add(sample);
                    w.write(String.join(",",sample.position,sample.stage,Integer.toString(sample.stones),sample.turn.name(),sample.level.name(),sample.phase,Integer.toString(sample.repetition),
                            number(ms),Long.toString(sample.nodes),Long.toString(sample.prunes),Integer.toString(m.row),Integer.toString(m.col),csv(SEED_NOTE))+"\n");
                }
                w.flush();System.out.println("BENCHMARK "+p.id+" "+level.name()+" completed 2 warmup + 5 measured");
            }
        }
        StringBuilder text=new StringBuilder("# Benchmark\n\n10 fixed positions × 3 levels × (2 warm-up + 5 measured). Raw CSV includes both phases; summaries exclude all 60 warm-ups (150 measured samples).\n\n"+
                "Each cell is median ± population standard deviation (denominator n). Wall-clock search time uses System.nanoTime; board copy and invariant checks are outside the timed region. Sequential benchmark, before concurrent arena. EASY seed uncontrolled. Nodes count recursive minimax entries only; 0 nodes does not mean no computation.\n\n"+
                "## Pooled measurements across the ten positions\n\nThese distributions mix different positions and tactical early returns; they are not a single-position confidence interval or playing-strength estimate.\n\n"+
                "| Level | n | ms median ± SD | Nodes median ± SD | Prunes median ± SD |\n|---|---:|---:|---:|---:|\n");
        for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values()) statsRow(text,level.name(),samples.stream().filter(s->s.level==level).toList());
        text.append("\n## Per position and level\n\n| Position / level | n | ms median ± SD | Nodes median ± SD | Prunes median ± SD |\n|---|---:|---:|---:|---:|\n");
        for(Position p:positions) for(MinimaxAI.Difficulty level:MinimaxAI.Difficulty.values())
            statsRow(text,p.id+" / "+level.name(),samples.stream().filter(s->s.position.equals(p.id)&&s.level==level).toList());
        text.append("\nFull ASCII boards and construction seeds are in benchmark-positions.md. Position seeds do not seed production EASY. Only five measured repetitions per cell; timing variance includes JIT/GC/OS effects.\n");
        markdown(output,"benchmark-summary.md",text.toString());
    }

    private static void arena(Path output,int gamesPerPair,int threads) throws Exception {
        long started=System.nanoTime();List<Game> games=new ArrayList<>();
        var executor=Executors.newFixedThreadPool(threads);
        var completed=new ExecutorCompletionService<Game>(executor);
        try(BufferedWriter w=writer(output,"arena-games.csv");BufferedWriter moves=writer(output,"arena-moves.csv")) {
            gameHeader(w);moves.write("game_id,ply,actor,color,row,col,elapsed_ms,minimax_nodes,alpha_beta_prunes\n");
            for(Pair pair:PAIRS) for(int i=0;i<gamesPerPair;i++) {
                final int index=i;String id=pair.id()+"-"+String.format(Locale.ROOT,"%03d",i+1);
                completed.submit(()->play(id,pair,index%2==0));
            }
            int total=gamesPerPair*PAIRS.size();
            for(int i=1;i<=total;i++) {
                Game g=completed.take().get();games.add(g);gameRow(w,g);
                for(Turn t:g.trace) moves.write(String.join(",",g.id,Integer.toString(t.ply),t.actor,t.color.name(),Integer.toString(t.row),Integer.toString(t.col),number(t.ms),Long.toString(t.nodes),Long.toString(t.prunes))+"\n");
                moves.flush();
                System.out.printf(Locale.ROOT,"ARENA %d/%d %s first=%s %s moves=%d %.3fs anomaly=%s%n",i,total,g.id,g.first,g.outcome,g.moves,g.totalMs/1000,g.anomaly);
            }
        } finally { executor.shutdownNow(); }
        games.sort(Comparator.comparing(Game::id));
        StringBuilder text=new StringBuilder("# AI arena\n\nRULE_B, 15×15, X starts, >=5 wins unless both ends are blocked by opponent; an edge is not a blocker. Win checked before 225-move draw. Start from an empty board; no randomized opening book.\n\n"+
                "A/B are distinct actors even in self-play. Each pair has "+gamesPerPair+" games, A starts "+(gamesPerPair/2)+", B starts "+(gamesPerPair/2)+". Three pilot games excluded. EASY seed: "+SEED_NOTE+". Exact played moves are saved in arena-moves.csv.\n\n"+
                "| Pair (A–B) | Games | A wins | Draws | B wins | Abnormal | A win % | Draw % | B win % | X wins | O wins |\n|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for(Pair pair:PAIRS) {
            List<Game> group=games.stream().filter(g->g.pair.equals(pair)).toList();
            long aw=group.stream().filter(g->g.outcome.equals("A_WIN")).count(),bw=group.stream().filter(g->g.outcome.equals("B_WIN")).count();
            long draw=group.stream().filter(g->g.outcome.equals("DRAW")).count(),bad=group.size()-aw-bw-draw;
            long xwins=group.stream().filter(g->g.outcome.equals(g.first+"_WIN")).count(),owins=aw+bw-xwins;
            text.append("| ").append(pair.id()).append(" | ").append(group.size()).append(" | ").append(aw).append(" | ").append(draw).append(" | ").append(bw).append(" | ").append(bad)
                    .append(" | ").append(number(100.0*aw/group.size())).append(" | ").append(number(100.0*draw/group.size())).append(" | ").append(number(100.0*bw/group.size()))
                    .append(" | ").append(xwins).append(" | ").append(owins).append(" |\n");
        }
        text.append("\nPercentages use all scheduled games in each pair, including any abnormal games in the denominator; abnormalities are never counted as draws/wins.\n\n")
                .append("Arena wall time: ").append(number((System.nanoTime()-started)/1e9)).append(" seconds; worker threads: ").append(threads).append(". Per-move arena timings include CPU contention; use sequential benchmark for timing comparisons.\n\n")
                .append("At most 225 legal placements per game. Invalid/null move, mutation, runtime exception, or nonterminal move-limit result is recorded as ABNORMAL, with details below. No hard time limit is injected into AI; a very slow individual search can delay completion.\n\n")
                .append("Self-play MEDIUM/HARD is deterministic from the empty board; 50 repetitions are not 50 independent opening positions. Win percentages describe this protocol, not Elo or general superiority.\n\n## Anomalies\n\n");
        List<Game> abnormal=games.stream().filter(g->!g.anomaly.isEmpty()).toList();
        if(abnormal.isEmpty()) text.append("None observed.\n");
        for(Game g:abnormal) text.append("- ").append(g.id).append(": ").append(g.anomaly).append('\n');
        markdown(output,"arena-summary.md",text.toString());
        if(!abnormal.isEmpty()) throw new IllegalStateException("Arena completed with "+abnormal.size()+" abnormal games; see summary");
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        Path output=null;int requested=50,threads=Math.min(2,Runtime.getRuntime().availableProcessors());String phase="all",revision="unspecified";
        for(int i=0;i<args.length;i++) switch(args[i]) {
            case "--output" -> output=Path.of(args[++i]).toAbsolutePath();
            case "--output-base64" -> output=Path.of(new String(Base64.getDecoder().decode(args[++i]),StandardCharsets.UTF_8)).toAbsolutePath();
            case "--games" -> requested=Integer.parseInt(args[++i]);
            case "--threads" -> threads=Integer.parseInt(args[++i]);
            case "--phase" -> phase=args[++i];
            case "--revision" -> revision=args[++i];
            default -> throw new IllegalArgumentException("Unknown argument "+args[i]);
        }
        if(output==null || requested<2 || requested%2!=0 || threads<1 || !List.of("all","pilot","benchmark").contains(phase))
            throw new IllegalArgumentException("Use --output PATH [--games EVEN_COUNT] [--threads N] [--phase all|pilot|benchmark] [--revision SHA]");
        Files.createDirectories(output);
        markdown(output,"environment.md","# Measurement environment\n\n- Timestamp (Asia/Ho_Chi_Minh): "+ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))+"\n- Source revision before measurement: "+revision+
                "\n- Java: "+System.getProperty("java.runtime.version")+"\n- Vendor: "+System.getProperty("java.vendor")+
                "\n- VM: "+System.getProperty("java.vm.name")+"\n- OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+
                "\n- Architecture: "+System.getProperty("os.arch")+"\n- Available logical processors: "+Runtime.getRuntime().availableProcessors()+
                "\n- Arena workers: "+threads+"; pilot and benchmark sequential.\n- EASY seed: "+SEED_NOTE+
                "\n- Each game owns its Board and two MinimaxAI instances; heuristic methods have no mutable shared state. No engine is searched concurrently. Production Board/AI source is not modified.\n"+
                "- Headless tool: no Swing UI, network, audio, or user Preferences. Compilation occurs in an external temporary copy.\n");
        if(phase.equals("benchmark")) { benchmark(output);return; }
        int games=pilot(output,requested);
        if(phase.equals("pilot")) return;
        benchmark(output);
        arena(output,games,threads);
    }
}
