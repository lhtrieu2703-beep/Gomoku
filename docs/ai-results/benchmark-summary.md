# Benchmark v2

Sequential, direct MinimaxAI calls. Warmups discarded. Wall time excludes board copy/invariant checks. p95 uses nearest-rank ceil(0.95*n). Depth 1 is EASY enum configuration; EASY does not execute recursive Minimax. Zero nodes may mean a tactical early return.

| Position | Level | Depth | n | median ms | p95 ms | max ms |
|---|---|---:|---:|---:|---:|---:|
| opening-1 | EASY | 1 | 20 | 0.143050 | 0.201300 | 0.283700 |
| opening-1 | MEDIUM | 2 | 20 | 15.212500 | 16.543300 | 17.314200 |
| opening-1 | HARD | 3 | 20 | 125.250400 | 139.621400 | 155.576500 |
| opening-2 | EASY | 1 | 20 | 0.060000 | 0.347200 | 0.616600 |
| opening-2 | MEDIUM | 2 | 20 | 9.184000 | 9.758900 | 10.276400 |
| opening-2 | HARD | 3 | 20 | 41.378350 | 44.144400 | 44.328700 |
| opening-3 | EASY | 1 | 20 | 0.154700 | 0.179900 | 0.187500 |
| opening-3 | MEDIUM | 2 | 20 | 20.541100 | 21.917300 | 22.031400 |
| opening-3 | HARD | 3 | 20 | 88.093850 | 92.159200 | 101.586000 |
| middle-10 | EASY | 1 | 20 | 0.065050 | 0.068500 | 0.070600 |
| middle-10 | MEDIUM | 2 | 20 | 45.298600 | 47.887100 | 47.932000 |
| middle-10 | HARD | 3 | 20 | 235.017250 | 248.160900 | 248.162400 |
| middle-14 | EASY | 1 | 20 | 0.073200 | 0.112800 | 0.124900 |
| middle-14 | MEDIUM | 2 | 20 | 44.536050 | 46.048700 | 46.219300 |
| middle-14 | HARD | 3 | 20 | 4.794000 | 5.047700 | 5.209300 |
| middle-18 | EASY | 1 | 20 | 0.098800 | 0.157000 | 0.354900 |
| middle-18 | MEDIUM | 2 | 20 | 4.473700 | 5.344900 | 5.374300 |
| middle-18 | HARD | 3 | 20 | 4.370600 | 5.079400 | 5.388000 |
| middle-20 | EASY | 1 | 20 | 0.092350 | 0.101800 | 0.110400 |
| middle-20 | MEDIUM | 2 | 20 | 49.424000 | 50.837800 | 52.620700 |
| middle-20 | HARD | 3 | 20 | 261.491200 | 272.116400 | 276.901900 |
| late-40 | EASY | 1 | 20 | 0.180350 | 0.193900 | 0.213500 |
| late-40 | MEDIUM | 2 | 20 | 70.805800 | 73.052200 | 73.174900 |
| late-40 | HARD | 3 | 20 | 7.556900 | 7.957500 | 9.208600 |
| late-60 | EASY | 1 | 20 | 0.191000 | 0.246400 | 0.375300 |
| late-60 | MEDIUM | 2 | 20 | 61.724850 | 65.230200 | 74.132800 |
| late-60 | HARD | 3 | 20 | 8.395650 | 8.936800 | 9.123200 |
| late-100 | EASY | 1 | 20 | 0.210600 | 0.228500 | 0.280400 |
| late-100 | MEDIUM | 2 | 20 | 46.835150 | 47.957900 | 49.236900 |
| late-100 | HARD | 3 | 20 | 5.493400 | 5.969600 | 6.107500 |

## Pooled by level (descriptive only)

**Warning: large between-position dispersion and tactical early returns. Pooled medians MUST NOT be used to conclude speed superiority, responsiveness on arbitrary boards, or playing strength. Inspect the per-position table.**

| Level | n | median ms | p95 ms | max ms | SD ms |
|---|---:|---:|---:|---:|---:|
| EASY | 200 | 0.136200 | 0.221300 | 0.616600 | 0.072559 |
| MEDIUM | 200 | 45.001400 | 70.963500 | 74.132800 | 21.701523 |
| HARD | 200 | 24.307150 | 261.466400 | 276.901900 | 94.524888 |

Measured rows: 600; warmups: 150. Construction/ASCII in benchmark-positions.md; no positions selected based on AI output. Late 40/60/100 are synthetic density categories, not representative human endgames. Small timing sample; JIT/GC/OS scheduling remain factors.
