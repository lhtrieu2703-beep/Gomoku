# Thí nghiệm ablation Alpha-Beta

Lần chạy MỚI: 2026-11-24T11:55:47.282347+07:00[Asia/Ho_Chi_Minh]; mode `full`. Không so sánh hiệu năng với số liệu ngày 31/10/2026.

Corpus: 10 thế từ `AIArena.corpus()`, RULE_B; MEDIUM 2 ply (16/10 ứng viên), HARD 3 ply (22/14 ứng viên). Mỗi (thế, mức, cờ): 5 lượt làm nóng, 20 lượt đo; mỗi lượt chạy ON rồi OFF, tuần tự trong cùng JVM. Bỏ warmup khi tổng hợp.

Thời gian đo bằng nanoTime, gồm sinh/sắp xếp ứng viên và lời gọi tìm kiếm trực tiếp; không gồm sao chép bàn, cấu hình, tạo watchdog, kiểm tra bất biến hay ghi file. p95 dùng nearest-rank ceil(0.95*n): với n=20 là mẫu thứ 19 sau sắp xếp. Số thập phân trong bảng được tính từ CSV, hiển thị 6 chữ số.

## Theo thế và mức

Mỗi ô thời gian là trung vị/p95/max (ms). Số nút và prune là số xác định của một lời gọi (đã kiểm tra ổn định qua cả warmup và đo). Tỉ lệ đều là OFF/ON; NA nếu mẫu số bằng 0.

| Thế | Mức | n ON/OFF | ON ms (median/p95/max) | OFF ms (median/p95/max) | Nút ON/OFF | Tỉ lệ nút | Tỉ lệ thời gian (median) | Prune ON |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| opening-1 | MEDIUM | 20/20 | 16.334650/18.139000/18.227100 | 18.484950/20.838500/21.318500 | 62/176 | 2.838710 | 1.131640 | 14 |
| opening-1 | HARD | 20/20 | 190.460050/201.892300/207.101400 | 652.054750/683.553000/696.473400 | 773/4642 | 6.005175 | 3.423578 | 62 |
| opening-2 | MEDIUM | 20/20 | 15.433150/17.792400/17.795200 | 19.143600/21.645200/22.076900 | 65/176 | 2.707692 | 1.240421 | 14 |
| opening-2 | HARD | 20/20 | 66.201450/71.617800/72.819200 | 317.464700/335.297400/336.474100 | 443/3376 | 7.620767 | 4.795434 | 39 |
| opening-3 | MEDIUM | 20/20 | 32.211450/37.411100/40.187500 | 35.248550/39.706300/40.363300 | 96/176 | 1.833333 | 1.094286 | 15 |
| opening-3 | HARD | 20/20 | 141.346250/153.852600/155.879600 | 771.387650/805.041600/806.417900 | 463/4642 | 10.025918 | 5.457433 | 45 |
| middle-10 | MEDIUM | 20/20 | 71.487250/80.908600/85.934800 | 73.589550/83.171800/89.781500 | 95/176 | 1.852632 | 1.029408 | 15 |
| middle-10 | HARD | 20/20 | 370.849800/404.281000/458.998900 | 1455.100300/1512.119100/1524.136300 | 719/4642 | 6.456189 | 3.923692 | 52 |
| middle-14 | MEDIUM | 20/20 | 70.088750/77.016200/77.672500 | 73.933300/80.551100/84.635200 | 76/176 | 2.315789 | 1.054853 | 15 |
| middle-14 | HARD | 20/20 | 7.199450/7.994400/8.296300 | 7.201600/7.975200/8.013900 | 0/0 | NA | 1.000299 | 0 |
| middle-18 | MEDIUM | 20/20 | 6.838800/7.780500/8.039300 | 6.686700/7.440800/7.535100 | 0/0 | NA | 0.977759 | 0 |
| middle-18 | HARD | 20/20 | 7.184750/8.449600/10.316600 | 6.936000/7.632600/7.675700 | 0/0 | NA | 0.965378 | 0 |
| middle-20 | MEDIUM | 20/20 | 78.388650/85.093600/85.332100 | 81.698550/88.592200/91.446400 | 64/176 | 2.750000 | 1.042224 | 14 |
| middle-20 | HARD | 20/20 | 422.659100/457.201300/487.439900 | 1564.592200/1584.865300/1665.890200 | 488/4642 | 9.512295 | 3.701783 | 66 |
| late-40 | MEDIUM | 20/20 | 115.147650/133.916500/134.524500 | 125.615900/144.671700/145.742600 | 41/176 | 4.292683 | 1.090912 | 15 |
| late-40 | HARD | 20/20 | 12.360800/13.106200/14.040600 | 12.250400/12.913000/12.919200 | 0/0 | NA | 0.991069 | 0 |
| late-60 | MEDIUM | 20/20 | 99.076300/108.906600/114.358000 | 105.740700/112.377000/113.744100 | 51/176 | 3.450980 | 1.067265 | 14 |
| late-60 | HARD | 20/20 | 14.320350/16.076500/17.088100 | 14.131300/16.118600/16.535600 | 0/0 | NA | 0.986799 | 0 |
| late-100 | MEDIUM | 20/20 | 79.336450/84.994500/87.953500 | 82.344350/88.207400/89.255500 | 69/176 | 2.550725 | 1.037913 | 13 |
| late-100 | HARD | 20/20 | 9.012700/10.573700/12.451700 | 8.934250/11.240000/11.447200 | 0/0 | NA | 0.991296 | 0 |

## Tổng hợp theo mức

Tổng 1 lượt/thế cộng số nút của từng thế một lần. Tổng đo cộng tất cả lượt measure (không cộng warmup). Trung vị tỉ lệ được lấy theo thế, không gộp mẫu thời gian của các thế.

| Mức | Tổng nút 1 lượt/thế ON/OFF | Tổng nút các lượt đo ON/OFF | Trung vị tỉ lệ nút | Trung vị tỉ lệ thời gian | Giảm nút (%) |
|---|---:|---:|---:|---:|---:|
| MEDIUM | 619/1584 | 12380/31680 | 2.707692 | 1.061059 | 60.921717 |
| HARD | 2886/21944 | 57720/438880 | 7.620767 | 2.211938 | 86.848341 |

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
- Các nhóm có minimax_nodes=0: middle-14/HARD (ON=0, OFF=0); middle-18/MEDIUM (ON=0, OFF=0); middle-18/HARD (ON=0, OFF=0); late-40/HARD (ON=0, OFF=0); late-60/HARD (ON=0, OFF=0); late-100/HARD (ON=0, OFF=0).
- Ở full, các nhóm 0 nút đã trả về từ tiền xử lý chiến thuật; các bàn corpus đều không trống và còn ứng viên.
- Thông tin máy/cấu hình đầy đủ: [environment.md](environment.md). Dữ liệu nguồn: [ablation-raw.csv](ablation-raw.csv).

## Bất thường

Không phát hiện bất thường trong các kiểm tra tự động.
