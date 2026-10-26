# GOMOKU PROJECT NOTES

## Task 3.5 decision log
- Baseline local commit 8cdbdc6 on task3-stabilize from origin/main 14bdbfe; 123 JUnit, 17 rule checks, console output 80 assertions before fixing empty checks.
- F1: protocol v2 START 2 UUID, MOVE UUID ply row col, client RESTART_REQUEST base, host RESTART base next. Host owns UUIDs, local gameId remains score-only. X/host still starts; no runtime rule selector, no ruleId sent. EOF only; no DISCONNECT write and no ACK/consent (Task 5).
- Restart request and host commit are separate phases. Same-base requests reset once; stale base is ignored. Client locks moves and sends once while awaiting the host. Version mismatch/5-second START timeout report translated errors. Invalid/overlong lines are ignored.
- F1 popup policy: dispose the old result dialog before reset; reject late result callbacks and gate victory playback by gameId. Preserve points already earned before reset. Protocol diagram and details: TASK35_PROTOCOL.md.
- F2: apply board/history, send MOVE, then notify UI/result popup. A gameId records/presents its result once; a callback returning from an old modal cannot update new-round history controls. Win/draw ordering regression added.
- F3: score terminal wins from complete runs with RULE_A/B and actual blocked endpoints; deduplicate overlapping patterns by the stones' bit mask per line; cap nonterminal evaluation below FIVE. Depth/difficulty/rules unchanged. Corpus covers both colors and rules: blocked/open six, single/cross double-three, open-four, edge five, and many simultaneous threats.
- F4: recompute combo text widths and pack after locale changes; board remains 596x596 preferred size with unchanged rendering/hit coordinates. Bounds checks cover direct VI/EN/PT startup, all locale transitions, VI->PT and VI->EN->PT. Current renders are in docs/task35-render/.

## 1. Cấu trúc thư mục và vai trò các lớp chính
- `src/game/model/Board.java`: Ma trận bàn cờ 15x15, đếm nước, đặt/hủy cờ, kiểm tra thắng (RULE_A/B) và hòa.
- `src/game/model/Piece.java`: Enum quân cờ (`NONE`, `X`, `O`) và hàm lấy quân đối thủ `opposite()`.
- `src/game/ai/Move.java`: Lớp dữ liệu lưu tọa độ nước đi `(row, col)`.
- `src/game/ai/HeuristicEvaluator.java`: Đánh giá điểm thế cờ tĩnh cho AI (chuỗi mở, bị chặn, bẫy 3-3, 4-4).
- `src/game/ai/MinimaxAI.java`: Thuật toán Minimax + Alpha-Beta Pruning tìm nước đi tối ưu theo 3 độ khó.
- `src/game/network/NetworkManager.java`: Quản lý Socket kết nối LAN (Host/Client), đa luồng gửi/nhận thông điệp.
- `src/game/view/BoardPanel.java`: Vẽ bàn cờ gỗ, xử lý tương tác chuột, quản lý lịch sử nước, Undo, kích hoạt AI.
- `src/game/view/MainFrame.java`: Cửa sổ Swing chính, thanh điều khiển, hiển thị bảng điểm, ngôn ngữ và mạng.
- `src/game/view/NetworkDialog.java`: Dialog Swing chọn tạo phòng (Host) hoặc vào phòng (Join IP).
- `src/game/view/Lang.java`: Quản lý đa ngôn ngữ (Tiếng Việt / English).
- `src/game/view/SoundManager.java`: Phát âm thanh hiệu ứng (đặt quân, chiến thắng) qua luồng âm thanh Java Sound.
- `src/game/Main.java`: Điểm khởi chạy ứng dụng GUI chính (`MainFrame`).
- `test/game/BoardWinTest.java`: Bộ test JUnit 5 kiểm thử chuyên sâu luật RULE_B (55 ca).
- `src/game/RuleAndDrawTest.java`: Kiểm thử luật chặn 2 đầu và bàn cờ kín 225 ô (17 ca).
- `src/game/FullGomokuTestSuite.java`: Bộ kiểm thử toàn diện luật RULE_A và RULE_B (57 ca / 80 assertions).
- `src/game/ConsoleTest.java` & `src/game/AIBenchmarkTest.java`: Test nhanh console và đo benchmark AI.
- `lib/junit-platform-console-standalone-1.10.2.jar`: Thư viện chạy độc lập bộ test JUnit 5 từ console.
- `run.bat`: Script Windows tự động biên dịch và chạy ứng dụng GUI.

