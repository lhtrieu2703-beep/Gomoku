# Visible board/DPI probe

Run from the repository root with JDK 24+ (measured with Microsoft JDK 25.0.3):

```powershell
./tools/board/run-board-probe.ps1
```

The script creates an external temporary source/test/resource/library copy. It builds
there, then launches four separate visible JVMs with `-Dsun.java2d.uiScale` set to
1.0, 1.25, 1.5 and 2.0. Images/logs default to the temporary copy, not repository docs
or bin. `-OutputDirectory PATH` selects where to copy evidence. The original build
directories and user Preferences are never changed.

The shared `game.BoardDisplayProbe` helper uses MainFrame, actual menu buttons and
modal ProfileDialog/SetupDialog actions, names messi/ronaldo, real Swing validation,
mouse events, PvP/PvAI and each mode's New Game action. It does not call recursive
doLayout, resize the frame, replace BoardPanel, or modify game logic. All actions
run on EDT; sleeps/event barriers run off EDT. Sound is disabled via RAM Preferences.

Each run records ancestor bounds, actual/preferred/minimum board size,
visible/showing/valid flags, window/work-area bounds and actual DPI transform. It
renders the content into a device-scaled BufferedImage and checks grid-colour pixels
at an ordinary grid intersection and black-stone pixels away from the last-move dot.
There are eight checkpoints per scale: empty/one move, before/after New Game, in
both PvP and PvAI. The PNGs are offscreen content renders, not desktop screenshots;
screen clipping is reported separately using the native work area.

BoardPaintAgent uses the standard JDK Class-File API to insert diagnostic entry and
normal-return counters in BoardPanel.paintComponent in memory. On-disk production
classes are never rewritten. Counts captured before printAll include native repaint
activity; render calls are counted separately. EDT/default uncaught exceptions are
recorded and make the probe fail. This cannot prove that arbitrary unrelated code
never catches an exception internally; the board painting path has no catch block.

The normal JUnit suite adds two BoardVisibilityTest invocations (1.0/1.5). Each uses
an isolated child JVM and the same shared scenario, without the paint agent. A -1
paint count means that the optional agent is absent; pixel assertions still apply.
Run all JUnit in an external copy because legacy tests write render images.

The small production change prevents collapse to 10x10. It deliberately does not
fit the frame or entire board to a smaller work area: bottom clipping remains a
separate window-sizing issue. Review docs/TASK4D_BOARD_MISSING.md for measured data.
