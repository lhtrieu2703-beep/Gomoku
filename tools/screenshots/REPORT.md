# Báo cáo chụp giao diện Gomoku — 2026-11-03

Đã mở đúng một phiên GUI thật, dùng java.awt.Robot; 7/7 ảnh. Không sửa mã sản phẩm, không dựng lại giao diện. Đã dùng chức năng xem ảnh mở riêng từng PNG cuối để kiểm: đúng màn, tiếng Việt, còn thanh tiêu đề, không bị cửa sổ khác che, không cắt nội dung và không có tên thật/đường dẫn máy.

## A. Lệnh chạy lại công cụ

Chạy ở gốc repository, chọn thư mục mới để không ghi đè 7 ảnh của lượt này:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/screenshots/run-screenshots.ps1 -OutputDirectory docs/screenshots/next-run
```

Lệnh đã chạy trong phiên này (đúng một lần):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/screenshots/run-screenshots.ps1
```

JVM chụp bắt đầu 10:51:15 và kết thúc 10:51:24 ngày 2026-11-03, Asia/Saigon; Java 25.0.3; exit code 0; `captured=7 unavailable=0 uncaught=0`.

Nguồn Java độc lập: [CaptureScreenshots.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/CaptureScreenshots.java>). Cách mở từng màn/constructor và kiểm kê công cụ cũ: [README.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/README.md>). Log thật: [capture.log](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/capture.log>); lệnh build/run và hash-check: [runner.log](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/runner.log>).

## B. Ảnh và kiểm tra bằng mắt

| Tên ảnh | Chụp được/không | Kích thước px cuối | Ghi chú |
|---|---|---:|---|
| [01_title.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/01_title.png>) | Chụp được | 951×986 | TITLE; GOMOKU và “Nhấn để bắt đầu”; tiếng Việt. |
| [02_menu.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/02_menu.png>) | Chụp được | 951×986 | MENU; tên mặc định “Người chơi 1”; ba nút chế độ; tiếng Việt. |
| [03_setup_dialog.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/03_setup_dialog.png>) | Chụp được | 294×213 | SetupDialog thật, chế độ chơi với máy, “Trung Bình” mặc định; tiếng Việt. |
| [04_board_playing.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/04_board_playing.png>) | Chụp được | 951×986 | PV_AI/MEDIUM; 4 X + 4 O; đủ lưới/thẻ/tỉ số và Về menu / Ván Mới / Đi Lại. Tên máy có dấu … do UI hiện tại tự rút gọn. |
| [05_settings.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/05_settings.png>) | Chụp được | 320×351 | SettingsDialog; chọn Tiếng Việt; âm thanh tắt trong phiên RAM. |
| [06_rules.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/06_rules.png>) | Chụp được | 799×698 | RulesDialog; hiển thị đủ 6 mục và ví dụ RULE_B; tiếng Việt. |
| [07_lan_dialog.png](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/screenshots/07_lan_dialog.png>) | Chụp được | 646×331 | NetworkDialog; IP dự phòng 127.0.0.1, port mặc định 8888; không Host/Join. Online/Host/Join/Port là từ có sẵn trong chuỗi sản phẩm. |

Kiểm tra sau chụp phát hiện native `Window.getBounds()` gồm dải resize vô hình Windows. Các ảnh gốc đã ghi đúng pixel màn hình nhưng dải này lấy thêm 8 px nền desktop ở trái/phải/dưới. Đã cắt trực tiếp vùng ngoài này từ PNG hiện có, giữ nguyên pixel trong cửa sổ/thanh tiêu đề, không scale, không vẽ lại, không chụp hoặc khởi chạy ứng dụng lần thứ hai. Mép bo góc/viền Windows vẫn là pixel thật.

Bằng chứng crop: [crop-audit.json](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/crop-audit.json>) ghi hash trước/sau, kích thước gốc và rectangle `(8,0,rawWidth-16,rawHeight-8)` của từng ảnh. [captures.tsv](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/captures.tsv>) giữ metadata gốc; [captures-final.tsv](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/captures-final.tsv>) ghi kích thước cuối. Phần cắt tự động đã thêm vào mã Java cho các lần tái hiện, biên dịch thành công, **chưa chạy lại GUI** với bản bổ sung. Không dùng chỉnh sửa ảnh để thay tên/IP hoặc che lỗi UI.

## C. Scale và độ phân giải thực tế

- Màn hình: **1920×1080 px**.
- Java transform: **1.25 × 1.25 (125%)**; Toolkit DPI **120**.
- Screen bounds Java: **1536×864 đơn vị logical**; work area ghi nhận cùng kích thước.
- Windows native Look and Feel: `com.sun.java.swing.plaf.windows.WindowsLookAndFeel`.
- Kích thước frame mặc định thực tế: **774×795 logical**; content **760×758 logical**. Không đổi kích thước, pack lại, DPI hay scale của hệ điều hành.
- Robot raw main-frame capture: **967×994 px**; ảnh vùng cửa sổ nhìn thấy sau bỏ viền vô hình: **951×986 px**.

## D. Màn KHÔNG CHỤP ĐƯỢC

Không có: 7/7 màn chụp được. Đã giữ nguyên hiện tượng tên “Máy (Trung Bình)” được PlayerCard rút gọn bằng dấu “…”. Đây là nội dung UI thực, không phải ảnh bị cửa sổ khác che hoặc crop mất phần thẻ.

Không đọc Preferences cá nhân: AppSettings nhận MemoryPreferences; language=VI, hồ sơ rỗng dùng tên mặc định, sound=false. Riêng JVM chụp dùng `jdk.net.hosts.file` để đường `NetworkManager.getLocalIP()` hiện có trả địa chỉ dự phòng 127.0.0.1; không sửa JLabel, file hosts hệ thống hay mã sản phẩm. Không mở kết nối LAN.

## E. Tệp đã tạo

Chỉ tạo tệp trong `tools/screenshots/` và 7 PNG trong `docs/screenshots/`. Không sửa/xóa tệp có trước. 41 tệp nguồn/tài nguyên sản phẩm khớp SHA-256 trước/sau.

- Công cụ: [CaptureScreenshots.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/CaptureScreenshots.java>), [run-screenshots.ps1](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/run-screenshots.ps1>), [loopback.hosts](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/loopback.hosts>).
- Hướng dẫn/kiểm kê: [README.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/README.md>); báo cáo này [REPORT.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/REPORT.md>).
- Bằng chứng: `tools/screenshots/runs/20261103-105113-27d5d3/` chứa runner.log, capture.log, capture.stderr.log (rỗng), capture.started, captures.tsv, captures-final.tsv, crop-audit.json, source-sha256.json, verification.json và classes/ đã dùng cho lượt thật.
- Output kiểm tra biên dịch: `tools/screenshots/preflight/` (chỉ class mới; không có lần chạy GUI thứ hai).
- Ảnh: bảy tệp liên kết ở bảng B.
- Danh sách đầy đủ mọi tệp mới, kể cả class build: [created-files.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/created-files.txt>).

Đối chiếu nguồn và số lần chạy được lưu tại [verification.json](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/screenshots/runs/20261103-105113-27d5d3/verification.json>). Các thay đổi test/log JUnit và packaging đã có từ trước tác vụ này không bị đụng tới.