## 2. Luồng xử lý một nước đi
- **Click chuột**: `BoardPanel.handleClick(px, py)` (BoardPanel.java:198) tính row/col -> kiểm tra `board.isValidMove(row, col)` (Board.java:54).
- **Thực thi nước đi**: Gọi `BoardPanel.executeMove(row, col, piece)` (BoardPanel.java:232):
  - Gọi `board.makeMove(row, col, piece)` (Board.java:62), thêm vào `moveHistory` (BoardPanel.java:234).
  - Kiểm tra thắng: `board.checkWin(row, col)` (Board.java:122) -> nếu true: gán `isGameOver = true`, báo `stateListener.onGameOver(msg, piece)` (BoardPanel.java:246).
  - Kiểm tra hòa: `board.isFull()` (Board.java:115) -> nếu true: gán `isGameOver = true`, báo hòa (BoardPanel.java:255).
  - Nếu chưa kết thúc: Đổi lượt `currentTurn = currentTurn.opposite()`, báo `stateListener.onTurnChanged` (BoardPanel.java:261-265).
- **Luồng AI**: Sau khi người đi ở PvAI, `BoardPanel.handleClick` gọi `triggerAI()` (BoardPanel.java:217). `SwingWorker` gọi `ai.findBestMove(snap)` chạy nền (BoardPanel.java:286), hàm `done()` gọi `executeMove(best.row, best.col, Piece.O)` (BoardPanel.java:294).
- **Luồng Mạng**: Click của người chơi gửi `networkManager.sendMove(row, col)` (BoardPanel.java:212). Bên nhận tại `NetworkManager.listenForMessages` (NetworkManager.java:109) đọc `MOVE r c`, gọi `listener.onMoveReceived`, `MainFrame:274` đẩy sang `BoardPanel.applyOpponentMove` (BoardPanel.java:224) rồi gọi `executeMove`.

## 3. Luật thắng hiện đang áp dụng
- **Cấu hình**: `Board.winRule` mặc định là `WinRule.RULE_B` (Board.java:17). `WinRule.RULE_A`: đúng 5 quân, 6+ không tính; `WinRule.RULE_B`: từ 5 quân trở lên đều thắng (Board.java:10-13).
- **Kiểm tra**: `Board.checkWin(row, col, rule)` (Board.java:129-184) quét 4 hướng (ngang, dọc, 2 chéo). Đếm quân liên tiếp từ ô vừa đánh. Điều kiện thắng: đủ số lượng quân và không bị đối thủ chặn đồng thời ở cả 2 đầu. Mép bàn cờ không bị coi là chặn.

## 4. Chế độ chơi và mức độ AI
- **PvP (Người vs Người)**: 2 người chơi chung trên một máy, luân phiên đánh theo `currentTurn` (BoardPanel.java:169, 261).
- **PvAI (Người vs Máy)**: Người đi trước (`Piece.X`), Máy đi sau (`Piece.O`). Thuật toán Minimax + Alpha-Beta (`MinimaxAI.java`).
  - 3 cấp độ khó tại `MinimaxAI.Difficulty` (MinimaxAI.java:15-32):
    - `EASY`: depth = 1, duyệt lân cận, chọn ngẫu nhiên trong top 3 nước tốt nhất.
    - `MEDIUM`: depth = 2, nhận diện thế ngắt quãng, ưu tiên chặn Open 4 khẩn cấp, cắt tỉa 16 nhánh.
    - `HARD`: depth = 3, nhận diện bẫy đôi 3-3 / 4-4, phân tích sâu, cắt tỉa 22 nhánh.
- **Online (LAN Socket)**: 2 máy nối qua TCP Socket (`NetworkManager.java`). Host đóng vai trò `Piece.X`, Client là `Piece.O` (BoardPanel.java:69, 142). Chỉ cho phép đánh khi `currentTurn == myPiece` (BoardPanel.java:102, 202).

## 5. Cơ chế Undo hiện tại
- **Lưu trữ**: `BoardPanel.moveHistory` kiểu `List<Move>` (BoardPanel.java:68).
- **Số nước hoàn tác**:
  - **PvP**: Hoàn tác đúng 1 nước (`undoMove`, BoardPanel.java:197-201).
  - **PvAI**: Xác định quân của nước cuối (`BoardPanel.java:205-207`). Nếu nước cuối là `Piece.O` (AI vừa đánh), rút 2 nước; nếu nước cuối là `Piece.X` (người chơi thắng hoặc hòa ở nước 225), rút đúng 1 nước (BoardPanel.java:207-213).
  - **Online**: Cấm Undo (`canUndo()` trả về false khi `gameMode == GameMode.ONLINE`, BoardPanel.java:144). Giao thức mạng không hỗ trợ Undo.
