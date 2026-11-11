# Task 5e — thông báo kết quả trên bàn cờ

Task 5e đã được commit ở **`36d2f98`** (`feat: replace end-of-game dialog with in-board overlay`), xác nhận bằng git log. Bản sửa đường dẫn Unicode cho BoardVisibilityTest bên dưới là phần bổ sung sau commit này.

**Xác nhận cuối cùng trên PowerShell thật ngày 12/11/2026: BoardVisibilityTest 2/2 đạt; JUnit đầy đủ 503/503 đạt, 124/124 container đạt, 0 skipped, 0 aborted, 0 failed.** Đã đọc hai log powershell-real-visibility-fixed.log và powershell-real-full-fixed.log. Người dùng xác nhận VISIBILITY_EXIT_CODE: 0 và FULL_EXIT_CODE: 0 từ console. Tiến trình PowerShell thật: **501/503 trước sửa Unicode → 503/503 sau sửa**. Kết quả sandbox bên dưới được giữ riêng như lịch sử, không thay thế kết quả xác nhận cuối cùng.

## Bước 0 ban đầu (lịch sử)

- Nhánh lúc bắt đầu: `task5e-win-overlay`; cây sạch.
- HEAD: `b075b58` (merge PR #8, chứa Task 5d `6dbeaa5`).
- Không chạy checkout, pull, fetch, stash, reset, clean, force, push hoặc merge.
- JDK 25.0.3, JUnit Console Standalone 1.10.2.
- Mốc trước sửa: **458 phát hiện, 446 chạy, 425 đạt, 21 lỗi, 12 không chạy vì 2 container lỗi**. Exit code 1.
- Baseline chạy trước mọi thay đổi mã nguồn/test. Log: `build/task5e-win-overlay/junit-baseline.log`.

## Lệnh đã dùng ở hai lần chạy sandbox ban đầu (lịch sử)

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

Trong lần triển khai overlay ban đầu, không sửa test lỗi socket, Registry, child JVM hoặc test phụ thuộc trạng thái Lang ngoài danh sách trên. Bản bổ sung sau commit 36d2f98 sửa riêng BoardVisibilityTest theo chẩn đoán Unicode đã xác minh; giữ nguyên mọi assertion.

## Test mới

**45 trường hợp mới** trong WinOverlayTest (39) và EndGameOverlayPlacementTest (6).

Bao phủ: hai người thắng có dấu; tên trắng dùng mặc định ở VI/EN/PT; hòa; Undo ở PVP/PV_AI; hành động trong dải và thanh dưới; callback cũ cả trước/sau khi ván tiếp theo kết thúc; ẩn/hiện lại và đổi ngôn ngữ; tỉ số/reset; AI ba độ khó; 18 tổ hợp bàn/kiểu quân/người thắng với cả ba ngôn ngữ; so từng pixel vùng quân/viền thắng khi hiện và ẩn overlay; dãy chéo 9 quân; Unicode/ellipsis; mọi vị trí của dãy 5–15 quân theo bốn hướng.

## Kết quả sandbox lúc triển khai overlay (lịch sử)

| Lần chạy | Phát hiện | Đã chạy | Đạt | Lỗi test | Không chạy vì container lỗi | Container lỗi | Exit |
|---|---:|---:|---:|---:|---:|---:|---:|
| Trước sửa | 458 | 446 | 425 | 21 | 12 | 2 | 1 |
| Sau sửa | 503 | 491 | 470 | 21 | 12 | 2 | 1 |
| Tập trung (phần mới và các test liên quan) | 76 | 76 | 76 | 0 | 0 | 0 | 0 |

**Bộ đầy đủ trong lần sandbox lịch sử này chưa đạt.** Kết quả xác nhận cuối cùng trên PowerShell thật được ghi riêng ở đầu báo cáo và phần bổ sung Unicode.

Các số liệu trong mục này thuộc lần chạy sandbox lúc triển khai. Sau đó người dùng đã chạy lại bằng PowerShell thật: các file `junit-baseline.log` và `junit-after.log` hiện chứa 503 test chạy, 501 đạt, 2 lỗi BoardVisibilityTest. Không dùng nội dung hiện tại của hai file này làm bằng chứng cho số liệu sandbox lịch sử. Kết quả và log mới của bản sửa Unicode được ghi riêng ở cuối báo cáo.

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
- Hai BoardVisibilityTest: ClassNotFoundException: game.BoardDisplayProbe. Người dùng đã xác minh nguyên nhân là classpath tuyệt đối bị launcher Windows chuyển Unicode theo sun.jnu.encoding=Cp1252 (Năm → N?m); classpath tương đối nạp được lớp. Đây là lỗi truyền đường dẫn, khác với lỗi quyền socket/Registry.
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

**Đã hoàn thành commit Task 5e: `36d2f98` — `feat: replace end-of-game dialog with in-board overlay`.** Cây sạch trên nhánh task5e-win-overlay lúc bắt đầu sửa Unicode. Trạng thái chưa commit được trong báo cáo cũ là lịch sử và đã được thay thế bằng xác nhận này.

Bản sửa Unicode và cập nhật báo cáo hiện chỉ thay đổi hai file: test/game/BoardVisibilityTest.java và docs/TASK5E_REPORT.md. Chưa tạo commit bổ sung, không push, không merge.

## Bổ sung 12/11/2026 — sửa đường dẫn Unicode của BoardVisibilityTest

### Thay đổi và lý do

Windows launcher chuyển classpath tuyệt đối chứa “Năm” thành “N?m” khi sun.jnu.encoding=Cp1252. Vì vậy JVM con không nạp được game.BoardDisplayProbe dù lớp đã biên dịch. Người dùng đã xác minh classpath tương đối nạp được lớp.

- Lấy workingDirectory từ thư mục làm việc, normalize và đặt rõ bằng ProcessBuilder.directory.
- Chuyển cả hai vị trí class (test và sản phẩm) thành đường dẫn tương đối với workingDirectory.
- Giữ folder ảnh/log là bin_test/board-visibility/scale-<scale> tương đối; đối số chuyển sang JVM con không còn chứa thư mục cha Unicode.
- Redirect probe.txt thực hiện ở JVM cha với log path tương đối, cùng thư mục làm việc.
- Hai scale 1.0 và 1.5, timeout, exit code, toàn bộ assertion pixel/số bản ghi và errors=0 giữ nguyên. Đã đối chiếu nguyên khối từ boolean finished tới cuối file với HEAD 36d2f98: giống hệt.
- Chỉ sửa test/game/BoardVisibilityTest.java và báo cáo này; không đổi mã sản phẩm, không thêm/bớt test.

Diff thực tế của test:

```diff
diff --git a/test/game/BoardVisibilityTest.java b/test/game/BoardVisibilityTest.java
index 08f63d3..112b57d 100644
--- a/test/game/BoardVisibilityTest.java
+++ b/test/game/BoardVisibilityTest.java
@@ -12,13 +12,16 @@ import static org.junit.jupiter.api.Assertions.*;
 class BoardVisibilityTest {
     @ParameterizedTest @ValueSource(strings={"1.0","1.5"})
     void realModalGameFlowPaintsGridAndStoneInChildJvm(String scale) throws Exception {
-        Path folder=Path.of("bin_test","board-visibility", "scale-"+scale).toAbsolutePath();
+        Path workingDirectory=Path.of("").toAbsolutePath().normalize();
+        Path folder=Path.of("bin_test","board-visibility", "scale-"+scale);
         Files.createDirectories(folder);Path log=folder.resolve("probe.txt");
-        String classpath=Path.of(BoardDisplayProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI())
-                +File.pathSeparator+Path.of(BoardPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI());
+        // The Windows launcher can decode argv using Cp1252; keep Unicode parent paths out of argv.
+        String classpath=workingDirectory.relativize(Path.of(BoardDisplayProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).normalize())
+                +File.pathSeparator+workingDirectory.relativize(Path.of(BoardPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI()).normalize());
         Process child=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),
                 "-Dsun.java2d.uiScale="+scale,"-Dstdout.encoding=UTF-8","-cp",classpath,
-                "game.BoardDisplayProbe",folder.toString()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
+                "game.BoardDisplayProbe",folder.toString()).directory(workingDirectory.toFile())
+                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
         boolean finished=child.waitFor(45,TimeUnit.SECONDS);
         if(!finished) { child.destroyForcibly(); child.waitFor(5,TimeUnit.SECONDS); }
         String evidence=Files.readString(log);
```

### Kết quả phân biệt theo môi trường

| Môi trường/lần chạy | Phát hiện | Đã chạy | Đạt | Lỗi test | Chưa chạy do setup | Container lỗi | Exit |
|---|---:|---:|---:|---:|---:|---:|---:|
| PowerShell thật trước sửa Unicode, trên Task 5e 36d2f98 | 503 | 503 | 501 | 2 | 0 | 0 | 1 |
| Sandbox sau sửa, chạy riêng BoardVisibilityTest trước | 2 | 2 | 2 | 0 | 0 | 0 | 0 |
| Sandbox sau sửa, chạy đầy đủ sau đó | 503 | 491 | 472 | 19 | 12 | 2 | 1 |
| PowerShell thật sau sửa Unicode, riêng BoardVisibilityTest | 2 | 2 | 2 | 0 | 0 | 0 | 0 |
| PowerShell thật sau sửa Unicode, JUnit đầy đủ — xác nhận cuối cùng | 503 | 503 | 503 | 0 | 0 | 0 | 0 |

Log PowerShell thật trước sửa đã được đọc lại và giữ riêng tại build/task5e-win-overlay/powershell-real-before-visibility-fix.log. Hai lỗi trong log này đều là BoardVisibilityTest scale 1.0 và 1.5.

Log PowerShell thật sau sửa đã đọc ngày 12/11/2026:

- build/task5e-win-overlay/powershell-real-visibility-fixed.log: 2 test phát hiện/chạy/đạt; 5/5 container đạt; skipped, aborted và failed đều 0; thời gian 9554 ms.
- build/task5e-win-overlay/powershell-real-full-fixed.log: 503 test phát hiện/chạy/đạt; 124/124 container đạt; skipped, aborted và failed đều 0; thời gian 58655 ms.
- Hai exit code 0 được người dùng xác nhận từ console PowerShell; các dòng VISIBILITY_EXIT_CODE/FULL_EXIT_CODE được in sau Tee-Object nên không nằm trong hai log JUnit này.

Nguyên văn summary JUnit đầy đủ trong log PowerShell thật sau sửa:

```text
[       124 containers found      ]
[         0 containers skipped    ]
[       124 containers started    ]
[         0 containers aborted    ]
[       124 containers successful ]
[         0 containers failed     ]
[       503 tests found           ]
[         0 tests skipped         ]
[       503 tests started         ]
[         0 tests aborted         ]
[       503 tests successful      ]
[         0 tests failed          ]
```

Log sandbox mới:
- build/task5e-win-overlay/junit-visibility-fix.log
- build/task5e-win-overlay/junit-full-after-visibility-fix.log
- build/task5e-win-overlay/failures-after-visibility-fix.json
- build/task5e-win-overlay/visibility-fix-run.txt: vị trí class, XML và probe/ảnh của lần chạy.

Thứ tự thực hiện trong sandbox: biên dịch mới trong bản sao build/task5e-win-overlay/visibility-fix-*, chạy riêng BoardVisibilityTest rồi mới chạy JUnit --scan-class-path trên cùng bộ class. Cả hai lần đều dùng --class-path 'bin_test;bin;src', headless=false, UTF-8 cho stdout/stderr; có XML báo cáo riêng.

Kết quả probe ở **cả hai scale**:
- Child JVM exit 0.
- 8 ảnh PNG/scale.
- 8 bản ghi PIXELS có grid=true/scale.
- 4 bản ghi blackStone=true expectedStone=true/scale.
- RESULT pixelFailure=false errors=0.
- URL loadedBoard vẫn trỏ đúng tới thư mục gốc Unicode được percent-encode, chứng minh lớp được nạp đúng.

Trong sandbox, hai trường hợp BoardVisibilityTest cũng **PASS trong bộ đầy đủ**, xác nhận từ XML. So sánh với danh sách lỗi sandbox lịch sử: đúng hai lỗi BoardVisibilityTest đã biến mất; 19 test lỗi và 2 container lỗi còn lại giữ nguyên tên/loại lỗi, không có lỗi mới. Các lỗi socket/Registry và lỗi Lang.current null phát sinh sau setup thất bại vẫn giữ nguyên trong sandbox, không sửa trong bản bổ sung này. Từng tên/loại lỗi còn lại có trong failures-after-visibility-fix.json và chính bảng lịch sử ở trên (bỏ hai dòng BoardVisibilityTest). Chúng không xuất hiện trong lần PowerShell thật sau sửa: toàn bộ 503 test và 124 container đều đạt.

**Đã xác nhận mục tiêu cuối cùng: 503 started, 503 successful, 0 skipped, 0 aborted, 0 failed; 124/124 container đạt trên PowerShell thật, FULL_EXIT_CODE: 0.** Không đồng nhất kết quả này với lần chạy sandbox. Lần cập nhật báo cáo cuối cùng chỉ đọc hai log, không chạy lại test và không sửa mã nguồn/test đã kiểm chứng.

### Lệnh PowerShell đã dùng để xác nhận (giữ để chạy lại khi cần)

Dán tại thư mục Gomoku. Khối này tự biên dịch trong thư mục build mới, chạy riêng BoardVisibilityTest trước rồi chạy đầy đủ. Giữ log và XML/ảnh mới; không ghi đè log baseline/after cũ hoặc ảnh đang được Git theo dõi.

```powershell
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$task5eOut = Join-Path (Get-Location).Path 'build/task5e-win-overlay'
$task5eRun = Join-Path $task5eOut ('powershell-visibility-fix-' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $task5eRun -Force | Out-Null
foreach ($item in @('src','test','resources','lib','build.ps1')) {
    Copy-Item -LiteralPath $item -Destination $task5eRun -Recurse -Force
}
$common = @(
    '-Djava.awt.headless=false', '-Dfile.encoding=UTF-8',
    '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
    '-jar', 'lib/junit-platform-console-standalone-1.10.2.jar', 'execute',
    '--class-path', 'bin_test;bin;src', '--details', 'summary',
    '--disable-banner', '--disable-ansi-colors', '--fail-if-no-tests'
)
Push-Location -LiteralPath $task5eRun
try {
    & .\build.ps1
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & java @common --select-class game.BoardVisibilityTest --reports-dir junit-visibility-xml 2>&1 |
        Tee-Object -FilePath (Join-Path $task5eOut 'powershell-real-visibility-fixed.log')
    Write-Output ('VISIBILITY_EXIT_CODE: ' + $LASTEXITCODE)
    & java @common --scan-class-path --reports-dir junit-full-xml 2>&1 |
        Tee-Object -FilePath (Join-Path $task5eOut 'powershell-real-full-fixed.log')
    Write-Output ('FULL_EXIT_CODE: ' + $LASTEXITCODE)
    Write-Output ('XML_AND_IMAGES: ' + $task5eRun)
} finally {
    Pop-Location
}
```

### Trạng thái sau bản sửa Unicode

```text
## task5e-win-overlay
 M docs/TASK5E_REPORT.md
 M test/game/BoardVisibilityTest.java
```

HEAD: 36d2f98. Task 5e đã commit hoàn thành; bản sửa Unicode này chưa commit bổ sung, không push, không merge.

