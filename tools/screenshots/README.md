# Chụp cửa sổ thật của Gomoku bằng Robot

Chạy tại gốc dự án bằng một lệnh (JDK có `java` và `javac` trong PATH):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/screenshots/run-screenshots.ps1
```

Công cụ chỉ có một tệp nguồn Java độc lập `CaptureScreenshots.java` (các helper là lớp lồng trong cùng tệp). Script biên dịch nguyên nguồn sản phẩm và công cụ vào `tools/screenshots/runs/<lượt>/classes`, rồi khởi động đúng một JVM có giao diện. Không biên dịch hoặc chạy test. Không sửa `game.*`, không dùng ảnh dựng lại, `printAll`, canvas, hoặc ImageGen. PNG lấy từ native-resolution variant của `Robot.createMultiResolutionScreenCapture(window.getBounds())`, gồm thanh tiêu đề. Trên Windows chỉ cắt phần resize vô hình ở hai bên/phía dưới theo insets và transform; không scale lại, vẽ lại, sửa chữ hay màu pixel.

Lượt 2026-11-03 khởi chạy Java đúng một lần, trước khi bổ sung bước cắt viền này. Khi xem bảy ảnh gốc, dải ngoài cửa sổ được đo từ raster là 8 px ở trái/phải/dưới. Đã cắt lossless trực tiếp các PNG của chính lượt đó, không khởi chạy hay chụp lại ứng dụng. Hash trước/sau và tọa độ crop được lưu trong `runs/<lượt>/crop-audit.json`; metadata native gốc `captures.tsv` được giữ nguyên, kích thước cuối trong `captures-final.tsv`. Phần cắt tự động được bổ sung vào mã công cụ và chỉ biên dịch kiểm tra sau lượt chụp; không tuyên bố đã chạy lại phiên GUI với phiên bản sau bổ sung.

Mặc định ảnh được ghi vào `docs/screenshots/` với bảy tên được yêu cầu. Không ghi đè ảnh tồn tại. Để tái hiện trong một phiên tương lai mà giữ nguyên bằng chứng, chọn thư mục mới dưới cùng gốc:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/screenshots/run-screenshots.ps1 -OutputDirectory docs/screenshots/next-run
```

Lượt hiện tại chỉ chạy một lần. Kết quả/thông số, lỗi và SHA-256 nguồn lưu dưới `tools/screenshots/runs/`; `docs/screenshots/` chỉ chứa ảnh. Nếu một màn không mở/chụp được mà phải sửa sản phẩm, kết quả ghi **KHÔNG CHỤP ĐƯỢC** cùng lý do; công cụ không tạo ảnh thay thế. Luồng chính có watchdog 60 giây. Không đổi scale màn hình, không truyền `sun.java2d.uiScale`, không thay kích thước/pack lại cửa sổ. Chỉ dịch vị trí cửa sổ vào vùng làm việc nếu nó vốn vừa vùng đó; tạm đặt luôn trên cùng để giảm che khuất. Luôn đóng phiên ứng dụng riêng sau chụp.

## Kiểm kê công cụ/ảnh cũ

- `docs/task4b-render/` hiện có 18 PNG: title/menu/game/profile/settings/rules cho VI/EN/PT. `test/game/Task4bPresentationTest.java#render` gọi `content.printAll` vào BufferedImage theo transform. Những ảnh này không phải ảnh desktop Robot và không có thanh tiêu đề, không dùng lại làm đầu ra mới.
- `tools/board/run-board-probe.ps1` tạo bản sao ngoài repo, build rồi chạy bốn JVM ép scale 1.0/1.25/1.5/2.0. `test/game/BoardDisplayProbe.java` đi qua nút menu, hộp modal, click/nước AI và New Game; đo bounds/paint/pixel, nhưng ảnh cũng là `content.printAll`. Có thể tham khảo luồng mở và chờ EDT, không tái sử dụng script vì nó chạy nhiều lần/ép scale và dùng tên mẫu khác.
- `tools/board/BoardPaintAgent.java` đếm `paintComponent` bằng instrumentation trong bộ nhớ. Không cần agent cho ảnh desktop; công cụ mới không instrument lớp sản phẩm.

