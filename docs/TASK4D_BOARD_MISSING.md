# Task 4d: missing board at high DPI

Measured 2026-10-30; branch task4d-board-missing from task4-start-screen 91c3019.
Other Gomoku chat was idle before switching; no other active chat used this cwd.
Production changes: only BoardPanel's minimum size; no frame/dialog/rule/AI/LAN changes.

## Reproduction before changing production code

Microsoft OpenJDK 25.0.3, Windows native Look and Feel, separate JVM for each explicit
sun.java2d.uiScale. Clean source build in an external temporary directory. Visible
MainFrame -> menu -> actual profile dialog (messi) -> actual two-player setup dialog
(ronaldo) -> Start, followed by a centre move and New Game. Also actual PvAI setup,
one human move plus completed AI response, and New Game. No forced recursive layout.

| Scale / actual transform | Native frame before AND after | Content | Board before | Board after |
|---|---|---|---|---|
| 1.0 | 778x805 | 760x758 | 596x596 | 596x596 |
| 1.25 | 774x795 | 760x758 | 596x596 | 596x596 |
| 1.5 | 772x735 | 760x704 | 10x10 | 596x570 |
| 2.0 | 768x551 | 760x528 | 10x10 | 596x482 |

All four PvP/PvAI/New Game paths had the same board bounds at each scale. Before
the fix, all eight pixel checkpoints failed at 1.5 and 2.0. At every scale the board
was in the content tree, visible, showing and valid. Instrumentation counted 18
paintComponent entries and 18 normal returns per JVM, including eight printAll
renders. No uncaught/EDT exception was observed. Painting runs but is clipped.

At 1.5 the native work area was 1280x720 logical; frame bounds were
(254,0,772,735). The centre wrapper was 760x544. Native packing/display limits
reduce the available content height below its 760x758 preferred size. This is why
the user's nominal 774x795 size is not the actual frame size at every scale here.

## Root cause and minimal fix

MainFrame.java:67 wraps the board in GridBagLayout. BoardPanel originally declared
only its preferred 596x596 size (BoardPanel.java:94); its inherited JPanel minimum
was 10x10. When the centre cannot accommodate preferred height, GridBagLayout falls
back to minimum sizing, shrinking BOTH dimensions to 10x10. The grid starts at
OFFSET=32, so all grid lines and stones lie outside the board's paint clip.

BoardPanel.java:95-96 adds a comment and sets minimum size to the same calculated
panelSize. This prevents the tiny fallback without changing preferred coordinates,
frame size policy, hit testing, dialogs or gameplay. GridBag can still clip the
fixed-coordinate board when space is insufficient; this does not solve window fit.

All 69 original bin class files matched the clean baseline classes by SHA-256.
This reproduction is a source/layout bug, not evidence of stale local bytecode.
The original bin directories were not cleaned or rebuilt during this task.

## Why the previous 267 tests missed it

Existing layout tests generally start games directly and force recursive doLayout.
F4LayoutTest.check also skips nonpositive bounds rather than requiring a usable
paint area. Task4bPresentationTest exercises a visible frame but at the existing
native 1.25 transform, calls the layout helper, and checks bounds/text rather than
grid/stone pixels. They did not launch native child JVMs at 1.5/2.0 on a reduced
logical work area. Saving an image is not an assertion that the grid was drawn.

## Validation and remaining scope

- New BoardVisibilityTest: two invocations, separate JVMs at 1.0 and 1.5. Baseline:
  1 pass / 1 fail; no agent required for the regression failure.
- After fix: full JUnit 269 found / 269 pass / 0 fail / 0 skipped / 0 aborted.
- Diagnostic matrix after fix: all four scales, 32 grid checkpoints and 16 expected
  black-stone checkpoints pass. No observed exception; entry/return counts match.
- Frame dimensions and toolbar bounds are unchanged before/after at each scale.
- Bottom grid rows and native toolbar can remain clipped on the small logical
  work area, especially at 2.0. Window sizing is explicitly outside this fix.
- Synthetic mouse events exercise product input handling; offscreen PNGs are not
  proof of physical mouse access to controls outside the native work area.
- Other monitors, L&F, OS/JDK versions and moving between mixed-DPI monitors were
  not checked. Run tools/board/run-board-probe.ps1 to reproduce on another display.
