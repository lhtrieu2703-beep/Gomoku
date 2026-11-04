# TASK 4b — báo cáo và bằng chứng thô (2026-10-29)
Nhánh `task4-start-screen`, gốc `f61e2d5`. A/B/C đã commit local; báo cáo này nằm trong commit D. Không push, merge, tag hoặc đổi tên nhánh. Output Git sau commit D được dán trong phản hồi bàn giao.
## 1. Các giai đoạn và phạm vi

| Giai đoạn | Commit local | Nội dung |
|---|---|---|
| A | `ea7c8b7` | Theme, nút, nền/avatar dự phòng, hồ sơ, copy resource, Preferences RAM |
| B | `f9c3c10` | Tiêu đề/menu/thiết lập/hồ sơ/cài đặt/luật |
| C | `8340acc` | Thẻ người chơi, căn giữa, quy tắc tỉ số |
| D | Commit chứa báo cáo này | Hoàn tất migration, kiểm cắt chữ, ảnh/đo kích thước, bằng chứng |

Đã có: [src/game/view/MainFrame.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:91>) (ba card 760×758); [src/game/view/MainFrame.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:111>) (hủy phiên/worker/dialog, giữ lựa chọn và điểm); [src/game/view/AppSettings.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/AppSettings.java:64>) (đọc hồ sơ); [src/game/model/ScoreTracker.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/model/ScoreTracker.java:72>) (chỉ xóa số, giữ danh tính ván để Undo không ghi trùng).

QA ảnh phát hiện và đã sửa: nhãn độ khó enum vốn là tiếng Việt; selector Cài đặt bị cắt Português; hai nút sách/menu ở game lệch 4 px so với menu. Các ca hồi quy tương ứng hiện PASS. Tên người chơi 2 chỉ ở RAM của phiên; hồ sơ người chơi 1 mới được lưu.
## 2. Số ca và toàn bộ output JUnit A–D

Đếm các lá của cây JUnit, không đếm container/phương thức parameterized thêm lần nữa. [docs/task4b-results/test-counts.csv](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-results/test-counts.csv>).

| Lớp | A | B | C | D |
|---|---:|---:|---:|---:|
| BoardWinTest | 55 | 55 | 55 | 55 |
| F1ProtocolTest | 13 | 13 | 13 | 13 |
| F2MoveOrderTest | 2 | 2 | 2 | 2 |
| F3HeuristicTest | 28 | 28 | 28 | 28 |
| F4LayoutTest | 6 | 6 | 6 | 6 |
| I18nSourceScanTest | 2 | 2 | 2 | 2 |
| LangPurityTest | 3 | 3 | 3 | 3 |
| NetworkStabilityTest | 7 | 7 | 7 | 7 |
| P1RegressionTest | 16 | 16 | 16 | 16 |
| P2LifecycleTest | 13 | 13 | 13 | 13 |
| SoundResourceTest | 5 | 5 | 5 | 5 |
| Task4LayoutTest | 6 | 6 | 6 | 6 |
| Task4LifecycleTest | 9 | 9 | 9 | 9 |
| Task4PreferencesTest | 3 | 3 | 3 | 3 |
| Task4StartScreenTest | 17 | 17 | 17 | 17 |
| Task4bFlowTest | 0 | 12 | 12 | 13 |
| Task4bFoundationTest | 15 | 15 | 15 | 15 |
| Task4bGameplayTest | 0 | 0 | 12 | 12 |
| Task4bPresentationTest | 0 | 0 | 0 | 16 |
| UILangSoundTest | 9 | 9 | 9 | 9 |
| UndoParityTest | 6 | 6 | 6 | 6 |
| UndoScoreTest | 11 | 11 | 11 | 11 |
| **Tổng** | **226** | **238** | **250** | **267** |

Cả bốn lần: 0 skipped, 0 aborted, 0 failed. Tổng lượt thực thi bốn lần là 981, không phải 981 test khác nhau; lần D có 267 ca. Nền 211 + 56 ca mới = 267.

### A — output thô

Lệnh: `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin" --scan-class-path --details tree --disable-banner --disable-ansi-colors`

[docs/task4b-results/A-junit.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-results/A-junit.txt>)