- **Khôi phục trạng thái & AI Worker**: Xóa quân trên bàn cờ (`board.undoMove`), đặt `isGameOver = false`, khôi phục `currentTurn` (PvP đảo ngược, PvAI đặt `Piece.X`), cập nhật `lastMoveRow/Col`, thông báo `stateListener`. Khi Undo, `aiExecutionToken++` được tăng và `aiWorker.cancel(true)` được gọi để hủy bỏ nước cờ nền đang tính toán của AI (BoardPanel.java:189-194). `gameId` được giữ nguyên không đổi.

## 6. Quản lý thành tích (Scoreboard)
- **Cấu trúc lưu trữ**: Tách biệt độc lập trong `ScoreTracker` (`src/game/model/ScoreTracker.java`). `MainFrame.java:40` sở hữu một instance `ScoreTracker`.
- **Ghi nhận theo gameId**: `ScoreTracker.recordResult(gameId, winner, humanPiece)` chỉ ghi nhận kết quả duy nhất một lần cho mỗi `gameId`. `MainFrame` gọi hàm này trong listener `onGameOver(gameId, ...)` (MainFrame.java:225-231).
- **Undo và Reset**: Undo không làm thay đổi `gameId`, không trừ điểm và không cho phép ghi đè điểm số của ván đó. Bấm "Ván mới" hoặc nhận `RESTART` từ mạng gọi `boardPanel.resetGame()`, tăng `gameId` lên 1 và gọi `scoreTracker.newGame()` (MainFrame.java:191-193, 290-292).
- **Lưu trữ RAM**: Điểm số (`humanWins`, `aiWins`, `draws`) chỉ lưu trong bộ nhớ RAM, dùng chung cho cả 3 chế độ (PvP/PvAI/Online) và mất toàn bộ khi tắt ứng dụng.

## 7. Giao thức mạng (Network Protocol)
Hiện tại `NetworkManager.java:109-125` chỉ hỗ trợ đúng 3 loại gói tin văn bản qua Socket TCP:
- `MOVE <row> <col>`: Gửi tọa độ nước cờ vừa đánh sang máy đối thủ.
- `RESTART`: Gửi tín hiệu yêu cầu chơi lại ván mới.
- `DISCONNECT`: Thông báo ngắt kết nối chủ động trước khi đóng Socket.

## 8. Biên dịch và kiểm thử
- **Lệnh biên dịch**:
  - Mã nguồn: `javac -encoding UTF-8 -d bin src/game/model/*.java src/game/ai/*.java src/game/network/*.java src/game/view/*.java src/game/*.java`
  - Test JUnit: `javac -encoding UTF-8 -cp "lib/*;bin" -d bin_test test/game/*.java`
- **Lệnh chạy test và số ca kiểm thử**:
  - `java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path "bin_test;bin" --scan-class-path`: Chạy toàn bộ 72 ca kiểm thử JUnit 5 (55 ca `BoardWinTest` + 11 ca `UndoScoreTest` + 6 ca `UndoParityTest`).
  - `java -cp bin game.RuleAndDrawTest`: Chạy `src/game/RuleAndDrawTest.java` (17 ca: 8 chặn hai đầu + 9 hòa/kín bàn cờ).
  - `java -cp bin game.FullGomokuTestSuite`: Chạy `src/game/FullGomokuTestSuite.java` (57 ca / 80 assertions so sánh RULE_A và RULE_B).
  - `java -cp bin game.ConsoleTest`: Kiểm thử trực quan console cơ bản.
  - `java -cp bin game.AIBenchmarkTest`: Đo lường thời gian, nodes duyệt và cắt tỉa của AI.

