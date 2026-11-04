# Arena v2

Openings: 16; attempted games: 160; globally unique / included: 127; exact sequences unique globally: 127; excluded duplicate rows: 33.

RULE_B, 15x15, X black starts; win before full-board draw. Each fixed opening is forced for both color assignments; engine play starts afterwards. Opening time is excluded from per-actor search timings. Each game stops at a win or 225 placements.

Deduplicate exact full color-coordinate sequences GLOBALLY, including across pairings; only one row per sequence contributes to statistics. All paired attempts remain available for audit. No symmetry folding of completed games. All attempted rows remain in CSV with sequence, hash, included and duplicate_of. Representative choice is deterministic: lexicographically first pair then opening id, preferring A-first on odd opening ids / B-first on even ids (not selected by outcome). Deduplication can unbalance color counts; the side table uses actual retained denominators.

Draws are reported but excluded from Wilson 95% intervals and the exact two-sided binomial test H0 p=0.5. Win/all uses all retained games; decisive rate uses wins/(wins+losses). B has complementary decisive rate/interval and the same p-value. NA means no decisive games.

**Inference limits:** Wilson/binomial assume independent Bernoulli trials. Paired openings and deterministic continuations violate strict independence; intervals/p-values here are nominal descriptive diagnostics, not confirmatory evidence. Five comparisons are unadjusted. Self-play actor A/B comparisons have no engine-strength meaning. Fixed small synthetic corpus, uncontrolled EASY RNG, and dedup selection preclude Elo/general superiority claims.

| Pair A-B | Attempts | Unique | A W/D/L | B W/D/L | A win/all | A decisive | A Wilson95 | B decisive | B Wilson95 | p two-sided |
|---|---:|---:|---|---|---:|---:|---|---:|---|---:|
| EASY-HARD | 32 | 32 | 0/0/32 | 32/0/0 | 0.000000 | 0.000000 | [0.000000, 0.107179] | 1.000000 | [0.892821, 1.000000] | 4.6566129e-10 |
| EASY-MEDIUM | 32 | 32 | 0/0/32 | 32/0/0 | 0.000000 | 0.000000 | [0.000000, 0.107179] | 1.000000 | [0.892821, 1.000000] | 4.6566129e-10 |
| HARD-HARD | 32 | 16 | 11/0/5 | 5/0/11 | 0.687500 | 0.687500 | [0.444044, 0.858354] | 0.312500 | [0.141646, 0.555956] | 0.21011353 |
| MEDIUM-HARD | 32 | 31 | 12/0/19 | 19/0/12 | 0.387097 | 0.387097 | [0.237331, 0.561759] | 0.612903 | [0.438241, 0.762669] | 0.28104152 |
| MEDIUM-MEDIUM | 32 | 16 | 7/1/8 | 8/1/7 | 0.437500 | 0.466667 | [0.248095, 0.698830] | 0.533333 | [0.301170, 0.751905] | 1 |

## Side effect (retained rows only)

Each entry is W/D/L; win/all. A/B identify engines named by the pair, including distinct seats in self-play.

| Pair | A as X / first | A as O / second | B as X / first | B as O / second |
|---|---|---|---|---|
| EASY-HARD | 0/0/16; 0.000000 | 0/0/16; 0.000000 | 16/0/0; 1.000000 | 16/0/0; 1.000000 |
| EASY-MEDIUM | 0/0/16; 0.000000 | 0/0/16; 0.000000 | 16/0/0; 1.000000 | 16/0/0; 1.000000 |
| HARD-HARD | 7/0/1; 0.875000 | 4/0/4; 0.500000 | 4/0/4; 0.500000 | 1/0/7; 0.125000 |
| MEDIUM-HARD | 8/0/7; 0.533333 | 4/0/12; 0.250000 | 12/0/4; 0.750000 | 7/0/8; 0.466667 |
| MEDIUM-MEDIUM | 3/0/5; 0.375000 | 4/1/3; 0.500000 | 3/1/4; 0.375000 | 5/0/3; 0.625000 |

## Conclusions

- EASY-HARD: Observed imbalance within this protocol; nominal interval excludes 0.5, but paired/dependent data do not establish general engine superiority.
- EASY-MEDIUM: Observed imbalance within this protocol; nominal interval excludes 0.5, but paired/dependent data do not establish general engine superiority.
- HARD-HARD: chưa đủ bằng chứng: Wilson95 contains 0.5.
- MEDIUM-HARD: chưa đủ bằng chứng: Wilson95 contains 0.5.
- MEDIUM-MEDIUM: chưa đủ bằng chứng: Wilson95 contains 0.5.

Full exact sequences replace arena-moves.csv; EASY engine randomness remains uncontrolled, while opening seed 20261031 and all recorded positions are reproducible.
