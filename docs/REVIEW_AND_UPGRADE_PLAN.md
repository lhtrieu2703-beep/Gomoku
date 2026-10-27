# Review hiện trạng và hướng nâng cấp Gomoku

Ngày khảo sát: 2026-10-26. Nhánh: task3-ui-sound-lang.

## Kết luận để quyết định chuyển task

Chưa nên chuyển sang tính năng lớn khác. Bản hiện tại phản hồi nhanh trong
kịch bản đo, các bản vá P1/P2 trước vẫn qua test, nhưng còn lỗi đồng bộ LAN,
thứ tự thông báo kết quả, bố cục sau đổi ngôn ngữ và đánh giá chiến thuật AI.
Tốc độ nhanh và tính đúng là hai tiêu chí riêng.

Lượt review này không sửa logic sản phẩm. Đã đọc lại mã, chạy lại test,
dựng probe với socket localhost thật, render giao diện bằng Look & Feel hệ
điều hành và đo hiệu năng. Probe được giữ trong tools/audit để tái hiện.

## 1. Bằng chứng kiểm tra hiện tại

| Hạng mục | Kết quả | Giới hạn |
| --- | --- | --- |
| Toàn bộ JUnit | 123/123 PASS; 0 skipped, aborted, failed | Chưa bao phủ các tình huống mới dưới đây |
| RuleAndDrawTest | 17/17 PASS | Kiểm tra luật/hòa, không thay kiểm thử UI/LAN |
| FullGomokuTestSuite | Output 80/80 | T53–T54 không thử hành vi UI thật; T55–T57 so chuỗi hằng |
| Preferences thật | /game/view giữ nguyên {soundEnabled=true} | Probe dùng Preferences trong bộ nhớ riêng |
| Git diff whitespace | Sạch | Chưa commit/push các sửa đổi của phiên làm việc |
| LAN | Hủy, reconnect, MOVE/RESTART/EOF đã có test localhost | Chưa thử hai máy, mạng chập chờn hay firewall thực |
| Audio | Test đóng Clip trên success/open error/start error | Chưa đánh giá thiết bị âm thanh thật |

### F1 — P1: MOVE cũ có thể lọt vào ván mới trong cùng kết nối

Vị trí:
[MainFrame:168](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:168>),
[NetworkManager:77](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/network/NetworkManager.java:77>),
[BoardPanel:273](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:273>).

Đã tái hiện bằng socket localhost thật: nhận MOVE vào hàng đợi dispatcher,
reset bàn trên EDT, sau đó mới chạy callback MOVE của cùng phiên. Ô (4,4)
trong bàn mới nhận X, moveCount=1 thay vì 0.

```text
OLD_MOVE_AFTER_RESTART=X moveCount=1 (expected NONE / 0)
```

Guard gameId hiện có bảo vệ callback đã đi vào BoardPanel trước reset.
Nó không biết ván gốc của gói khi NetworkManager chỉ gọi BoardPanel sau reset.
Session token đã sửa việc chuyển kết nối, nhưng chưa giải quyết chuyển ván
trong cùng kết nối. Đây là hai ranh giới khác nhau.

Đề xuất: host quyết định mã ván dùng chung, MOVE mang mã ván và số thứ tự
nước; restart có quy trình xác nhận và vô hiệu hóa input trong lúc đổi ván.
Kiểm tra cả mã ván, thứ tự, màu và tọa độ. Bắt tay xác nhận phiên bản giao
thức/luật trước khi chơi. Không dùng gameId cục bộ hiện tại làm mã chung,
vì số lần reset ở hai máy có thể khác nhau.

### F2 — P2: đối thủ chưa nhận nước thắng khi hộp thoại kết quả đang mở

Vị trí:
[BoardPanel:250](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:250>),
[MainFrame:209](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:209>).

executeMove gọi onGameOver đồng bộ, MainFrame mở JOptionPane modal, còn
sendMove chỉ chạy sau executeMove trả về. Probe kiểm tra ngay trong callback:

```text
FINAL_MOVE_SENDS_AT_GAMEOVER=4 (expected 5 to notify opponent before popup)
FINAL_MOVE_SENDS_AFTER_CALLBACK=5
```

Recorder thay việc ghi socket trong probe để quan sát thứ tự gọi; modal
JOptionPane được xác định từ mã MainFrame. Hệ quả là máy thắng phải đóng
popup thì đối thủ mới nhận nước cuối.

Đề xuất: giao dịch nước đi hoàn tất, gửi/sắp lịch gửi sự kiện mạng, cập nhật
UI rồi mới trình bày thông báo kết quả. Có thể dùng banner kết quả ngay trên
bàn, tránh để một callback modal chặn chuỗi xử lý nước đi.