## 9. Hạn chế và rủi ro phát hiện trong code
- **[ĐÃ SỬA] Ghi trùng thành tích khi Undo**: Đã thêm `gameId` và lớp `ScoreTracker` (`src/game/model/ScoreTracker.java`); kết quả đầu tiên của một `gameId` là kết quả cuối cùng, Undo không làm sai lệch điểm số.
- **[ĐÃ SỬA] Race Condition luồng AI Worker**: Đã quản lý `aiWorker`, token `aiExecutionToken`, `capturedGameId`, `capturedMoveCount` trong `BoardPanel.java:335-350`; hủy và loại bỏ triệt để nước đi trễ khi `resetGame()` hoặc `undoMove()`.
- **[ĐÃ SỬA] Parity Undo PvAI**: Đã sửa nhánh PvAI trong `BoardPanel.undoMove()` (BoardPanel.java:204-213); phân biệt quân cuối là O (rút 2 nước) hay X (rút 1 nước), ngăn chặn lỗi người chơi đi 2 nước liên tiếp.
- **Gán cứng lượt chơi khi Undo PvAI**: Dòng 214 `BoardPanel.java` gán cố định `currentTurn = Piece.X`, sẽ lỗi nếu sau này mở rộng cho AI đi trước.
- **Chế độ Online thiếu đồng bộ trạng thái & gói tin trễ**: Giao thức không có gói tin Undo. Ngoài ra, giao thức mạng không mang `gameId`, nên gói `MOVE` đến trễ sau gói `RESTART` có thể lọt vào ván mới nếu ô cờ đó hợp lệ (giới hạn còn lại).
- **Thành tích gộp chung và mất khi thoát**: Điểm số dùng chung cho cả 3 mode (PvP/AI/Online) và mất toàn bộ khi tắt app do chỉ lưu trong RAM.

## 10. Tiến độ
| Hạng mục | Trạng thái | Commit hoặc nhánh liên quan | Ghi chú |
| :--- | :--- | :--- | :--- |
| Luật RULE_B và chặn hai đầu | Xong | Commit `7c82de0` | Đã triển khai trong `Board.java`, chặn 2 đầu, mép không chặn; kiểm thử đạt 100%. |
| Xử lý hòa và khóa sau kết thúc | Xong | Commit `7c82de0` | Xử lý hòa khi đủ 225 ô (`Board.java:115`), khóa click chuột khi kết thúc ván (`BoardPanel.java:234`). |
| PROJECT_NOTES | Xong | Nhánh `fix-undo-score` | Đã cập nhật đầy đủ kiến trúc, số dòng thực tế, cơ chế `ScoreTracker` và test suite. |
| Undo và tách thành tích (Task 2) | Xong (code + test tự động 72/72) | Nhánh `fix-undo-score` | Tách `ScoreTracker`, quản lý theo `gameId`, vá race condition AI worker, sửa parity Undo PvAI. **Kiểm thử tay đã làm**: Ca C (Undo sau khi AI thắng, điểm 0-1) đạt. **CHƯA kiểm thử tay**: chế độ LAN thật, Undo sau khi người thắng qua giao diện (đã có test tự động P1/P1b/P4/P5). |
| Các test hiện có (số ca từng bộ) | Xong | Nhánh `fix-undo-score` | JUnit 72 ca (55 `BoardWinTest` + 11 `UndoScoreTest` + 6 `UndoParityTest`), `RuleAndDrawTest` (17 ca), `FullGomokuTestSuite` (57 ca / 80 assertions) đều PASS 100%. |

### Backlog tồn đọng:
1. Lỗi AI chỉ đánh quanh quân góc đầu tiên, bỏ qua thế cờ ở giữa bàn.
2. Ô Chế độ (Game Mode) và Độ khó (Difficulty) hiển thị trống trên giao diện khi khởi động.

### Kiểm chứng Task 3 — 2026-10-26
- Task 3 hoàn thành triển khai UI, âm thanh và VI/EN/PT; chưa commit lần 2, chưa push.
- JUnit: 86/86 PASS, 0 skipped, 0 failed; RuleAndDrawTest: 17/17; FullGomokuTestSuite: 80/80 assertions (57 tình huống).
- Chạy JUnit với bin_test đứng trước bin để tránh class test cũ còn trong bin che bản mới.
- Sandbox hiện tại: 79/86 PASS, 7 lỗi SecurityException do Windows Registry. Chạy ngoài sandbox bằng quyền được cấp; không bỏ test, không dùng PreferencesFactory giả.
- AppSettings đã hỗ trợ inject Preferences. Hai bộ test UI dùng node có UUID; @AfterEach removeNode, flush, xác nhận node test bị xóa và so sánh toàn bộ key/value node /game/view với snapshot trước test. Kết quả node thật trước/sau: {soundEnabled=true}, key language không tồn tại và vẫn không tồn tại.
- NetworkDialog là modal, tiêu đề lấy qua Lang tại constructor; không cập nhật trực tiếp khi Lang đổi trong lúc dialog còn mở. Mở lại dialog sẽ lấy ngôn ngữ hiện tại.
- LangPurityTest C kiểm tra tỷ lệ tương phản từ các màu khai báo trong test; chưa chứng minh màu thực tế của ô combo bằng kiểm tra pixel.

