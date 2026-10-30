# Benchmark

10 fixed positions × 3 levels × (2 warm-up + 5 measured). Raw CSV includes both phases; summaries exclude all 60 warm-ups (150 measured samples).

Each cell is median ± population standard deviation (denominator n). Wall-clock search time uses System.nanoTime; board copy and invariant checks are outside the timed region. Sequential benchmark, before concurrent arena. EASY seed uncontrolled. Nodes count recursive minimax entries only; 0 nodes does not mean no computation.

## Pooled measurements across the ten positions

These distributions mix different positions and tactical early returns; they are not a single-position confidence interval or playing-strength estimate.

| Level | n | ms median ± SD | Nodes median ± SD | Prunes median ± SD |
|---|---:|---:|---:|---:|
| EASY | 50 | 0.211300 ± 0.101670 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| MEDIUM | 50 | 72.085950 ± 35.207848 | 64.500000 ± 26.284786 | 14.000000 ± 4.346263 |
| HARD | 50 | 41.526850 ± 153.228939 | 221.500000 ± 305.016786 | 19.500000 ± 27.350320 |

## Per position and level

| Position / level | n | ms median ± SD | Nodes median ± SD | Prunes median ± SD |
|---|---:|---:|---:|---:|
| opening-1 / EASY | 5 | 0.163100 ± 0.002585 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| opening-1 / MEDIUM | 5 | 25.069000 ± 0.391954 | 62.000000 ± 0.000000 | 14.000000 ± 0.000000 |
| opening-1 / HARD | 5 | 202.852200 ± 5.614921 | 773.000000 ± 0.000000 | 62.000000 ± 0.000000 |
| opening-2 / EASY | 5 | 0.077900 ± 0.040390 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| opening-2 / MEDIUM | 5 | 14.272700 ± 0.336371 | 65.000000 ± 0.000000 | 14.000000 ± 0.000000 |
| opening-2 / HARD | 5 | 66.143700 ± 1.250884 | 443.000000 ± 0.000000 | 39.000000 ± 0.000000 |
| opening-3 / EASY | 5 | 0.243700 ± 0.010872 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| opening-3 / MEDIUM | 5 | 33.370500 ± 1.284216 | 96.000000 ± 0.000000 | 15.000000 ± 0.000000 |
| opening-3 / HARD | 5 | 143.727500 ± 3.085911 | 463.000000 ± 0.000000 | 45.000000 ± 0.000000 |
| middle-10 / EASY | 5 | 0.116900 ± 0.013888 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-10 / MEDIUM | 5 | 72.433600 ± 2.997693 | 95.000000 ± 0.000000 | 15.000000 ± 0.000000 |
| middle-10 / HARD | 5 | 382.086600 ± 10.909385 | 719.000000 ± 0.000000 | 52.000000 ± 0.000000 |
| middle-14 / EASY | 5 | 0.145300 ± 0.025524 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-14 / MEDIUM | 5 | 70.414200 ± 1.092187 | 76.000000 ± 0.000000 | 15.000000 ± 0.000000 |
| middle-14 / HARD | 5 | 7.691300 ± 0.242023 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-18 / EASY | 5 | 0.218500 ± 0.048181 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-18 / MEDIUM | 5 | 6.874800 ± 0.067926 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-18 / HARD | 5 | 7.094300 ± 0.328361 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-20 / EASY | 5 | 0.169300 ± 0.043202 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| middle-20 / MEDIUM | 5 | 77.944000 ± 2.540500 | 64.000000 ± 0.000000 | 14.000000 ± 0.000000 |
| middle-20 / HARD | 5 | 425.603200 ± 11.846724 | 488.000000 ± 0.000000 | 66.000000 ± 0.000000 |
| late-40 / EASY | 5 | 0.313000 ± 0.079532 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| late-40 / MEDIUM | 5 | 113.532300 ± 0.521591 | 41.000000 ± 0.000000 | 15.000000 ± 0.000000 |
| late-40 / HARD | 5 | 13.058600 ± 1.314534 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| late-60 / EASY | 5 | 0.339700 ± 0.010077 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| late-60 / MEDIUM | 5 | 102.093600 ± 0.820115 | 51.000000 ± 0.000000 | 14.000000 ± 0.000000 |
| late-60 / HARD | 5 | 14.360400 ± 1.199385 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| late-100 / EASY | 5 | 0.374100 ± 0.010604 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |
| late-100 / MEDIUM | 5 | 77.421000 ± 0.662665 | 69.000000 ± 0.000000 | 13.000000 ± 0.000000 |
| late-100 / HARD | 5 | 8.394800 ± 0.326976 | 0.000000 ± 0.000000 | 0.000000 ± 0.000000 |

Full ASCII boards and construction seeds are in benchmark-positions.md. Position seeds do not seed production EASY. Only five measured repetitions per cell; timing variance includes JIT/GC/OS effects.
