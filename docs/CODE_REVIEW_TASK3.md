# Rà soát mã nguồn đến Task 3 — 2026-10-26

> Review tổng hợp mới nhất: [REVIEW_AND_UPGRADE_PLAN.md](REVIEW_AND_UPGRADE_PLAN.md).
> Lần kiểm tra sau bản vá P2 tái hiện thêm lỗi MOVE cũ qua ranh giới ván,
> gửi nước cuối sau callback kết quả và header VI → PT mất nút âm thanh.
> Heuristic và giới hạn của các ca test console vẫn còn mở. Chưa chốt Task 3.

Phạm vi: model, Minimax/heuristic, BoardPanel, MainFrame, LAN, ngôn ngữ,
âm thanh, Preferences và các bộ kiểm thử. Đây là báo cáo review; chưa sửa
logic sản phẩm ở thời điểm review ban đầu, chưa commit hoặc push.

## Cập nhật sau bản vá P1 — 2026-10-26

- Ba lỗi P1 (mục 1–3) đã sửa. Đổi ngôn ngữ chỉ cập nhật UI và dịch lại trạng
  thái thắng/hòa; Undo PvP khôi phục quân vừa rút; MOVE kiểm tra chế độ,
  lượt đối thủ và gameId khi xử lý callback đã xếp hàng trên EDT.
- Thêm `test/game/P1RegressionTest.java`: 16 ca PASS. 12 ca ban đầu đã chạy
  đỏ trên mã cũ trước khi sửa. Các thao tác Swing chạy trên EDT; test mới
  dùng Preferences trong bộ nhớ riêng.
- JUnit toàn bộ: 102/102 PASS, 0 skipped/aborted/failed. Kiểm tra node thật
  /game/view không đổi: {soundEnabled=true}.
- RuleAndDrawTest: 17/17. FullGomokuTestSuite: output 80/80; giới hạn các ca
  kiểm thử hằng ở phần dưới vẫn còn, chưa sửa trong phạm vi ba lỗi P1.
- Gói mạng đã chờ EDT trước một lần reset bị loại. Gói cũ chỉ mới đến callback
  sau khi chuyển phiên vẫn cần định danh phiên/ván trong giao thức (P2).
- Các mục P2 phía dưới vẫn còn mở; chưa kiểm thử LAN hai máy, chưa commit/push.

## Kết luận

### Cập nhật bản vá ổn định P2 — 2026-10-26

- Đã sửa các mục 5–10 bên dưới: listener được gỡ khi dispose; frame dừng AI
  và đóng kết nối; dialog hủy cả host/join đang chờ, trả selector về chế độ
  cũ và giữ nguyên ván khi hủy; đóng dialog thành công giữ kết nối.
- NetworkManager dùng Session sở hữu socket/server/stream riêng. Cancel
  vô hiệu hóa callback trước khi đóng socket; luồng cũ không đóng được socket
  mới. Callback được kiểm tra danh tính phiên lúc thực thi trên dispatcher EDT.
- JOIN có timeout 5 giây, các worker LAN/audio là daemon. MOVE hỏng định dạng
  bị bỏ qua thay vì làm rớt kết nối. MOVE trên EDT áp dụng ngay để giữ thứ tự
  callback MOVE/RESTART, không xếp thêm một lượt invokeLater.
- Lỗi HOST/JOIN/CHANNEL trả về dạng dữ liệu và được Lang dịch VI/EN/PT.
  Chi tiết exception hệ điều hành không được dùng làm chuỗi UI.
- Audio giới hạn 2 luồng phát, 8 tác vụ chờ; Clip đóng trong finally cả khi
  open/start lỗi, phát xong hoặc hết thời gian chờ. Test Clip dùng proxy,
  không yêu cầu thiết bị âm thanh thật.
- Minimax kiểm tra interrupt ở vòng lặp/đệ quy, finally hoàn tác các nước đệ
  quy khi bị hủy. Mỗi SwingWorker có MinimaxAI riêng và chụp difficulty lúc
  bắt đầu; thống kê không dùng chung giữa worker cũ/mới.
- I18nSourceScanTest đọc src/game/view tương đối với thư mục gốc dự án.
- 21 ca hồi quy mới: NetworkStabilityTest (7, socket localhost thật),
  P2LifecycleTest (11, Swing/AI/ngôn ngữ), SoundResourceTest (3).
