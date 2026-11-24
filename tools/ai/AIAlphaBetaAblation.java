package game.tools;

import game.ai.MinimaxAI;
import game.ai.Move;
import game.model.Board;
import game.model.Piece;

import java.io.BufferedWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Standalone ablation. No Swing, network, move delays, or arena games are run. */
public final class AIAlphaBetaAblation {
    private record Position(String id, Board board, Piece turn) {}
    private record Sample(double ms, long nodes, long prunes, Move move, Integer value, String status) {}

    private AIAlphaBetaAblation() {}

    // Reuse the ORIGINAL private corpus factory, without changing the old tool or
    // maintaining a second implementation of its seeds/shuffles/placement rules.
    private static List<Position> corpus() throws ReflectiveOperationException {
        Method factory = AIArena.class.getDeclaredMethod("corpus");
        factory.setAccessible(true);
        List<Position> positions = new ArrayList<>();
        for (Object position : (List<?>) factory.invoke(null)) {
            Class<?> type = position.getClass();
            Method id = type.getDeclaredMethod("id"), board = type.getDeclaredMethod("board"),
                    turn = type.getDeclaredMethod("turn");
            id.setAccessible(true); board.setAccessible(true); turn.setAccessible(true);
            positions.add(new Position((String) id.invoke(position), (Board) board.invoke(position),
                    (Piece) turn.invoke(position)));
        }
        if (positions.size() != 10) throw new IllegalStateException("Expected the original ten boards");
        return positions;
    }

    private static void unchanged(Board before, Board after) {
        if (before.getMoveCount() != after.getMoveCount() || before.getWinRule() != after.getWinRule())
            throw new IllegalStateException("Search changed count/rule");
        for (int r = 0; r < Board.SIZE; r++) for (int c = 0; c < Board.SIZE; c++)
            if (before.getPiece(r, c) != after.getPiece(r, c))
                throw new IllegalStateException("Search changed cell " + r + "," + c);
    }

    /** The search runs directly on the calling thread; only the watchdog is scheduled. */
    private static final class Deadline implements AutoCloseable {
        private final Thread owner = Thread.currentThread();
        private final ScheduledFuture<?> task;
        private boolean active = true;
        private boolean timedOut;

        Deadline(ScheduledExecutorService watchdog, long timeoutMs) {
            task = watchdog.schedule(this::expire, timeoutMs, TimeUnit.MILLISECONDS);
        }

        private synchronized void expire() {
            if (active) {
                timedOut = true;
                owner.interrupt();
            }
        }

        synchronized boolean timedOut() { return timedOut; }

        @Override public synchronized void close() {
            active = false;
            task.cancel(false);
            // Clear only this deadline's interruption, after the search has unwound.
            if (timedOut) Thread.interrupted();
        }
    }

    private static Sample sample(Position p, MinimaxAI ai, MinimaxAI.Difficulty level,
                                 String mode, boolean pruning, ScheduledExecutorService watchdog,
                                 long timeoutMs) {
        Board work = p.board.copy(); // Neither this copy nor the checks below are timed.
        ai.setPruningEnabled(pruning);
        Move move = null;
        Integer value = null;
        Deadline deadline = new Deadline(watchdog, timeoutMs);
        long start = System.nanoTime();
        long elapsed;
        try {
            if (mode.equals("search-only")) {
                MinimaxAI.SearchMeasurement result = ai.searchOnlyForMeasurement(work, level, pruning);
                move = result.move(); value = result.rootValue();
            } else {
                move = ai.findBestMove(work);
                value = ai.getLastRootValue();
            }
        } catch (CancellationException ex) {
            if (!deadline.timedOut()) throw ex;
        } finally {
            elapsed = System.nanoTime() - start;
            deadline.close();
        }
        unchanged(p.board, work);
        boolean timeout = deadline.timedOut() || elapsed > TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        if (!pruning && ai.getPruningCount() != 0) throw new IllegalStateException("Pruning while disabled");
        if (!timeout && (move == null || !work.isValidMove(move.row, move.col)))
            throw new IllegalStateException("Invalid result: " + p.id + " " + level);
        return new Sample(elapsed / 1e6, ai.getNodesEvaluated(), ai.getPruningCount(),
                timeout ? null : move, timeout ? null : value, timeout ? "timeout" : "ok");
    }