### F3 — P2: heuristic còn chấm sai luật và đếm trùng đe dọa

Vị trí:
[HeuristicEvaluator:102](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/ai/HeuristicEvaluator.java:102>),
[HeuristicEvaluator:146](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/ai/HeuristicEvaluator.java:146>).

```text
BLOCKED_SIX win=false heuristic=200000000
SINGLE_THREE heuristic=2264300
```

OXXXXXXO không thắng theo luật Board đang dùng, nhưng nhận điểm gấp đôi FIVE.
Một chuỗi XXX mở ở giữa bàn bị hai cửa sổ chồng nhau tính như bẫy đôi,
vượt DOUBLE_THREE=2,000,000.

Đề xuất: xác định toàn chuỗi và hai đầu thật; biểu diễn đe dọa bằng tập quân,
hướng và các ô tiếp diễn để khử trùng. Không lấy tổng điểm cả bàn làm bằng
chứng rằng một nước cụ thể tạo double-three/open-four. Kết quả thắng thật
phải luôn ưu tiên hơn mọi điểm đánh giá chưa kết thúc.

Đây là sửa tính đúng của AI trước khi tăng depth. Cần bộ thế cờ chiến thuật
cho từng luật, bao gồm đảo màu, xoay/đối xứng, chuỗi sát biên và thế ngắt quãng.

### F4 — P2: đổi VI → PT làm mất nút âm thanh khỏi header

Vị trí:
[MainFrame:72](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:72>),
[MainFrame:510](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:510>).

Frame được pack theo ngôn ngữ lúc khởi tạo và không cho resize. Đổi ngôn ngữ
chỉ revalidate/repaint. Trong render Swing trên máy hiện tại:

| Trường hợp | Kích thước frame |
| --- | --- |
| Khởi tạo VI | 676 × 755 |
| Khởi tạo EN | 680 × 755 |
| Khởi tạo PT | 728 × 755 |
| Khởi tạo VI rồi đổi PT | Vẫn 676 × 755 |

Nút loa bị FlowLayout đẩy xuống y=28 ở container cao 28, nên bị cắt hoàn toàn.
Đã nhìn thấy trong ảnh render, không chỉ suy luận từ preferredSize.

- [Ảnh VI](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/audit-2026-10-26/audit-VI.png>)
- [Ảnh VI → PT bị mất nút](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/audit-2026-10-26/audit-switch-PT.png>)
- [Ảnh khởi tạo PT trực tiếp](</C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/audit-2026-10-26/audit-PT.png>)

Đề xuất: tách điều khiển thành hai hàng hoặc dùng layout co giãn; tính lại
kích thước sau đổi ngôn ngữ trong giới hạn vùng màn hình làm việc. Nếu co
bàn cờ, phải dùng cùng một phép biến đổi tọa độ cho vẽ, hover và click.
Kiểm tra các locale và DPI 100/125/150%, không chỉ màu chữ trong renderer.

### F5 — P2: độ bao phủ test và benchmark đang tạo cảm giác an toàn quá mức

- FullGomokuTestSuite T55–T57 không gọi logic sản phẩm; T53–T54 cũng chưa thử
  hành vi được mô tả. Không đưa những dòng này vào báo cáo như bằng chứng
  đã có đầu hàng/hết giờ hoặc chặn tương tác đúng.
- LangPurityTest C lấy Color khai báo trong test. UI có thể thay màu hoặc
  mất nút nhưng test vẫn xanh. Cần kiểm tra component bounds và render thật.
- AIBenchmarkTest in kết luận hiệu năng cố định sau chỉ một thế cờ. Một nhánh
  chiến thuật trả sớm với 0 nodes không chứng minh Minimax depth=3 nhanh.

## 2. Có mượt không? Số đo và phạm vi

Chạy trên máy hiện tại, Java hiện có trong môi trường. Mỗi cấu hình AI có
3 lượt warmup và 15 lượt đo. Bốn thế cờ được sinh với seed cố định, bỏ nước
tạo thắng trong lúc dựng thế. Kiểm tra nước trả về còn trống và moveCount
không thay đổi sau tìm kiếm.

| Số quân | Easy median / max | Medium median / max | Hard median / max | Nodes Hard |
| ---: | ---: | ---: | ---: | ---: |
| 1 | 0.14 / 0.32 ms | 9.58 / 13.40 ms | 38.30 / 40.82 ms | 454 |
| 21 | 0.16 / 0.71 ms | 3.70 / 4.20 ms | 3.73 / 4.79 ms | 0 |
| 61 | 0.21 / 0.28 ms | 4.09 / 4.52 ms | 4.33 / 4.81 ms | 0 |
| 121 | 0.24 / 0.33 ms | 2.20 / 4.85 ms | 2.03 / 2.58 ms | 0 |

