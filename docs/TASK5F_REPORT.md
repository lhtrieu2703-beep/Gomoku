# Task 5f — Sóng sáng dọc dãy thắng và một đợt hạt vàng

Ngày báo cáo: 12/11/2026.

Đã hoàn tất hiệu ứng và kiểm tra hồi quy. **JUnit đầy đủ trong sandbox chưa đạt**: trước sửa 486/491 test đã chạy đạt; sau sửa 514/519 đạt. Cả 503 test cũ giữ nguyên trạng thái, 28 test mới đều đạt. Hai lần cùng có 5 test lỗi và 2 container lỗi, khiến 12 test không chạy. Kiểm tra tập trung đạt 85/85. Chưa có kết quả PowerShell ngoài sandbox cho Task 5f.

## Nền mã và giới hạn Git

- Khi bắt đầu đã xác nhận `main` GitHub chứa Task 5e: merge PR #9 tại `5671979`, gồm overlay `36d2f98` và bản sửa Unicode `08f75a6`.
- `main` cục bộ vẫn ở `b075b58`. Nhánh làm việc hiện tại là `task5e-win-overlay`, HEAD `08f75a6d1a6251b10995b52ef812efc560777bbc`; cây sạch trước khi triển khai.
- Cây mã của HEAD có hash `a6b7c4e4d8f4138a6bece0876b57ae4cfbe8c2d2`, đã đối chiếu trùng cây mã của `main` GitHub sau merge Task 5e. Vì vậy nội dung nền đã có Task 5e và bản sửa Unicode, dù tên nhánh chưa đúng yêu cầu.
- Thao tác cập nhật/tạo nhánh bị chặn bởi quyền ghi `.git`. Đã báo việc này trước khi sửa. **Chưa tạo được `task5f-win-wave`; toàn bộ thay đổi còn trên cây làm việc của `task5e-win-overlay`.** Người dùng cần tạo nhánh đúng nền bằng PowerShell thật trước khi commit.
- Không chạy `git add`, `git commit`, `git push` cho Task 5f. Không reset, clean, stash hoặc hoàn tác thay đổi.

## Cách cài đặt

`BoardPanel` lấy nguyên `winningLine` do `Board.findWinningLine` trả về sau khi bộ luật xác nhận thắng. `WinWave` chạy trên toàn bộ danh sách theo thứ tự sẵn có, không cắt còn 5 quân và không tự tính lại luật thắng. Hòa không khởi động hiệu ứng; cả người chơi và AI thắng đều khởi động, dùng cùng màu vàng.

Mỗi `BoardPanel` sở hữu một `WinWave` và **một `javax.swing.Timer` tạo một lần**, chu kỳ 16 ms, bật coalescing. Start, tick và stop đều chạy trên EDT. Đồng hồ đơn điệu được tiêm qua `LongSupplier` đang có của bàn cờ. `paint` chỉ vẽ, không cập nhật thời gian, sinh hạt hoặc tạo/start timer. Không thêm thư viện hay `Thread.sleep`.

Trong 3 giây, tâm sáng đi từ trước quân đầu đến sau quân cuối; cường độ của từng quân giảm theo khoảng cách với tâm sóng và dịu về 0. Hình sáng dùng gradient xuyên tâm. Khi đủ 3.000.000.000 ns, timer dừng, danh sách hạt và đường hiệu ứng được xóa, bàn trở về ảnh tĩnh như trước khi chạy hiệu ứng.

Hạt tái dùng **`TitleParticles.Particle` của Task 5c**, gồm kích thước, nhấp nháy và tính opacity. Bổ sung constructor/factory cho đợt hạt hữu hạn và `advanceBurst`, không thay đường constructor/advance/respawn của Tiêu đề. Bảng màu vàng cũ chuyển từ `StartScreen` sang `TitleParticles.color` để hai nơi dùng chung; giá trị và thứ tự màu của Tiêu đề giữ nguyên. Mỗi quân phát 8 hạt cùng một đợt khi bắt đầu, có vận tốc, trọng lực và thời gian sống 1,35–2,15 giây; không tái sinh hay phát thêm đợt mới.