```text
[Preferences] /test/task4/name-b45f77c4-4351-49aa-b030-85bd1b11b25f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-9db8b2a0-5189-4721-a7d5-c96f8df2abcb RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-77ae4a97-04f0-44ed-9359-27fa0fff3d81 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1] Tổng hàm String không tham số: 60
[C1] Allowlist methods (PT có thể == EN): [online]
[C1] PASS — 60 methods × 3 languages
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1b] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C2] modeCombo renderer class: game.view.MainFrame$4
[C2] modeCombo painted field avg pixel: RGB(107,89,77)
[C2] modeCombo selected: bg=RGB(217,164,65) fg=RGB(59,42,30)
[C2] modeCombo contrast ratio selected: 6.08
[C2] diffCombo renderer class: game.view.MainFrame$5
[C2] diffCombo painted field avg pixel: RGB(126,118,112)
[C2] diffCombo selected: bg=RGB(217,164,65) fg=RGB(59,42,30)
[C2] diffCombo contrast ratio selected: 6.08
[C2] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B2] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B3] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C3] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D1] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D2] prod path: /game/view
[D2] test path: /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511
[D2] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I2] PASS
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/uiLangSound-fff8d96d-cd46-4a96-90e3-f2dfc78b8511 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[A] Kiểm 60 methods với bản PT
[A] PASS – không có ký tự VI trong bản PT
[Preferences] /test/task3/langPurity-23ca65cd-d8ff-48c4-af75-e284644650d8 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B] Kiểm 60 methods với bản EN
[B] PASS – toàn bộ EN là ASCII thuần
[Preferences] /test/task3/langPurity-23ca65cd-d8ff-48c4-af75-e284644650d8 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C] bg=RGB(59,42,30) fg=WHITE → contrast ratio = 11.94
[C] selBg=RGB(217,164,65) fg=WHITE → contrast ratio selected = 6.08
[C] PASS
[Preferences] /test/task3/langPurity-23ca65cd-d8ff-48c4-af75-e284644650d8 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/langPurity-23ca65cd-d8ff-48c4-af75-e284644650d8 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I1a] PASS – Không còn Lang.isVI() trong view/*.java (trừ Lang.java)
[I1b] PASS – Không còn string literal tiếng Việt có dấu trong view/*.java
╷
├─ JUnit Jupiter ✔
│  ├─ Task4LifecycleTest ✔
│  │  ├─ menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult() ✔
│  │  ├─ queuedOpponentMoveCannotChangeBoardAfterMenu() ✔
│  │  ├─ undoRemainsAvailableAfterWin() ✔
│  │  ├─ menuClosesRealLanSocketAndAllConnectionDialogs() ✔
│  │  ├─ menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning() ✔
│  │  ├─ queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart() ✔
│  │  ├─ menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged() ✔
│  │  ├─ menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame() ✔
│  │  └─ menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart() ✔
│  ├─ Task4StartScreenTest ✔
│  │  ├─ normalizeUnicodeWhitespaceAndLongNames(String, String) ✔
│  │  │  ├─ [1] null,  ✔
│  │  │  ├─ [2]   ,  ✔
│  │  │  ├─ [3]   Nguyễn Ánh  , Nguyễn Ánh ✔
│  │  │  ├─ [4] João Gonçalves, João Gonçalves ✔
│  │  │  ├─ [5] Conceição, Conceição ✔
│  │  │  ├─ [6] Ánh, Ánh ✔
│  │  │  ├─ [7] abcdefghijklmnopXYZ, abcdefghijklmnop ✔
│  │  │  ├─ [8] 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯, 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯 ✔
│  │  │  ├─ [9] A B, A B ✔
│  │  │  └─ [10] <html>João, <html>João ✔
│  │  ├─ emptyNameUsesTranslatedDefaultAndRemainsImplicit(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ achievementIconShowsExistingSessionTotalsAndMenuKeepsThem() ✔
│  │  ├─ languageChoiceOnMenuPreservesTypedNameAndSynchronizesBothSelectors() ✔
│  │  ├─ fieldLimitsUnicodeWithoutSplittingEmojiAndDisplaysNamesAsPlainText() ✔
│  │  └─ startAndMenuSwitchCardsInTheSameFrame() ✔
│  ├─ F2MoveOrderTest ✔
│  │  └─ finalMoveIsSentBeforeResultAndResultIsProcessedOnce(boolean) ✔
│  │     ├─ [1] false ✔
│  │     └─ [2] true ✔
│  ├─ Task4LayoutTest ✔
│  │  └─ bothCardsFitAllLocalesOnStartupAndAfterTransitions(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ Task4PreferencesTest ✔
│  │  └─ nameAndLanguageRoundTripThroughInjectedNode(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ Task4bFoundationTest ✔
│  │  ├─ buttonsPaintFourDistinctStatesUnderBothLooks(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ missingImagesRenderAndCoverCacheTracksDevicePixels() ✔
│  │  ├─ invalidAvatarsDefaultToZero(int) ✔
│  │  │  ├─ [1] -1 ✔
│  │  │  ├─ [2] 8 ✔
│  │  │  ├─ [3] 99 ✔
│  │  │  └─ [4] 2147483647 ✔
│  │  ├─ profileNames(String, String) ✔
│  │  │  ├─ [1] ,  ✔
│  │  │  ├─ [2] A, A ✔
│  │  │  ├─ [3] abcdefghijklmnop, abcdefghijklmnop ✔
│  │  │  ├─ [4] abcdefghijklmnopq, abcdefghijklmnop ✔
│  │  │  ├─ [5]    ,  ✔
│  │  │  ├─ [6]  João , João ✔
│  │  │  └─ [7]  Đặng , Đặng ✔
│  │  └─ profileRoundTripKeepsLegacyKeyAndImplicitDefault() ✔
│  ├─ UndoParityTest ✔
│  │  ├─ P2: X(7,7), undoMove(): size 0, lượt X ✔
│  │  ├─ P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1 ✔
│  │  ├─ P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X ✔
│  │  ├─ P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X ✔
│  │  ├─ P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi ✔
│  │  └─ P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X ✔
│  ├─ BoardWinTest ✔
│  │  ├─ Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O ✔
│  │  ├─ Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X ✔
│  │  ├─ T01: . X X X X X* . -> THẮNG ✔
│  │  ├─ T02: O X X X X* X . -> THẮNG ✔
│  │  ├─ T03: . X X* X X X O -> THẮNG ✔
│  │  ├─ T04: O X* X X X X O -> KHÔNG THẮNG ✔
│  │  ├─ T05: # X X X X X* . -> THẮNG ✔
│  │  ├─ T06: # X X X X X* O -> THẮNG ✔
│  │  ├─ T07: O X X X X X* # -> THẮNG ✔
│  │  ├─ T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG ✔
│  │  ├─ T09: O . X X X X X* . O (O không liền kề) -> THẮNG ✔
│  │  ├─ T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG ✔
│  │  ├─ T11: . X X X X X X* . -> THẮNG ✔
│  │  ├─ T12: O X X X X X X* . -> THẮNG ✔
│  │  ├─ T13: O X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T14: # X X X X X X* . -> THẮNG ✔
│  │  ├─ T15: # X X X X X X* O -> THẮNG ✔
│  │  ├─ T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG ✔
│  │  ├─ T17: . X X X X X X X* . -> THẮNG ✔
│  │  ├─ T18: O X X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T19: Hàng 9 quân mở 2 đầu -> THẮNG ✔
│  │  ├─ T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG ✔
│  │  ├─ T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG ✔
│  │  ├─ T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG ✔
│  │  ├─ T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG ✔
│  │  ├─ T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG ✔
│  │  ├─ T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG ✔
│  │  ├─ T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG ✔
│  │  ├─ T29: . X X X O X* X . -> KHÔNG THẮNG ✔
│  │  ├─ T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG ✔
│  │  ├─ T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG ✔
│  │  ├─ T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG ✔
│  │  ├─ T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) ✔
│  │  ├─ T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG ✔
│  │  ├─ T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG ✔
│  │  ├─ T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG ✔
│  │  ├─ T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG ✔
│  │  ├─ T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG ✔
│  │  ├─ T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG ✔
│  │  ├─ T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG ✔
│  │  ├─ T47: . O O O O O* . -> O THẮNG ✔
│  │  ├─ T48: X O O O O O* X -> KHÔNG THẮNG ✔
│  │  ├─ T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA ✔
│  │  ├─ T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) ✔
│  │  ├─ T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên ✔
│  │  ├─ T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ ✔
│  │  └─ Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô ✔
│  ├─ SoundResourceTest ✔
│  │  ├─ staleResultClosesClipWithoutStarting() ✔
│  │  ├─ closesClipWhenStartFails() ✔
│  │  ├─ closesClipAfterPlaybackStops() ✔
│  │  ├─ closesClipWhenOpenFails() ✔
│  │  └─ queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice() ✔
│  ├─ P2LifecycleTest ✔
│  │  ├─ disposingFrameCancelsItsWorkerAndPreservesBoard() ✔
│  │  ├─ cancellationInsideRecursiveSearchUnwindsAllMoves() ✔
│  │  ├─ closingDialogCancelsPendingHostAndIgnoresLateResults() ✔
│  │  ├─ disposedFrameDoesNotReceiveLanguageEvents() ✔
│  │  ├─ successfulDialogDisposalKeepsConnection() ✔
│  │  ├─ receivedMoveIsAppliedBeforeNextEdtNetworkCallback() ✔
│  │  ├─ cancellingOnlineSelectionRestoresModeWithoutResettingGame() ✔
│  │  ├─ interruptedAiAbortsWithoutMutatingBoard() ✔
│  │  └─ networkErrorsAreTranslated(ErrorType) ✔
│  │     ├─ [1] HOST ✔
│  │     ├─ [2] JOIN ✔
│  │     ├─ [3] CHANNEL ✔
│  │     ├─ [4] START_TIMEOUT ✔
│  │     └─ [5] VERSION ✔
│  ├─ F4LayoutTest ✔
│  │  ├─ allTransitionsKeepControlsInsideParents(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  └─ directStartupBoundsAndRender(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ NetworkStabilityTest ✔
│  │  ├─ reconnectCannotBeClosedOrNotifiedByPreviousSession() ✔
│  │  ├─ staleErrorIsDiscardedWhenNewSessionStarts() ✔
│  │  ├─ callbacksAlreadyQueuedAreDiscardedAfterCancel() ✔
│  │  ├─ cancelPendingHostReleasesPortWithoutError() ✔
│  │  ├─ occupiedPortReportsTypedHostError() ✔
│  │  ├─ hostAndClientExchangeMovesRestartAndDisconnect() ✔
│  │  └─ malformedMoveDoesNotKillConnectionAndOrderIsPreserved() ✔
│  ├─ F1ProtocolTest ✔
│  │  ├─ wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored() ✔
│  │  ├─ queuedWireMoveKeepsOldRoundAfterReset() ✔
│  │  ├─ waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult() ✔
│  │  ├─ garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect() ✔
│  │  ├─ invalidPlyOrColorDoesNotChangeBoard(int) ✔
│  │  │  ├─ [1] 0 ✔
│  │  │  ├─ [2] 2 ✔
│  │  │  ├─ [3] 3 ✔
│  │  │  └─ [4] 225 ✔
│  │  ├─ simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ mismatchedVersionClosesSession() ✔
│  │  ├─ startTimeoutClosesSession() ✔
│  │  └─ clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof() ✔
│  ├─ UILangSoundTest ✔
│  │  ├─ C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist) ✔
│  │  ├─ C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ ✔
│  │  ├─ C2 – modeCombo/diffCombo renderer: bg tối, fg trắng khi không selected; contrast... ✔
│  │  ├─ B2 – Click btnSound toggle SoundManager.isSoundEnabled() ✔
│  │  ├─ B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off ✔
│  │  ├─ C3 – MainFrame labels cập nhật đúng sau đổi ngôn ngữ VI→EN→PT ✔
│  │  ├─ D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → ... ✔
│  │  ├─ D2 – Preferences test node khác node production ✔
│  │  └─ I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager ✔
│  ├─ LangPurityTest ✔
│  │  ├─ A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...) ✔
│  │  ├─ B – EN chỉ chứa ASCII (0x00-0x7F) ✔
│  │  └─ C – Combo display field: contrast ratio nền/chữ >= 4.5 (WCAG AA) ✔
│  ├─ UndoScoreTest ✔
│  │  ├─ U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0) ✔
│  │  ├─ U02: Undo xong, thành tích vẫn 0-1, isGameOver = false ✔
│  │  ├─ 6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false) ✔
│  │  ├─ U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false ✔
│  │  ├─ U08: Race condition AI - token cũ bị vô hiệu hóa khi resetGame hoặc undoMove ✔
│  │  ├─ 8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm ... ✔
│  │  ├─ U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ... ✔
│  │  ├─ U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đún... ✔
│  │  ├─ 7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua ✔
│  │  ├─ U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng ✔
│  │  └─ U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm ✔
│  ├─ I18nSourceScanTest ✔
│  │  ├─ I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java) ✔
│  │  └─ I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.jav... ✔
│  ├─ P1RegressionTest ✔
│  │  ├─ languageChangePreservesFinishedGameAndTranslatesResult(Piece) ✔
│  │  │  ├─ [1] - ✔
│  │  │  ├─ [2] X ✔
│  │  │  └─ [3] O ✔
│  │  ├─ pvpUndoAfterWinRestoresWinningPlayerTurn(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsQueuedMoveAfterNewGame() ✔
│  │  ├─ onlineAcceptsOnlyOneMoveFromCorrectOpponent(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsOpponentMoveOnLocalTurn() ✔
│  │  ├─ languageChangePreservesActiveGame(GameMode) ✔
│  │  │  ├─ [1] Người vs Người ✔
│  │  │  ├─ [2] Người vs Máy ✔
│  │  │  └─ [3] Chơi Online (LAN) ✔
│  │  ├─ onlineRejectsMoveAfterSwitchingMode(GameMode) ✔
│  │  │  ├─ [1] Người vs Người ✔
│  │  │  └─ [2] Người vs Máy ✔
│  │  ├─ onlineRejectsInvalidSquaresAndMovesAfterGameOver() ✔
│  │  └─ pvpUndoAfterDrawRestoresLastPlayerTurn() ✔
│  └─ F3HeuristicTest ✔
│     ├─ blockedSixIsNotFive(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ singleThreeIsNotDoubleThree(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ nonTerminalThreatsNeverOutrankWin(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openSixMatchesSelectedRule(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openFourIsThreatButNotTerminal(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     └─ actualCrossDoubleThreeIsRecognized(WinRule, Piece) ✔
│        ├─ [1] RULE_A, X ✔
│        ├─ [2] RULE_A, O ✔
│        ├─ [3] RULE_B, X ✔
│        └─ [4] RULE_B, O ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 8180 ms
[        47 containers found      ]
[         0 containers skipped    ]
[        47 containers started    ]
[         0 containers aborted    ]
[        47 containers successful ]
[         0 containers failed     ]
[       226 tests found           ]
[         0 tests skipped         ]
[       226 tests started         ]
[         0 tests aborted         ]
[       226 tests successful      ]
[         0 tests failed          ]

```

### B — output thô

Lệnh: `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin" --scan-class-path --details tree --disable-banner --disable-ansi-colors`

[docs/task4b-results/B-junit.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-results/B-junit.txt>)

