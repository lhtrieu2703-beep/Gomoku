# Ablation environment

- Timestamp (Asia/Saigon): 2026-11-24T11:46:33.107186500+07:00[Asia/Ho_Chi_Minh]
- Source revision: 51c3cffd6453417d88fcb4805a17722de861112e (working tree changes in src/tools/ai)
- Java: 25.0.3+9-LTS Microsoft
- JVM: OpenJDK 64-Bit Server VM
- JVM arguments: [-Xms256m, -Xmx1g, -Dstdout.encoding=UTF-8]
- CPU: 12th Gen Intel(R) Core(TM) i7-12650H
- Physical cores: 10
- Logical processors: 16
- OS: Windows 11 10.0 amd64
- Mode: search-only
- Positions: [opening-1, middle-20]
- Warmups per position/level/pruning: 1; measurements: 2
- Timeout per call (ms): 30000
- One JVM; sequential direct calls; order on, off for every repetition, including warmups.
- nanoTime covers the direct search call, including candidate generation/order and result collection; board copy, configuration, watchdog setup, invariants, CSV and console output are excluded.
- A daemon watchdog only interrupts the calling thread; no parallel searches or timed Future dispatch.
- Full-mode root_value is empty when tactical/empty-board/no-candidate paths bypass root search. Move.score is never substituted.
- status is an additional CSV column (ok/timeout); timeout samples have no completed move/value and stop the run after preserving the row.
- Original corpus is reused via reflection in measurement code only. Late boards are synthetic stone-count categories.
- New run; no performance comparison with 31/10/2026.

- Measurement loop wall seconds: 7.795267
- Run status: completed