Thứ tự vẽ là bàn/quân → sóng/hạt → viền thắng đỏ/trắng → các component con (overlay/nút hiện kết quả). Ngoài việc vẽ dưới component con, vùng clip còn loại toàn bộ hình chữ nhật overlay hoặc nút hiện kết quả đang mở. Vì vậy hạt không làm thay đổi cả nền bán trong suốt lẫn chữ của overlay. Không sửa vị trí/kích thước overlay hoặc layout bàn cờ.

Undo/reset đi qua `clearResult`; Về menu/đóng cửa sổ vô hiệu hóa bàn; `removeNotify` cũng dừng hiệu ứng. Nút Ván Mới dừng ngay cả khi đang yêu cầu khởi động lại qua LAN. Stop xóa hạt, dừng timer và hủy các lệnh start đã xếp hàng bằng epoch/gameId. Tick đã xếp hàng sau stop không có tác dụng. Đổi ngôn ngữ, ẩn hoặc hiện lại overlay không stop/start hiệu ứng.

Các thao tác giao diện thực chạy trên EDT. Để giữ nguyên các test mô hình cũ gọi `BoardPanel` ngoài EDT mà không có cửa sổ, trường hợp đó không khởi động animation; bàn đang hiển thị sẽ marshal start sang EDT. Đây chỉ là cách tương thích với cách gọi cũ, không thay hành vi của bàn hiển thị trong app.

## Ràng buộc được giữ nguyên

- Không sửa `Board.java` hay logic RULE_B: 5+ quân thắng, chỉ vô hiệu khi bị chặn kín hai đầu. SHA-256 trước/sau của `Board.java` đều là `22FC2404698A6E689AE75420E278F34CF71A694B1783255691207A04696854CF`.
- Undo sau thắng vẫn thu hồi đúng điểm về 0, xóa overlay, dãy thắng và hiệu ứng; có test cho cả người chơi và AI thắng.
- Không thay cơ chế tính/lưu điểm hoặc cấu hình giao diện. Tỉ số và giao diện bàn/quân vẫn chỉ theo phiên như trước.
- Không đổi kích thước/layout: khung 774×795, DPI thực 125% trong ảnh.
- **Không sửa bất kỳ test cũ nào.** 43 file Java test có sẵn có SHA-256 trùng mốc trước sửa, bao gồm các test của Task 5e và các test ban đầu. Không có phương thức test cũ cần liệt kê ngoại lệ.

## File sửa và file mới

| Loại | File | Nội dung |
|---|---|---|
| Sửa | `src/game/view/BoardPanel.java` | Sở hữu hiệu ứng; start khi thắng; dừng theo lifecycle; vẽ với clip an toàn cho overlay. |
| Sửa | `src/game/view/MainFrame.java` | Dừng hiệu ứng ngay khi nhấn Ván Mới, kể cả chờ restart LAN. |
| Sửa | `src/game/view/TitleParticles.java` | Tái dùng model Particle cho đợt hạt hữu hạn; chia sẻ bảng màu vàng. |
| Sửa | `src/game/view/StartScreen.java` | Đọc bảng màu từ TitleParticles; không đổi màu/hành vi Tiêu đề. |
| Mới | `src/game/view/WinWave.java` | Một timer EDT, sóng sáng 3 giây và một đợt hạt. |
| Mới | `test/game/view/WinWaveTest.java` | 28 trường hợp JUnit mới. |
| Mới | `tools/screenshots/CaptureWinWave.java` | Chụp các pha từ production tick ở DPI thực 125%. |
| Mới | `docs/screenshots/task5f-win-wave/*.png` | 9 ảnh được liệt kê bên dưới. |
| Mới | `docs/TASK5F_REPORT.md` | Báo cáo này. |

## Test mới

28 trường hợp trong `WinWaveTest`, đều đạt ở bộ đầy đủ:

- Khởi động cho X/O ở PVP, PV_AI và ONLINE; AI thắng cũng có hiệu ứng vàng.
- Hòa không khởi động; dãy 5 quân bị chặn hai đầu không khởi động theo RULE_B.
- Sóng đi qua mọi quân theo thứ tự với dãy 5, 9 và 15 quân.
- Đồng hồ giả: còn chạy ở mốc 3 giây trừ 1 ns, dừng đúng 3 giây; tick cũ không khởi động lại. Không sleep thật.
- Một đợt hạt dùng Particle cũ, số hạt chỉ giảm và hết trước khi kết thúc sóng.
- Undo, Ván Mới/Về menu từ overlay và thanh dưới, dispose, removeNotify: dừng timer và xóa hạt ngay.
- 100 ván liên tiếp tái dùng cùng một timer, một listener, không tích lũy hạt/timer chạy sau khi kết thúc mỗi vòng.
- Undo trong PV_AI thu hồi đúng điểm của cả hai bên.
- Repaint không tiến thời gian, không tạo/start lại timer.
- Đổi VI/EN/PT và ẩn/hiện overlay giữ nguyên thời điểm bắt đầu, tiến độ, timer và số hạt.
- Pixel overlay và bounds giữ nguyên trong lúc sóng chạy, khung giữ 774×795. Sau 3 giây, pixel bàn bằng ảnh tĩnh ban đầu, viền đỏ/trắng còn nguyên.
- Cập nhật trạng thái ngoài EDT bị từ chối.

## Kết quả JUnit thực tế trong sandbox

JDK Microsoft OpenJDK 25.0.3+9-LTS, JUnit Console Standalone 1.10.2. Mỗi lần chạy đầy đủ dùng bản sao source và thư mục class mới trong build, tránh ghi đè ảnh do các test cũ tạo vào file đang được Git theo dõi.

| Lần chạy | Phát hiện | Đã chạy | Đạt | Lỗi test | Không chạy vì container lỗi | Container đạt/tổng | Container lỗi |
|---|---:|---:|---:|---:|---:|---:|---:|
| Trước sửa | 503 | 491 | 486 | 5 | 12 | 122/124 | 2 |
| Sau sửa, lần hoàn tất | 531 | 519 | 514 | 5 | 12 | 127/129 | 2 |
| Tập trung | 85 | 85 | 85 | 0 | 0 | 21/21 | 0 |

Hai lần đầy đủ đều 0 skipped và 0 aborted theo summary JUnit. 12 test không chạy là hậu quả setup container lỗi; **không coi chúng là đã đạt**. XML gắn lỗi setup vào các testcase con nên tổng `errors=17` trong XML tương ứng 5 test lỗi + 12 test không được chạy; dùng summary console để phân biệt test/container.

- Baseline: `build/task5f-win-wave/junit-baseline.log`; XML: `build/task5f-win-wave/baseline-20261112-060510/junit-xml/`.
- Sau sửa hoàn tất: `build/task5f-win-wave/junit-after.log`; XML: `build/task5f-win-wave/after-20261112-062141/junit-xml/`.
- Tập trung: `build/task5f-win-wave/junit-focused.log` (28 test mới cùng các kiểm tra cũ về TitleParticles, overlay, màu bàn/quân).
- Đối chiếu: `comparison.json`, `failures-baseline.json`, `failures-after.json`, `new-tests.json` trong cùng thư mục build. **503 test cũ: 0 thay đổi trạng thái; 28 test mới: 0 lỗi; 7 mục lỗi (5 test + 2 container): 0 thay đổi tên/loại lỗi.** Không có lỗi mới ở BoardPanel/MainFrame/WinWave/TitleParticles/overlay.
- Kiểm tra cuối: 6 file source/test mới hoặc sửa trùng SHA-256 với bản sao đã chạy JUnit sau sửa; không có thay đổi mã sau lần chạy hoàn tất. `git diff --check` exit code 0. Hai khối PowerShell trong báo cáo đều được parser PowerShell kiểm tra, không có lỗi cú pháp.

### Danh sách lỗi giống hệt trước/sau

| Test/container | Loại lỗi trước và sau |
|---|---|
| `Task4PreferencesTest.profileAndLanguageRoundTripThroughInMemoryNode` — `[1] VI` | `SecurityException`: Windows Registry Preferences `Access denied`. |
| Cùng phương thức — `[2] EN` | Cùng lỗi Registry; cleanup còn có lỗi phụ `path is null` đã có ở baseline. |
| Cùng phương thức — `[3] PT` | Cùng lỗi Registry; cleanup còn có lỗi phụ `path is null` đã có ở baseline. |
| `P2LifecycleTest.closingDialogCancelsPendingHostAndIgnoresLateResults` | `NullPointerException`: `Lang.current == null` tại `Lang.networkDialogTitle`. |
| `P2LifecycleTest.successfulDialogDisposalKeepsConnection` | Cùng lỗi `Lang.current == null`. |
| Container `UILangSoundTest` | `SecurityException`: Registry Preferences `Access denied`; test con không chạy. |
| Container `LangPurityTest` | Cùng lỗi Registry; test con không chạy. |