```text
[Preferences] /test/task4/name-07d4adef-932a-417f-9408-a41544d1f4a9 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-7145de21-c527-48af-b964-80025ef4263a RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-0b223dc2-520f-4398-996f-18963cf34673 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1] Tổng hàm String không tham số: 72
[C1] Allowlist methods (PT có thể == EN): [online]
[C1] PASS — 72 methods × 3 languages
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1b] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C2] Shared buttons: actual painted fields and text contrast PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B2] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B3] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C3] Menu and open settings update immediately PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D1] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D2] prod path: /game/view
[D2] test path: /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe
[D2] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I2] PASS
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/uiLangSound-af7d4221-98eb-46df-88ab-52e168e649fe RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[A] Kiểm 72 methods với bản PT
[A] PASS – không có ký tự VI trong bản PT
[Preferences] /test/task3/langPurity-d9dac66b-baa0-461c-88cd-de146225eff0 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B] Kiểm 72 methods với bản EN
[B] PASS – toàn bộ EN là ASCII thuần
[Preferences] /test/task3/langPurity-d9dac66b-baa0-461c-88cd-de146225eff0 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C] Theme normal=11.939820994161197 primary=6.07910030277538 PASS
[Preferences] /test/task3/langPurity-d9dac66b-baa0-461c-88cd-de146225eff0 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/langPurity-d9dac66b-baa0-461c-88cd-de146225eff0 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I1a] PASS – Không còn Lang.isVI() trong view/*.java (trừ Lang.java)
[I1b] PASS – Không còn string literal tiếng Việt có dấu trong view/*.java
╷
├─ JUnit Jupiter ✔
│  ├─ Task4LifecycleTest ✔
│  │  ├─ menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult() ✔
│  │  ├─ queuedOpponentMoveCannotChangeBoardAfterMenu() ✔
│  │  ├─ undoRemainsAvailableAfterWin() ✔
│  │  ├─ menuClosesRealLanSocketAndAllConnectionDialogs() ✔
│  │  ├─ menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning() ✔
│  │  ├─ queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart() ✔
│  │  ├─ menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged() ✔
│  │  ├─ menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame() ✔
│  │  └─ menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart() ✔
│  ├─ Task4StartScreenTest ✔
│  │  ├─ profileChangesAndMenuKeepExistingSessionTotals() ✔
│  │  ├─ normalizeUnicodeWhitespaceAndLongNames(String, String) ✔
│  │  │  ├─ [1] null,  ✔
│  │  │  ├─ [2]   ,  ✔
│  │  │  ├─ [3]   Nguyễn Ánh  , Nguyễn Ánh ✔
│  │  │  ├─ [4] João Gonçalves, João Gonçalves ✔
│  │  │  ├─ [5] Conceição, Conceição ✔
│  │  │  ├─ [6] Ánh, Ánh ✔
│  │  │  ├─ [7] abcdefghijklmnopXYZ, abcdefghijklmnop ✔
│  │  │  ├─ [8] 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯, 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯 ✔
│  │  │  ├─ [9] A B, A B ✔
│  │  │  └─ [10] <html>João, <html>João ✔
│  │  ├─ profileFieldLimitsUnicodeAndNamesRemainPlainText() ✔
│  │  ├─ emptyProfileUsesTranslatedDefaultWithoutPersistingIt(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ settingsApplyLanguageImmediatelyWithoutChangingProfileDraft() ✔
│  │  └─ titleMenuSetupAndGameUseTheSameFrame() ✔
│  ├─ F2MoveOrderTest ✔
│  │  └─ finalMoveIsSentBeforeResultAndResultIsProcessedOnce(boolean) ✔
│  │     ├─ [1] false ✔
│  │     └─ [2] true ✔
│  ├─ Task4LayoutTest ✔
│  │  └─ bothCardsFitAllLocalesOnStartupAndAfterTransitions(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ Task4PreferencesTest ✔
│  │  └─ nameAndLanguageRoundTripThroughInjectedNode(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ Task4bFoundationTest ✔
│  │  ├─ buttonsPaintFourDistinctStatesUnderBothLooks(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ missingImagesRenderAndCoverCacheTracksDevicePixels() ✔
│  │  ├─ invalidAvatarsDefaultToZero(int) ✔
│  │  │  ├─ [1] -1 ✔
│  │  │  ├─ [2] 8 ✔
│  │  │  ├─ [3] 99 ✔
│  │  │  └─ [4] 2147483647 ✔
│  │  ├─ profileNames(String, String) ✔
│  │  │  ├─ [1] ,  ✔
│  │  │  ├─ [2] A, A ✔
│  │  │  ├─ [3] abcdefghijklmnop, abcdefghijklmnop ✔
│  │  │  ├─ [4] abcdefghijklmnopq, abcdefghijklmnop ✔
│  │  │  ├─ [5]    ,  ✔
│  │  │  ├─ [6]  João , João ✔
│  │  │  └─ [7]  Đặng , Đặng ✔
│  │  └─ profileRoundTripKeepsLegacyKeyAndImplicitDefault() ✔
│  ├─ UndoParityTest ✔
│  │  ├─ P2: X(7,7), undoMove(): size 0, lượt X ✔
│  │  ├─ P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1 ✔
│  │  ├─ P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X ✔
│  │  ├─ P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X ✔
│  │  ├─ P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi ✔
│  │  └─ P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X ✔
│  ├─ BoardWinTest ✔
│  │  ├─ Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O ✔
│  │  ├─ Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X ✔
│  │  ├─ T01: . X X X X X* . -> THẮNG ✔
│  │  ├─ T02: O X X X X* X . -> THẮNG ✔
│  │  ├─ T03: . X X* X X X O -> THẮNG ✔
│  │  ├─ T04: O X* X X X X O -> KHÔNG THẮNG ✔
│  │  ├─ T05: # X X X X X* . -> THẮNG ✔
│  │  ├─ T06: # X X X X X* O -> THẮNG ✔
│  │  ├─ T07: O X X X X X* # -> THẮNG ✔
│  │  ├─ T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG ✔
│  │  ├─ T09: O . X X X X X* . O (O không liền kề) -> THẮNG ✔
│  │  ├─ T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG ✔
│  │  ├─ T11: . X X X X X X* . -> THẮNG ✔
│  │  ├─ T12: O X X X X X X* . -> THẮNG ✔
│  │  ├─ T13: O X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T14: # X X X X X X* . -> THẮNG ✔
│  │  ├─ T15: # X X X X X X* O -> THẮNG ✔
│  │  ├─ T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG ✔
│  │  ├─ T17: . X X X X X X X* . -> THẮNG ✔
│  │  ├─ T18: O X X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T19: Hàng 9 quân mở 2 đầu -> THẮNG ✔
│  │  ├─ T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG ✔
│  │  ├─ T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG ✔
│  │  ├─ T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG ✔
│  │  ├─ T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG ✔
│  │  ├─ T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG ✔
│  │  ├─ T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG ✔
│  │  ├─ T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG ✔
│  │  ├─ T29: . X X X O X* X . -> KHÔNG THẮNG ✔
│  │  ├─ T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG ✔
│  │  ├─ T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG ✔
│  │  ├─ T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG ✔
│  │  ├─ T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) ✔
│  │  ├─ T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG ✔
│  │  ├─ T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG ✔
│  │  ├─ T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG ✔
│  │  ├─ T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG ✔
│  │  ├─ T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG ✔
│  │  ├─ T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG ✔
│  │  ├─ T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG ✔
│  │  ├─ T47: . O O O O O* . -> O THẮNG ✔
│  │  ├─ T48: X O O O O O* X -> KHÔNG THẮNG ✔
│  │  ├─ T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA ✔
│  │  ├─ T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) ✔
│  │  ├─ T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên ✔
│  │  ├─ T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ ✔
│  │  └─ Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô ✔
│  ├─ SoundResourceTest ✔
│  │  ├─ staleResultClosesClipWithoutStarting() ✔
│  │  ├─ closesClipWhenStartFails() ✔
│  │  ├─ closesClipAfterPlaybackStops() ✔
│  │  ├─ closesClipWhenOpenFails() ✔
│  │  └─ queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice() ✔
│  ├─ P2LifecycleTest ✔
│  │  ├─ disposingFrameCancelsItsWorkerAndPreservesBoard() ✔
│  │  ├─ cancellationInsideRecursiveSearchUnwindsAllMoves() ✔
│  │  ├─ closingDialogCancelsPendingHostAndIgnoresLateResults() ✔
│  │  ├─ disposedFrameDoesNotReceiveLanguageEvents() ✔
│  │  ├─ successfulDialogDisposalKeepsConnection() ✔
│  │  ├─ receivedMoveIsAppliedBeforeNextEdtNetworkCallback() ✔
│  │  ├─ cancellingOnlineSelectionRestoresModeWithoutResettingGame() ✔
│  │  ├─ interruptedAiAbortsWithoutMutatingBoard() ✔
│  │  └─ networkErrorsAreTranslated(ErrorType) ✔
│  │     ├─ [1] HOST ✔
│  │     ├─ [2] JOIN ✔
│  │     ├─ [3] CHANNEL ✔
│  │     ├─ [4] START_TIMEOUT ✔
│  │     └─ [5] VERSION ✔
│  ├─ F4LayoutTest ✔
│  │  ├─ allTransitionsKeepControlsInsideParents(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  └─ directStartupBoundsAndRender(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ NetworkStabilityTest ✔
│  │  ├─ reconnectCannotBeClosedOrNotifiedByPreviousSession() ✔
│  │  ├─ staleErrorIsDiscardedWhenNewSessionStarts() ✔
│  │  ├─ callbacksAlreadyQueuedAreDiscardedAfterCancel() ✔
│  │  ├─ cancelPendingHostReleasesPortWithoutError() ✔
│  │  ├─ occupiedPortReportsTypedHostError() ✔
│  │  ├─ hostAndClientExchangeMovesRestartAndDisconnect() ✔
│  │  └─ malformedMoveDoesNotKillConnectionAndOrderIsPreserved() ✔
│  ├─ F1ProtocolTest ✔
│  │  ├─ wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored() ✔
│  │  ├─ queuedWireMoveKeepsOldRoundAfterReset() ✔
│  │  ├─ waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult() ✔
│  │  ├─ garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect() ✔
│  │  ├─ invalidPlyOrColorDoesNotChangeBoard(int) ✔
│  │  │  ├─ [1] 0 ✔
│  │  │  ├─ [2] 2 ✔
│  │  │  ├─ [3] 3 ✔
│  │  │  └─ [4] 225 ✔
│  │  ├─ simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ mismatchedVersionClosesSession() ✔
│  │  ├─ startTimeoutClosesSession() ✔
│  │  └─ clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof() ✔
│  ├─ UILangSoundTest ✔
│  │  ├─ C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist) ✔
│  │  ├─ C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ ✔
│  │  ├─ C2 – Shared custom button painting and contrast >= 4.5 ✔
│  │  ├─ B2 – Click btnSound toggle SoundManager.isSoundEnabled() ✔
│  │  ├─ B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off ✔
│  │  ├─ C3 – Menu and settings update VI/EN/PT immediately ✔
│  │  ├─ D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → ... ✔
│  │  ├─ D2 – Preferences test node khác node production ✔
│  │  └─ I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager ✔
│  ├─ Task4bFlowTest ✔
│  │  ├─ titleAcceptsMouseAndAnyKeyAndStopsItsTimer() ✔
│  │  ├─ enterCommitsProfileAndEscapeCancelsEveryDialogDraft() ✔
│  │  ├─ rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ setupRemembersDifficultyAndPlayerTwoOnlyAfterStart() ✔
│  │  └─ allCardsAndDialogsFitBothLooksWithStableFrameSize(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ LangPurityTest ✔
│  │  ├─ A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...) ✔
│  │  ├─ B – EN chỉ chứa ASCII (0x00-0x7F) ✔
│  │  └─ C – Shared theme/button contrast >= 4.5 (WCAG AA) ✔
│  ├─ UndoScoreTest ✔
│  │  ├─ U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0) ✔
│  │  ├─ U02: Undo xong, thành tích vẫn 0-1, isGameOver = false ✔
│  │  ├─ 6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false) ✔
│  │  ├─ U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false ✔
│  │  ├─ U08: Race condition AI - token cũ bị vô hiệu hóa khi resetGame hoặc undoMove ✔
│  │  ├─ 8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm ... ✔
│  │  ├─ U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ... ✔
│  │  ├─ U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đún... ✔
│  │  ├─ 7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua ✔
│  │  ├─ U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng ✔
│  │  └─ U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm ✔
│  ├─ I18nSourceScanTest ✔
│  │  ├─ I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java) ✔
│  │  └─ I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.jav... ✔
│  ├─ P1RegressionTest ✔
│  │  ├─ languageChangePreservesFinishedGameAndTranslatesResult(Piece) ✔
│  │  │  ├─ [1] - ✔
│  │  │  ├─ [2] X ✔
│  │  │  └─ [3] O ✔
│  │  ├─ pvpUndoAfterWinRestoresWinningPlayerTurn(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsQueuedMoveAfterNewGame() ✔
│  │  ├─ onlineAcceptsOnlyOneMoveFromCorrectOpponent(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsOpponentMoveOnLocalTurn() ✔
│  │  ├─ languageChangePreservesActiveGame(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  ├─ [2] Jogar contra o computador ✔
│  │  │  └─ [3] Jogar em rede (LAN) ✔
│  │  ├─ onlineRejectsMoveAfterSwitchingMode(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  └─ [2] Jogar contra o computador ✔
│  │  ├─ onlineRejectsInvalidSquaresAndMovesAfterGameOver() ✔
│  │  └─ pvpUndoAfterDrawRestoresLastPlayerTurn() ✔
│  └─ F3HeuristicTest ✔
│     ├─ blockedSixIsNotFive(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ singleThreeIsNotDoubleThree(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ nonTerminalThreatsNeverOutrankWin(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openSixMatchesSelectedRule(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openFourIsThreatButNotTerminal(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     └─ actualCrossDoubleThreeIsRecognized(WinRule, Piece) ✔
│        ├─ [1] RULE_A, X ✔
│        ├─ [2] RULE_A, O ✔
│        ├─ [3] RULE_B, X ✔
│        └─ [4] RULE_B, O ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 9632 ms
[        50 containers found      ]
[         0 containers skipped    ]
[        50 containers started    ]
[         0 containers aborted    ]
[        50 containers successful ]
[         0 containers failed     ]
[       238 tests found           ]
[         0 tests skipped         ]
[       238 tests started         ]
[         0 tests aborted         ]
[       238 tests successful      ]
[         0 tests failed          ]

```

### C — output thô

Lệnh: `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin" --scan-class-path --details tree --disable-banner --disable-ansi-colors`

[docs/task4b-results/C-junit.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-results/C-junit.txt>)