Ba thế đông quân trả về qua nhánh chiến thuật. Các con số này không xếp hạng
sức mạnh Easy/Medium/Hard và không đại diện mọi thế cờ.

Thêm 10 lượt thực thi Hard qua BoardPanel trên EDT/SwingWorker:

- Thời gian lượt: median 35.28 ms, max 109.69 ms.
- 119 mẫu heartbeat EDT: median 0.05 ms, p95 0.27 ms, max 3.19 ms.

Đây là kiểm tra lịch xử lý BoardPanel, không mở cửa sổ đầy đủ để người chơi
tương tác, âm thanh tắt. Chưa đo GPU/render khi kéo cửa sổ, thiết bị audio,
mạng hai máy, máy yếu, màn hình DPI khác hay soak test hàng giờ.
Kết luận phù hợp: chưa thấy nghẽn EDT trong kịch bản đã đo; vẫn còn lỗi tính đúng.

## 3. Nguồn tham khảo và dấu hiệu phát triển hiện nay

Đây là khảo sát chọn lọc nguồn gốc của dự án/nền tảng và đơn vị tổ chức,
không phải thống kê thị phần. Các tính năng công bố trên website chưa được
mình kiểm thử đầy đủ; nội dung beta được hiểu là beta.

| Nguồn | Điều đã kiểm chứng trên nguồn | Ý nghĩa đối với đồ án |
| --- | --- | --- |
| [RIF — International Rules of Gomoku](https://www.renju.net/gomokurules/) | Bàn 15×15, thắng đúng 5, trình tự khai cuộc Swap2 | Tách tên luật thắng và luật khai cuộc, ghi rõ quy tắc đang chơi |
| [Gomocup — quy tắc](https://gomocup.org/detail-information/) | Có Freestyle, Standard, Renju và Caro; Caro của giải dùng đúng 5 và không chặn hai đầu | Khi so engine phải chọn cùng luật, không gộp mọi biến thể thành một |
| [Gomocup 2026](https://gomocup.org/results/gomocup-result-2026/) | Có engine dùng alpha–beta/TT/iterative deepening và engine neural/MCTS; Rapfi thắng Freestyle 15×15, AlphaGomoku thắng Caro | Học cách kết hợp tìm kiếm, tactical layer, kiểm chứng; chưa cần chuyển nền tảng AI |
| [Rapfi](https://github.com/dotb1ue/rapfi) | Alpha–beta với evaluation classical/NNUE; GUI và engine tách nhau, giao tiếp qua Piskvork | Tham khảo kiến trúc engine và dùng làm đối chứng khi tương thích luật |
| [Board Game Arena — Gomoku](https://en.boardgamearena.com/gamepanel?game=gomoku) | Nêu rõ biến thể/khai cuộc; có tùy chọn tọa độ và đánh dấu nước cuối | Ưu tiên giải thích luật, đọc bàn rõ và chống đánh nhầm |
| [PlayOK — Gomoku](https://www.playok.com/en/gomoku/) | Công bố phòng chơi, xếp hạng, thống kê và game records | Luồng phòng ổn định, lưu ván và xem lại là tính năng có giá trị lâu dài |
| [Gomoku Party](https://gomokuparty.com/) | Công bố puzzle, study/variation và engine review; phần coaching ghi beta | Hướng học cờ và phân tích lại ván đáng tham khảo, không chỉ tăng sức AI |
| [Gomoku Party — dữ liệu](https://gomokuparty.com/database/) | Công bố bản ghi SGF, metadata luật, tập dữ liệu CC0 | Có thể nghiên cứu replay và corpus; phải lọc đúng luật và kiểm tra dữ liệu tải thực tế |
| [Yixin-Board](https://github.com/dotb1ue/Yixin-Board) | GUI dùng giao thức engine riêng và hỗ trợ nhiều engine tương thích | Tách engine/controller/view giúp đổi engine mà không sửa toàn bộ UI |
| [Gomoku AI protocol](https://plastovicka.github.io/protocl2en.htm) | Giao thức tiến trình có TURN/BOARD/INFO/END và tham số thời gian/luật | Đây là giao thức engine, không thay trực tiếp giao thức LAN giữa người chơi |
| [Allis — threat-space search](https://research.tilburguniversity.edu/en/publications/go-moku-and-threat-space-search/) | Tài liệu nghiên cứu gốc năm 1993 về tìm kiếm không gian đe dọa | Định nghĩa chiến thuật từ ô cần đánh/phòng thủ và chuỗi ép buộc, thay vì chỉ cộng điểm cửa sổ |

### Nhận định của mình từ khảo sát

Ba hướng thấy rõ trong các nguồn đã xem: engine kết hợp chiến thuật với tìm
kiếm/đánh giá tốt hơn; nền tảng ghi rõ nhiều biến thể; trải nghiệm học cờ
qua bản ghi, xem lại, puzzle và phân tích. Đây là nhận định từ mẫu nguồn,
không khẳng định mọi sản phẩm đều theo cùng một hướng.

Luật hiện tại của dự án phải được giữ theo quyết định đã chấp nhận:

| Luật | Độ dài thắng | Chặn hai đầu |
| --- | --- | --- |
| Dự án RULE_A | Đúng 5 | Không thắng nếu cả hai đầu là đối thủ |
| Dự án RULE_B, mặc định | Từ 5 trở lên | Không thắng nếu cả hai đầu là đối thủ |

RULE_B hiện tại không tương đương Freestyle thông thường; RULE_A cũng không
nên tự đặt tên là Standard RIF vì còn điều kiện chặn. BGA và Gomocup còn có
cách mô tả Caro khác nhau ở overline. Ghi rõ một tên luật của dự án và mô tả
điều kiện; chỉ thêm biến thể chuẩn khi đã có bộ kiểm thử riêng. Không đổi
luật hiện có chỉ để giống một website.

## 4. Ý kiến ưu tiên nâng cấp

Mình đề xuất định hướng: **game Caro/Gomoku gọn, phản hồi nhanh, chơi LAN
đáng tin và hỗ trợ học cờ bằng replay + gợi ý có giải thích**. Đây là lựa
chọn sản phẩm cho đồ án này, không phải tính năng đã triển khai.

| Ưu tiên | Nâng cấp | Giá trị | Điều kiện / phạm vi |
| --- | --- | --- | --- |
| A — phải làm trước | Sửa F1–F5 | Ván đúng, kết quả đến đúng lúc, nút không biến mất, test có ý nghĩa | Hoàn tất đợt ổn định hiện tại |
| B — rất nên làm | Tọa độ hàng/cột, highlight chuỗi thắng, danh sách/số nước đi tùy chọn | Đọc bàn dễ, giải thích và demo tốt | Mức nhỏ–vừa; không chèn chữ lên mọi quân mặc định |
| B — rất nên làm | Lưu/mở ván, replay tiến/lùi và về vị trí bất kỳ | Chia sẻ bug, kiểm thử lại, học từ ván cũ | Lưu luật, thứ tự nước, màu, kết quả và phiên bản định dạng |
| B — rất nên làm | Sửa heuristic + corpus chiến thuật | AI đánh hợp lý, hint có căn cứ | Bắt buộc trước gợi ý/đánh giá ván |
| C — tiếp theo | Gợi ý 1–3 nước và lý do ngắn | Giá trị học tập và khác biệt của đồ án | Ví dụ “chặn nước thắng ngay”, “tạo hai đường đe dọa”; không bịa phần trăm thắng |
| C — tiếp theo | Iterative deepening với ngân sách thời gian | Phản hồi có giới hạn trên máy yếu, giữ nước tốt nhất từ depth hoàn tất | Giữ hủy worker, fallback hợp lệ, test timeout |
| C — sau đo đạc | Cache thế cờ bằng Zobrist/TT, đánh giá lại đường bị ảnh hưởng | Giảm tính lặp, tăng độ sâu có kiểm soát | Khóa gồm lượt và luật; bảng có giới hạn bộ nhớ, test bound/depth |
| C — trải nghiệm | Bảng thông báo kết quả trên bàn, trạng thái phòng rõ, âm lượng riêng | Ít gián đoạn, người chơi biết đang chờ/kết nối/lỗi | Không để thông báo chặn giao dịch nước đi |
| D — khi bản cơ sở ổn | Chọn màu luân phiên, đồng hồ và khai cuộc Swap2 | Công bằng hơn, tiến tới chế độ thi đấu | Đổi state machine, nhân đôi test lượt/Undo; nên làm từng phần |
| D — nếu còn thời gian | Engine mạnh bên ngoài để phân tích; puzzle tuyển chọn | Mở rộng luyện tập | Chọn đúng luật; kiểm tra giấy phép của engine/dữ liệu trước khi phân phối |

### Cách nâng AI mình ưu tiên

1. Sửa điểm thắng/overline và nhận diện mối đe dọa độc lập.
2. Kiểm tra nước thắng ngay, nước phòng thủ bắt buộc và chuỗi ép thắng;
   không để tổng điểm của các mẫu yếu vượt ưu tiên một kết quả thắng thật.
3. Giữ alpha–beta, bổ sung iterative deepening + hạn thời gian.
4. Sau benchmark mới thêm TT/Zobrist, incremental evaluation và VCF.
5. Đánh giá bằng tỷ lệ giải đúng corpus và self-play nhiều khai cuộc,
   đổi màu. Không gọi mức Hard mạnh hơn chỉ vì depth cao hơn.

Để báo cáo đồ án rõ ràng, mỗi phép đo phải ghi seed/corpus, luật, giới hạn
thời gian, warmup, số lần chạy, median/p95/max, nodes và nhánh trả lời sớm.

### Những phần nên để sau

Huấn luyện NNUE/AlphaZero, tài khoản/Elo toàn cầu, cloud matchmaking, cửa
hàng skin, bàn 3D và nhiều biến thể cùng lúc đều tăng phạm vi đáng kể.
Mình chưa ưu tiên chúng trước replay, luật đúng và LAN ổn định. Neural
evaluation cần dữ liệu/huấn luyện/đánh giá riêng; không mặc định tốt hơn
heuristic trong một đồ án có thời gian giới hạn.

## 5. Kiến trúc và phát hành

- Tách dần phần áp dụng nước đi/kết quả/đổi ván thành GameSession hoặc
  controller. Board giữ dữ liệu/luật; Swing nhận sự kiện để vẽ; network nhận
  sự kiện đã hoàn tất. Làm từng bước có regression, không viết lại toàn bộ.
- Giao thức LAN nên có version, mã ván do host quyết định, ply và ruleId;
  log chẩn đoán có giới hạn để tái hiện lỗi. UI chỉ trình bày thông tin cần
  cho người chơi, không hiển thị token/chi tiết nội bộ.
- Chuẩn hóa build/test bằng một lệnh, tách source sản phẩm và test khỏi
  thư mục class cũ. Chốt phiên bản Java, tránh test class cũ che bản mới.
- Khi phát hành, có thể đóng gói kèm runtime bằng
  [jpackage](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html),
  để máy demo không phải cài JDK và biên dịch khi mở game. Bản đóng gói cần
  được tạo/kiểm tra trên nền tảng đích.

## 6. Điều kiện mình muốn đạt trước khi chuyển task lớn

1. Probe F1–F4 chuyển thành regression test và PASS sau sửa.
2. Kiểm thử LAN thắng/hòa/chơi lại đồng thời/ngắt giữa lượt/kết nối lại;
   không có ô hoặc điểm khác nhau giữa hai bên sau mỗi nước.
3. Không callback kết quả hay popup nào làm chậm gửi nước cuối.
4. Bộ thế chiến thuật phân biệt được single-three/double-three và luật chặn.
5. Header không cắt nút ở VI/EN/PT, các kích thước màn hình/DPI mục tiêu.
6. Thay hoặc ghi pending các ca console không kiểm hành vi; test render
   phát hiện được nút nằm ngoài container.
7. Chạy từ build sạch, toàn bộ JUnit PASS, Preferences thật không đổi.
8. Chơi thử LAN hai máy và audio thật, rồi mới chốt commit/push.

Mốc trải nghiệm đề xuất để đo sau sửa: tương tác thường không chặn EDT
quá 50 ms; ngân sách AI ban đầu có thể đặt khoảng 100/300/1000 ms theo độ
khó và cho phép dừng. Đây là mục tiêu thiết kế, chưa là cam kết đo đủ mọi máy.

## 7. Tái hiện review

Từ thư mục gốc repo, sau biên dịch source vào bin:

```powershell
javac -encoding UTF-8 -cp bin -d bin_test tools/audit/CurrentAuditProbe.java tools/audit/AuditBenchmark.java
java -Dstdout.encoding=UTF-8 -cp "bin_test;bin" CurrentAuditProbe
java -Dstdout.encoding=UTF-8 -cp "bin_test;bin" AuditBenchmark
```

Trong PowerShell có thể cần đặt tham số JVM trong dấu nháy đơn:
`java '-Dstdout.encoding=UTF-8' ...`. Probe socket cần quyền localhost;
các test Preferences cần quyền Registry của người dùng. Các probe không
đọc/ghi node cài đặt thật. Ảnh probe xuất vào bin_test/audit-*.png; bản ảnh
được lưu kèm báo cáo trong docs/audit-2026-10-26.
