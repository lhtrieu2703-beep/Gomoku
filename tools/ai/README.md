# AI validation v2 (commit C)

Run `./tools/ai/run-ai-validation.ps1` from the repo root. Requires JDK and Python 3.10+ (standard library only). It compiles production sources in an external temporary copy; all searches call MinimaxAI directly, never BoardPanel/AiMoveDelay. Default: 16 openings, five pairs, two color assignments per opening (160 attempts), two independent game workers. Sequential benchmark: ten fixed boards x three levels x five discarded warmups plus twenty measured repetitions. CLI options: -Openings, -Measurements, -Warmups, -Threads, -OutputDirectory. The verifier reads the recorded warmup/measurement protocol from environment.md.

Openings use construction seed 20261031, X at centre plus 2/3 alternating moves in the central 5x5. Reject duplicate colored board states modulo rotations/reflections BEFORE any engine search. Every opening is played twice with actors swapping X/O. EASY's private unseeded Random is unchanged; only openings and corpus construction are exactly reproducible. Full move sequences are embedded compactly in arena-games.csv for independent replay.

summarize-ai-results.py builds Markdown summaries and annotates all attempted games with SHA-256, included, duplicate_of. Exact colored move sequences are deduplicated globally, even across different pairings; a repeated sequence contributes only once. Deterministic representatives sort first by pair then opening id, prefer odd opening ids A-first / even ids B-first, never based on result. Retained side counts can be unbalanced and are reported explicitly. Self-play A/B labels are seats, not different engines.

Draws stay in W/D/L and win/all rates but are excluded from decisive win rates, Wilson 95% intervals and exact two-sided binomial H0 p=0.5. Statistical independence is not guaranteed by paired deterministic games: p-values/intervals are nominal descriptive diagnostics, unadjusted for five comparisons, not confirmatory strength/Elo evidence. A CI containing 0.5 is explicitly labelled "chưa đủ bằng chứng".

Benchmark summaries report every position/level separately (median, nearest-rank p95, max); pooled level statistics carry a dispersion warning and are not used for speed/strength conclusions. Dense boards are synthetic controlled subsets, not typical human endgames. EASY depth=1 is an enum label, not recursive search.

After running: `python tools/ai/verify-ai-results.py`. The read-only verifier independently replays RULE_B, verifies paired openings/symmetry uniqueness, dedup representatives, WDL, Wilson, exact-binomial p-values, side counts, benchmark positions and timing summaries. No AI, UI, rules, audio or network source is modified.

Results: arena-games.csv, arena-summary.md, benchmark.csv, benchmark-summary.md, benchmark-positions.md, environment.md. Old pilot files are obsolete (fixed sample protocol, actual elapsed time recorded); arena-moves.csv is replaced by compact full sequences within arena-games.csv.

Run all JUnit only in an external src/test/resources/lib/build.ps1 copy. Use unchanged recursive build.ps1 and --scan-class-path, including test/game/ai/AITacticsTest.java. Legacy UI tests write images.