### Bản vá ba lỗi P1 sau review — 2026-10-26
- Đổi ngôn ngữ giữ nguyên bàn cờ, lịch sử, lượt, gameId, điểm và AI đang suy nghĩ; trạng thái thắng/hòa được dịch lại qua Lang.
- Undo PvP lấy quân vừa rút để khôi phục đúng lượt, kể cả sau X/O thắng hoặc hòa.
- Nhận MOVE kiểm tra ONLINE, lượt đối thủ, ô hợp lệ, trạng thái kết thúc và gameId của callback đã xếp hàng trước reset. Chưa thay giao thức mạng hay vòng đời kết nối P2.
- Thêm P1RegressionTest: 16 ca; tổng JUnit hiện tại 102/102 PASS (0 skipped/failed). 12 ca hồi quy ban đầu đã xác nhận FAIL trên code cũ rồi PASS sau bản vá.
- RuleAndDrawTest 17/17; FullGomokuTestSuite output 80/80, nhưng T55–T57 so sánh chuỗi hằng nên chưa chứng minh hành vi thực tế. Xem CODE_REVIEW_TASK3.md để biết các giới hạn và P2 còn mở.
- Node Preferences thật /game/view trước/sau không đổi: {soundEnabled=true}. Chưa commit/push bản vá.

### Bản vá ổn định P2 — 2026-10-26
- LAN: mỗi phiên sở hữu socket riêng; hủy cả bind/connect/accept đang chờ, bỏ callback phiên cũ tại EDT, timeout JOIN 5 giây, giữ thứ tự MOVE/RESTART và bỏ gói MOVE hỏng định dạng.
- Dialog hủy trả về chế độ trước và giữ ván; kết nối thành công đóng dialog mà giữ socket. Port hợp lệ là 1–65535. Lỗi kết nối được dịch VI/EN/PT qua Lang.
- Dispose MainFrame gỡ listener ngôn ngữ, hủy AI và đóng LAN; không giữ frame cũ trong danh sách listener static.
- Audio dùng tối đa 2 luồng daemon/8 tác vụ chờ; luôn đóng Clip sau phát, timeout hoặc lỗi open/start.
- AI nhận interrupt và hoàn tác nước đang duyệt khi hủy; mỗi worker dùng MinimaxAI riêng và difficulty cố định cho lần tìm kiếm đó. Chưa sửa heuristic chiến thuật.
- Bỏ đường dẫn ổ C gán cứng trong I18nSourceScanTest; chạy test từ thư mục gốc repo.
- Thêm 21 test: 7 LAN socket localhost thật, 11 lifecycle/AI/ngôn ngữ, 3 audio proxy. JUnit toàn bộ 123/123 PASS, 0 skipped/failed; RuleAndDrawTest 17/17; FullGomokuTestSuite output 80/80 (giới hạn T53–T57 còn ghi trong báo cáo review).
- Node Preferences thật /game/view không đổi: {soundEnabled=true}. Chưa kiểm thử hai máy LAN và âm thanh trên thiết bị thật; chưa commit/push.

### Review tổng hợp trước khi chuyển task — 2026-10-26
- Chạy lại: JUnit 123/123 PASS, không skipped/aborted/failed; RuleAndDrawTest 17/17; output FullGomokuTestSuite 80/80 với giới hạn T53–T57 như đã ghi.
- Chưa chốt Task 3: probe mới tái hiện MOVE của ván trước được dispatcher giao sau reset lọt vào ván mới; nước thắng được gửi sau callback kết quả/modal; đổi VI → PT cắt mất nút âm thanh khỏi header. Heuristic chấm overline bị chặn và single-three vẫn sai.
- Đo có giới hạn: Hard 1 quân median 38.30 ms/max 40.82 ms; 10 lượt Hard qua BoardPanel median 35.28 ms/max 109.69 ms; heartbeat EDT max 3.19 ms. Không xem đây là kiểm thử đầy đủ UI/audio/mạng hai máy.
- Đã khảo sát nguồn RIF, Gomocup 2026, Rapfi, BGA, PlayOK và các sản phẩm có replay/puzzle/study. Đề xuất ưu tiên sửa đúng luật/giao thức/UI, rồi replay/lưu ván, gợi ý có giải thích và iterative deepening.
- Báo cáo và nguồn: REVIEW_AND_UPGRADE_PLAN.md. Probe tái hiện: tools/audit; ảnh render: docs/audit-2026-10-26. Lượt review này chưa thay logic sản phẩm, chưa commit/push.