## Cách mở từng màn — lấy từ mã nguồn thật

| Màn | Constructor/luồng sản phẩm hiện có | Công cụ gọi |
|---|---|---|
| TITLE | `MainFrame(AppSettings)` gọi `showScreen(Screen.TITLE)`, `pack()` rồi `setLocationRelativeTo(null)` | `new MainFrame(settings)`, `setVisible(true)` |
| MENU | `StartScreen.advance()` gọi callback `MainFrame.showMenu()` | `frame.getStartScreen().advance()` |
| SetupDialog | nút AI của MainMenu → `MainFrame.showSetup(true)` → `new SetupDialog(MainFrame, boolean)` | `getAiButton().doClick(0)`; boolean thực tế là true |
| BOARD | nút Start của SetupDialog → `owner.beginGame(BoardPanel.GameMode.PV_AI, selectedDifficulty)` | `getStartButton().doClick(0)`; giữ MEDIUM mặc định |
| SettingsDialog | nút Settings → `MainFrame.showSettings()` → `new SettingsDialog(MainFrame)` | `frame.getSettingsButton().doClick(0)` |
| RulesDialog | nút Rules → `MainFrame.showRules()` → `new RulesDialog(MainFrame)` | `frame.getRulesButton().doClick(0)` |
| NetworkDialog | nút LAN của MainMenu → `MainFrame.openNetworkDialog()` → `new NetworkDialog(JFrame, NetworkManager, Runnable)` | `frame.returnToMenu()`, `getLanButton().doClick(0)`; dùng NetworkManager sẵn của frame |

Setup/Settings/Rules kế thừa `LocalizedDialog`, gọi `super(owner, true)`. NetworkDialog gọi `super(parent, title, true)`. Đây đều là modal. Công cụ dùng `SwingUtilities.invokeLater` để bấm nút mở trên EDT; vòng sự kiện modal vẫn xử lý các yêu cầu EDT. Luồng chính poll `getOwnedWindows/isShowing`, chờ vẽ/activation, gọi `Robot.waitForIdle` và chụp; không ngủ hoặc chụp trên EDT. Đóng/bấm Start/đọc trạng thái Swing đều qua EDT.

BOARD không bơm quân trực tiếp vào model và không dùng `executeMoveForTest`. Mỗi lượt gọi `BoardPanel.playMove(row,col)`; nếu ô đã bị máy chiếm thì thử ứng viên kế tiếp. Chờ SwingWorker và độ trễ mặc định 800 ms hoàn tất trước lượt tiếp. Bốn lượt người + bốn lượt AI tạo tám quân, chưa bên nào đủ năm quân để phát sinh hộp kết quả. Lịch sử thật được ghi trong capture.log.

## Tiếng Việt và dữ liệu cá nhân

`AppSettings(Preferences)` cho phép inject node. Công cụ tạo `MemoryPreferences extends AbstractPreferences`, gọi `settings.saveLang(Lang.Language.VI)` và `settings.saveSound(false)`, rồi truyền vào `MainFrame`. Constructor sản phẩm đọc `loadLang()` và gọi `Lang.set`; hồ sơ trống hiển thị tên mặc định `Người chơi 1`. Không gọi constructor AppSettings mặc định, không đọc/ghi Preferences người dùng, không đưa tên hay đường dẫn máy vào UI.

NetworkDialog tự hiển thị `NetworkManager.getLocalIP()`. Để không lộ địa chỉ máy, riêng JVM chụp dùng `-Djdk.net.hosts.file=tools/screenshots/loopback.hosts`. File không chứa tên máy; nếu không phân giải được hostname, chính `getLocalIP()` hiện có trả `127.0.0.1`. Property và native capture variant được đối chiếu từ mã JDK 25 cài tại máy (`java.net.InetAddress`, `java.awt.Robot`). Không sửa nhãn IP, không redaction ảnh, không đổi file hosts hệ thống; không nhấn Host/Join. Nếu địa chỉ thực tế không bằng loopback, công cụ từ chối chụp LAN và ghi lý do.