- Toàn bộ JUnit: 123/123 PASS, 0 skipped/aborted/failed. Các test có Registry
  và localhost chạy ngoài sandbox; node thật /game/view không đổi:
  {soundEnabled=true}. RuleAndDrawTest 17/17; output FullGomokuTestSuite 80/80.
- Còn mở: heuristic ở mục 4; thay các kiểm thử hằng T53–T57 và test contrast;
  kiểm thử hai máy LAN/thiết bị âm thanh thật. Giao thức vẫn chưa mang gameId,
  nên chưa xử lý triệt để MOVE cũ đến muộn qua ranh giới ván trong cùng phiên.
- Chưa commit hoặc push.

Chưa nên coi Task 3 đã hoàn thiện. Có lỗi chức năng cần sửa trước tối ưu
hiệu năng. Các test hiện có vẫn bỏ sót một số tình huống chơi thực tế.
Luật thắng trong Board và cơ chế ScoreTracker theo gameId có nền tảng tốt;
việc sao chép Board cho AI và kiểm tra token trước áp dụng kết quả cũng đúng
hướng. Những phần này chưa cần viết lại toàn bộ.

## Lỗi được tái hiện

### 1. P1 — Đổi ngôn ngữ làm mất ván và có thể gây lệch bàn LAN

- Vị trí: `src/game/view/MainFrame.java:542`.
- `updateLanguage()` gọi `boardPanel.resetGame()` mỗi lần Lang phát sự kiện.
- Thử bằng MainFrame trên EDT với Preferences trong bộ nhớ: đánh một nước
  PvP rồi VI → EN. Lịch sử từ 1 về 0, gameId từ 2 thành 3.
- Với LAN, reset chỉ xảy ra phía đổi ngôn ngữ, không gửi RESTART cho đối thủ.
- Hướng sửa: chỉ cập nhật nội dung UI. Lưu trạng thái kết quả bằng dữ liệu
  winner/draw để dịch lại nhãn kết thúc; giữ bàn, lượt, gameId và AI đang chạy.

### 2. P1 — Undo PvP sau nước thắng trả sai lượt

- Vị trí: `src/game/view/BoardPanel.java:200`.
- Nước bình thường đảo currentTurn, nhưng nước thắng/hòa return trước bước
  đảo. Undo lại luôn gọi opposite nên sai với ván vừa kết thúc.
- Tái hiện bằng playMove thật: X thắng ở nước thứ 9, Undo rút quân X nhưng
  currentTurn là O. Kỳ vọng phải là X.
- Hướng sửa: lấy quân tại ô vừa rút trước khi undo, khôi phục lượt về quân đó.
  Thêm regression cho X thắng, O thắng và nước hòa cuối cùng.

### 3. P1 — Nước LAN không được xác thực chế độ và lượt

- Vị trí: `src/game/view/BoardPanel.java:264-272`.
- applyOpponentMove chỉ kiểm tra gameOver và ô hợp lệ, sau đó dùng currentTurn.
- Khi host đang lượt X, gọi nhận nước đối thủ vẫn đặt X. Sau chuyển sang PvP,
  gọi nhận nước LAN vẫn đặt quân lên bàn PvP.
- Hướng sửa: kiểm tra ONLINE, phiên kết nối và lượt đối thủ ở thời điểm chạy
  callback EDT; đặt đúng quân đối thủ. Giao thức cần định danh ván nếu muốn
  loại bỏ gói cũ sau restart, vì chỉ kiểm tra lượt chưa giải quyết hết vấn đề.

### 4. P2 — Heuristic mâu thuẫn với luật Board và nhận nhầm bẫy đôi

- Vị trí: `src/game/ai/HeuristicEvaluator.java:102,129,146-158`.
- Với hàng OXXXXXXO, Board.checkWin=false theo RULE_B, nhưng heuristic cho
  X 200,000,000, gấp đôi FIVE=100,000,000. Cửa sổ 5 ô bỏ qua hai đầu thật
  của toàn chuỗi 6 nên đánh giá nhầm thành thắng.
- Chỉ một chuỗi XXX mở ở giữa bàn nhận 2,264,300 điểm, vượt DOUBLE_THREE
  2,000,000: hai cửa sổ chồng nhau bị tính như hai mối đe dọa độc lập.
