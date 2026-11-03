# Sửa thu hồi tỉ số sau Undo — 03/11/2026

## A. Hành vi cũ và nguyên nhân

Đã xác nhận trước sửa: nhánh `task4-start-screen`, HEAD `8121c9ea8184018ff3aeff4df2d8e65b04dc3963`, cây sạch; tạo `fix-score-undo` từ HEAD này.

Ở `8121c9e`, ScoreTracker dùng Set gameId và chỉ đếm kết quả đầu tiên (dòng 10–17, 27–39). Không lưu bên thắng/hòa theo ID nên không thể trừ đúng kết quả. BoardPanel.undoMove xóa isGameOver/winner nhưng chỉ báo turn/history, không báo mở lại ván (dòng 236–278 cũ). MainFrame chỉ record khi onGameOver; callback đó gặp ID đã ghi nên từ chối kết quả lần hai (dòng 83–87 cũ).

Đường gọi thật: nút Đi Lại → BoardPanel.undoMove → listener.onGameReopened → MainFrame thu hồi → refreshGameStatus. Nút xử lý trên EDT; phần thêm chỉ thao tác Map/bộ đếm và cập nhật trạng thái hiện có, không I/O/chờ/blocking. Đường kết thúc thắng/hòa vẫn BoardPanel.executeMove → onGameOver(gameId, ...) → MainFrame recordResult. [BoardPanel báo kết thúc](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:379>), [MainFrame ghi kết quả](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:88>).

## B. Test cũ mâu thuẫn và trình tự bắt buộc

Đã thực hiện đúng thứ tự: sửa sản phẩm → thêm duy nhất onGameReopened vào hai setUp → biên dịch sạch → chạy hai lớp cũ một lần, kèm ca reset thuần logic → xác nhận đúng bốn FAIL → mới đổi đúng bốn tên/assertion/thông báo/DisplayName. Không có FAIL ngoài bốn ca. [Log trước đổi assertion](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/score-undo-old-tests-fail.log>).

Lượt cũ: 18 đã chạy, 14 PASS, 4 FAIL, 0 ERROR, 0 SKIPPED. U03, P1b, U07 và resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain đều PASS; thân/tên/assertion của chúng giữ nguyên so với HEAD.

| Test cũ | Thông báo FAIL nguyên văn từ XML | Lý do |
|---|---|---|
| `UndoScoreTest.testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse` | `Thành tích AI thắng VẪN PHẢI LÀ 1 (không bị xóa) ==> expected: <1> but was: <0>` | AI phải 0 sau Undo, assertion cũ đòi 1. |
| `UndoScoreTest.test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01` | `Điểm thua vẫn là 1 ==> expected: <1> but was: <0>` | FAIL ở assertion ngay sau Undo: AI phải 0, cũ đòi 1; sau người thắng kết quả mới phải 1–0. |
| `UndoParityTest.testP1_XWin_UndoOneMove` | `Điểm thắng của người chơi không đổi ==> expected: <1> but was: <0>` | Người phải 0 sau Undo, cũ đòi 1. |
| `UndoParityTest.testP4_DrawAtMove225_UndoOneMove` | `Điểm hòa giữ nguyên ==> expected: <1> but was: <0>` | Hòa phải 0 sau Undo, cũ đòi 1. |

U03/P1b chơi lại cùng bên thắng nên tổng cuối giống cũ. U07 tự ghi hòa vào tracker nhưng không đưa BoardPanel vào trạng thái kết thúc; Undo lúc panel chưa kết thúc không thu hồi. Ca reset chỉ reset số rồi gọi record lại, không gọi revoke. Vì thế các ca này PASS không mâu thuẫn hành vi mới.

Bằng chứng diff sản phẩm + fixture trước sửa assertion: [patch lưu trước đổi test](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/score-undo/product-and-wiring-before-assertions.patch>).

## C. Diff và thiết kế

| Tệp/vị trí hiện tại | Thay đổi |
|---|---|
| [ScoreTracker](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/model/ScoreTracker.java:14>) | Map gameId → HUMAN/AI/DRAW; revokeResult xóa ID, trừ đúng bộ đếm, ID chưa ghi trả false. resetScores đánh dấu CLEARED để giữ chống ghi trùng sau reset và không trừ nhầm khi kết quả đã bị reset. +25/−10 dòng. |
| [GameStateListener](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:88>) và [undoMove](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:240>) | Callback default rỗng; chụp wasGameOver trước Undo; chỉ báo mở lại khi trước đó kết thúc. Tổng +3 dòng. |
| [MainFrame callback](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/MainFrame.java:82>) | Kiểm màn đang chơi, đúng ID, ván đã mở và offline; revoke rồi refreshGameStatus. +5 dòng. Hàm refresh hiện có cập nhật hòa tại dòng 190 và hai thẻ tại 195/196. |
| [UndoScoreTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/UndoScoreTest.java:44>) | Chỉ thêm callback trong setUp và đổi đúng U02, test8_And_9; các thân test khác nguyên vẹn. +14/−11 dòng. |
| [UndoParityTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/UndoParityTest.java:44>) | Chỉ thêm callback trong setUp và đổi đúng P1/P4; các thân test khác nguyên vẹn. +10/−7 dòng. |

