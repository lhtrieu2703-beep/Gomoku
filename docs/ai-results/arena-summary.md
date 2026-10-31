# AI arena

RULE_B, 15×15, X starts, >=5 wins unless both ends are blocked by opponent; an edge is not a blocker. Win checked before 225-move draw. Start from an empty board; no randomized opening book.

A/B are distinct actors even in self-play. Each pair has 50 games, A starts 25, B starts 25. Three pilot games excluded. EASY seed: uncontrolled: production Random has no seed API; src unchanged. Exact played moves are saved in arena-moves.csv.

| Pair (A–B) | Games | A wins | Draws | B wins | Abnormal | A win % | Draw % | B win % | X wins | O wins |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| EASY-MEDIUM | 50 | 0 | 0 | 50 | 0 | 0.000000 | 0.000000 | 100.000000 | 25 | 25 |
| EASY-HARD | 50 | 0 | 0 | 50 | 0 | 0.000000 | 0.000000 | 100.000000 | 25 | 25 |
| MEDIUM-HARD | 50 | 25 | 0 | 25 | 0 | 50.000000 | 0.000000 | 50.000000 | 50 | 0 |
| EASY-EASY | 50 | 14 | 24 | 12 | 0 | 28.000000 | 48.000000 | 24.000000 | 11 | 15 |
| MEDIUM-MEDIUM | 50 | 0 | 50 | 0 | 0 | 0.000000 | 100.000000 | 0.000000 | 0 | 0 |
| HARD-HARD | 50 | 25 | 0 | 25 | 0 | 50.000000 | 0.000000 | 50.000000 | 50 | 0 |

Percentages use all scheduled games in each pair, including any abnormal games in the denominator; abnormalities are never counted as draws/wins.

Arena wall time: 364.026990 seconds; worker threads: 2. Per-move arena timings include CPU contention; use sequential benchmark for timing comparisons.

At most 225 legal placements per game. Invalid/null move, mutation, runtime exception, or nonterminal move-limit result is recorded as ABNORMAL, with details below. No hard time limit is injected into AI; a very slow individual search can delay completion.

Self-play MEDIUM/HARD is deterministic from the empty board; 50 repetitions are not 50 independent opening positions. Win percentages describe this protocol, not Elo or general superiority.

## Anomalies

None observed.