    private static String number(double n) { return String.format(Locale.ROOT, "%.6f", n); }

    private static void writeSample(BufferedWriter writer, Position p, MinimaxAI.Difficulty level,
                                    String mode, boolean pruning, String phase, int repetition,
                                    Sample sample) throws Exception {
        writer.write(String.join(",", p.id, level.name(), Integer.toString(level.depth), mode,
                pruning ? "on" : "off", phase, Integer.toString(repetition), number(sample.ms),
                Long.toString(sample.nodes), Long.toString(sample.prunes),
                sample.move == null ? "" : Integer.toString(sample.move.row),
                sample.move == null ? "" : Integer.toString(sample.move.col),
                sample.value == null ? "" : sample.value.toString(), sample.status) + "\n");
        writer.flush(); // Outside the timed interval; retain evidence if a later call fails.
    }

    private static String boardText(List<Position> positions) {
        StringBuilder text = new StringBuilder("# Ablation corpus\n\nOriginal AIArena.corpus(), RULE_B; zero-based coordinates.\n");
        for (Position p : positions) {
            text.append("\n## ").append(p.id).append("; stones=").append(p.board.getMoveCount())
                    .append("; turn=").append(p.turn).append("\n\n```text\n    012345678901234\n");
            for (int r = 0; r < Board.SIZE; r++) {
                text.append(String.format(Locale.ROOT, "%02d  ", r));
                for (int c = 0; c < Board.SIZE; c++) text.append(switch (p.board.getPiece(r, c)) {
                    case X -> 'X'; case O -> 'O'; case NONE -> '.';
                });
                text.append('\n');
            }
            text.append("```\n");
        }
        return text.toString();
    }