```text
[Preferences] /test/task4/name-05101877-2eb9-42e0-a9de-9ff7411f14db RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-21ff0af7-5123-4057-a2ef-233e6fc055f4 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-d670b4e7-f44b-4a1e-9445-580024e884c2 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1] Tổng hàm String không tham số: 72
[C1] Allowlist methods (PT có thể == EN): [online]
[C1] PASS — 72 methods × 3 languages
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1b] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C2] Shared buttons: actual painted fields and text contrast PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B2] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B3] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C3] Menu and open settings update immediately PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D1] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D2] prod path: /game/view
[D2] test path: /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f
[D2] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I2] PASS
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/uiLangSound-b5fc6e8e-45fe-4152-bc39-0de51ab4f12f RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[A] Kiểm 72 methods với bản PT
[A] PASS – không có ký tự VI trong bản PT
[Preferences] /test/task3/langPurity-0aff0c59-b9cf-429c-be37-8a22743f248c RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B] Kiểm 72 methods với bản EN
[B] PASS – toàn bộ EN là ASCII thuần
[Preferences] /test/task3/langPurity-0aff0c59-b9cf-429c-be37-8a22743f248c RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C] Theme normal=11.939820994161197 primary=6.07910030277538 PASS
[Preferences] /test/task3/langPurity-0aff0c59-b9cf-429c-be37-8a22743f248c RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/langPurity-0aff0c59-b9cf-429c-be37-8a22743f248c RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I1a] PASS – Không còn Lang.isVI() trong view/*.java (trừ Lang.java)
[I1b] PASS – Không còn string literal tiếng Việt có dấu trong view/*.java
╷
├─ JUnit Jupiter ✔
│  ├─ Task4LifecycleTest ✔
│  │  ├─ menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult() ✔
│  │  ├─ queuedOpponentMoveCannotChangeBoardAfterMenu() ✔
│  │  ├─ undoRemainsAvailableAfterWin() ✔
│  │  ├─ menuClosesRealLanSocketAndAllConnectionDialogs() ✔
│  │  ├─ menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning() ✔
│  │  ├─ queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart() ✔
│  │  ├─ menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged() ✔
│  │  ├─ menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame() ✔
│  │  └─ menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart() ✔
│  ├─ Task4StartScreenTest ✔
│  │  ├─ profileChangesAndMenuKeepExistingSessionTotals() ✔
│  │  ├─ normalizeUnicodeWhitespaceAndLongNames(String, String) ✔
│  │  │  ├─ [1] null,  ✔
│  │  │  ├─ [2]   ,  ✔
│  │  │  ├─ [3]   Nguyễn Ánh  , Nguyễn Ánh ✔
│  │  │  ├─ [4] João Gonçalves, João Gonçalves ✔
│  │  │  ├─ [5] Conceição, Conceição ✔
│  │  │  ├─ [6] Ánh, Ánh ✔
│  │  │  ├─ [7] abcdefghijklmnopXYZ, abcdefghijklmnop ✔
│  │  │  ├─ [8] 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯, 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯 ✔
│  │  │  ├─ [9] A B, A B ✔
│  │  │  └─ [10] <html>João, <html>João ✔
│  │  ├─ profileFieldLimitsUnicodeAndNamesRemainPlainText() ✔
│  │  ├─ emptyProfileUsesTranslatedDefaultWithoutPersistingIt(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ settingsApplyLanguageImmediatelyWithoutChangingProfileDraft() ✔
│  │  └─ titleMenuSetupAndGameUseTheSameFrame() ✔
│  ├─ F2MoveOrderTest ✔
│  │  └─ finalMoveIsSentBeforeResultAndResultIsProcessedOnce(boolean) ✔
│  │     ├─ [1] false ✔
│  │     └─ [2] true ✔
│  ├─ Task4LayoutTest ✔
│  │  └─ bothCardsFitAllLocalesOnStartupAndAfterTransitions(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ Task4PreferencesTest ✔
│  │  └─ nameAndLanguageRoundTripThroughInjectedNode(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ Task4bFoundationTest ✔
│  │  ├─ buttonsPaintFourDistinctStatesUnderBothLooks(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ missingImagesRenderAndCoverCacheTracksDevicePixels() ✔
│  │  ├─ invalidAvatarsDefaultToZero(int) ✔
│  │  │  ├─ [1] -1 ✔
│  │  │  ├─ [2] 8 ✔
│  │  │  ├─ [3] 99 ✔
│  │  │  └─ [4] 2147483647 ✔
│  │  ├─ profileNames(String, String) ✔
│  │  │  ├─ [1] ,  ✔
│  │  │  ├─ [2] A, A ✔
│  │  │  ├─ [3] abcdefghijklmnop, abcdefghijklmnop ✔
│  │  │  ├─ [4] abcdefghijklmnopq, abcdefghijklmnop ✔
│  │  │  ├─ [5]    ,  ✔
│  │  │  ├─ [6]  João , João ✔
│  │  │  └─ [7]  Đặng , Đặng ✔
│  │  └─ profileRoundTripKeepsLegacyKeyAndImplicitDefault() ✔
│  ├─ UndoParityTest ✔
│  │  ├─ P2: X(7,7), undoMove(): size 0, lượt X ✔
│  │  ├─ P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1 ✔
│  │  ├─ P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X ✔
│  │  ├─ P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X ✔
│  │  ├─ P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi ✔
│  │  └─ P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X ✔
│  ├─ BoardWinTest ✔
│  │  ├─ Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O ✔
│  │  ├─ Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X ✔
│  │  ├─ T01: . X X X X X* . -> THẮNG ✔
│  │  ├─ T02: O X X X X* X . -> THẮNG ✔
│  │  ├─ T03: . X X* X X X O -> THẮNG ✔
│  │  ├─ T04: O X* X X X X O -> KHÔNG THẮNG ✔
│  │  ├─ T05: # X X X X X* . -> THẮNG ✔
│  │  ├─ T06: # X X X X X* O -> THẮNG ✔
│  │  ├─ T07: O X X X X X* # -> THẮNG ✔
│  │  ├─ T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG ✔
│  │  ├─ T09: O . X X X X X* . O (O không liền kề) -> THẮNG ✔
│  │  ├─ T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG ✔
│  │  ├─ T11: . X X X X X X* . -> THẮNG ✔
│  │  ├─ T12: O X X X X X X* . -> THẮNG ✔
│  │  ├─ T13: O X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T14: # X X X X X X* . -> THẮNG ✔
│  │  ├─ T15: # X X X X X X* O -> THẮNG ✔
│  │  ├─ T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG ✔
│  │  ├─ T17: . X X X X X X X* . -> THẮNG ✔
│  │  ├─ T18: O X X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T19: Hàng 9 quân mở 2 đầu -> THẮNG ✔
│  │  ├─ T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG ✔
│  │  ├─ T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG ✔
│  │  ├─ T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG ✔
│  │  ├─ T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG ✔
│  │  ├─ T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG ✔
│  │  ├─ T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG ✔
│  │  ├─ T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG ✔
│  │  ├─ T29: . X X X O X* X . -> KHÔNG THẮNG ✔
│  │  ├─ T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG ✔
│  │  ├─ T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG ✔
│  │  ├─ T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG ✔
│  │  ├─ T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) ✔
│  │  ├─ T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG ✔
│  │  ├─ T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG ✔
│  │  ├─ T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG ✔
│  │  ├─ T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG ✔
│  │  ├─ T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG ✔
│  │  ├─ T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG ✔
│  │  ├─ T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG ✔
│  │  ├─ T47: . O O O O O* . -> O THẮNG ✔
│  │  ├─ T48: X O O O O O* X -> KHÔNG THẮNG ✔
│  │  ├─ T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA ✔
│  │  ├─ T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) ✔
│  │  ├─ T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên ✔
│  │  ├─ T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ ✔
│  │  └─ Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô ✔
│  ├─ SoundResourceTest ✔
│  │  ├─ staleResultClosesClipWithoutStarting() ✔
│  │  ├─ closesClipWhenStartFails() ✔
│  │  ├─ closesClipAfterPlaybackStops() ✔
│  │  ├─ closesClipWhenOpenFails() ✔
│  │  └─ queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice() ✔
│  ├─ P2LifecycleTest ✔
│  │  ├─ disposingFrameCancelsItsWorkerAndPreservesBoard() ✔
│  │  ├─ cancellationInsideRecursiveSearchUnwindsAllMoves() ✔
│  │  ├─ closingDialogCancelsPendingHostAndIgnoresLateResults() ✔
│  │  ├─ disposedFrameDoesNotReceiveLanguageEvents() ✔
│  │  ├─ successfulDialogDisposalKeepsConnection() ✔
│  │  ├─ receivedMoveIsAppliedBeforeNextEdtNetworkCallback() ✔
│  │  ├─ cancellingOnlineSelectionRestoresModeWithoutResettingGame() ✔
│  │  ├─ interruptedAiAbortsWithoutMutatingBoard() ✔
│  │  └─ networkErrorsAreTranslated(ErrorType) ✔
│  │     ├─ [1] HOST ✔
│  │     ├─ [2] JOIN ✔
│  │     ├─ [3] CHANNEL ✔
│  │     ├─ [4] START_TIMEOUT ✔
│  │     └─ [5] VERSION ✔
│  ├─ F4LayoutTest ✔
│  │  ├─ allTransitionsKeepControlsInsideParents(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  └─ directStartupBoundsAndRender(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ NetworkStabilityTest ✔
│  │  ├─ reconnectCannotBeClosedOrNotifiedByPreviousSession() ✔
│  │  ├─ staleErrorIsDiscardedWhenNewSessionStarts() ✔
│  │  ├─ callbacksAlreadyQueuedAreDiscardedAfterCancel() ✔
│  │  ├─ cancelPendingHostReleasesPortWithoutError() ✔
│  │  ├─ occupiedPortReportsTypedHostError() ✔
│  │  ├─ hostAndClientExchangeMovesRestartAndDisconnect() ✔
│  │  └─ malformedMoveDoesNotKillConnectionAndOrderIsPreserved() ✔
│  ├─ F1ProtocolTest ✔
│  │  ├─ wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored() ✔
│  │  ├─ queuedWireMoveKeepsOldRoundAfterReset() ✔
│  │  ├─ waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult() ✔
│  │  ├─ garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect() ✔
│  │  ├─ invalidPlyOrColorDoesNotChangeBoard(int) ✔
│  │  │  ├─ [1] 0 ✔
│  │  │  ├─ [2] 2 ✔
│  │  │  ├─ [3] 3 ✔
│  │  │  └─ [4] 225 ✔
│  │  ├─ simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ mismatchedVersionClosesSession() ✔
│  │  ├─ startTimeoutClosesSession() ✔
│  │  └─ clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof() ✔
│  ├─ UILangSoundTest ✔
│  │  ├─ C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist) ✔
│  │  ├─ C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ ✔
│  │  ├─ C2 – Shared custom button painting and contrast >= 4.5 ✔
│  │  ├─ B2 – Click btnSound toggle SoundManager.isSoundEnabled() ✔
│  │  ├─ B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off ✔
│  │  ├─ C3 – Menu and settings update VI/EN/PT immediately ✔
│  │  ├─ D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → ... ✔
│  │  ├─ D2 – Preferences test node khác node production ✔
│  │  └─ I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager ✔
│  ├─ Task4bFlowTest ✔
│  │  ├─ titleAcceptsMouseAndAnyKeyAndStopsItsTimer() ✔
│  │  ├─ enterCommitsProfileAndEscapeCancelsEveryDialogDraft() ✔
│  │  ├─ rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ setupRemembersDifficultyAndPlayerTwoOnlyAfterStart() ✔
│  │  └─ allCardsAndDialogsFitBothLooksWithStableFrameSize(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ LangPurityTest ✔
│  │  ├─ A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...) ✔
│  │  ├─ B – EN chỉ chứa ASCII (0x00-0x7F) ✔
│  │  └─ C – Shared theme/button contrast >= 4.5 (WCAG AA) ✔
│  ├─ UndoScoreTest ✔
│  │  ├─ U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0) ✔
│  │  ├─ U02: Undo xong, thành tích vẫn 0-1, isGameOver = false ✔
│  │  ├─ 6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false) ✔
│  │  ├─ U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false ✔
│  │  ├─ U08: Race condition AI - token cũ bị vô hiệu hóa khi resetGame hoặc undoMove ✔
│  │  ├─ 8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm ... ✔
│  │  ├─ U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ... ✔
│  │  ├─ U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đún... ✔
│  │  ├─ 7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua ✔
│  │  ├─ U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng ✔
│  │  └─ U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm ✔
│  ├─ I18nSourceScanTest ✔
│  │  ├─ I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java) ✔
│  │  └─ I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.jav... ✔
│  ├─ Task4bGameplayTest ✔
│  │  ├─ resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain() ✔
│  │  ├─ computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ boardIsCentredAndMouseUsesUnchangedCoordinates(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame() ✔
│  │  ├─ scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu() ✔
│  │  └─ realGameResultsUpdateBothCardsAndDrawCount(Piece) ✔
│  │     ├─ [1] - ✔
│  │     ├─ [2] X ✔
│  │     └─ [3] O ✔
│  ├─ P1RegressionTest ✔
│  │  ├─ languageChangePreservesFinishedGameAndTranslatesResult(Piece) ✔
│  │  │  ├─ [1] - ✔
│  │  │  ├─ [2] X ✔
│  │  │  └─ [3] O ✔
│  │  ├─ pvpUndoAfterWinRestoresWinningPlayerTurn(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsQueuedMoveAfterNewGame() ✔
│  │  ├─ onlineAcceptsOnlyOneMoveFromCorrectOpponent(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsOpponentMoveOnLocalTurn() ✔
│  │  ├─ languageChangePreservesActiveGame(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  ├─ [2] Jogar contra o computador ✔
│  │  │  └─ [3] Jogar em rede (LAN) ✔
│  │  ├─ onlineRejectsMoveAfterSwitchingMode(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  └─ [2] Jogar contra o computador ✔
│  │  ├─ onlineRejectsInvalidSquaresAndMovesAfterGameOver() ✔
│  │  └─ pvpUndoAfterDrawRestoresLastPlayerTurn() ✔
│  └─ F3HeuristicTest ✔
│     ├─ blockedSixIsNotFive(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ singleThreeIsNotDoubleThree(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ nonTerminalThreatsNeverOutrankWin(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openSixMatchesSelectedRule(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     ├─ openFourIsThreatButNotTerminal(WinRule, Piece) ✔
│     │  ├─ [1] RULE_A, X ✔
│     │  ├─ [2] RULE_A, O ✔
│     │  ├─ [3] RULE_B, X ✔
│     │  └─ [4] RULE_B, O ✔
│     └─ actualCrossDoubleThreeIsRecognized(WinRule, Piece) ✔
│        ├─ [1] RULE_A, X ✔
│        ├─ [2] RULE_A, O ✔
│        ├─ [3] RULE_B, X ✔
│        └─ [4] RULE_B, O ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 10082 ms
[        54 containers found      ]
[         0 containers skipped    ]
[        54 containers started    ]
[         0 containers aborted    ]
[        54 containers successful ]
[         0 containers failed     ]
[       250 tests found           ]
[         0 tests skipped         ]
[       250 tests started         ]
[         0 tests aborted         ]
[       250 tests successful      ]
[         0 tests failed          ]

```

