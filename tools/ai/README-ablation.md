# Alpha-Beta ablation

This experiment changes only the Alpha-Beta cutoff flag. Production instances default to pruning enabled. No rules, heuristic, ordering, candidate caps, UI, or network implementation changes are involved. `searchOnlyForMeasurement` calls the same root kernel as `findBestMove`, bypassing tactical shortcuts. `Move.score` remains the ordering score; the measured root value is stored separately.

Requires JDK 25, PowerShell, and Python 3.10+ (standard library). Run from the repository root. The runner compiles the AI/model sources and both measurement classes in an external temporary directory. It records CPU/core/OS/Java metadata; on Windows it queries physical cores through CIM.

```powershell
# Small pilot first (1 warmup + 2 measurements per variant).
./tools/ai/run-ai-ablation.ps1 -Positions 'opening-1,middle-20' -Warmups 1 -Measurements 2 -OutputDirectory 'docs/ai-results-ablation/pilot-new'

# The main search-only experiment; choose a NEW directory on subsequent runs.
./tools/ai/run-ai-ablation.ps1 -Mode search-only -Warmups 5 -Measurements 20 -OutputDirectory 'docs/ai-results-ablation/rerun-search-only'

# Production behavior, including tactical preprocessing.
./tools/ai/run-ai-ablation.ps1 -Mode full -Warmups 5 -Measurements 20 -OutputDirectory 'docs/ai-results-ablation/rerun-full'

# Regenerate/validate summaries without running searches.
python tools/ai/summarize-ai-ablation.py --output-dir docs/ai-results-ablation
python tools/ai/summarize-ai-ablation.py --output-dir docs/ai-results-ablation/full

# Entire JUnit suite, with no exclusions, in an external copy (UI enabled).
./tools/ai/run-ai-ablation-junit.ps1 -OutputDirectory 'docs/ai-results-ablation/junit-rerun'
```

Existing output files are never overwritten by the measurement runner. Default output on a fresh run is `docs/ai-results-ablation`; the checked-in historical `docs/ai-results` directory is not modified. JUnit render artifacts stay in the temporary copy; only new logs/XML/results are copied into the selected output directory.

The Java CLI supports `--mode search-only|full` (default search-only), `--measurements` (20), `--warmups` (5), `--output-dir`, plus `--positions` and `--timeout-ms`. After compiling `AIArena.java`, `AIAlphaBetaAblation.java` and `src/game/{ai,model}/*.java` to a class directory:

```powershell
java -Xms256m -Xmx1g -cp classes game.tools.AIAlphaBetaAblation --mode search-only --warmups 5 --measurements 20 --output-dir docs/ai-results-ablation/new-java-run
```

Use the PowerShell wrapper to populate CPU/physical-core fields automatically. Its Base64 output-path transport avoids Windows Unicode argv issues; the required plain `--output-dir` Java option is also available.

The exact original `AIArena.corpus()` factory is invoked via reflection from measurement code, without changing that old class. The exported boards are additionally checked cell-by-cell, stone-count and side-to-move against `docs/ai-results/benchmark-positions.md`. No positions are chosen based on performance or AI answers. EASY is excluded.

Every repetition is ON, OFF in one JVM, including warmups, before proceeding to the next repetition. Timing excludes board copy, configuration, watchdog setup, board invariants, CSV and console output. `minimax_nodes` counts entries into `minimax`, including leaves, but not the root loop, candidate evaluations, or all board positions inspected by heuristic code. This is why node ratios need not equal runtime ratios.

The watchdog schedules an interrupt after 30 seconds (configurable downwards with `-TimeoutMs`). Searches still run directly on the caller, not in a timed Future. Existing cancellation checks unwind trial moves through `finally`. On timeout the CSV retains a row with `status=timeout`, partial counters and empty move/root value, then the run stops with exit code 2. The summarizer reports incomplete data and exits nonzero. The additional `status` column follows all requested columns. Full-mode tactical early returns have an empty `root_value`, never an invented zero or `Move.score`.

The CSVs include warmups, but summaries exclude them. p95 is nearest-rank, index `ceil(0.95*n)-1` after sorting. Summaries report deterministic nodes/prunes per call, total nodes both per corpus traversal and across all measured calls, per-position OFF/ON ratios, their medians by level, and `(1 - ON/OFF)*100` node reduction. Any move/value mismatch, missing pair, changing node count, nonzero OFF pruning or corpus discrepancy is retained in the report and causes nonzero exit.

The default candidate order, caps and strict root `>` tie-breaking remain intact. Existing recursive returns for an immediate win remain intact even with pruning disabled. Thus this measures the current capped Minimax kernel with/without Alpha-Beta, rather than an exhaustive full-board Gomoku solver. `search-only` and `full` are distinct experiments, each in its own JVM/output directory; performance comparisons are within each run. All recorded results are NEW runs on 24/11/2026, independent of old performance numbers and old JUnit milestones.
