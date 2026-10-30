# AI validation (Task 5)

Production AI, Board and existing tests are unchanged. The new JUnit class is
`game.ai.AITacticsTest`; the standalone arena is excluded from normal test discovery.

From the repository root, run:

```powershell
./tools/ai/run-ai-validation.ps1
```

The script compiles an external temporary source copy, then writes only measurement
outputs to `docs/ai-results/`. It retains the temporary build for inspection; it
never cleans, compiles into, or renders images in the original repository.
Options: `-Phase pilot|benchmark|all`, `-GamesPerPair 50`, `-Threads 2`, and
`-OutputDirectory PATH`. An even game count is required. Defaults run three pilot
games, a sequential benchmark, then all six pairs. If the conservative pilot
estimate exceeds two hours, the tool reduces the count to 30 per pair and prints
the decision. This is not a hard AI timeout.

EASY has an inaccessible unseeded Random. The user chose to preserve src and accept
non-reproducible random choices. No reflection, custom seed injection, or behavior
changes are used. Fixed benchmark construction seeds do not seed EASY. Full arena
move traces permit replay of observed games, not reproduction of future searches.

Output:

- `pilot-games.csv`, `pilot-summary.md`: three games and pre-run time estimate.
- `arena-games.csv`: result, starters, moves, wall time, each actor's move mean/max.
- `arena-moves.csv`: exact played moves, color/actor, time, nodes and cutoffs.
- `arena-summary.md`: A/B and X/O wins, draws, abnormalities and percentages.
- `benchmark.csv`: 60 warm-ups plus 150 measured samples, with `phase` column.
- `benchmark-summary.md`: median and population SD, per position and pooled.
- `benchmark-positions.md`: all ten ASCII positions and independent construction.
- `environment.md`: timestamp, Java/OS/logical CPU count, thread and seed policy.

After completion, `python tools/ai/verify-ai-results.py` independently replays every
placement with a Python RULE_B oracle and recomputes CSV/Markdown counts, percentages,
timing aggregates, medians and population SD. It reads files and prints checks only.
Python is optional for running the Java arena itself.

The arena uses exclusive Board/MinimaxAI instances per game. Up to two independent
games run concurrently; the source has no mutable shared heuristic state. Benchmark
and pilot run sequentially before the arena to avoid arena CPU contention. Measured
search time excludes board copying/invariant verification. Arena total game time
includes bookkeeping and invariant checks. Invalid moves and board mutation are
abnormal results, never silently converted into draws or wins.

The stone-count corpus contains opening (3/4/5), middle (10/14/18/20), and denser
(40/60/100) positions. Dense draw-pattern subsets are controlled synthetic positions,
not claims about typical human endgames. Every fixture is checked for pre-existing
wins. No fixture or expected answer is chosen from AI output.

MEDIUM/HARD use deterministic search from the same empty board. Repeating a pairing
does not provide independent opening diversity, Elo, or a general strength ranking.
Five timing repetitions per position are a small sample; pooled distributions mix
different positions and tactical early exits. All these limits accompany results.

Run the complete JUnit suite only in an external source/test/resource/library copy:
existing UI tests overwrite their render images. Use the unchanged `build.ps1`, then
the JUnit console runner with `--scan-class-path`. The new test reports its fixed
expectations, actual selected moves and full ASCII boards; a tactical failure remains
a real failure. No `@Disabled` is added. EASY's blocked-five case checks nonterminal
classification and legality, not deterministic defense: a random legal choice of the
blocked gap alone does not expose the engine's internal belief about winning.