### D — output thô

Lệnh: `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin" --scan-class-path --details tree --disable-banner --disable-ansi-colors`

[docs/task4b-results/D-junit.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-results/D-junit.txt>)

```text
[Preferences] /test/task4/name-96615762-539e-4180-b093-3f771911c4d4 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-c2495620-67dc-490a-95d4-d56b5f214bca RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task4/name-5fd1576a-265f-4fc2-bfe4-3747ff39c1a2 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1] Tổng hàm String không tham số: 72
[C1] Allowlist methods (PT có thể == EN): [online]
[C1] PASS — 72 methods × 3 languages
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C1b] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C2] Shared buttons: actual painted fields and text contrast PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B2] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B3] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C3] Menu and open settings update immediately PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D1] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[D2] prod path: /game/view
[D2] test path: /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330
[D2] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I2] PASS
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/uiLangSound-4cc4ee95-8347-447f-9c09-eaba41273330 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[A] Kiểm 72 methods với bản PT
[A] PASS – không có ký tự VI trong bản PT
[Preferences] /test/task3/langPurity-60612b73-e511-4431-98e3-a93aabdcb042 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[B] Kiểm 72 methods với bản EN
[B] PASS – toàn bộ EN là ASCII thuần
[Preferences] /test/task3/langPurity-60612b73-e511-4431-98e3-a93aabdcb042 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[C] Theme normal=11.939820994161197 primary=6.07910030277538 PASS
[Preferences] /test/task3/langPurity-60612b73-e511-4431-98e3-a93aabdcb042 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[Preferences] /test/task3/langPurity-60612b73-e511-4431-98e3-a93aabdcb042 RAM node removed; production /game/view unchanged: {playerName=a, soundEnabled=true}
[I1a] PASS – Không còn Lang.isVI() trong view/*.java (trừ Lang.java)
[I1b] PASS – Không còn string literal tiếng Việt có dấu trong view/*.java
SIZE VI TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER title-VI 950x948
SIZE VI MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER menu-VI 950x948
SIZE VI GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER game-VI 950x948
RENDER profile-VI 478x425
RENDER settings-VI 319x313
RENDER rules-VI 798x660
SIZE EN TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER title-EN 950x948
SIZE EN MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER menu-EN 950x948
SIZE EN GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER game-EN 950x948
RENDER profile-EN 478x425
RENDER settings-EN 314x313
RENDER rules-EN 798x660
SIZE PT TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER title-PT 950x948
SIZE PT MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER menu-PT 950x948
SIZE PT GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
RENDER game-PT 950x948
RENDER profile-PT 478x425
RENDER settings-PT 293x313
RENDER rules-PT 798x660
╷
├─ JUnit Jupiter ✔
│  ├─ Task4LifecycleTest ✔
│  │  ├─ menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult() ✔
│  │  ├─ queuedOpponentMoveCannotChangeBoardAfterMenu() ✔
│  │  ├─ undoRemainsAvailableAfterWin() ✔
│  │  ├─ menuClosesRealLanSocketAndAllConnectionDialogs() ✔
│  │  ├─ menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning() ✔
│  │  ├─ queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart() ✔
│  │  ├─ menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged() ✔
│  │  ├─ menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame() ✔
│  │  └─ menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart() ✔
│  ├─ Task4StartScreenTest ✔
│  │  ├─ profileChangesAndMenuKeepExistingSessionTotals() ✔
│  │  ├─ normalizeUnicodeWhitespaceAndLongNames(String, String) ✔
│  │  │  ├─ [1] null,  ✔
│  │  │  ├─ [2]   ,  ✔
│  │  │  ├─ [3]   Nguyễn Ánh  , Nguyễn Ánh ✔
│  │  │  ├─ [4] João Gonçalves, João Gonçalves ✔
│  │  │  ├─ [5] Conceição, Conceição ✔
│  │  │  ├─ [6] Ánh, Ánh ✔
│  │  │  ├─ [7] abcdefghijklmnopXYZ, abcdefghijklmnop ✔
│  │  │  ├─ [8] 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯, 🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯 ✔
│  │  │  ├─ [9] A B, A B ✔
│  │  │  └─ [10] <html>João, <html>João ✔
│  │  ├─ profileFieldLimitsUnicodeAndNamesRemainPlainText() ✔
│  │  ├─ emptyProfileUsesTranslatedDefaultWithoutPersistingIt(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ settingsApplyLanguageImmediatelyWithoutChangingProfileDraft() ✔
│  │  └─ titleMenuSetupAndGameUseTheSameFrame() ✔
│  ├─ F2MoveOrderTest ✔
│  │  └─ finalMoveIsSentBeforeResultAndResultIsProcessedOnce(boolean) ✔
│  │     ├─ [1] false ✔
│  │     └─ [2] true ✔
│  ├─ Task4LayoutTest ✔
│  │  └─ allThreeCardsFitAllLocalesOnStartupAndAfterTransitions(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ Task4PreferencesTest ✔
│  │  └─ profileAndLanguageRoundTripThroughInMemoryNode(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ Task4bFoundationTest ✔
│  │  ├─ buttonsPaintFourDistinctStatesUnderBothLooks(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ missingImagesRenderAndCoverCacheTracksDevicePixels() ✔
│  │  ├─ invalidAvatarsDefaultToZero(int) ✔
│  │  │  ├─ [1] -1 ✔
│  │  │  ├─ [2] 8 ✔
│  │  │  ├─ [3] 99 ✔
│  │  │  └─ [4] 2147483647 ✔
│  │  ├─ profileNames(String, String) ✔
│  │  │  ├─ [1] ,  ✔
│  │  │  ├─ [2] A, A ✔
│  │  │  ├─ [3] abcdefghijklmnop, abcdefghijklmnop ✔
│  │  │  ├─ [4] abcdefghijklmnopq, abcdefghijklmnop ✔
│  │  │  ├─ [5]    ,  ✔
│  │  │  ├─ [6]  João , João ✔
│  │  │  └─ [7]  Đặng , Đặng ✔
│  │  └─ profileRoundTripKeepsLegacyKeyAndImplicitDefault() ✔
│  ├─ UndoParityTest ✔
│  │  ├─ P2: X(7,7), undoMove(): size 0, lượt X ✔
│  │  ├─ P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1 ✔
│  │  ├─ P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X ✔
│  │  ├─ P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X ✔
│  │  ├─ P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi ✔
│  │  └─ P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X ✔
│  ├─ BoardWinTest ✔
│  │  ├─ Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O ✔
│  │  ├─ Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X ✔
│  │  ├─ T01: . X X X X X* . -> THẮNG ✔
│  │  ├─ T02: O X X X X* X . -> THẮNG ✔
│  │  ├─ T03: . X X* X X X O -> THẮNG ✔
│  │  ├─ T04: O X* X X X X O -> KHÔNG THẮNG ✔
│  │  ├─ T05: # X X X X X* . -> THẮNG ✔
│  │  ├─ T06: # X X X X X* O -> THẮNG ✔
│  │  ├─ T07: O X X X X X* # -> THẮNG ✔
│  │  ├─ T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG ✔
│  │  ├─ T09: O . X X X X X* . O (O không liền kề) -> THẮNG ✔
│  │  ├─ T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG ✔
│  │  ├─ T11: . X X X X X X* . -> THẮNG ✔
│  │  ├─ T12: O X X X X X X* . -> THẮNG ✔
│  │  ├─ T13: O X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T14: # X X X X X X* . -> THẮNG ✔
│  │  ├─ T15: # X X X X X X* O -> THẮNG ✔
│  │  ├─ T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG ✔
│  │  ├─ T17: . X X X X X X X* . -> THẮNG ✔
│  │  ├─ T18: O X X X X X X X* O -> KHÔNG THẮNG ✔
│  │  ├─ T19: Hàng 9 quân mở 2 đầu -> THẮNG ✔
│  │  ├─ T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG ✔
│  │  ├─ T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG ✔
│  │  ├─ T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG ✔
│  │  ├─ T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG ✔
│  │  ├─ T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG ✔
│  │  ├─ T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG ✔
│  │  ├─ T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG ✔
│  │  ├─ T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG ✔
│  │  ├─ T29: . X X X O X* X . -> KHÔNG THẮNG ✔
│  │  ├─ T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG ✔
│  │  ├─ T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG ✔
│  │  ├─ T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG ✔
│  │  ├─ T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG ✔
│  │  ├─ T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) ✔
│  │  ├─ T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG ✔
│  │  ├─ T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG ✔
│  │  ├─ T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG ✔
│  │  ├─ T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG ✔
│  │  ├─ T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG ✔
│  │  ├─ T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG ✔
│  │  ├─ T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG ✔
│  │  ├─ T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG ✔
│  │  ├─ T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG ✔
│  │  ├─ T47: . O O O O O* . -> O THẮNG ✔
│  │  ├─ T48: X O O O O O* X -> KHÔNG THẮNG ✔
│  │  ├─ T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA ✔
│  │  ├─ T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) ✔
│  │  ├─ T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên ✔
│  │  ├─ T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ ✔
│  │  └─ Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô ✔
│  ├─ SoundResourceTest ✔
│  │  ├─ staleResultClosesClipWithoutStarting() ✔
│  │  ├─ closesClipWhenStartFails() ✔
│  │  ├─ closesClipAfterPlaybackStops() ✔
│  │  ├─ closesClipWhenOpenFails() ✔
│  │  └─ queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice() ✔
│  ├─ P2LifecycleTest ✔
│  │  ├─ disposingFrameCancelsItsWorkerAndPreservesBoard() ✔
│  │  ├─ cancellationInsideRecursiveSearchUnwindsAllMoves() ✔
│  │  ├─ closingDialogCancelsPendingHostAndIgnoresLateResults() ✔
│  │  ├─ disposedFrameDoesNotReceiveLanguageEvents() ✔
│  │  ├─ successfulDialogDisposalKeepsConnection() ✔
│  │  ├─ receivedMoveIsAppliedBeforeNextEdtNetworkCallback() ✔
│  │  ├─ cancellingOnlineSelectionRestoresModeWithoutResettingGame() ✔
│  │  ├─ interruptedAiAbortsWithoutMutatingBoard() ✔
│  │  └─ networkErrorsAreTranslated(ErrorType) ✔
│  │     ├─ [1] HOST ✔
│  │     ├─ [2] JOIN ✔
│  │     ├─ [3] CHANNEL ✔
│  │     ├─ [4] START_TIMEOUT ✔
│  │     └─ [5] VERSION ✔
│  ├─ F4LayoutTest ✔
│  │  ├─ allTransitionsKeepControlsInsideParents(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  └─ directStartupBoundsAndRender(Language) ✔
│  │     ├─ [1] VI ✔
│  │     ├─ [2] EN ✔
│  │     └─ [3] PT ✔
│  ├─ NetworkStabilityTest ✔
│  │  ├─ reconnectCannotBeClosedOrNotifiedByPreviousSession() ✔
│  │  ├─ staleErrorIsDiscardedWhenNewSessionStarts() ✔
│  │  ├─ callbacksAlreadyQueuedAreDiscardedAfterCancel() ✔
│  │  ├─ cancelPendingHostReleasesPortWithoutError() ✔
│  │  ├─ occupiedPortReportsTypedHostError() ✔
│  │  ├─ hostAndClientExchangeMovesRestartAndDisconnect() ✔
│  │  └─ malformedMoveDoesNotKillConnectionAndOrderIsPreserved() ✔
│  ├─ F1ProtocolTest ✔
│  │  ├─ wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored() ✔
│  │  ├─ queuedWireMoveKeepsOldRoundAfterReset() ✔
│  │  ├─ waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult() ✔
│  │  ├─ garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect() ✔
│  │  ├─ invalidPlyOrColorDoesNotChangeBoard(int) ✔
│  │  │  ├─ [1] 0 ✔
│  │  │  ├─ [2] 2 ✔
│  │  │  ├─ [3] 3 ✔
│  │  │  └─ [4] 225 ✔
│  │  ├─ simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean) ✔
│  │  │  ├─ [1] false ✔
│  │  │  └─ [2] true ✔
│  │  ├─ mismatchedVersionClosesSession() ✔
│  │  ├─ startTimeoutClosesSession() ✔
│  │  └─ clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof() ✔
│  ├─ UILangSoundTest ✔
│  │  ├─ C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist) ✔
│  │  ├─ C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ ✔
│  │  ├─ C2 – Shared custom button painting and contrast >= 4.5 ✔
│  │  ├─ B2 – Click btnSound toggle SoundManager.isSoundEnabled() ✔
│  │  ├─ B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off ✔
│  │  ├─ C3 – Menu and settings update VI/EN/PT immediately ✔
│  │  ├─ D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → ... ✔
│  │  ├─ D2 – Preferences test node khác node production ✔
│  │  └─ I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager ✔
│  ├─ Task4bFlowTest ✔
│  │  ├─ titleAcceptsMouseAndAnyKeyAndStopsItsTimer() ✔
│  │  ├─ enterCommitsProfileAndEscapeCancelsEveryDialogDraft() ✔
│  │  ├─ lanMenuJoinUsesRealSocketAndStartsTheExistingOnlineGame() ✔
│  │  ├─ rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ setupRemembersDifficultyAndPlayerTwoOnlyAfterStart() ✔
│  │  └─ allCardsAndDialogsFitBothLooksWithStableFrameSize(Language, boolean) ✔
│  │     ├─ [1] VI, false ✔
│  │     ├─ [2] VI, true ✔
│  │     ├─ [3] EN, false ✔
│  │     ├─ [4] EN, true ✔
│  │     ├─ [5] PT, false ✔
│  │     └─ [6] PT, true ✔
│  ├─ LangPurityTest ✔
│  │  ├─ A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...) ✔
│  │  ├─ B – EN chỉ chứa ASCII (0x00-0x7F) ✔
│  │  └─ C – Shared theme/button contrast >= 4.5 (WCAG AA) ✔
│  ├─ UndoScoreTest ✔
│  │  ├─ U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0) ✔
│  │  ├─ U02: Undo xong, thành tích vẫn 0-1, isGameOver = false ✔
│  │  ├─ 6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false) ✔
│  │  ├─ U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false ✔
│  │  ├─ U08: Race condition AI - token cũ bị vô hiệu hóa khi resetGame hoặc undoMove ✔
│  │  ├─ 8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm ... ✔
│  │  ├─ U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ... ✔
│  │  ├─ U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đún... ✔
│  │  ├─ 7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua ✔
│  │  ├─ U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng ✔
│  │  └─ U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm ✔
│  ├─ I18nSourceScanTest ✔
│  │  ├─ I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java) ✔
│  │  └─ I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.jav... ✔
│  ├─ Task4bGameplayTest ✔
│  │  ├─ resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain() ✔
│  │  ├─ computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ boardIsCentredAndMouseUsesUnchangedCoordinates(Language) ✔
│  │  │  ├─ [1] VI ✔
│  │  │  ├─ [2] EN ✔
│  │  │  └─ [3] PT ✔
│  │  ├─ resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame() ✔
│  │  ├─ scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu() ✔
│  │  └─ realGameResultsUpdateBothCardsAndDrawCount(Piece) ✔
│  │     ├─ [1] - ✔
│  │     ├─ [2] X ✔
│  │     └─ [3] O ✔
│  ├─ P1RegressionTest ✔
│  │  ├─ languageChangePreservesFinishedGameAndTranslatesResult(Piece) ✔
│  │  │  ├─ [1] - ✔
│  │  │  ├─ [2] X ✔
│  │  │  └─ [3] O ✔
│  │  ├─ pvpUndoAfterWinRestoresWinningPlayerTurn(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsQueuedMoveAfterNewGame() ✔
│  │  ├─ onlineAcceptsOnlyOneMoveFromCorrectOpponent(Piece) ✔
│  │  │  ├─ [1] X ✔
│  │  │  └─ [2] O ✔
│  │  ├─ onlineRejectsOpponentMoveOnLocalTurn() ✔
│  │  ├─ languageChangePreservesActiveGame(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  ├─ [2] Jogar contra o computador ✔
│  │  │  └─ [3] Jogar em rede (LAN) ✔
│  │  ├─ onlineRejectsMoveAfterSwitchingMode(GameMode) ✔
│  │  │  ├─ [1] 2 jogadores no mesmo computador ✔
│  │  │  └─ [2] Jogar contra o computador ✔
│  │  ├─ onlineRejectsInvalidSquaresAndMovesAfterGameOver() ✔
│  │  └─ pvpUndoAfterDrawRestoresLastPlayerTurn() ✔
│  ├─ F3HeuristicTest ✔
│  │  ├─ blockedSixIsNotFive(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  ├─ edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  ├─ singleThreeIsNotDoubleThree(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  ├─ nonTerminalThreatsNeverOutrankWin(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  ├─ openSixMatchesSelectedRule(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  ├─ openFourIsThreatButNotTerminal(WinRule, Piece) ✔
│  │  │  ├─ [1] RULE_A, X ✔
│  │  │  ├─ [2] RULE_A, O ✔
│  │  │  ├─ [3] RULE_B, X ✔
│  │  │  └─ [4] RULE_B, O ✔
│  │  └─ actualCrossDoubleThreeIsRecognized(WinRule, Piece) ✔
│  │     ├─ [1] RULE_A, X ✔
│  │     ├─ [2] RULE_A, O ✔
│  │     ├─ [3] RULE_B, X ✔
│  │     └─ [4] RULE_B, O ✔
│  └─ Task4bPresentationTest ✔
│     ├─ runtimeResourcePackagingAndImageLoadingWorkOnBinClasspath() ✔
│     ├─ profileClearCounterAndFocusSelectAllUseActualEditor() ✔
│     ├─ suppliedRulesKeysAreVerbatimAndPortugueseIsPure(Language) ✔
│     │  ├─ [1] VI ✔
│     │  ├─ [2] EN ✔
│     │  └─ [3] PT ✔
│     ├─ renderAllSixViewsAndMeasureDisplayedFrame(Language) ✔
│     │  ├─ [1] VI ✔
│     │  ├─ [2] EN ✔
│     │  └─ [3] PT ✔
│     ├─ translatedButtonAndLabelTextFitsIncludingTransitions(Language, boolean) ✔
│     │  ├─ [1] VI, false ✔
│     │  ├─ [2] VI, true ✔
│     │  ├─ [3] EN, false ✔
│     │  ├─ [4] EN, true ✔
│     │  ├─ [5] PT, false ✔
│     │  └─ [6] PT, true ✔
│     └─ buttonsRenderFourStatesAndActivateWithEnterAndSpace(boolean) ✔
│        ├─ [1] false ✔
│        └─ [2] true ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 14413 ms
[        59 containers found      ]
[         0 containers skipped    ]
[        59 containers started    ]
[         0 containers aborted    ]
[        59 containers successful ]
[         0 containers failed     ]
[       267 tests found           ]
[         0 tests skipped         ]
[       267 tests started         ]
[         0 tests aborted         ]
[       267 tests successful      ]
[         0 tests failed          ]

```