- MinimaxAI dùng các ngưỡng này để trả nước ngay trước tìm kiếm, nên lỗi có
  thể làm AI bỏ qua Minimax trên nhận diện chiến thuật sai.
- Hướng sửa: đánh giá chuỗi cực đại đúng luật, khử trùng các mối đe dọa;
  không dùng tổng điểm cả bàn như bằng chứng một nước tạo được bẫy mới.

### 5. P2 — Frame dispose vẫn bị giữ bởi listener ngôn ngữ

- Vị trí: `src/game/view/MainFrame.java:266`; `Lang.java:22-39`.
- Listener this::updateLanguage được thêm vào danh sách static, không được
  gỡ khi dispose. Đổi ngôn ngữ sau dispose vẫn làm gameId frame cũ tăng.
- Điều này giữ frame và toàn bộ cây component trong bộ nhớ, đồng thời khiến
  test có tác động chéo qua các frame tưởng đã được dọn dẹp.
- Hướng sửa: giữ một Runnable cố định làm field; removeChangeListener với
  đúng instance đó khi dispose. Nếu hỗ trợ mở lại frame cần quản lý đăng ký lại.

## Rủi ro khác qua đọc mã

### 6. P2 — Đóng hộp thoại LAN chưa hủy thao tác kết nối

- `NetworkDialog` không có handler đóng để hủy host/join.
- `MainFrame` chỉ disconnect khi isConnected=true, nên thao tác host đang
  chờ accept không được hủy khi quay sang chế độ khác.
- Sau đóng dialog, modeCombo có thể vẫn ONLINE trong khi BoardPanel còn ở
  chế độ trước. Kết nối đến muộn có thể tự chuyển bàn về ONLINE.
- Cần quản lý kết quả dialog (connected/cancelled), hủy cả kết nối đang chờ,
  đưa combo về chế độ thực tế và bỏ callback của phiên cũ.

### 7. P2 — Các lần kết nối dùng chung tài nguyên thay đổi giữa nhiều thread

- Vị trí: `src/game/network/NetworkManager.java:54-100,128-135,160-179`.
- Các thread host/join/listen đọc ghi chung socket/in/out/isConnected; thread
  cũ finally gọi disconnect() trên tài nguyên field hiện tại, có nguy cơ đóng
  cả phiên mới. Không có định danh phiên hoặc cơ chế khóa vòng đời.
- Hướng sửa: tài nguyên thuộc từng phiên, callback kèm token, hủy đóng đúng
  phiên. Đặt timeout kết nối rõ ràng. Không chỉ thêm volatile rồi coi đã xong.
- Rủi ro này được xác định từ mã, chưa chạy kiểm thử LAN hai máy hoặc stress.

### 8. P2 — Clip âm thanh không được đóng

- Vị trí: `src/game/view/SoundManager.java:83-85`.
- Mỗi lần phát tạo Clip mới nhưng không đóng sau khi kết thúc; stream cũng
  không có quản lý vòng đời rõ ràng. Chơi lâu có nguy cơ giữ tài nguyên audio.
- Hướng sửa: đóng Clip khi STOP, dọn cả khi open lỗi; cân nhắc tổng hợp buffer
  một lần và phát qua cơ chế có giới hạn số luồng/clip. Chưa đo tài nguyên mixer.

### 9. P2 — Hủy SwingWorker chưa dừng việc tính AI

- BoardPanel gọi cancel(true), nhưng MinimaxAI không kiểm tra interrupt ở
  vòng lặp, tạo candidate hay đệ quy. Token chặn kết quả cũ vào bàn, còn CPU
  vẫn có thể tiếp tục làm việc. Worker cũ và mới dùng cùng MinimaxAI với các
  field difficulty/thống kê thay đổi được.
- Hướng sửa: kiểm tra hủy trong tìm kiếm, chụp difficulty cho từng lượt,
  kết quả/thống kê thuộc từng lần tìm kiếm và hoàn tác Board bằng finally.

### 10. P2 — Lỗi kết nối LAN chưa đi qua hệ thống ngôn ngữ

