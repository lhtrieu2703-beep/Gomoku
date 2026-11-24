# Thí nghiệm ablation Alpha-Beta

Lần chạy MỚI: 2026-11-24T11:46:33.107186500+07:00[Asia/Ho_Chi_Minh]; mode `search-only`. Không so sánh hiệu năng với số liệu ngày 31/10/2026.

Corpus: 2 thế từ `AIArena.corpus()`, RULE_B; MEDIUM 2 ply (16/10 ứng viên), HARD 3 ply (22/14 ứng viên). Mỗi (thế, mức, cờ): 1 lượt làm nóng, 2 lượt đo; mỗi lượt chạy ON rồi OFF, tuần tự trong cùng JVM. Bỏ warmup khi tổng hợp.

Thời gian đo bằng nanoTime, gồm sinh/sắp xếp ứng viên và lời gọi tìm kiếm trực tiếp; không gồm sao chép bàn, cấu hình, tạo watchdog, kiểm tra bất biến hay ghi file. p95 dùng nearest-rank ceil(0.95*n): với n=20 là mẫu thứ 19 sau sắp xếp. Số thập phân trong bảng được tính từ CSV, hiển thị 6 chữ số.

## Theo thế và mức

Mỗi ô thời gian là trung vị/p95/max (ms). Số nút và prune là số xác định của một lời gọi (đã kiểm tra ổn định qua cả warmup và đo). Tỉ lệ đều là OFF/ON; NA nếu mẫu số bằng 0.

| Thế | Mức | n ON/OFF | ON ms (median/p95/max) | OFF ms (median/p95/max) | Nút ON/OFF | Tỉ lệ nút | Tỉ lệ thời gian (median) | Prune ON |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| opening-1 | MEDIUM | 2/2 | 18.246850/21.420700/21.420700 | 19.249900/22.262600/22.262600 | 62/176 | 2.838710 | 1.054971 | 14 |
| opening-1 | HARD | 2/2 | 129.228350/135.641800/135.641800 | 416.310750/425.874800/425.874800 | 773/4642 | 6.005175 | 3.221513 | 62 |
| middle-20 | MEDIUM | 2/2 | 43.665900/44.663500/44.663500 | 49.750500/51.985300/51.985300 | 64/176 | 2.750000 | 1.139344 | 14 |
| middle-20 | HARD | 2/2 | 393.622900/422.307700/422.307700 | 1570.616450/1590.827700/1590.827700 | 488/4642 | 9.512295 | 3.990155 | 66 |

## Tổng hợp theo mức

Tổng 1 lượt/thế cộng số nút của từng thế một lần. Tổng đo cộng tất cả lượt measure (không cộng warmup). Trung vị tỉ lệ được lấy theo thế, không gộp mẫu thời gian của các thế.

| Mức | Tổng nút 1 lượt/thế ON/OFF | Tổng nút các lượt đo ON/OFF | Trung vị tỉ lệ nút | Trung vị tỉ lệ thời gian | Giảm nút (%) |
|---|---:|---:|---:|---:|---:|
| MEDIUM | 126/352 | 252/704 | 2.794355 | 1.097158 | 64.204545 |
| HARD | 1261/9284 | 2522/18568 | 7.758735 | 3.605834 | 86.417492 |

## Kiểm tra tính đúng đắn

- Đã so sánh 8 cặp lượt đo ON/OFF; 0 cặp khác nhau (khác nước: 0; khác root_value: 0).
- Khác nhau khi làm nóng: 0 cặp.
- CSV có 24 dòng: 8 warmup, 16 measure; timeout: 0.
- Đã kiểm tra corpus từng ô/số quân/bên đi với benchmark-positions.md gốc; đủ lượt, thứ tự xen kẽ, độ sâu, tính ổn định số nút/nước/giá trị; OFF prune=0 và OFF nodes>=ON nodes.
- Runner kiểm tra bất biến từng ô/số quân/luật sau mỗi lời gọi; chỉ ghi mẫu ok khi nước trả về hợp lệ.
- root_value là giá trị Minimax gốc, không phải Move.score. Ở full, ô trống nghĩa là tiền xử lý trả về trước Minimax; hai ô trống chỉ xác nhận cùng không chạy tìm kiếm gốc.

## Giới hạn và điểm cần lưu ý

- Số nút là đại lượng xác định cho code/corpus/cấu hình này. Thời gian có nhiễu do JIT, GC, lịch CPU, nhiệt độ và tải nền. Không suy ra tốc độ chỉ từ tỉ lệ nút; chưa đo khoảng tin cậy thống kê.
- Hai cấu hình giữ nguyên sinh/sắp xếp/giới hạn ứng viên, lượng giá, độ sâu, dấu > khi chọn nước gốc và try/finally rút quân. Cờ chỉ vô hiệu hóa các cutoff beta<=alpha. Các return khi đã tìm thấy thắng trong đệ quy vẫn giữ nguyên: đây là so sánh kernel hiện tại, không phải Minimax duyệt toàn bộ ô hợp lệ của Gomoku.
- search-only bỏ các shortcut chiến thuật và dùng cùng kernel gốc; nước của nó có thể khác full. Các thế late-40/60/100 là tập con tổng hợp, không đại diện mọi tàn cuộc thực tế.
- Không có nhóm search-only nào có minimax_nodes=0; cả hai cờ đều vào Minimax ở mọi thế/mức.
- Thông tin máy/cấu hình đầy đủ: [environment.md](environment.md). Dữ liệu nguồn: [ablation-raw.csv](ablation-raw.csv).

## Bất thường

Không phát hiện bất thường trong các kiểm tra tự động.