Lỗi Registry là hạn chế quyền của sandbox. Hai lỗi `Lang.current == null` là lỗi có sẵn ở baseline, không kết luận là lỗi môi trường chỉ vì đã tồn tại. Không sửa hoặc bỏ qua các test này. Các test socket và child JVM/BoardVisibility không lỗi trong cả hai lần chạy Task 5f hoàn tất; không mang danh sách lỗi của các phiên Task 5e cũ sang kết quả hiện tại.

### Lần JVM bị dừng giữa chừng

Lần sau sửa đầu tiên (`after-20261112-061857`) bị native crash `EXCEPTION_INVALID_HANDLE (0xc0000008)` trong `ntdll.dll`, trước summary JUnit. Log riêng: `build/task5f-win-wave/junit-after-native-crash.log`; đường dẫn run lưu ở `native-crash-run.txt`. Đây là lần chạy không hoàn tất, không được tính là pass hay dùng thay bảng trên. Đã chạy lại trên bộ class mới và lấy kết quả hoàn tất ở `after-20261112-062141`. Không thay mã để né native crash.

## Lệnh PowerShell chạy JUnit

Lệnh JUnit thực tế ở cả baseline và lần sau sửa hoàn tất (classpath tương đối được xác nhận qua `sun.java.command` trong XML):

```powershell
& java '-Djava.awt.headless=false' '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path 'bin_test;bin;src' --scan-class-path --reports-dir junit-xml --details summary --disable-banner --disable-ansi-colors --fail-if-no-tests
```

Dán nguyên khối dưới đây vào PowerShell thật tại thư mục Gomoku để build mới, chạy riêng 28 test mới rồi chạy toàn bộ. Khối này không sửa source, test hoặc Git; các log xác nhận ngoài sandbox tách riêng, không ghi đè baseline/sandbox. Mục tiêu lần đầy đủ ngoài sandbox là **531/531**, 0 lỗi; hiện chưa xác nhận kết quả này.

```powershell
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
[Console]::OutputEncoding = [Text.UTF8Encoding]::new()
$task5fRoot = (Get-Location).Path
$task5fOut = Join-Path $task5fRoot 'build/task5f-win-wave'
$task5fRun = Join-Path $task5fOut ('powershell-real-' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $task5fRun -Force | Out-Null
foreach ($item in @('src','test','resources','lib','build.ps1')) {
    Copy-Item -LiteralPath (Join-Path $task5fRoot $item) -Destination $task5fRun -Recurse -Force
}
$task5fCommon = @(
    '-Djava.awt.headless=false', '-Dfile.encoding=UTF-8',
    '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
    '-jar', 'lib/junit-platform-console-standalone-1.10.2.jar', 'execute',
    '--class-path', 'bin_test;bin;src', '--details', 'summary',
    '--disable-banner', '--disable-ansi-colors', '--fail-if-no-tests'
)
Push-Location -LiteralPath $task5fRun
try {
    & .\build.ps1
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & java @task5fCommon --select-class game.view.WinWaveTest --reports-dir junit-wave-xml 2>&1 |
        Tee-Object -FilePath (Join-Path $task5fOut 'powershell-real-wave.log')
    $task5fWaveExit = $LASTEXITCODE
    Write-Output ('WAVE_EXIT_CODE: ' + $task5fWaveExit) |
        Tee-Object -FilePath (Join-Path $task5fOut 'powershell-real-wave.log') -Append
    & java @task5fCommon --scan-class-path --reports-dir junit-xml 2>&1 |
        Tee-Object -FilePath (Join-Path $task5fOut 'powershell-real-full.log')
    $task5fFullExit = $LASTEXITCODE
    Write-Output ('FULL_EXIT_CODE: ' + $task5fFullExit) |
        Tee-Object -FilePath (Join-Path $task5fOut 'powershell-real-full.log') -Append
    Write-Output ('XML_AND_IMAGES: ' + $task5fRun)
} finally {
    Pop-Location
}
```

