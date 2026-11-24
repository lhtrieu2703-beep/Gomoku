# Thí nghiệm ablation Alpha-Beta

Lần chạy MỚI: 2026-11-24T11:47:15.994801500+07:00[Asia/Ho_Chi_Minh]; mode `search-only`. Không so sánh hiệu năng với số liệu ngày 31/10/2026.

Corpus: 10 thế từ `AIArena.corpus()`, RULE_B; MEDIUM 2 ply (16/10 ứng viên), HARD 3 ply (22/14 ứng viên). Mỗi (thế, mức, cờ): 5 lượt làm nóng, 20 lượt đo; mỗi lượt chạy ON rồi OFF, tuần tự trong cùng JVM. Bỏ warmup khi tổng hợp.

Thời gian đo bằng nanoTime, gồm sinh/sắp xếp ứng viên và lời gọi tìm kiếm trực tiếp; không gồm sao chép bàn, cấu hình, tạo watchdog, kiểm tra bất biến hay ghi file. p95 dùng nearest-rank ceil(0.95*n): với n=20 là mẫu thứ 19 sau sắp xếp. Số thập phân trong bảng được tính từ CSV, hiển thị 6 chữ số.

## Theo thế và mức

Mỗi ô thời gian là trung vị/p95/max (ms). Số nút và prune là số xác định của một lời gọi (đã kiểm tra ổn định qua cả warmup và đo). Tỉ lệ đều là OFF/ON; NA nếu mẫu số bằng 0.

| Thế | Mức | n ON/OFF | ON ms (median/p95/max) | OFF ms (median/p95/max) | Nút ON/OFF | Tỉ lệ nút | Tỉ lệ thời gian (median) | Prune ON |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| opening-1 | MEDIUM | 20/20 | 14.060150/15.833300/16.040100 | 16.440550/17.948000/18.102200 | 62/176 | 2.838710 | 1.169301 | 14 |
| opening-1 | HARD | 20/20 | 200.702800/210.809400/222.118100 | 701.331800/729.263000/752.702900 | 773/4642 | 6.005175 | 3.494380 | 62 |
| opening-2 | MEDIUM | 20/20 | 14.518650/16.465100/16.824100 | 18.736800/20.769100/22.841300 | 65/176 | 2.707692 | 1.290533 | 14 |
| opening-2 | HARD | 20/20 | 68.826500/74.469300/77.080800 | 338.031450/349.851600/352.291200 | 443/3376 | 7.620767 | 4.911356 | 39 |
| opening-3 | MEDIUM | 20/20 | 32.990600/38.165200/40.001200 | 35.493450/38.909900/40.292600 | 96/176 | 1.833333 | 1.075866 | 15 |
| opening-3 | HARD | 20/20 | 145.042650/157.037600/158.055000 | 802.041150/890.171600/930.696400 | 463/4642 | 10.025918 | 5.529692 | 45 |
| middle-10 | MEDIUM | 20/20 | 72.949700/79.698300/83.151800 | 74.393700/88.029300/92.574300 | 95/176 | 1.852632 | 1.019794 | 15 |
| middle-10 | HARD | 20/20 | 384.237350/402.211000/404.166300 | 1519.504550/1570.298100/1748.659300 | 719/4642 | 6.456189 | 3.954599 | 52 |
| middle-14 | MEDIUM | 20/20 | 70.228850/77.159500/79.192500 | 74.667400/80.111800/84.959800 | 76/176 | 2.315789 | 1.063201 | 15 |
| middle-14 | HARD | 20/20 | 354.243450/370.855400/379.656300 | 1475.249400/1534.638400/1539.271100 | 494/4642 | 9.396761 | 4.164507 | 56 |
| middle-18 | MEDIUM | 20/20 | 82.643500/90.354500/90.788700 | 88.916100/93.003300/99.845200 | 42/176 | 4.190476 | 1.075899 | 15 |
| middle-18 | HARD | 20/20 | 358.547350/372.882800/377.872900 | 1772.874300/1832.733000/1900.867300 | 430/4642 | 10.795349 | 4.944603 | 44 |
| middle-20 | MEDIUM | 20/20 | 77.039800/80.834500/85.695400 | 81.446600/84.530400/84.651000 | 64/176 | 2.750000 | 1.057202 | 14 |
| middle-20 | HARD | 20/20 | 414.676000/452.271000/472.763200 | 1572.576700/1857.086100/1990.181500 | 488/4642 | 9.512295 | 3.792302 | 66 |
| late-40 | MEDIUM | 20/20 | 106.438600/109.812800/116.095100 | 110.895900/112.463800/112.751500 | 41/176 | 4.292683 | 1.041877 | 15 |
| late-40 | HARD | 20/20 | 368.791450/375.309400/391.608100 | 2188.765350/2248.299900/2263.644500 | 378/4642 | 12.280423 | 5.934968 | 34 |
| late-60 | MEDIUM | 20/20 | 94.433400/95.860000/96.298400 | 98.319450/102.449400/102.867500 | 51/176 | 3.450980 | 1.041151 | 14 |
| late-60 | HARD | 20/20 | 544.587950/556.377500/559.114200 | 1948.350050/1967.014100/1977.047900 | 452/3942 | 8.721239 | 3.577659 | 43 |
| late-100 | MEDIUM | 20/20 | 73.666600/76.225400/76.239400 | 77.578250/79.455200/80.779000 | 69/176 | 2.550725 | 1.053099 | 13 |
| late-100 | HARD | 20/20 | 363.559300/395.617500/398.647300 | 1456.567950/1492.517500/1519.050800 | 374/2248 | 6.010695 | 4.006411 | 30 |

## Tổng hợp theo mức

Tổng 1 lượt/thế cộng số nút của từng thế một lần. Tổng đo cộng tất cả lượt measure (không cộng warmup). Trung vị tỉ lệ được lấy theo thế, không gộp mẫu thời gian của các thế.

| Mức | Tổng nút 1 lượt/thế ON/OFF | Tổng nút các lượt đo ON/OFF | Trung vị tỉ lệ nút | Trung vị tỉ lệ thời gian | Giảm nút (%) |
|---|---:|---:|---:|---:|---:|
| MEDIUM | 661/1760 | 13220/35200 | 2.728846 | 1.060201 | 62.443182 |
| HARD | 5014/42060 | 100280/841200 | 9.059000 | 4.085459 | 88.078935 |

## Kiểm tra tính đúng đắn

- Đã so sánh 400 cặp lượt đo ON/OFF; 0 cặp khác nhau (khác nước: 0; khác root_value: 0).
- Khác nhau khi làm nóng: 0 cặp.
- CSV có 1000 dòng: 200 warmup, 800 measure; timeout: 0.
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
