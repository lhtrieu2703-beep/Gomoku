# Task 5e — thông báo kết quả trên bàn cờ

## Bước 0

- Nhánh lúc bắt đầu: `task5e-win-overlay`; cây sạch.
- HEAD: `b075b58` (merge PR #8, chứa Task 5d `6dbeaa5`).
- Không chạy checkout, pull, fetch, stash, reset, clean, force, push hoặc merge.
- JDK 25.0.3, JUnit Console Standalone 1.10.2.
- Mốc trước sửa: **458 phát hiện, 446 chạy, 425 đạt, 21 lỗi, 12 không chạy vì 2 container lỗi**. Exit code 1.
- Baseline chạy trước mọi thay đổi mã nguồn/test. Log: `build/task5e-win-overlay/junit-baseline.log`.

## Lệnh đầy đủ đã dùng ở cả hai lần chạy

Dán nguyên khối vào PowerShell tại thư mục Gomoku. Mỗi lần dùng bộ class mới, tránh class cũ còn sót. Các test cũ tạo ảnh trong thư mục làm việc nên chạy từ bản sao bên trong build; không ghi đè ảnh/tài liệu đang được Git theo dõi.

```powershell
$ErrorActionPreference = 'Stop'
$task5eRoot = (Get-Location).Path
$task5eOut = Join-Path $task5eRoot 'build/task5e-win-overlay'
$task5eRun = Join-Path $task5eOut ('run-' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $task5eRun -Force | Out-Null
foreach ($item in @('src','test','resources','lib','build.ps1')) {
    Copy-Item -LiteralPath $item -Destination $task5eRun -Recurse -Force
}
Push-Location -LiteralPath $task5eRun
try {
    & .\build.ps1
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & java '-Djava.awt.headless=false' '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path 'bin_test;bin;src' --scan-class-path --reports-dir junit-xml --details summary --disable-banner --disable-ansi-colors --fail-if-no-tests 2>&1 |
        Tee-Object -FilePath (Join-Path $task5eOut 'junit-latest.log')
    Write-Output ('JUNIT_EXIT_CODE: ' + $LASTEXITCODE)
} finally {
    Pop-Location
}
```

Sau lần đầu:

```powershell
Copy-Item -LiteralPath build/task5e-win-overlay/junit-latest.log -Destination build/task5e-win-overlay/junit-baseline.log
```

Sau lần cuối:

```powershell
Copy-Item -LiteralPath build/task5e-win-overlay/junit-latest.log -Destination build/task5e-win-overlay/junit-after.log
```

Các thư mục XML được ghi trong `build/task5e-win-overlay/baseline-run.txt` và `after-run.txt`.

## Khảo sát xác nhận

- MainFrame callback onGameOver trước sửa (dòng 90–97) tạo JOptionPane/createDialog và gọi setVisible(true); dùng chung cho thắng, AI thắng và hòa.
- BoardPanel.buildWinMessage dùng Lang.p1Wins/p2Wins/youWin/aiWins cố định; không đọc profile. Nhánh hòa gọi Lang.drawGame.
- Thanh trên dùng BoardPanel.getGameOverMessage; tên profile/secondProfile trước đây chỉ dùng trên thẻ người chơi.
- Đã bỏ resultDialog và vòng modal ở luồng kết thúc ván; các hộp thoại kết nối/cài đặt không thuộc thay đổi này.

## Cách hiển thị

- Dải mặc định: 548×146, x=24; y=426 khi tâm dãy ở hàng 0–7 hoặc hòa, y=24 khi tâm dãy ở hàng 8–14. BoardPanel vẫn 596×596, JFrame vẫn 774×795.
- Dùng trung bình chỉ số hàng của toàn bộ winningLine để chọn nửa xa nhất; trường hợp chính giữa chọn dưới.
- Kiểm tra hình chữ nhật quanh từng ô thắng, bán kính 22 px, gồm toàn bộ ô 38 px và viền đỏ/trắng. Không cho phép giao với dải.
- Nếu một overline dọc/chéo trải qua cả hai nửa làm dải ngang va vào quân, dùng góc trống của cùng nửa đã chọn: 248×174, ba nút xếp dọc. Các test bao phủ mọi vị trí/direction và độ dài 5–15.
- Nền gỗ nâu với alpha 225–235/255 và đường viền vàng; nút có thể nhận focus/Enter.
- “Xem bàn cờ” ẩn dải; nút nhỏ “Hiện kết quả” nằm trong chính vùng đã kiểm tra an toàn để mở lại. Đổi ngôn ngữ khi đang ẩn giữ trạng thái ẩn.
- Tên lấy từ profile/secondProfile; AI dùng Lang.computerName(difficulty), đối thủ LAN dùng nhãn hiện có. Tên trống dùng nhãn mặc định và tên quân hiện có.
- Biểu tượng gọi cùng StonePainter/PieceStyle với bàn cờ; ba kiểu quân, ba bàn đều được kiểm tra.
- Tên dài dùng dấu ASCII “...”, không cắt giữa surrogate pair. Giữ đầy đủ tên trong tooltip/accessibility; thanh trạng thái tối đa ba dòng.
- Tỉ số nhận trực tiếp các giá trị humanWins/aiWins/draws từ ScoreTracker, không tự cộng điểm.
- Undo xóa dải, nút mở lại và winningLine; callback onGameReopened thu hồi điểm như trước.
- Nút ở dải và thanh dưới dùng chung các hành động Ván Mới/Về menu. Giữ guard screen/gameId và cơ chế từ chối callback cũ.

## Test cũ đã sửa và lý do

| Phương thức | Lý do và kiểm tra thay thế |
|---|---|
| F1ProtocolTest.waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult | Bỏ truy cập resultDialog và invokeLater dành cho vòng modal; kiểm tra overlay hiện, vòng LAN mới ẩn overlay, kết quả gameId cũ bị bỏ. |
| Task4LifecycleTest.menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult | Kiểm tra overlay bị ẩn khi về menu; vẫn kiểm tra dialog Rules đóng, giữ điểm và bỏ kết quả muộn. |
| Task4LifecycleTest.undoRemainsAvailableAfterWin | Bỏ thao tác dispose dialog; bấm Undo trong khi overlay đang hiện, xác nhận điểm về 0, overlay ẩn và viền thắng bị xóa. |
| Task4bGameplayTest.realGameResultsUpdateBothCardsAndDrawCount (X/O/hòa) | Bỏ đóng resultDialog; xác nhận overlay hiện, giữ toàn bộ kiểm tra tỉ số/thẻ. |
| P1RegressionTest.languageChangePreservesFinishedGameAndTranslatesResult (X/O/hòa) | Giữ listener thật để kiểm tra overlay dịch khi đổi ngôn ngữ; câu hòa đổi theo yêu cầu HÒA CỜ; giữ kiểm tra gameId/lịch sử/trạng thái. |

Không sửa test lỗi socket, Registry, child JVM hoặc test phụ thuộc trạng thái Lang ngoài danh sách trên.

## Test mới

**45 trường hợp mới** trong WinOverlayTest (39) và EndGameOverlayPlacementTest (6).

Bao phủ: hai người thắng có dấu; tên trắng dùng mặc định ở VI/EN/PT; hòa; Undo ở PVP/PV_AI; hành động trong dải và thanh dưới; callback cũ cả trước/sau khi ván tiếp theo kết thúc; ẩn/hiện lại và đổi ngôn ngữ; tỉ số/reset; AI ba độ khó; 18 tổ hợp bàn/kiểu quân/người thắng với cả ba ngôn ngữ; so từng pixel vùng quân/viền thắng khi hiện và ẩn overlay; dãy chéo 9 quân; Unicode/ellipsis; mọi vị trí của dãy 5–15 quân theo bốn hướng.

## Kết quả thực tế trước/sau

| Lần chạy | Phát hiện | Đã chạy | Đạt | Lỗi test | Không chạy vì container lỗi | Container lỗi | Exit |
|---|---:|---:|---:|---:|---:|---:|---:|
| Trước sửa | 458 | 446 | 425 | 21 | 12 | 2 | 1 |
| Sau sửa | 503 | 491 | 470 | 21 | 12 | 2 | 1 |
| Tập trung (phần mới và các test liên quan) | 76 | 76 | 76 | 0 | 0 | 0 | 0 |

**Bộ đầy đủ chưa đạt.** Không báo 503/503.

Đã so sánh XML cho toàn bộ 458 test cũ: **0 test đổi trạng thái**. 45 test mới: **45 đạt, 0 lỗi**. Đã so sánh tên và dòng lỗi đầu tiên trong console log: **23 mục lỗi trước/sau giống hệt** (21 test + 2 container), **không có lỗi mới**. Bằng chứng JSON: failures-before.json, failures-after.json, new-tests.json trong thư mục build/task5e-win-overlay.

Lệnh không bị chặn ở mức khởi chạy, cả hai lần chạy đầy đủ đều kết thúc và trả exit 1. Socket trong test và Registry bị từ chối trong sandbox; phiên này không cung cấp quyền chạy lệnh ngoài sandbox. Không thay/loại bỏ test để che lỗi.

### Từng mục lỗi (cùng tên và loại lỗi ở cả hai lần)

| Test/container | Lỗi thực tế |
|---|---|
| `NetworkStabilityTest:reconnectCannotBeClosedOrNotifiedByPreviousSession()` | `java.net.SocketTimeoutException: Accept timed out` |
| `NetworkStabilityTest:staleErrorIsDiscardedWhenNewSessionStarts()` | `java.net.SocketTimeoutException: Accept timed out` |
| `NetworkStabilityTest:callbacksAlreadyQueuedAreDiscardedAfterCancel()` | `java.net.SocketTimeoutException: Accept timed out` |
| `NetworkStabilityTest:hostAndClientExchangeMovesRestartAndDisconnect()` | `org.opentest4j.AssertionFailedError: expected: <connected:true> but was: <null>` |
| `NetworkStabilityTest:malformedMoveDoesNotKillConnectionAndOrderIsPreserved()` | `java.net.SocketTimeoutException: Accept timed out` |
| `UILangSoundTest` | `java.lang.SecurityException: Could not open windows registry node Software\JavaSoft\Prefs at root 0xffffffff80000001: Access denied` |
| `BoardVisibilityTest:realModalGameFlowPaintsGridAndStoneInChildJvm(String):[1] 1.0` | `org.opentest4j.AssertionFailedError: Missing board pixels at scale 1.0` |
| `BoardVisibilityTest:realModalGameFlowPaintsGridAndStoneInChildJvm(String):[2] 1.5` | `org.opentest4j.AssertionFailedError: Missing board pixels at scale 1.5` |
| `Task4LifecycleTest:menuClosesRealLanSocketAndAllConnectionDialogs()` | `java.net.SocketTimeoutException: Accept timed out` |
| `Task4PreferencesTest:profileAndLanguageRoundTripThroughInMemoryNode(Language):[1] VI` | `java.lang.SecurityException: Could not open windows registry node Software\JavaSoft\Prefs at root 0xffffffff80000001: Access denied` |
| `Task4PreferencesTest:profileAndLanguageRoundTripThroughInMemoryNode(Language):[2] EN` | `java.lang.SecurityException: Could not open windows registry node Software\JavaSoft\Prefs at root 0xffffffff80000001: Access denied` |
| `Task4PreferencesTest:profileAndLanguageRoundTripThroughInMemoryNode(Language):[3] PT` | `java.lang.SecurityException: Could not open windows registry node Software\JavaSoft\Prefs at root 0xffffffff80000001: Access denied` |
| `P2LifecycleTest:closingDialogCancelsPendingHostAndIgnoresLateResults()` | `java.lang.NullPointerException: Cannot invoke "game.view.Lang$Language.ordinal()" because "game.view.Lang.current" is null` |
| `P2LifecycleTest:successfulDialogDisposalKeepsConnection()` | `java.net.SocketTimeoutException: Accept timed out` |
| `F1ProtocolTest:queuedWireMoveKeepsOldRoundAfterReset()` | `java.net.SocketTimeoutException: Accept timed out` |
| `F1ProtocolTest:garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect()` | `java.net.SocketTimeoutException: Accept timed out` |
| `F1ProtocolTest:simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean):[1] false` | `java.net.SocketException: Permission denied: connect` |
| `F1ProtocolTest:simultaneousSameBaseRestartAndStaleRequestResetOnce(boolean):[2] true` | `java.net.SocketException: Permission denied: connect` |
| `F1ProtocolTest:mismatchedVersionClosesSession()` | `java.net.SocketTimeoutException: Accept timed out` |
| `F1ProtocolTest:startTimeoutClosesSession()` | `java.net.SocketTimeoutException: Accept timed out` |
| `F1ProtocolTest:clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof()` | `java.net.SocketTimeoutException: Accept timed out` |
| `Task4bFlowTest:lanMenuJoinUsesRealSocketAndStartsTheExistingOnlineGame()` | `org.opentest4j.AssertionFailedError: expected: <GAME> but was: <MENU>` |
| `LangPurityTest` | `java.lang.SecurityException: Could not open windows registry node Software\JavaSoft\Prefs at root 0xffffffff80000001: Access denied` |

Phân loại:
- SocketException Permission denied: connect: quyền socket bị chặn.
- SocketTimeoutException và hai assertion connected/GAME: luồng kết nối không thành công trong cùng điều kiện sandbox; nguyên nhân gốc không được sửa trong task này.
- Registry SecurityException: bị từ chối truy cập Windows Preferences.
- Hai BoardVisibilityTest: log child JVM ghi ClassNotFoundException: game.BoardDisplayProbe, tồn tại từ baseline; không kết luận đây là lỗi quyền socket/Registry.
- P2LifecycleTest.closingDialogCancelsPendingHostAndIgnoresLateResults: Lang.current null; setup của test Registry có thể thất bại trước khi lưu origLang, sau đó cleanup gọi Lang.set(null). Đây là lỗi nền/cascade đã có trước sửa, không chỉnh trong task này.
- Hai container UILangSoundTest/LangPurityTest thất bại ở setup; 12 test con không chạy ở cả hai lần. XML có thể ghi error cho các test con này; số đếm trong bảng theo summary console.

### 12 test chưa chạy vì container setup lỗi (Registry)

- UILangSoundTest.c3_menuAndSettingsUpdateImmediately
- UILangSoundTest.d2_inMemoryNodeIsDifferentFromProduction
- UILangSoundTest.b2_settingsSoundToggle
- UILangSoundTest.d1_appSettingsRoundTrip
- UILangSoundTest.c2_sharedButtonPaintAndContrast
- UILangSoundTest.b3_settingsSoundTooltipByLang
- UILangSoundTest.i2_speakerIconVisual
- UILangSoundTest.c1_langAllKeysNotNullNotEmpty
- UILangSoundTest.c1b_connectingMsgContainsIp
- LangPurityTest.b_enAsciiOnly
- LangPurityTest.a_ptNoVietnameseChars
- LangPurityTest.c_sharedThemeContrast

## Ảnh DPI 125%

Thư mục: docs/screenshots/task5e-win-overlay. PNG 967×994 native pixels, frame logic 774×795, scale thực 1.25; không ép uiScale, không chỉnh sửa ảnh. Đã xem trực tiếp cả 7 ảnh.

| Ảnh | Nội dung |
|---|---|
| 01_vi_horizontal_win.png | Ngọc Ánh thắng ngang ở nửa trên, dải dưới; bàn gỗ/quân cổ điển. |
| 02_en_win.png | Oliver WINS, dãy ở dưới, dải trên; bàn xám/quân kính. |
| 03_vi_draw.png | HÒA CỜ, dải dưới; bàn ngọc/quân kim loại. |
| 04_vi_ai_wins.png | Máy (Trung Bình) thắng, dải trên, đúng quân trắng kiểu kính. |
| 05_pt_long_name.png | Nhãn PT và tên dài. |
| 06_view_board_restore_button.png | Dải đã ẩn, có nút Hiện kết quả. |
| 07_compact_overline.png | Dãy chéo 9 quân, thông báo thu gọn ở góc dưới trái; toàn bộ viền đỏ còn thấy. |

Đây là fixture kết quả xác định qua executeMoveForTest và callback sản phẩm thật; ảnh thua AI không chứng minh một trận AI tìm kiếm thực tế. Công cụ dùng Robot.createMultiResolutionScreenCapture. Kiểm tra từng vòng đỏ trên ảnh native: **356–362 pixel đỏ/quân**, không quân thắng nào bị che. Log: build/task5e-win-overlay/screenshots.log.

Chạy lại công cụ sau khi biên dịch sản phẩm:

```powershell
& .\build.ps1
& javac -encoding UTF-8 -cp bin -d build/task5e-win-overlay/capture tools/screenshots/CaptureWinOverlay.java
& java '-Djava.awt.headless=false' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -cp 'build/task5e-win-overlay/capture;bin;resources' CaptureWinOverlay
```

## File thay đổi

- src/game/view/BoardPanel.java
- src/game/view/MainFrame.java
- src/game/view/Lang.java
- src/game/view/EndGameOverlay.java (mới)
- src/game/view/TextFit.java (mới)
- test/game/F1ProtocolTest.java
- test/game/Task4LifecycleTest.java
- test/game/Task4bGameplayTest.java
- test/game/P1RegressionTest.java
- test/game/WinOverlayTest.java (mới)
- test/game/view/EndGameOverlayPlacementTest.java (mới)
- tools/screenshots/CaptureWinOverlay.java (mới)
- docs/screenshots/task5e-win-overlay/ (7 PNG mới)
- docs/TASK5E_REPORT.md (báo cáo này)

Log, class và XML nằm trong build/ (được .gitignore). Không đổi rules, AI, ScoreTracker hay giao thức LAN.

## Commit

Commit yêu cầu: `feat: replace end-of-game dialog with in-board overlay`.
**Chưa commit.** Git add bị từ chối trước khi stage bất kỳ file nào:

```text
fatal: Unable to create 'C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/.git/index.lock': Permission denied
```

HEAD vẫn là `b075b58`; không push, không merge. Người dùng có thể tự stage/commit với thông điệp trên.

`git status --short --branch` sau cùng:

```text
## task5e-win-overlay
 M src/game/view/BoardPanel.java
 M src/game/view/Lang.java
 M src/game/view/MainFrame.java
 M test/game/F1ProtocolTest.java
 M test/game/P1RegressionTest.java
 M test/game/Task4LifecycleTest.java
 M test/game/Task4bGameplayTest.java
?? docs/TASK5E_REPORT.md
?? docs/screenshots/task5e-win-overlay/
?? src/game/view/EndGameOverlay.java
?? src/game/view/TextFit.java
?? test/game/WinOverlayTest.java
?? test/game/view/EndGameOverlayPlacementTest.java
?? tools/screenshots/CaptureWinOverlay.java
```