Để tái lập baseline, phải dùng mã nền trước Task 5f; chạy trên cây hiện tại sẽ có 531 test. Không tạo lại/ghi đè baseline sau khi sửa.

## Ảnh DPI thực 125%

9 PNG trong `docs/screenshots/task5f-win-wave`, chụp cửa sổ thật bằng `Robot.createMultiResolutionScreenCapture`, không ép `sun.java2d.uiScale`. Khung Swing 774×795, ảnh native 967×994. Helper dùng cùng `WinWave.tick(long)` của sản phẩm, tạm dừng timer chỉ trong công cụ chụp để lấy pha ổn định; log `timer=false` ở ảnh đang sáng là chủ ý này, không phải trạng thái timer trong app. Đồng hồ/tick giả giúp chụp đúng pha; ảnh không chứng minh độ mượt của một lần chạy 3 giây liên tục.

| Ảnh | Pha/nội dung |
|---|---|
| `01_horizontal_wave_start.png` | 0,5 giây, sáng ở quân đầu dãy ngang. |
| `02_horizontal_wave_middle.png` | 1,5 giây, sáng ở giữa dãy ngang. |
| `03_horizontal_wave_end.png` | 2,5 giây, sáng ở quân cuối dãy ngang. |
| `04_gold_burst.png` | 0,22 giây, một đợt hạt vàng từ dãy thắng. |
| `05_after_stop_static.png` | Đã tick đủ 3 giây: inactive, timer dừng, 0 hạt, viền thắng vẫn nguyên. |
| `06_vertical_wave.png` | Dãy dọc; bàn Charcoal Stone, quân Glass. |
| `07_diagonal_wave.png` | Dãy chéo; bàn Dark Jade, quân Metallic. |
| `08_nine_stone_overline.png` | Dãy chéo 9 quân, overlay góc giữ nguyên, không che dãy. |
| `09_ai_winner_wave.png` | AI thắng, hiệu ứng vẫn chạy; overlay ở nửa trên. |

Log `build/task5f-win-wave/screenshots.log` xác nhận `NATIVE_DPI 1.25 FRAME 774x795 FORCE_SCALE none`, `CAPTURED 9 ERRORS 0`. Công cụ kiểm tra pixel đỏ #E53935 quanh **từng** quân thắng của mọi ảnh, kể cả dãy 9 quân. Đã xem lại cả 9 ảnh: đầu/giữa/cuối sáng đúng vị trí, viền đỏ còn rõ, chữ overlay không bị hạt che. Test JUnit còn so toàn bộ vùng overlay và ảnh bàn tĩnh sau khi dừng.

## Cần kiểm tay và các lựa chọn triển khai

1. Chạy khối PowerShell thật để xác nhận 531/531, vì sandbox vẫn chặn Registry; giữ riêng log thật và log sandbox.
2. Chơi một ván thực để đánh giá độ mượt/độ sáng trong 3 giây liên tục, nhất là quân trắng, bàn sáng và dãy sát mép. Ảnh và đồng hồ giả xác nhận pha/hình học/lifecycle, chưa thay thế đánh giá độ mượt bằng mắt.
3. Trong lúc hiệu ứng đang chạy, bấm Đi Lại/Ván Mới/Về menu từ cả thanh dưới lẫn overlay, đóng cửa sổ, đổi VI/EN/PT và ẩn/hiện lại kết quả; kiểm tra Tiêu đề vẫn như Task 5c.
4. Chuẩn bị nhánh `task5f-win-wave` đúng nền `main` đã merge Task 5e bằng PowerShell thật trước khi commit. Bản làm việc hiện chưa có nhánh đó; chưa add/commit/push.

Các thông số người dùng chưa chỉ định được chọn như sau: timer 16 ms, 8 hạt/quân, hạt sống 1,35–2,15 giây và sóng đủ 3 giây; thứ tự sóng lấy nguyên `winningLine`; AI dùng vàng vì Task 5c không có bảng màu AI riêng. Dãy dài vẫn dùng toàn bộ danh sách. Ngoài giới hạn tạo nhánh và xác nhận bộ đầy đủ ngoài sandbox nêu trên, không có thay đổi yêu cầu về luật, điểm, ngôn ngữ hoặc layout.