    public static void main(String[] args) throws Exception {
        String mode = "search-only", revision = "unspecified", selected = "";
        int measurements = 20, warmups = 5;
        long timeoutMs = 30_000;
        Path output = Path.of("docs/ai-results-ablation");
        for (int i = 0; i < args.length; i++) switch (args[i]) {
            case "--mode" -> mode = args[++i];
            case "--measurements" -> measurements = Integer.parseInt(args[++i]);
            case "--warmups" -> warmups = Integer.parseInt(args[++i]);
            case "--output-dir" -> output = Path.of(args[++i]);
            case "--output-dir-base64" -> output = Path.of(new String(Base64.getDecoder().decode(args[++i]), StandardCharsets.UTF_8));
            case "--positions" -> selected = args[++i];
            case "--timeout-ms" -> timeoutMs = Long.parseLong(args[++i]);
            case "--revision" -> revision = args[++i];
            default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
        }
        if (!List.of("search-only", "full").contains(mode) || measurements < 1 || warmups < 0 || timeoutMs < 1 || timeoutMs > 30_000)
            throw new IllegalArgumentException("Invalid mode/count/timeout");
        List<Position> positions = corpus();
        if (!selected.isEmpty()) {
            List<String> ids = Arrays.asList(selected.split(","));
            positions.removeIf(p -> !ids.contains(p.id));
            if (positions.size() != ids.size()) throw new IllegalArgumentException("Unknown/duplicate position: " + selected);
        }
        output = output.toAbsolutePath().normalize();
        // Fail before opening any output: never overwrite historical or previous measurements.
        if (output.getFileName().toString().equalsIgnoreCase("ai-results"))
            throw new IllegalArgumentException("Do not write to the historical ai-results directory");
        Files.createDirectories(output);
        for (String file : List.of("ablation-raw.csv", "environment.md", "ablation-positions.md"))
            if (Files.exists(output.resolve(file))) throw new IllegalArgumentException("Output already exists: " + file);
        Files.writeString(output.resolve("ablation-positions.md"), boardText(positions), StandardCharsets.UTF_8);
        Files.writeString(output.resolve("environment.md"), "# Ablation environment\n\n- Timestamp (Asia/Saigon): "
                + ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")) + "\n- Source revision: " + revision
                + "\n- Java: " + System.getProperty("java.runtime.version") + " " + System.getProperty("java.vendor")
                + "\n- JVM: " + System.getProperty("java.vm.name")
                + "\n- JVM arguments: " + java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()
                + "\n- CPU: " + System.getenv("ABLATION_CPU_NAME")
                + "\n- Physical cores: " + System.getenv("ABLATION_PHYSICAL_CORES")
                + "\n- Logical processors: " + Runtime.getRuntime().availableProcessors()
                + "\n- OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch")
                + "\n- Mode: " + mode + "\n- Positions: " + positions.stream().map(Position::id).toList()
                + "\n- Warmups per position/level/pruning: " + warmups + "; measurements: " + measurements
                + "\n- Timeout per call (ms): " + timeoutMs
                + "\n- One JVM; sequential direct calls; order on, off for every repetition, including warmups."
                + "\n- nanoTime covers the direct search call, including candidate generation/order and result collection; board copy, configuration, watchdog setup, invariants, CSV and console output are excluded."
                + "\n- A daemon watchdog only interrupts the calling thread; no parallel searches or timed Future dispatch."
                + "\n- Full-mode root_value is empty when tactical/empty-board/no-candidate paths bypass root search. Move.score is never substituted."
                + "\n- status is an additional CSV column (ok/timeout); timeout samples have no completed move/value and stop the run after preserving the row."
                + "\n- Original corpus is reused via reflection in measurement code only. Late boards are synthetic stone-count categories."
                + "\n- New run; no performance comparison with 31/10/2026.\n", StandardCharsets.UTF_8);
        var watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ablation-deadline"); thread.setDaemon(true); return thread;
        });
        long wallStart = System.nanoTime();
        boolean timedOut = false;
        boolean completed = false;
        try (BufferedWriter writer = Files.newBufferedWriter(output.resolve("ablation-raw.csv"), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            writer.write("position,level,depth,mode,pruning,phase,repetition,elapsed_ms,minimax_nodes,alpha_beta_prunes,best_row,best_col,root_value,status\n");
            outer: for (Position p : positions) for (MinimaxAI.Difficulty level : List.of(MinimaxAI.Difficulty.MEDIUM, MinimaxAI.Difficulty.HARD)) {
                MinimaxAI on = new MinimaxAI(p.turn, level), off = new MinimaxAI(p.turn, level);
                for (int i = 0; i < warmups + measurements; i++) {
                    String phase = i < warmups ? "warmup" : "measure";
                    int repetition = i < warmups ? i + 1 : i - warmups + 1;
                    for (boolean pruning : new boolean[]{true, false}) {
                        Sample result = sample(p, pruning ? on : off, level, mode, pruning, watchdog, timeoutMs);
                        writeSample(writer, p, level, mode, pruning, phase, repetition, result);
                        if (result.status.equals("timeout")) {
                            System.err.println("TIMEOUT " + p.id + " " + level + " pruning=" + pruning + " elapsed_ms=" + number(result.ms));
                            timedOut = true; break outer;
                        }
                    }
                }
                System.out.println("ABLATION " + mode + " " + p.id + " " + level.name() + ": " + warmups + " warmups + " + measurements + " measurements per variant");
            }
            completed = !timedOut;
        } finally {
            watchdog.shutdownNow();
            Files.writeString(output.resolve("environment.md"), "\n- Measurement loop wall seconds: " + number((System.nanoTime() - wallStart) / 1e9)
                    + "\n- Run status: " + (timedOut ? "timeout (incomplete)" : completed ? "completed" : "failed (incomplete)") + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
        if (timedOut) System.exit(2);
    }
}