## 3. Mọi test cũ được sửa/thay

Bảng đối chiếu đầy đủ: [docs/TASK4B_TEST_MIGRATION.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/TASK4B_TEST_MIGRATION.md>). Các lớp cũ giữ nguyên số invocation; không xóa ca để lấy PASS và không dùng @Disabled. Fixture ghi Preferences chuyển toàn bộ sang RAM; đối chiếu node thật chỉ đọc.

# Task 4b — đối chiếu test cũ (D1)

Mốc so sánh: `f61e2d5`. Không xóa lớp JUnit nào, không thêm `@Disabled`.
Các test còn giữ tên vẫn giữ hành vi hồi quy; chỉ thay điểm truy cập giao diện/fixture khi cần.
Số ca của mỗi lớp cũ giữ nguyên. Parameterized invocation được đếm thành từng ca trong output JUnit.

| Test cũ | Lý do thay đổi | Test thay thế / bằng chứng hiện tại |
|---|---|---|
| `UILangSoundTest.c1_langAllKeysNotNullNotEmpty` | Fixture Preferences chuyển sang RAM; giữ reflection VI/EN/PT | Cùng tên; quét đủ các hàm Lang hiện tại |
| `UILangSoundTest.c1b_connectingMsgContainsIp` | Fixture RAM, giữ kiểm tra IP | Cùng tên |
| `UILangSoundTest.c2_comboRendererContrast` | Hai combobox game đã bị loại bỏ theo C5 | `UILangSoundTest.c2_sharedButtonPaintAndContrast`; thêm `Task4bPresentationTest.buttonsRenderFourStatesAndActivateWithEnterAndSpace` |
| `UILangSoundTest.b2_soundButtonToggle` | Nút âm thanh chuyển vào Cài đặt | `UILangSoundTest.b2_settingsSoundToggle` |
| `UILangSoundTest.b3_soundTooltipByLang` | Tooltip được đọc từ nút trong Cài đặt | `UILangSoundTest.b3_settingsSoundTooltipByLang` |
| `UILangSoundTest.c3_mainFrameLabelsUpdate` | Nhãn chế độ/selector cũ bị loại | `UILangSoundTest.c3_menuAndSettingsUpdateImmediately` |
| `UILangSoundTest.d1_appSettingsRoundTrip` | Thay node registry riêng bằng Preferences RAM; giữ fallback/invalid/lang/sound | Cùng tên |
| `UILangSoundTest.d2_testNodeDifferentFromProduction` | Không tạo AppSettings production chỉ để lấy đường dẫn | `UILangSoundTest.d2_inMemoryNodeIsDifferentFromProduction`; đối chiếu node thật chỉ đọc trong `PreferencesIsolation` |
| `UILangSoundTest.i2_speakerIconVisual` | Fixture RAM và nền Theme; giữ kiểm pixel dấu X đỏ và bật/tắt | Cùng tên |
| `LangPurityTest.a_ptNoVietnameseChars` | Fixture RAM; giữ phép quét PT | Cùng tên; bổ sung kiểm rules/độ khó có tham số ở các lớp Task4b |
| `LangPurityTest.b_enAsciiOnly` | Fixture RAM; giữ phép quét các hàm không tham số | Cùng tên; nội dung luật EN có dấu nhân × được kiểm bằng fixture nguyên văn riêng |
| `LangPurityTest.c_comboContrastRatio` | Combobox game và màu tím cũ bị loại | `LangPurityTest.c_sharedThemeContrast`; kiểm màu nâu/kem/vàng thực tế |
| `Task4StartScreenTest.startAndMenuSwitchCardsInTheSameFrame` | Hai card cũ trở thành TITLE/MENU/GAME và thiết lập modal | `Task4StartScreenTest.titleMenuSetupAndGameUseTheSameFrame` |
| `Task4StartScreenTest.emptyNameUsesTranslatedDefaultAndRemainsImplicit` | Tên nhập trong hồ sơ; mặc định giờ là Player 1 | `Task4StartScreenTest.emptyProfileUsesTranslatedDefaultWithoutPersistingIt` (3 ngôn ngữ) |
| `Task4StartScreenTest.normalizeUnicodeWhitespaceAndLongNames` | Chỉ định dạng lại; giữ 10 bộ dữ liệu và cả hai assertion | Cùng tên; không giảm kiểm tra |
| `Task4StartScreenTest.fieldLimitsUnicodeWithoutSplittingEmojiAndDisplaysNamesAsPlainText` | Ô nhập chuyển sang ProfileDialog | `Task4StartScreenTest.profileFieldLimitsUnicodeAndNamesRemainPlainText` |
| `Task4StartScreenTest.languageChoiceOnMenuPreservesTypedNameAndSynchronizesBothSelectors` | Không còn hai selector; chỉ Cài đặt chọn ngôn ngữ | `Task4StartScreenTest.settingsApplyLanguageImmediatelyWithoutChangingProfileDraft` |
| `Task4StartScreenTest.achievementIconShowsExistingSessionTotalsAndMenuKeepsThem` | Nút cúp bị loại theo yêu cầu; giữ kiểm tỉ số | `Task4StartScreenTest.profileChangesAndMenuKeepExistingSessionTotals`; thêm `Task4bGameplayTest.realGameResultsUpdateBothCardsAndDrawCount` |
| `Task4PreferencesTest.nameAndLanguageRoundTripThroughInjectedNode` | Lưu thêm avatar; không ghi node test vào registry | `Task4PreferencesTest.profileAndLanguageRoundTripThroughInMemoryNode` (3 ngôn ngữ) |
| `Task4LayoutTest.bothCardsFitAllLocalesOnStartupAndAfterTransitions` | Kiểm cả ba card, hồ sơ thay ô nhập StartScreen, ngôn ngữ qua trạng thái | `Task4LayoutTest.allThreeCardsFitAllLocalesOnStartupAndAfterTransitions` (3×2); thêm `Task4bFlowTest.allCardsAndDialogsFitBothLooksWithStableFrameSize` |
| `F4LayoutTest.directStartupBoundsAndRender` | Helper render đọc nhãn thẻ trái; ảnh không ghi đè tư liệu Task 4 | Cùng tên; thêm `Task4bPresentationTest.renderAllSixViewsAndMeasureDisplayedFrame` |
| `F4LayoutTest.allTransitionsKeepControlsInsideParents` | Cùng thay đổi helper; giữ VI→PT và VI→EN→PT | Cùng tên; thêm `Task4bPresentationTest.translatedButtonAndLabelTextFitsIncludingTransitions` |
| `P1RegressionTest.languageChangePreservesActiveGame` | Nhãn chế độ cũ bị loại; trạng thái PvP nay chứa tên đang chơi | Cùng tên; kiểm nhãn menu và `Lang.namedTurn`, giữ board/gameId/lượt/worker/score |
| `P1RegressionTest.languageChangePreservesFinishedGameAndTranslatesResult` | Helper lấy trạng thái từ nguồn mới thay JLabel cũ | Cùng tên; giữ ba kết quả và board/gameId/history |
| `P2LifecycleTest.disposedFrameDoesNotReceiveLanguageEvents` | Theo dõi nút PvAI trên menu thay nhãn mode cũ | Cùng tên; vẫn kiểm frame đã dispose không nhận Lang event |
| `P2LifecycleTest.cancellingOnlineSelectionRestoresModeWithoutResettingGame` | Không dùng modeCombo; gọi điểm mở NetworkDialog và đọc selectedMode | Cùng tên; giữ gameId/history/Undo, chế độ PvP sau hủy |
| `Task4LifecycleTest.menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame` | Chọn HARD qua trạng thái thay diffCombo | Cùng tên; vẫn hủy worker thật và gọi lại completion thật |
| `Task4LifecycleTest.menuClosesRealLanSocketAndAllConnectionDialogs` | Về menu nay giữ ONLINE; muốn PvAI phải chọn qua SetupDialog | Cùng tên; thêm assertion giữ ONLINE, rồi xác nhận thiết lập PvAI; vẫn kiểm EOF/socket/dialog/gameId |
| `Task4LifecycleTest.menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart` | Không modeCombo; về menu giữ PvP, không tự về PvAI | Cùng tên; vẫn kiểm session cũ đóng và không phục hồi state sau modal |
| `Task4LifecycleTest.menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult` | Nút cúp bị loại; dùng RulesDialog làm cửa sổ phụ đang mở | Cùng tên; vẫn kiểm đóng mọi owned dialog, giữ điểm và loại kết quả cũ |
| `Task4LifecycleTest.undoRemainsAvailableAfterWin` | Bắt đầu PvP qua nguồn trạng thái thay modeCombo | Cùng tên; vẫn bấm Undo sản phẩm sau thắng, còn 8 nước và điểm không đổi |