- NetworkManager.java:63,80,99 tạo thông báo tiếng Việt cố định.
- MainFrame chuyển nguyên chuỗi đến NetworkDialog.onConnectionFailed.
  Vì vậy người dùng chọn EN/PT vẫn thấy lỗi tiếng Việt.
- Source scan hiện chỉ quét view nên không phát hiện.
- Hướng sửa: callback trả loại lỗi và tham số; view dùng Lang để định dạng.

## Giới hạn của bằng chứng kiểm thử hiện tại

- FullGomokuTestSuite.java:413-415: T55–T57 so sánh hai chuỗi hằng giống nhau,
  không gọi logic đầu hàng, hết giờ hay kết thúc ván. T53 chỉ kiểm tra thắng
  rồi `isOver && true`, chưa thử click bị từ chối; T54 chỉ thử opposite().
  Không dùng output 80/80 làm bằng chứng các hành vi này đã được kiểm chứng.
- I18nSourceScanTest.java:22-23 gán cứng đường dẫn ổ C của một máy; chuyển
  thư mục hoặc checkout khác sẽ không kiểm đúng source đang chạy.
- LangPurityTest C dùng Color tự khai báo trong test; production đổi sang màu
  khác thì test vẫn xanh. Cần lấy màu/component thực và chạy Swing trên EDT.
- Một số test UI tạo/chỉnh Swing ngoài EDT. Cần gom thao tác UI vào EDT để
  kết quả đáng tin hơn và tránh sai khác với cách ứng dụng chạy thật.
- Cần giữ classpath `bin_test;bin`, hoặc dùng thư mục build sạch, tránh class
  test cũ trong bin che bản mới.

## Hiệu năng: nên làm sau khi sửa lỗi chức năng

Benchmark có sẵn, một lần chạy trong phiên review:

| Mức | Thời gian | Nodes | Pruning |
| --- | ---: | ---: | ---: |
| Easy | 2 ms | 0 | 0 |
| Medium | 81 ms | 77 | 15 |
| Hard | 6 ms | 0 | 0 |

Hard ở đây trả về từ nhánh chiến thuật, không chứng minh Minimax depth=3 nhanh
hơn Medium. Kết luận in cố định của AIBenchmarkTest về cắt hàng trăm/ngàn node
không được số liệu lần này hỗ trợ. Cần nhiều thế cờ, warmup, nhiều lần đo,
và phân biệt trả lời chiến thuật với tìm kiếm thực sự.

Sau đó mới profile evaluateMove/getCandidateMoves: mỗi candidate thử cả hai
quân và đánh giá lại toàn bàn, tạo nhiều mảng/list đường chéo. Có thể tái dùng
dữ liệu đường cờ và đánh giá các đường bị ảnh hưởng. Chưa cần tăng depth,
thêm transposition table hay viết lại UI khi chưa có số đo chứng minh cần.

## Output tái hiện

Probe nằm trong bin_test/ReviewProbe.java (thư mục build ignored), chạy trên
EDT cho các thao tác Swing, dùng AbstractPreferences trong bộ nhớ riêng,
không đọc/ghi node Registry thật của ứng dụng:

```text
PVP_WIN gameOver=true turn=X
PVP_UNDO expectedTurn=X actualTurn=O
LANG_CHANGE history=0 gameId=2->3
DISPOSED_FRAME language listener still active=true
LAN_WRONG_TURN expected=NONE actual=X
LAN_IN_PVP expected=NONE actual=X
BLOCKED_SIX boardWin=false heuristicScore=200000000 FIVE=100000000
SINGLE_OPEN_THREE heuristicScore=2264300 DOUBLE_THREE=2000000
```

## Thứ tự xử lý đề xuất

1. Regression rồi sửa reset khi đổi ngôn ngữ, Undo PvP cuối ván và xác thực MOVE.
2. Sửa vòng đời LAN, listener ngôn ngữ và Clip; hoàn thiện lỗi EN/PT.
3. Sửa nhận diện chiến thuật AI và hủy tìm kiếm.
4. Thay các kiểm thử hằng bằng kiểm thử hành vi; bỏ đường dẫn tuyệt đối trong test.
5. Chạy lại tất cả test và kiểm thử LAN thật; đo hiệu năng nhiều thế cờ.
6. Cập nhật trạng thái Task 3 theo bằng chứng rồi mới commit/push bản hoàn thiện.
