# Gomoku (Cờ Caro) – Java Swing

Đồ án cơ sở: trò chơi Gomoku (cờ caro) trên bàn 15×15, viết bằng Java Swing, có AI dùng thuật toán Minimax kết hợp cắt tỉa Alpha-Beta.

- **Sinh viên:** Lê Hoàng Triều – Lớp 24CSE
- **Ngôn ngữ / giao diện:** Java, Swing
- **Giao diện hỗ trợ:** Tiếng Việt, English, Português

## Tính năng

- Ba chế độ chơi: **Chơi với máy**, **2 người cùng máy**, **Chơi LAN** (hai máy cùng mạng).
- Ba mức AI: **Dễ**, **Trung Bình**, **Khó**.
  - Dễ không chạy Minimax. Bàn trống thì đánh vào giữa; nếu đối thủ có thể thắng ngay, máy chặn với xác suất 70%; nếu không chặn thì chọn ngẫu nhiên trong tối đa ba nước có điểm heuristic cơ bản cao nhất.
  - Trung Bình và Khó dùng Minimax + Alpha-Beta.
- Đi lại (Undo): chế độ 2 người rút 1 nước; khi chơi với máy rút cả nước của máy. **Chế độ LAN không có đi lại.** Đi lại sau khi ván kết thúc sẽ mở lại ván và thu hồi kết quả đã ghi vào tỉ số.
- Tỉ số theo phiên chơi (không lưu khi tắt game), hiển thị trên hai thẻ người chơi; số ván hòa hiển thị ở giữa. Nút **Ván Mới** xóa bàn cờ và giữ tỉ số (kết quả ván trước vẫn được tính).
- Cài đặt: đổi ngôn ngữ, bật/tắt âm thanh, đặt lại tỉ số.

## Luật chơi (RULE_B)

- Bàn cờ 15×15, quân Đen đi trước.
- Thắng khi có từ 5 quân cùng màu liên tiếp theo hàng ngang, hàng dọc hoặc đường chéo. Dãy 6 quân trở lên cũng thắng.
- Dãy bị quân đối thủ chặn sát **cả hai đầu** thì **không** tính thắng. Chặn một đầu, hoặc một đầu sát mép bàn, vẫn thắng.
- Bàn đầy mà chưa ai thắng thì hòa.
- Không có luật cấm tay (3-3, 4-4) và không giới hạn thời gian mỗi nước.

## Chạy game

### Cách 1: Bản đóng gói cho Windows (không cần cài Java)

1. Tải `Gomoku-Windows-x64.zip` tại [trang Releases](../../releases/latest).
2. Chuột phải vào file ZIP, chọn **Extract All** (không chạy trực tiếp từ trong ZIP).
3. Mở thư mục `Gomoku` vừa giải nén và chạy `Gomoku.exe`. Giữ nguyên `Gomoku.exe` cùng hai thư mục `app`, `runtime`.

Yêu cầu: Windows 10 (bản 1903 trở lên) hoặc Windows 11, 64-bit. Lần đầu mở, Windows SmartScreen có thể cảnh báo vì file chưa được ký số; chọn **More info** rồi **Run anyway**.

Có thể kiểm tra file tải về bằng PowerShell:

```powershell
(Get-FileHash .\Gomoku-Windows-x64.zip -Algorithm SHA256).Hash
```

So khớp với mã trong file `Gomoku-Windows-x64.zip.sha256` đi kèm bản phát hành.

### Cách 2: Chạy từ mã nguồn

Cần JDK có `javac` (bản đóng gói được tạo bằng JDK 25.0.3), `javac` và `java` nằm trong `PATH`. Từ thư mục gốc dự án, chạy `run.bat`: script biên dịch mã nguồn vào thư mục `bin`, chép `resources/` vào `bin`, rồi chạy `game.Main`.

Hoặc làm thủ công trong Command Prompt:

```bat
javac -encoding UTF-8 -d bin src/game/model/*.java src/game/ai/*.java src/game/network/*.java src/game/view/*.java src/game/*.java
xcopy resources bin /E /I /Y
java -cp bin game.Main
```

### Tạo lại bản đóng gói Windows

Cần JDK 25 trở lên (đầy đủ `javac`, `jar`, `jdeps`, `jlink`, `jpackage`) và công cụ `mt.exe` của Windows SDK (tìm được qua `PATH` hoặc thư mục Windows Kits). Chạy trong PowerShell:

```powershell
.\package-windows.ps1 -Version 1.0.1
```

Kết quả nằm trong `dist/<mã thời gian>/` (thư mục `dist/` không được Git theo dõi). Chi tiết xem `docs/PACKAGING.md`.

## Chơi qua LAN

1. Hai máy chạy cùng bản game và cùng mạng Wi-Fi/LAN.
2. Máy thứ nhất chọn **Chơi LAN** → **Tạo Phòng (Host – Quân Đen)**, cổng mặc định `8888`.
3. Máy thứ hai nhập địa chỉ IPv4 của máy thứ nhất (xem bằng lệnh `ipconfig`), cùng cổng `8888`, chọn **Vào Phòng (Join – Quân Trắng)**.
4. Địa chỉ `127.0.0.1` chỉ dùng để thử hai cửa sổ game trên cùng một máy. Nếu Windows Firewall hỏi, cho phép trên mạng riêng (Private).

## Kiểm thử

- Bộ JUnit chạy không giao diện (headless) bằng `test/run-headless-2026-11-03.ps1`. Lần chạy cuối ngày 03/11/2026: **190/190 PASS**. Phạm vi và các phần không kiểm tự động được liệt kê trong `docs/TEST-SCOPE-2026-11-03.md`.
- Giao diện, âm thanh và luồng Undo/tỉ số trong `MainFrame` được kiểm tay (không có kiểm thử tự động ở mức giao diện). Bản đóng gói được kiểm tay ngày 04/11/2026: khởi động, âm thanh, đi lại giữa ván, đi lại sau khi ván kết thúc, đóng game.
- Kết quả đánh giá AI (đấu AI với AI, đo thời gian) nằm trong `docs/ai-results/`.

## Cấu trúc thư mục

| Thư mục / tệp | Nội dung |
|---|---|
| `src/game/model` | `Board`, `Piece`, `ScoreTracker` |
| `src/game/ai` | `MinimaxAI`, `HeuristicEvaluator`, `Move` |
| `src/game/network` | `NetworkManager` (giao thức LAN dạng văn bản) |
| `src/game/view` | Giao diện Swing (`MainFrame`, `BoardPanel`, `StartScreen`, `MainMenu`, ...) |
| `src/game/Main.java` | Điểm vào chương trình |
| `resources/` | Tài nguyên (hình ảnh, âm thanh, ...) |
| `test/` | Bộ kiểm thử JUnit và script chạy |
| `tools/` | Công cụ phụ trợ (kiểm định AI, chụp ảnh, ...) |
| `docs/` | Tài liệu, kết quả kiểm thử, ảnh chụp giao diện |
| `run.bat` | Biên dịch và chạy game từ mã nguồn |
| `package-windows.ps1` | Tạo bản đóng gói Windows |

## Hạn chế đã biết

- Chế độ LAN chưa được kiểm thử trên hai máy thật.
- Chưa có đồng hồ cờ, đầu hàng, chọn bên, luật cấm tay.
- Âm thanh chỉ có bật/tắt, không có thanh âm lượng.
- Cửa sổ 774×795 bị tràn ở mức phóng đại 150% trên màn hình 1080p; hiển thị ổn nhất ở 125%.
- Ở mức Trung Bình, tên đối thủ trên thẻ người chơi bị cắt bớt do chiều rộng thẻ cố định.
- Chưa có kết luận thống kê cho việc mức Khó mạnh hơn Trung Bình; chỉ có bằng chứng Dễ thua cả hai mức còn lại.