Các ca vòng đời còn lại không đổi nội dung assertion: `queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart`, `menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged`, `menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning`, `queuedOpponentMoveCannotChangeBoardAfterMenu`.

`PreferencesIsolation` là helper, không phải ca JUnit: các node dùng để ghi là RAM và được removeNode; snapshot node thật `/game/view` chỉ đọc, chỉ mở khi node đã tồn tại. `MemoryPreferences.removeNodeSpi` cũng xóa child khỏi bộ nhớ của parent, tránh trả lại node đã bị xóa.

Ca mới theo giai đoạn: A thêm `Task4bFoundationTest` 15 ca; B thêm `Task4bFlowTest` 12 ca; C thêm `Task4bGameplayTest` 12 ca; D thêm 1 ca LAN từ menu trong Flow và `Task4bPresentationTest` 16 ca. Tổng mới 56; nền 211 thành 267. Không có PENDING trong JUnit.

## 4. Bàn cờ lệch trái và phần sửa

Ở `f61e2d5`, `src/game/view/MainFrame.java:285` gắn trực tiếp `boardPanel` vào `BorderLayout.CENTER`. Panel bị giãn 760×596 nhưng grid vẫn vẽ/hit-test từ `OFFSET=32`, bước ô 38. Vì vậy lưới trải x=32..564 trong panel rộng 760, lệch trái.

Hiện tại [src/game/view/MainFrame.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:67>) đặt panel 596×596 trong GridBagLayout. Tọa độ và kích thước quân giữ nguyên: [src/game/view/BoardPanel.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:25>). Lưới trong content có x=114..646: lề hai bên 114 px logic, kiểm sai số ≤1 px bằng `Task4bGameplayTest.boardIsCentredAndMouseUsesUnchangedCoordinates`. Hai nút sách/menu được assert cùng x, cùng 40×40 giữa menu và game.

AI, model Board và toàn bộ src/game/network giống từng byte so với f61e2d5. Thân Undo/playMove/executeMove không đổi (chuẩn hóa newline để so sánh); thay đổi BoardPanel chỉ là palette, gọi phần vẽ quân dùng chung và giữ mode trong resetForMenu. SHA256 của thân `undoMove`: `90c1a4863a61663e1eb65e7a1a4a26f22df4dc88d936c0f85d07eb789b85696e`.

## 5. Resource, biên dịch và chạy

