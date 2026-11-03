# Gomoku sound diagnostics and verification

Production changes are confined to src/game/view/SoundManager.java. Existing tests, settings persistence, UI, callers, rules, AI, networking and language files are unchanged. Baseline snapshot commit: 398656a on fix-sound.

## Manual real-device check: ten placement sounds

From the project root, use a new label (the script refuses an existing output folder):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/sound/run-probe.ps1 -Label manual-listen -IntervalMs 1000 -TenOnly
```

The probe enables sound only inside its own JVM; it does not read/write AppSettings or user Preferences. It submits ten public playPlacePieceSound requests from the EDT. A separate observer records real cached-Clip Start/Stop events, isOpen/isRunning and the immutable frame position in each LineEvent. A PASS requires ten full-frame STOP events (2646 samples each); it cannot establish audible output or acoustic latency. Device calls, waits and log I/O do not run on the EDT. The observer closes its own child-JVM Clip when done.

Omit -TenOnly to inspect supported formats and compare cold/open-new Clip playback with repeated stop/rewind/start on one Clip. The extra format/reuse controls use silent PCM. -DisableDiagnostics was used against the temporary pre-fix instrumented source; the final production source has no SOUND-DIAG logging at all. It logs actual exceptions as [SOUND] with complete stack traces on the audio worker.

## Retained evidence

- SoundManager.before.java.txt: exact pre-fix Java text (line endings normalized). baseline-src/SoundManager.java is that baseline compiled only for the deterministic cold-start regression; it is never used by the app or normal build.
- SoundManager.diagnostic.java.txt / SoundManager.diagnostic-light.java.txt: temporary diagnostics used before the functional fix. They are text evidence, not compiled product sources. The first instrumentation queried native frame state inside event callbacks and altered timing; do not use its largest delay or timeout alone as causal proof. The light version reads Clip state asynchronously and preserves immutable LineEvent positions.
- before/: first instrumented run; before-light/: nonblocking-event diagnostics + format controls; before-no-diag/: the same public-call path with diagnostics disabled, plus format controls. No native exceptions were observed in those runs.
- after-cache/: 10/10 at 200-ms request intervals, but not sufficient validation of cold startup.
- after-normal-cadence/: intermediate patch FAILED, 9/10 completed; first STOP had frame position 0. Kept without editing results.
- cold-before.log / cold-final.log: the same deterministic fake-device probe delays STOP by 1250 ms. Original code FAILS because it closes at roughly 1096 ms; final code PASSES, waiting about 1277 ms. This is a software regression probe, separate from the real-device evidence.
- after-final/: final production implementation, ten public requests at 1000-ms intervals; 10/10 full-frame stops, one Clip, no stderr exceptions.
- final-resource-tests.log / final-resource-test-xml/: five unchanged SoundResourceTest cases pass against final source.
- results.json / fix-sound.patch: result index and production-only patch.

## Full headless suite

run-headless-after-fix.ps1 reads and executes the existing test/run-headless-2026-11-03.ps1 unchanged. Only script-root context and log/build/XML destinations are substituted in memory; every test selector and test source stays unchanged. The first run wrote docs/junit-run-after-fix-sound.log and passed 158/158 before the last timeout correction. The user explicitly authorized one extra run on the final patch; -FinalReview wrote docs/junit-run-after-fix-sound-final.log and passed 158/158. Neither historical log was overwritten. Do not invoke -FinalReview again for this task.

Final full clean compilation was also done into fresh tools/sound/final-clean-classes and final-clean-test-classes. The final real-device probe and final full-suite build produced identical SoundManager bytecode.

Full Vietnamese diagnosis and report: [SOUND_FIX_2026-11-03.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/SOUND_FIX_2026-11-03.md>).