Callback default giữ tương thích các listener ngoài phạm vi. F2MoveOrderTest, CurrentAuditProbe, AuditBenchmark không đổi. Hai fixture được nối callback theo ngoại lệ người dùng đã cho phép. Không đổi gameId, token SwingWorker hoặc animation.

## D. Biên dịch và test cuối

Lệnh thực tế đã chạy từ thư mục gốc:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/score-undo/run-headless.ps1
```

Wrapper dùng nguyên nội dung script gốc trong bộ nhớ, đổi đường đầu ra/log, thêm ba selector cho hai fixture và test mới; file `test/run-headless-2026-11-03.ps1` giữ nguyên. Có chặn chạy lại/ghi đè bằng chứng. Biên dịch javac toàn bộ src và test vào thư mục mới rỗng, rồi duy nhất một lệnh JUnit execute cho bộ cuối.

Java Microsoft OpenJDK 25.0.3+9-LTS; JUnit Jupiter 5.10.2 / Platform 1.10.2; `java.awt.headless=true`. Lượt cuối 13:03:25–13:03:30 giờ Asia/Saigon (thời điểm chính xác trong log).

| Tổng | PASS | FAIL | ERROR | SKIPPED |
|---:|---:|---:|---:|---:|
| 190 | 190 | 0 | 0 | 0 |

**158 ca gốc + 17 ca cũ trước đây bị loại (hai lớp fixture) + 15 lượt test mới = 190.** 8 phương thức mới sinh 15 lượt do tham số hóa. Đối chiếu danh tính testcase trong XML xác nhận đủ 158 ca gốc. Không có FAIL/ERROR cuối.

[Log cuối](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log>); [XML Jupiter](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-after-fix-score-undo/xml/TEST-junit-jupiter.xml>); XML Vintage/Platform Suite cùng thư mục (0 ca). Không có Maven/Gradle/HTML.

| Lớp test | Đã chạy | PASS | FAIL |
|---|---:|---:|---:|
| `game.BoardWinTest` | 55 | 55 | 0 |
| `game.F3HeuristicTest` | 28 | 28 | 0 |
| `game.Task4bGameplayTest` | 1 | 1 | 0 |
| `game.UndoParityTest` | 6 | 6 | 0 |
| `game.UndoScoreTest` | 11 | 11 | 0 |
| `game.ai.AITacticsTest` | 32 | 32 | 0 |
| `game.ai.HeuristicOrderingTest` | 4 | 4 | 0 |
| `game.ai.MinimaxEasyContractTest` | 1 | 1 | 0 |
| `game.model.BoardModelContractTest` | 5 | 5 | 0 |
| `game.model.ScoreTrackerContractTest` | 10 | 10 | 0 |
| `game.model.ScoreTrackerRevokeTest` | 15 | 15 | 0 |
| `game.network.NetworkMessageTest` | 22 | 22 | 0 |

Test mới trong [ScoreTrackerRevokeTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/model/ScoreTrackerRevokeTest.java>):

| Tên phương thức | Lượt | Hành vi |
|---|---:|---|
| revokingEachResultRestoresPreviousTotals | 6 | Thu hồi thắng hai bên/hòa; human là X hoặc O; khôi phục tổng trước đó và bỏ ID. |
| revokingUnknownIdLeavesExistingResultsUnchanged | 1 | ID chưa ghi không ảnh hưởng tổng hoặc ID khác. |
| humanWinThenRevokeThenOpponentWinReplacesResult | 2 | A thắng → thu hồi (mô phỏng Undo) → B thắng cùng ID: A0, B1. |
| drawThenRevokeThenWinRemovesDraw | 2 | Hòa → thu hồi → thắng: hòa0, thắng1. |
| duplicateIdIsRejectedWithoutRevoke | 1 | Vẫn từ chối ghi hai lần khi chưa thu hồi. |
| revokingOneGamePreservesOtherGames | 1 | Chỉ trừ ván đã chọn, giữ các ván còn lại. |
| revokingResultClearedByResetDoesNotSubtractAnotherGame | 1 | Reset rồi thu hồi ID cũ không làm giảm điểm ván mới. |
| repeatedRevokeDoesNotSubtractTwice | 1 | Thu hồi lần hai không trừ tiếp. |

Từng lượt test và phân loại kịch bản chuỗi nước/hành vi đơn lẻ: [190 ca đã chạy](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/score-undo/EXECUTED-TESTS.md>). Các test mới chỉ kiểm logic kết quả, không dùng Swing. Các fixture Undo có chuỗi nước dựng sẵn bằng API test; không chứng minh AI thực sự chọn các nước đó.

## E. CHƯA XÁC NHẬN và kiểm tay

**Hai lớp Undo chạy qua fixture mô phỏng listener, KHÔNG đi qua MainFrame thật. Việc nối thu hồi trong MainFrame chưa có test tự động; CHƯA XÁC NHẬN khi thao tác GUI thật, cần kiểm tay.** Headless không tạo được JFrame; không dùng GUI trong test mới. Không khẳng định kiểm tự động được thẻ điểm hoặc hộp kết quả thực tế.

Các kịch bản cần kiểm tay:

1. Offline A thắng: A1–B0 → đóng thông báo → Đi Lại: A0–B0 → B thắng: **A0–B1**. Kiểm cả PvP và PvAI; kiểm hai thẻ cập nhật, gameId không đổi.
2. Hòa1 → Đi Lại: hòa0 → thắng: hòa0, bên thắng+1; kiểm nhãn hòa.
3. Undo khi chưa kết thúc giữ điểm các ván trước; Undo nhiều lần không trừ lặp; cùng bên thắng lại chỉ tính một ván.
4. Ván Mới giữ điểm ván đã kết thúc, chuyển chế độ/độ khó reset theo hành vi cũ; sau kết thúc → Undo → Ván Mới, kết quả bị thu hồi không được tính (hạn chế đã chấp nhận). LAN tiếp tục không có Undo.

## F. Tệp và Git

Nhánh `fix-score-undo`, từ `8121c9e`. Commit chứa báo cáo này là commit sửa lỗi; mã commit được trả trong phản hồi cuối để tránh tự tham chiếu hash trong nội dung commit. Không push, không merge.

5 tệp có sẵn thay đổi: 3 tệp sản phẩm và 2 tệp test ở mục C. Chỉ một tệp test mới; các tệp khác mới tạo là log, wrapper và bằng chứng. Danh sách đầy đủ tệp được đưa vào commit: [file manifest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/tools/score-undo/created-and-modified-files.txt>).

Đối chiếu SHA-256 toàn bộ file tracked trước sửa: chỉ đúng 5 tệp có sẵn thay đổi; script gốc, log cũ, âm thanh và mọi tệp khác nguyên vẹn. Baseline hash lưu trong tools/score-undo/baseline-sha256.json. .class sinh trong old-classes/old-test-classes và thư mục output cuối bị gitignore, không đưa vào commit. README.md trong classes/ là bản resources được runner sao chép, không phải chỉnh sản phẩm.

## G. Không sửa có chủ đích

Không sửa UI/bố cục, RULE_B, Board model, AI, mạng, ngôn ngữ, âm thanh/bật tắt/persistence. Không refactor. Không đổi New Game/chuyển chế độ/độ khó, cơ chế ID/token hay animation. Không sửa thêm test cũ ngoài bốn ca được nêu và callback wiring duy nhất trong hai setUp. U03/P1b/U07/reset giữ cả tên và thông báo cũ theo yêu cầu.

## H. Bốn test thay đổi thuộc nhóm nào của lượt 158

Theo REPORT.md gốc mục test bị loại: [báo cáo 03/11 gốc](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/REPORT.md:270>). Cả bốn **không nằm trong 158 ca đã chạy**; thuộc nhóm bị loại GUI/Swing do fixture dùng BoardPanel. Không phải socket thật. Lượt mới được người dùng cho phép chạy hai lớp fixture headless, không mở JFrame.

| Tệp | Tên cũ → tên mới | Nhóm gốc / lý do | Lượt cuối |
|---|---|---|---|
| `test/game/UndoScoreTest.java` | `testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse` → `testU02_UndoAfterAiWin_RevokesAiResult_ReopensGame` | Bị loại; GUI/Swing BoardPanel trong hook/thân test | PASS |
| `test/game/UndoScoreTest.java` | `test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01` → `test8_And_9_AiWin_Undo_PlayerWin_ReplacesResult` | Bị loại; GUI/Swing BoardPanel trong hook/thân test | PASS |
| `test/game/UndoParityTest.java` | `testP1_XWin_UndoOneMove` → `testP1_XWin_UndoRevokesHumanResult` | Bị loại; GUI/Swing BoardPanel trong hook/thân test | PASS |
| `test/game/UndoParityTest.java` | `testP4_DrawAtMove225_UndoOneMove` → `testP4_DrawAtMove225_UndoRevokesDrawResult` | Bị loại; GUI/Swing BoardPanel trong hook/thân test | PASS |

Tổng tăng 32 lượt so với 158: 17 ca fixture cũ được bổ sung vào phạm vi, 15 lượt mới. 125 phương thức bị loại cũ còn lại vẫn không được chọn (142 cũ − 17). Những ca không được chọn không tính PASS/FAIL.
