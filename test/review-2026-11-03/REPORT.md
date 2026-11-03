# Báo cáo kiểm thử Gomoku — 2026-11-03

Kết quả chỉ lấy từ lượt JUnit thực tế trong log và XML mới. Phạm vi được người dùng chốt: các ca thuần logic của năm lớp; giữ nguyên test cũ, chọn lớp/phương thức khi chạy. Kiểm kê và khoảng trống được ghi trước lượt chạy tại [INVENTORY.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/INVENTORY.md>).

## A. Lệnh đã chạy và log

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File test/run-headless-2026-11-03.ps1
```

Lệnh entry có hai lần gọi: lần đầu lỗi chọn Java trước khi Java/JUnit được khởi động; lần sau dùng Java đầu tiên trong PATH và chạy JUnit **đúng một lần**. Giữ nguyên lỗi bootstrap trong log, không xóa bằng chứng. Không chạy bộ baseline, test đơn lẻ hay chạy lại sau khi có kết quả.

- Log đầy đủ stdout/stderr, lệnh biên dịch, Java, JUnit và mã thoát: [junit-run-2026-11-03.log](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-2026-11-03.log>).
- Bắt đầu lượt thành công: `2026-11-03T10:35:38.7383198+07:00`.
- Kết thúc: `2026-11-03T10:35:44.0869175+07:00`; JUnit báo `Test run finished after 820 ms`.
- Java thật trong log: `openjdk version "25.0.3" 2026-04-21 LTS`; Microsoft OpenJDK `25.0.3+9-LTS`.
- Build: các lệnh javac của build.ps1, chỉ chuyển thư mục -d vào test/review-2026-11-03 để không ghi bin/bin_test ngoài test. Biên dịch toàn bộ src/ và test/, copy tài nguyên vào thư mục output trong test/.
- JUnit 5.10.2 / Platform 1.10.2; `-Djava.awt.headless=true`; chọn 8 lớp đầy đủ và một phương thức thuần ScoreTracker của Task4bGameplayTest.
- XML thật: [TEST-junit-jupiter.xml](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/xml/TEST-junit-jupiter.xml>), [TEST-junit-vintage.xml](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/xml/TEST-junit-vintage.xml>), [TEST-junit-platform-suite.xml](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/xml/TEST-junit-platform-suite.xml>). Không có Maven Surefire/Gradle/HTML.

### Lệnh JUnit chính xác trong log

```text
COMMAND: C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot\bin\java.exe "-Djava.awt.headless=true" "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" "-jar" "lib/junit-platform-console-standalone-1.10.2.jar" "execute" "--class-path" "test/review-2026-11-03/test-classes;test/review-2026-11-03/classes" "--select-class" "game.BoardWinTest" "--select-class" "game.F3HeuristicTest" "--select-class" "game.ai.AITacticsTest" "--select-method" "game.Task4bGameplayTest#resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain" "--select-class" "game.model.BoardModelContractTest" "--select-class" "game.model.ScoreTrackerContractTest" "--select-class" "game.ai.MinimaxEasyContractTest" "--select-class" "game.ai.HeuristicOrderingTest" "--select-class" "game.network.NetworkMessageTest" "--config" "junit.jupiter.execution.parallel.enabled=false" "--reports-dir" "test/review-2026-11-03/xml" "--details" "tree" "--disable-banner" "--disable-ansi-colors" "--fail-if-no-tests"
```

## B. Tổng số / PASS / FAIL / ERROR / SKIPPED

| Tổng số đã chạy | PASS | FAIL | ERROR | SKIPPED |
|---:|---:|---:|---:|---:|
| 158 | 158 | 0 | 0 | 0 |

Có 116 lượt cũ và 42 lượt mới; 93 phương thức test (75 cũ + 18 mới). Không đếm container hay phương thức tham số hóa thêm một lần nữa. 0 aborted. JUnit exit code 0.

**Bị loại riêng:** 142 phương thức test JUnit cũ, không tính vào PASS/FAIL hoặc SKIPPED của JUnit. Đây là số phương thức; số lượt tham số hóa của phần không chạy **CHƯA XÁC NHẬN**, không lấy số lịch sử từ báo cáo cũ. Các main/probe không phải JUnit được ghi riêng trong H.

**Phân loại từng ca đã chạy:** [EXECUTED-TESTS.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/EXECUTED-TESTS.md>) ghi cả 158 lượt với tên/tham số, lớp được kiểm, kết quả và loại. 158 ca hành vi đơn lẻ; 0 replay ván cờ theo lượt.

## C. Bảng theo lớp được kiểm

Mỗi ca gán một lớp chính để không cộng trùng. F3HeuristicTest còn kiểm Board và AITacticsTest dùng Board/Heuristic để kiểm fixture; không cộng các assertion phụ thành test riêng.

| Lớp được kiểm | Số test | PASS | FAIL |
|---|---:|---:|---:|
| game.model.Board | 60 | 60 | 0 |
| game.model.ScoreTracker | 11 | 11 | 0 |
| game.ai.MinimaxAI | 33 | 33 | 0 |
| game.ai.HeuristicEvaluator | 32 | 32 | 0 |
| game.network.NetworkManager | 22 | 22 | 0 |

Chi tiết theo lớp JUnit để đối chiếu trực tiếp XML:

| Lớp JUnit | Số test | PASS | FAIL |
|---|---:|---:|---:|
| game.BoardWinTest | 55 | 55 | 0 |
| game.F3HeuristicTest | 28 | 28 | 0 |
| game.Task4bGameplayTest | 1 | 1 | 0 |
| game.ai.AITacticsTest | 32 | 32 | 0 |
| game.ai.HeuristicOrderingTest | 4 | 4 | 0 |
| game.ai.MinimaxEasyContractTest | 1 | 1 | 0 |
| game.model.BoardModelContractTest | 5 | 5 | 0 |
| game.model.ScoreTrackerContractTest | 10 | 10 | 0 |
| game.network.NetworkMessageTest | 22 | 22 | 0 |

## D. Mọi test FAIL/ERROR và thông báo nguyên văn

Không có test FAIL/ERROR trong XML: `failures="0" errors="0"`.

Lỗi bootstrap trước khi chạy test (không phải test ERROR), giữ nguyên phần đầu trong log:

```text
Exception calling "Start" with "0" argument(s): "The system cannot find the file specified"
```

Nguyên nhân: Get-Command java -CommandType Application trả về nhiều java.exe; runner đầu tiên ghép các đường dẫn thành một chuỗi. Chỉ sửa runner trong test/ để lấy kết quả đầu tiên. Không sửa test sau lượt JUnit, không sửa sản phẩm.

## E. Test mới thêm và test cũ giữ nguyên

18 phương thức mới, 42 lượt tham số hóa. Mỗi tên dưới đây kiểm một hành vi; không thêm lại bộ RULE_B hoặc các ca AI chiến thuật đã có. Mã **nguyên tệp** có package/import: [NEW-TESTS.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/NEW-TESTS.md>).

- [BoardModelContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/model/BoardModelContractTest.java>)
  - `outOfBoundsPlacementIsRejectedWithoutChangingBoard`
  - `undoRemovesOnlyRequestedStoneAndDecrementsMoveCount`
- [ScoreTrackerContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/model/ScoreTrackerContractTest.java>)
  - `humanWinIsRecordedForEitherHumanPiece`
  - `opponentWinIsRecordedForEitherHumanPiece`
  - `drawIsRecordedForEitherHumanPiece`
  - `duplicateGameIdRejectsSecondResultAndPreservesFirst`
  - `resetScoresClearsAllThreeCounters`
- [MinimaxEasyContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/ai/MinimaxEasyContractTest.java>)
  - `easyChoosesMoveWithoutEvaluatingMinimaxNodes`
- [HeuristicOrderingTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/ai/HeuristicOrderingTest.java>)
  - `winningPositionOutranksOrdinaryPosition`
  - `openFourOutranksOrdinaryPosition`
- [NetworkMessageTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/network/NetworkMessageTest.java>)
  - `moveLineDispatchesPeerRoundPlyAndCoordinates`
  - `invalidMoveLineDoesNotDispatchAMove`
  - `sendMoveFormatsTheCurrentRoundMessage`
  - `clientRestartRequestFormatsTheBaseRoundMessage`
  - `restartRequestLineOnHostCommitsANewRound`
  - `hostRestartFormatsBaseAndNewRoundMessage`
  - `restartLineOnClientReplacesTheMatchingRound`
  - `restartLineForDifferentBaseDoesNotChangeRound`

**Test cũ giữ nguyên:** toàn bộ 28 tệp JUnit / 217 phương thức, 3 helper/probe và 3 tài nguyên test; hash SHA-256 trước/sau khớp. 75 phương thức trong 4 tệp được chọn chạy: 55 BoardWinTest, 7 F3HeuristicTest, 12 AITacticsTest, 1 resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain của Task4bGameplayTest. Danh sách từng phương thức cũ/lớp/hành vi đầy đủ trong [INVENTORY.md](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/INVENTORY.md>); danh sách test cũ không chạy tại H.

## F. CHƯA XÁC NHẬN / không kiểm được và lý do

- **Board.undoMove khôi phục lượt: CHƯA XÁC NHẬN bằng test thuần Board.** Mã Board không giữ lượt; API undoMove(row,col) chỉ rút quân/giảm moveCount. Kiểm lượt cần BoardPanel, bị cấm trong lượt này. Không tự tạo lớp controller hay quy tắc luân phiên.
- **START: không kiểm được nếu không sửa mã** trong phạm vi không socket. Parse và tạo START nằm trong connectAndListen(Session) cùng Socket/ServerSocket và timeout. Không tạo test giả mô phỏng lại parser. MOVE/RESTART_REQUEST/RESTART đã chạy bằng Session/PrintWriter/StringWriter trong bộ nhớ, reflection chỉ với cấu trúc private đọc từ nguồn.
- EASY **không có hợp đồng luôn thắng/chặn tức thì** như MEDIUM/HARD: nhánh EASY chặn theo xác suất 70%, rồi chọn ngẫu nhiên top 3. Không áp yêu cầu chiến thuật của mức cao vào EASY. Đã kiểm ba mức qua AITacticsTest và nodes/pruning=0 của EASY bằng test mới.
- Test EASY mới seed Random=20261103 bằng reflection vì constructor không nhận seed. **Test cũ AITacticsTest vẫn dùng Random chưa seed**; giữ nguyên theo yêu cầu, các assertion EASY chỉ kiểm bất biến/tính hợp lệ, không đoán ô ngẫu nhiên.
- Số lượt tham số của 142 phương thức bị loại: **CHƯA XÁC NHẬN**, vì không thực thi chúng. Danh mục phương thức lấy từ source thật; kết quả của chúng không được suy ra từ các báo cáo cũ.
- Các chương trình main/benchmark không có @Test, không nằm trong báo cáo XML và không được gọi riêng trong lượt JUnit này.

## G. Danh sách tệp tạo/sửa

Không sửa tệp cũ. Các tệp nguồn test mới:

- [BoardModelContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/model/BoardModelContractTest.java>)
- [ScoreTrackerContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/model/ScoreTrackerContractTest.java>)
- [MinimaxEasyContractTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/ai/MinimaxEasyContractTest.java>)
- [HeuristicOrderingTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/ai/HeuristicOrderingTest.java>)
- [NetworkMessageTest.java](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/network/NetworkMessageTest.java>)

Các tệp hỗ trợ mới đều trong test/: runner PowerShell, INVENTORY.md, NEW-TESTS.md, EXECUTED-TESTS.md, REPORT.md, existing-tests.json, baseline-sha256.json, executed-tests.json, results.json, created-files.txt, cùng XML và output javac/tài nguyên dưới review-2026-11-03/.

Ngoại lệ duy nhất ngoài test/: [junit-run-2026-11-03.log](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-2026-11-03.log>) theo Bước 4 và xác nhận của người dùng. Danh mục **mọi tệp đã tạo còn trên đĩa**, gồm cả output build: [created-files.txt](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/created-files.txt>).

Đối chiếu SHA-256: toàn bộ tệp src/, resources/, test cũ, build.ps1 và .gitignore giữ nguyên so với đầu phiên. .gitignore và các tệp packaging/docs đã có thay đổi từ trước; không đụng tới và không tính là thay đổi của lượt này.

## H. Test bị loại khỏi lượt chạy

**142 phương thức JUnit**, chọn loại bằng danh sách --select-class/--select-method, không dùng @Disabled, không sửa mã cũ. Các phương thức có hook Swing vẫn bị loại dù thân test chỉ gọi AI/chuẩn hóa tên. Một số test thuần thuộc lớp khác năm lớp yêu cầu được ghi rõ là ngoài phạm vi, không gán sai lý do GUI/socket.

| Nhóm lý do | Số phương thức |
|---|---:|
| GUI + socket thật | 6 |
| GUI/Swing (trong thân test hoặc hook của lớp) | 102 |
| ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) | 22 |
| socket thật | 12 |

| Tên tệp | Tên test | Lý do loại |
|---|---|---|
| test/game/AiMoveDelayTest.java | `waitsOnlyForTheMissingPart` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `rejectsNegativeDelayOrElapsedTime` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `fastSearchKeepsThinkingAndInputLockedUntilTheOriginalDeadline` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `elapsedMinimumBeforeWorkerCompletionAddsNoTimer` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `zeroDelayInjectionAppliesOnCompletionWithoutATimer` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `undoButtonDuringWaitCancelsQueuedMoveEvenAfterAnotherHumanMove` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `newGameButtonCancelsQueuedMoveBeforeStartingAnotherRound` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `menuCancelsQueuedMoveEvenAfterReturningToGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `changingModeCancelsThePendingAiMove` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `changingDifficultyRestartsSearchButOldTimerCannotTouchTheNewTimer` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `disposingWindowCancelsThePendingMoveWithoutChangingTheBoard` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `languageChangeUpdatesThinkingTextInPlaceWithoutRebuildingTheGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `humanWinOrFullBoardDrawNeverSchedulesAnAiMove` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/AiMoveDelayTest.java | `otherModesDoNotUseTheDelayClockOrScheduleAi` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/BoardVisibilityTest.java | `realModalGameFlowPaintsGridAndStoneInChildJvm` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F1ProtocolTest.java | `invalidPlyOrColorDoesNotChangeBoard` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F1ProtocolTest.java | `wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F1ProtocolTest.java | `queuedWireMoveKeepsOldRoundAfterReset` | GUI + socket thật |
| test/game/F1ProtocolTest.java | `simultaneousSameBaseRestartAndStaleRequestResetOnce` | socket thật |
| test/game/F1ProtocolTest.java | `clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof` | socket thật |
| test/game/F1ProtocolTest.java | `startTimeoutClosesSession` | socket thật |
| test/game/F1ProtocolTest.java | `mismatchedVersionClosesSession` | socket thật |
| test/game/F1ProtocolTest.java | `garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect` | socket thật |
| test/game/F1ProtocolTest.java | `waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F2MoveOrderTest.java | `finalMoveIsSentBeforeResultAndResultIsProcessedOnce` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F4LayoutTest.java | `directStartupBoundsAndRender` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/F4LayoutTest.java | `allTransitionsKeepControlsInsideParents` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/I18nSourceScanTest.java | `i1a_noIsVICall` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/I18nSourceScanTest.java | `i1b_noVietnameseInStringLiterals` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/LangPurityTest.java | `a_ptNoVietnameseChars` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/LangPurityTest.java | `b_enAsciiOnly` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/LangPurityTest.java | `c_sharedThemeContrast` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/NetworkStabilityTest.java | `cancelPendingHostReleasesPortWithoutError` | socket thật |
| test/game/NetworkStabilityTest.java | `hostAndClientExchangeMovesRestartAndDisconnect` | socket thật |
| test/game/NetworkStabilityTest.java | `malformedMoveDoesNotKillConnectionAndOrderIsPreserved` | socket thật |
| test/game/NetworkStabilityTest.java | `callbacksAlreadyQueuedAreDiscardedAfterCancel` | socket thật |
| test/game/NetworkStabilityTest.java | `reconnectCannotBeClosedOrNotifiedByPreviousSession` | socket thật |
| test/game/NetworkStabilityTest.java | `staleErrorIsDiscardedWhenNewSessionStarts` | socket thật |
| test/game/NetworkStabilityTest.java | `occupiedPortReportsTypedHostError` | socket thật |
| test/game/P1RegressionTest.java | `languageChangePreservesActiveGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `languageChangePreservesFinishedGameAndTranslatesResult` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `pvpUndoAfterWinRestoresWinningPlayerTurn` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `pvpUndoAfterDrawRestoresLastPlayerTurn` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `onlineRejectsOpponentMoveOnLocalTurn` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `onlineRejectsMoveAfterSwitchingMode` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `onlineAcceptsOnlyOneMoveFromCorrectOpponent` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `onlineRejectsQueuedMoveAfterNewGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P1RegressionTest.java | `onlineRejectsInvalidSquaresAndMovesAfterGameOver` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `disposedFrameDoesNotReceiveLanguageEvents` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `disposingFrameCancelsItsWorkerAndPreservesBoard` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `closingDialogCancelsPendingHostAndIgnoresLateResults` | GUI + socket thật |
| test/game/P2LifecycleTest.java | `successfulDialogDisposalKeepsConnection` | GUI + socket thật |
| test/game/P2LifecycleTest.java | `cancellingOnlineSelectionRestoresModeWithoutResettingGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `receivedMoveIsAppliedBeforeNextEdtNetworkCallback` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `networkErrorsAreTranslated` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `interruptedAiAbortsWithoutMutatingBoard` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/P2LifecycleTest.java | `cancellationInsideRecursiveSearchUnwindsAllMoves` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/PlayerPieceBadgeTest.java | `pureMappingAssignsBlackToXAndWhiteToO` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/PlayerPieceBadgeTest.java | `badgePixelsUseBoardPaletteAndAntialiasedWhiteOutline` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/PlayerPieceBadgeTest.java | `realCardsFollowModeSideAndLanguageWithoutChangingScoresOrTurnBorder` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/PlayerPieceBadgeTest.java | `longNamesEllipsizeInsideBothCardsWithoutPushingBadgesOut` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/SoundResourceTest.java | `closesClipAfterPlaybackStops` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/SoundResourceTest.java | `closesClipWhenOpenFails` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/SoundResourceTest.java | `closesClipWhenStartFails` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/SoundResourceTest.java | `staleResultClosesClipWithoutStarting` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/SoundResourceTest.java | `queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4bFlowTest.java | `lanMenuJoinUsesRealSocketAndStartsTheExistingOnlineGame` | GUI + socket thật |
| test/game/Task4bFlowTest.java | `rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFlowTest.java | `titleAcceptsMouseAndAnyKeyAndStopsItsTimer` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFlowTest.java | `enterCommitsProfileAndEscapeCancelsEveryDialogDraft` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFlowTest.java | `setupRemembersDifficultyAndPlayerTwoOnlyAfterStart` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFlowTest.java | `allCardsAndDialogsFitBothLooksWithStableFrameSize` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFoundationTest.java | `profileNames` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4bFoundationTest.java | `invalidAvatarsDefaultToZero` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4bFoundationTest.java | `profileRoundTripKeepsLegacyKeyAndImplicitDefault` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4bFoundationTest.java | `missingImagesRenderAndCoverCacheTracksDevicePixels` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bFoundationTest.java | `buttonsPaintFourDistinctStatesUnderBothLooks` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bGameplayTest.java | `computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bGameplayTest.java | `realGameResultsUpdateBothCardsAndDrawCount` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bGameplayTest.java | `scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bGameplayTest.java | `resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bGameplayTest.java | `boardIsCentredAndMouseUsesUnchangedCoordinates` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bPresentationTest.java | `buttonsRenderFourStatesAndActivateWithEnterAndSpace` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bPresentationTest.java | `renderAllSixViewsAndMeasureDisplayedFrame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bPresentationTest.java | `translatedButtonAndLabelTextFitsIncludingTransitions` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bPresentationTest.java | `profileClearCounterAndFocusSelectAllUseActualEditor` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4bPresentationTest.java | `suppliedRulesKeysAreVerbatimAndPortugueseIsPure` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4bPresentationTest.java | `runtimeResourcePackagingAndImageLoadingWorkOnBinClasspath` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/Task4LayoutTest.java | `allThreeCardsFitAllLocalesOnStartupAndAfterTransitions` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `menuClosesRealLanSocketAndAllConnectionDialogs` | GUI + socket thật |
| test/game/Task4LifecycleTest.java | `menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart` | GUI + socket thật |
| test/game/Task4LifecycleTest.java | `queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `queuedOpponentMoveCannotChangeBoardAfterMenu` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4LifecycleTest.java | `undoRemainsAvailableAfterWin` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4PreferencesTest.java | `profileAndLanguageRoundTripThroughInMemoryNode` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `titleMenuSetupAndGameUseTheSameFrame` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `emptyProfileUsesTranslatedDefaultWithoutPersistingIt` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `normalizeUnicodeWhitespaceAndLongNames` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `profileFieldLimitsUnicodeAndNamesRemainPlainText` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `settingsApplyLanguageImmediatelyWithoutChangingProfileDraft` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/Task4StartScreenTest.java | `profileChangesAndMenuKeepExistingSessionTotals` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/TwoPlayerSetupDialogTest.java | `bothPlayersHaveSymmetricHeadersAndNameLabels` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/TwoPlayerSetupDialogTest.java | `duplicateWarningUpdatesImmediatelyAndBlocksButtonAndEnter` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/TwoPlayerSetupDialogTest.java | `avatarLocksFollowBothChoicesAndLockedIconsAreDimmed` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/TwoPlayerSetupDialogTest.java | `startCommitsBothDraftsAndCancelDiscardsChanges` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/TwoPlayerSetupValidationTest.java | `resolvesBlankAndTrimmedNames` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `comparesEffectiveNamesAfterTrimIgnoringCase` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `defaultsAreExplicitAndLocaleIndependent` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `locksTheOtherPlayersAvatarInBothDirections` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `keepsAnAvailableSecondAvatar` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `resolvesOnlyTheInitialSecondAvatarConflict` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/TwoPlayerSetupValidationTest.java | `duplicateAvatarsCannotStartEvenWithDistinctNames` | ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) |
| test/game/UILangSoundTest.java | `c1_langAllKeysNotNullNotEmpty` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `c1b_connectingMsgContainsIp` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `c2_sharedButtonPaintAndContrast` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `b2_settingsSoundToggle` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `b3_settingsSoundTooltipByLang` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `c3_menuAndSettingsUpdateImmediately` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `d1_appSettingsRoundTrip` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `d2_inMemoryNodeIsDifferentFromProduction` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UILangSoundTest.java | `i2_speakerIconVisual` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP1_XWin_UndoOneMove` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP1b_PlayAgainAfterUndo_ScoreUnchanged` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP2_OnlyOneMove_Undo` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP3_XAndO_UndoTwoMoves` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP4_DrawAtMove225_UndoOneMove` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoParityTest.java | `testP5_AlternatingPieceCountAfterUndo` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU01_AiWinRecorded` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU03_PlayAgainAndFinish_NoDuplicateScore` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU04_UndoWhenHistoryEmpty_NoException` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU05_UndoMultipleTimesUntilEmpty_RestoresStateCorrectly` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU06_ResetGame_NewGameId_ScoresPreserved_NewResultRecorded` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU07_DrawGame_Undo_DrawScorePreserved` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `testU08_AiWorkerTokenDiscardedOnResetOrUndo` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `test6e_PvpUndoOneMove_PvAiUndoTwoMoves_OnlineUndoForbidden` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `test7_OnlineMoveIgnoredWhenGameOver` | GUI/Swing (trong thân test hoặc hook của lớp) |
| test/game/UndoScoreTest.java | `test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01` | GUI/Swing (trong thân test hoặc hook của lớp) |

Ngoài danh sách JUnit trên: BoardDisplayProbe.main là probe GUI. RuleAndDrawTest.main, ConsoleTest.main, AIBenchmarkTest.main là custom console/benchmark ngoài JUnit; FullGomokuTestSuite.main trộn thêm Swing. Không chạy các main, không tính vào tổng 158, 142 hay PASS/FAIL. Chi tiết từng chương trình trong INVENTORY.md.
