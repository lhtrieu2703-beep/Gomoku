# Kết quả ablation Alpha-Beta — 24/11/2026

Đây là dữ liệu của các lần chạy mới. Không so sánh hiệu năng với số liệu ngày 31/10/2026 và không cộng kết quả JUnit vào mốc 531 lượt ngày 12/11/2026. Thư mục `docs/ai-results/` cũ không thay đổi.

| Lần chạy | Dữ liệu | Kết quả |
|---|---|---|
| Pilot | [pilot/ablation-raw.csv](pilot/ablation-raw.csv), [tổng hợp](pilot/ablation-summary.md) | opening-1 và middle-20; 1 warmup + 2 lượt đo/cờ/mức; 24 dòng, 8 cặp đo, 0 khác nước/giá trị |
| Chính: search-only | [ablation-raw.csv](ablation-raw.csv), [ablation-summary.md](ablation-summary.md) | 10 thế, 5 warmup + 20 lượt đo/cờ/mức; 1000 dòng, 400 cặp đo, 0 khác nước/giá trị, 0 timeout |
| Đối chiếu: full | [full/ablation-raw.csv](full/ablation-raw.csv), [tổng hợp](full/ablation-summary.md) | Cùng corpus và số lượt; 1000 dòng, 400 cặp đo, 0 khác nước/giá trị, 0 timeout |
| Kiểm tra ngưỡng an toàn | [timeout-check/ablation-raw.csv](timeout-check/ablation-raw.csv), [báo cáo chưa đủ mẫu](timeout-check/ablation-summary.md) | Cố ý đặt 1 ms; giữ 1 dòng timeout, dừng và báo dữ liệu chưa đầy đủ. Không dùng trong kết luận hiệu năng |
| Toàn bộ JUnit | [junit/junit-run.log](junit/junit-run.log), [JSON](junit/junit-results.json), [điều kiện/lệnh](junit/junit-environment.md) | 550 phát hiện, 550 chạy, 550 đạt, 0 lỗi, 0 bỏ qua, 0 hủy |

Pilot hoàn tất trước lần đo chính. Lời gọi lâu nhất trong pilot là 1590.827700 ms; vòng đo mất 7.795267 giây. Lần chính search-only: lời gọi lâu nhất 2263.644500 ms, vòng đo 459.456787 giây. Full: lời gọi lâu nhất 1665.890200 ms, vòng đo 180.949473 giây. Các giá trị này lấy từ CSV/environment, bao gồm cả warmup khi tìm max. Không có lời gọi tự nhiên nào vượt ngưỡng 30000 ms.

Thông tin máy: Intel Core i7-12650H, 10 lõi vật lý, 16 bộ xử lý logic; Windows 11 (Java báo version 10.0), amd64; Microsoft OpenJDK 25.0.3+9-LTS. Mỗi lần đo dùng một JVM với `-Xms256m -Xmx1g`; cấu hình này chỉ thuộc công cụ đo. Xem [environment.md](environment.md) và [full/environment.md](full/environment.md).

JUnit dùng `build.ps1` nguyên bản và `--scan-class-path`, không loại lớp/gói/test, không chạy song song. Các test Swing/socket hiện có cũng chạy. Tất cả source/test/resources/lib được sao chép vào thư mục tạm; ảnh do test giao diện tạo không ghi về repo. Đối chiếu XML xác nhận 19 lượt thuộc `AlphaBetaAblationTest`, 531 lượt thuộc test đã có trong CHÍNH lần chạy mới này; cả hai nhóm đều không có failure/error/skipped. Thời gian JUnit báo: 66762 ms; 134 containers đạt. Không sửa test cũ.

## Giả định và điểm cần lưu ý

- Dùng đúng `AIArena.corpus()` qua reflection trong công cụ đo, rồi kiểm tra bảng xuất ra với benchmark cũ từng ô/số quân/bên đi; không sao chép lại thuật toán sinh fixture hay chọn lại thế theo kết quả AI.
- `minimax_nodes` đếm số lần vào hàm minimax (kể cả lá), không tính vòng gốc hoặc các đánh giá để sinh/sắp xếp ứng viên. Vì vậy tỉ lệ giảm nút không đồng nhất với tỉ lệ thời gian.
- Cờ chỉ tắt các cutoff `beta <= alpha`; các return thắng ngay bên trong đệ quy được giữ nguyên như code gốc. Vẫn giữ ứng viên đã giới hạn, thứ tự ổn định và dấu `>` ở gốc. Đây là ablation của kernel hiện có.
- Trong search-only không nhóm nào có 0 nút. Trong full có 6 nhóm trả về từ tiền xử lý: middle-14/HARD, middle-18/MEDIUM, middle-18/HARD, late-40/HARD, late-60/HARD, late-100/HARD. Ở đó root_value trống vì không có tìm kiếm gốc: 120 cặp đo thuộc trường hợp này; không coi chúng là 120 giá trị Minimax đã tính.
- Cột bổ sung `status` dùng đánh dấu ok/timeout; không dùng giá trị giả cho nước hay root_value khi timeout. Kiểm tra timeout cố ý có báo cáo bất thường do thiếu mẫu đúng như thiết kế; đây không phải lỗi của hai lần đo chính.
- Thời gian còn nhiễu JIT/GC/lịch CPU/nhiệt độ/tải nền; không ghi nhận riêng nhiệt độ hoặc mức tải nền, không đo khoảng tin cậy. Thứ tự cố định ON rồi OFF theo yêu cầu, không đảo thứ tự theo lượt.
- Pilot và JUnit được chạy từ working tree khi các file công cụ/test mới chưa commit; điều này được ghi trong environment. Source AI đã commit trước lần đo chính. Bản sửa công cụ sau lần đo chỉ làm rõ báo lỗi/timeout và định dạng metadata, không đổi kernel hay các lời gọi đo thành công.
- Ngoài timeout cố ý, không phát hiện sai khác nước/giá trị, đột biến số nút, thay đổi bàn cờ hoặc test thất bại. Truy vấn số lõi CPU qua CIM ban đầu bị sandbox từ chối; sau khi được cấp quyền, pilot chạy thành công rồi mới đo đầy đủ.

Lệnh chạy lại và mô tả CLI: [tools/ai/README-ablation.md](../../tools/ai/README-ablation.md). Luôn dùng thư mục đầu ra mới để giữ nguyên dữ liệu đã đo.