Trước Task 4b, run.bat chỉ javac rồi javaw -cp bin: javac KHÔNG copy resource. Nay [build.ps1](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/build.ps1>) và [run.bat](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/run.bat>) copy nội dung resources/ vào bin/ sau biên dịch. build.ps1 chỉ cần PowerShell/JDK, không cần rg. Chạy từ gốc repo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1
java -cp bin game.Main
```

Tên ExecutionPolicy Bypass ở lệnh trên chỉ là tùy chọn chạy script PowerShell, không thay Preferences factory/node hoặc vô hiệu hóa assertion.

Đặt ảnh thật tại `resources/images/background.png`; classpath `/images/background.png`. Avatar tại `resources/avatars/avatar_0.png` tới `avatar_7.png` (128×128); classpath `/avatars/avatar_0.png`…`/avatars/avatar_7.png`. Nạp bằng getResource tại [src/game/view/ResourceImages.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/ResourceImages.java:11>). Chưa có bitmap thật nào: nền gradient #8B5E34→#5A3A22, avatar tròn và chữ đầu tên. Cover giữ tỉ lệ/cắt giữa; cache theo kích thước pixel thiết bị ở [src/game/view/BackgroundPanel.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BackgroundPanel.java:25>). Test resource ghi ảnh fixture trong bin_test rồi xóa, không thêm ảnh nền giả vào resources.

## 6. Kích thước cửa sổ hiển thị và ảnh

Các dòng dưới là nguyên văn từ D-junit.txt. Đơn vị của getSize() là px logic Swing; ảnh xuất nhân transform 1.25. L&F native Windows, không thêm setSize/setMinimumSize, setResizable(false).

```text
SIZE VI TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE VI MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE VI GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE EN TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE EN MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE EN GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE PT TITLE frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE PT MENU frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
SIZE PT GAME frame.getSize()=774x795 content.getSize()=760x758 transform=AffineTransform[[1.25, 0.0, 0.0], [0.0, 1.25, 0.0]]
```

Đã xuất đủ 18/18 ảnh, không thiếu ảnh nào. Ảnh render content pane theo cách F4; không chụp viền/title bar của hệ điều hành. Title của JDialog vẫn được kiểm bằng test.

| Màn hình | VI | EN | PT |
|---|---|---|---|
| title | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/title-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/title-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/title-PT.png>) |
| menu | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/menu-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/menu-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/menu-PT.png>) |
| profile | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/profile-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/profile-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/profile-PT.png>) |
| settings | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/settings-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/settings-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/settings-PT.png>) |
| rules | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/rules-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/rules-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/rules-PT.png>) |
| game | [VI](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/game-VI.png>) | [EN](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/game-EN.png>) | [PT](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/task4b-render/game-PT.png>) |

## 7. Nhãn quân và nội dung luật

Lang hiện có nhãn VI **Quân Đen/Quân Trắng**, EN **Black/White**, PT **Preto/Branco** trong thông báo lượt/kết nối. `pieceName` giữ các nhãn này ở [src/game/view/Lang.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/Lang.java:36>). Nội dung rules.l1…l6 và hai nhãn ví dụ giữ nguyên văn bảng B6, gồm cách chia số nhiều “as Pretas” trong PT; fixture tám key mỗi ngôn ngữ ở test/resources/task4b-rules. Không thay luật trọng tài để khớp nội dung UI. Luật app hiện dùng RULE_B như trước.

## 8. Kiểm bổ sung và giới hạn

Node thật sau test vẫn `{playerName=a, soundEnabled=true}`, khớp snapshot trước từng lớp; test không lưu profile/ngôn ngữ/điểm vào node thật. Không thay Preferences factory và không bypass SecurityException; dữ liệu test ghi vào AbstractPreferences RAM.

Đã kiểm tự động VI/EN/PT × Metal/Windows, chuyển ngôn ngữ khi dialog mở, vòng đời AI/dialog và socket localhost thật; đã xem ảnh render. Cache nền đã được kiểm với transform 1.25 và 1.5 trong BufferedImage, nhưng **DPI 150% trên màn hình thật: chưa kiểm**. **LAN hai máy: chưa kiểm**. **Âm thanh qua loa/thiết bị thật: chưa kiểm**. Chưa có ảnh nền/avatar thật nên bố cục chủ thể của ảnh tương lai chưa kiểm.

Bộ console chỉ tính ca thực chạy; T55/T56/T57 vẫn PENDING như trước, không dùng làm bằng chứng cho Task 4b.

### RuleAndDrawTest — output thô

```text
==================================================
   BẮT ĐẦU KIỂM THỬ LUẬT CHẶN 2 ĐẦU & HÒA CỜ
==================================================

--- PHẦN 1: KIỂM THỬ LUẬT CHẶN HAI ĐẦU ---
[PASS] 1. Chuỗi 5 quân hở 2 đầu (_XXXXX_) -> THẮNG
[PASS] 2. Chuỗi 5 quân bị chặn 1 đầu bởi O (OXXXXX_) -> THẮNG
[PASS] 3. Chuỗi 5 quân bị chặn 2 đầu bởi O (OXXXXXO) -> KHÔNG THẮNG
[PASS] 4. Chuỗi 5 quân sát mép bàn cờ, đầu kia hở (|XXXXX_) -> THẮNG
[PASS] 5. Chuỗi 5 quân sát mép, 1 đầu bị O chặn (|XXXXXO) -> THẮNG
[PASS] 6. Chuỗi chéo bị chặn 2 đầu bởi O -> KHÔNG THẮNG
[PASS] 7. Chuỗi chéo bị chặn 1 đầu bởi O -> THẮNG
[PASS] 8. Chuỗi 6 quân bị chặn 2 đầu bởi O (OXXXXXXO) -> KHÔNG THẮNG

--- PHẦN 2: KIỂM THỬ BÀN CỜ ĐÁNH KÍN 225 Ô & HÒA CỜ ---
[PASS] 1. Đã đánh 224 ô -> isFull() phải là false
[PASS] 2. Nước đi tại ô cuối cùng (14, 14) phải hợp lệ
[PASS] 3. Đánh nước thứ 225 thành công
[PASS] 4. Bàn cờ hiện tại đã đánh kín 225 ô (isFull == true)
[PASS] 5. Nước thứ 225 không tạo thế thắng (checkWin == false) -> Xử HÒA
[PASS] 6. Không thể đánh thêm bất kỳ ô nào sau khi kín bàn
[PASS] 7. Bàn cờ trước nước 225 có 224 nước
[PASS] 8. Nước thứ 225 tạo thành 5 quân X -> checkWin() PHẢI LÀ TRUE
[PASS] 9. Bàn cờ đồng thời đầy 225 ô (isFull == true)

==================================================
KẾT QUẢ: ĐÃ VƯỢT QUA 17 / 17 KIỂM THỬ.
>>> TẤT CẢ CÁC KIỂM THỬ ĐỀU CHÍNH XÁC 100%! <<<
==================================================
```

### FullGomokuTestSuite — output thô

```text
======================================================================
              BẮT ĐẦU CHẠY BỘ KIỂM THỬ TOÀN DIỆN 57 CA
======================================================================

[PASS] T01 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | . X X X X X* .
[PASS] T02 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | O X X X X* X .
[PASS] T03 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | . X X* X X X O
[PASS] T04 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | O X* X X X X O
[PASS] T05 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | # X X X X X* .
[PASS] T06 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | # X X X X X* O
[PASS] T07 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | O X X X X X* #
[PASS] T08 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | # X X X X X* # chéo 5 ô
[PASS] T09 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | O . X X X X X* . O
[PASS] T10 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | O X X X X X* . O
[PASS] T11 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6 mở 2 đầu
[PASS] T11 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 6 mở 2 đầu
[PASS] T12 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6, 1 đầu O
[PASS] T12 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 6, 1 đầu O
[PASS] T13 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6 chặn 2 đầu
[PASS] T13 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6 chặn 2 đầu
[PASS] T14 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6 sát mép #
[PASS] T14 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 6 sát mép #
[PASS] T15 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 6 mép # và O
[PASS] T15 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 6 mép # và O
[PASS] T16 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng chéo 6 hai đầu mép #
[PASS] T16 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng chéo 6 hai đầu mép #
[PASS] T17 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 7 mở 2 đầu
[PASS] T17 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 7 mở 2 đầu
[PASS] T18 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 7 chặn 2 đầu
[PASS] T18 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 7 chặn 2 đầu
[PASS] T19 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 9 mở 2 đầu
[PASS] T19 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 9 mở 2 đầu
[PASS] T20 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hàng 15 ô chạm cả 2 mép
[PASS] T20 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hàng 15 ô chạm cả 2 mép
[PASS] T21 | RULE A | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Nối 2+2 thành 5
[PASS] T21 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Nối 2+2 thành 5
[PASS] T22 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Nối 3+3 thành 7
[PASS] T22 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Nối 3+3 thành 7
[PASS] T23 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Nối 3+2 thành 6
[PASS] T23 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Nối 3+2 thành 6
[PASS] T24 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Nối thành 5 nhưng bị O chặn 2 đầu
[PASS] T24 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Nối thành 5 nhưng bị O chặn 2 đầu
[PASS] T25 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Chỉ có 4 quân
[PASS] T26 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Có ô trống ở giữa
[PASS] T27 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Bị O ngắt ở giữa
[PASS] T28 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Còn ô trống ở giữa
[PASS] T29 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Bị O ngắt
[PASS] T30 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Đánh chỗ khác, không tạo hàng
[PASS] T31 | RULE A | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Dọc 5 mở cứu ngang 5 bị chặn
[PASS] T31 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Dọc 5 mở cứu ngang 5 bị chặn
[PASS] T32 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Cả hai hướng đều bị chặn 2 đầu
[PASS] T32 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Cả hai hướng đều bị chặn 2 đầu
[PASS] T33 | RULE A | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Cả ngang và dọc 5 hợp lệ
[PASS] T33 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Cả ngang và dọc 5 hợp lệ
[PASS] T34 | RULE A | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Thắng nhờ dọc 5 ở Rule A
[PASS] T34 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Thắng cả hai hướng ở Rule B
[PASS] T35 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Hai hàng 6 đều loại ở Rule A
[PASS] T35 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Hai hàng 6 đều thắng ở Rule B
[PASS] T36 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Ngang 6 loại, dọc 5 chặn ở Rule A
[PASS] T36 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Thắng nhờ ngang 6 ở Rule B
[PASS] T37 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Cả hai đều bị chặn 2 đầu
[PASS] T37 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Cả hai đều bị chặn 2 đầu
[PASS] T38 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Tất cả các hướng chỉ có 4 quân
[PASS] T38 | RULE B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Tất cả các hướng chỉ có 4 quân
[PASS] T39 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Dọc 5 ở giữa bàn
[PASS] T40 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Chéo chính 5 ở giữa bàn
[PASS] T41 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Chéo phụ 5 ở giữa bàn
[PASS] T42 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Cột 0-4 biên trái
[PASS] T43 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Cột 10-14 biên phải, 1 đầu O
[PASS] T44 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Góc (0,0) chéo 5 ô, 1 đầu O
[PASS] T45 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Góc (14,14) chéo 5 ô, đầu kia mở
[PASS] T46 | RULE A | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | Chéo 6 ô loại ở Rule A
[PASS] T46 | RULE B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Chéo 6 ô thắng ở Rule B
[PASS] T47 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | O thắng hợp lệ
[PASS] T48 | RULE A/B | Kỳ vọng: KHÔNG THẮNG | Thực tế: KHÔNG THẮNG | O bị X chặn kín 2 đầu
[PASS] T49 | RULE A/B | Kỳ vọng: HÒA         | Thực tế: HÒA         | 225 ô kín không có ai thắng
[PASS] T50 | RULE A/B | Kỳ vọng: THẮNG       | Thực tế: THẮNG       | Nước thứ 225 thắng ưu tiên hơn hòa
[PASS] T51 | RULE A/B | Kỳ vọng: TỪ CHỐI     | Thực tế: TỪ CHỐI     | Đánh vào ô đã có quân
[PASS] T52 | RULE A/B | Kỳ vọng: TỪ CHỐI     | Thực tế: TỪ CHỐI     | Đánh ngoài bàn cờ
[PASS] T53 | RULE A/B | Kỳ vọng: TỪ CHỐI     | Thực tế: TỪ CHỐI     | Nước đi thật sau khi thắng bị từ chối
[PASS] T54 | RULE A/B | Kỳ vọng: TỪ CHỐI     | Thực tế: TỪ CHỐI     | Người chơi online không đi hai nước liên tiếp
[PENDING] T55 | Đầu hàng: chưa có chức năng sản phẩm
[PENDING] T56 | Hết giờ: chưa có đồng hồ sản phẩm
[PENDING] T57 | Thứ tự kết thúc tích hợp: chưa có kịch bản console tương ứng

======================================================================
KẾT QUẢ ASSERTIONS: 77 / 77 ĐẠT (PASS)!
ĐÃ CHẠY: 54 tình huống, 77 assertions; PENDING: 3.
>>> TẤT CẢ ASSERTIONS ĐÃ CHẠY ĐỀU PASS; PENDING KHÔNG TÍNH VÀO PASS. <<<
======================================================================
```

## 9. Git và bàn giao

Chỉ bốn commit local A/B/C/D trên task4-start-screen. Không push/merge/tag/đổi tên nhánh. `git status`, `git log --oneline -8`, `git diff f61e2d5 --stat` sau commit cuối nằm trong output bàn giao; không đưa SHA của commit D vào chính nội dung commit để tránh vòng tự tham chiếu.
