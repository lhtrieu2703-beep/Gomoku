# Kiểm kê trước khi bổ sung test — 2026-11-03

Nguồn: đọc trực tiếp mã nguồn hiện có; chưa chạy test tại bước kiểm kê. Có **28 tệp JUnit / 217 phương thức test**. Một phương thức tham số hóa có thể tạo nhiều lượt chạy; không đồng nhất hai cách đếm. Giữ nguyên tất cả tệp cũ.

## Công cụ và luật đã xác nhận

- Build: PowerShell + javac trong `build.ps1`; không có pom.xml, Gradle hoặc wrapper.
- Build gốc: `powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1`.
- Lệnh JUnit gốc trong docs/PROJECT_NOTES.md: `java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin;src" --scan-class-path --details summary`.
- JUnit **5**, Jupiter **5.10.2**, Platform **1.10.2** (đọc manifest của JAR đi kèm); không suy ra từ tên JAR rằng đây là JUnit 4.
- `Board.SIZE=15`, `WIN_COUNT=5`; mặc định RULE_B. `checkWin`: >=5 quân liên tiếp; chỉ đối thủ đứng ngay cạnh hai đầu mới ngăn thắng; mép không tính chặn. Có 4 hướng (0,1), (1,0), (1,1), (1,-1).
- `Board.undoMove(int row,int col)` chỉ xóa ô và giảm moveCount; Board không có biến lượt hay getCurrentTurn. Lượt nằm trong BoardPanel, bị loại theo yêu cầu.
- `ScoreTracker.resetScores()` xóa ba bộ đếm, giữ recordedGameIds. `newGame()` không xóa lịch sử hay điểm.
- Minimax: EASY depth=1 nhưng đi nhánh chọn heuristic/ngẫu nhiên trước vòng minimax; MEDIUM depth=2, HARD depth=3. Thắng/chặn bắt buộc chỉ nằm ở nhánh MEDIUM/HARD. EASY chặn theo xác suất 70%.
- NetworkManager: START trộn trong connectAndListen với Socket; processLine(Session,String) tách riêng MOVE/RESTART_REQUEST/RESTART. Session và processLine là private nhưng có thể dựng/gọi bằng reflection trong test, dùng StringWriter, không cần socket.

## Từng test hiện có

Tên hành vi tiếng Anh dưới đây được tách từ đúng tên phương thức và đối chiếu thân test; khi có DisplayName thì giữ mô tả đó, với ghi chú sửa các mô tả cũ rộng hơn assertion thật. Model: game.model; AI: game.ai; NetworkManager: game.network; các lớp UI: game.view. Mỗi hàng ghi phạm vi lớp được quan sát trong ca và fixture của nó.

### test/game/AiMoveDelayTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `waitsOnlyForTheMissingPart` | game.view.AiMoveDelay | waits Only For The Missing Part |
| `rejectsNegativeDelayOrElapsedTime` | game.view.AiMoveDelay; game.view.BoardPanel | rejects Negative Delay Or Elapsed Time |
| `fastSearchKeepsThinkingAndInputLockedUntilTheOriginalDeadline` | game.view.BoardPanel | fast Search Keeps Thinking And Input Locked Until The Original Deadline |
| `elapsedMinimumBeforeWorkerCompletionAddsNoTimer` | game.view.BoardPanel | elapsed Minimum Before Worker Completion Adds No Timer |
| `zeroDelayInjectionAppliesOnCompletionWithoutATimer` | game.view.BoardPanel | zero Delay Injection Applies On Completion Without ATimer |
| `undoButtonDuringWaitCancelsQueuedMoveEvenAfterAnotherHumanMove` | AiMoveDelay; BoardPanel; MainFrame | undo Button During Wait Cancels Queued Move Even After Another Human Move |
| `newGameButtonCancelsQueuedMoveBeforeStartingAnotherRound` | AiMoveDelay; BoardPanel; MainFrame | new Game Button Cancels Queued Move Before Starting Another Round |
| `menuCancelsQueuedMoveEvenAfterReturningToGame` | AiMoveDelay; BoardPanel; MainFrame | menu Cancels Queued Move Even After Returning To Game |
| `changingModeCancelsThePendingAiMove` | AiMoveDelay; BoardPanel; MainFrame | changing Mode Cancels The Pending Ai Move |
| `changingDifficultyRestartsSearchButOldTimerCannotTouchTheNewTimer` | AiMoveDelay; BoardPanel; MainFrame | changing Difficulty Restarts Search But Old Timer Cannot Touch The New Timer |
| `disposingWindowCancelsThePendingMoveWithoutChangingTheBoard` | AiMoveDelay; BoardPanel; MainFrame | disposing Window Cancels The Pending Move Without Changing The Board |
| `languageChangeUpdatesThinkingTextInPlaceWithoutRebuildingTheGame` | AiMoveDelay; BoardPanel; MainFrame | language Change Updates Thinking Text In Place Without Rebuilding The Game |
| `humanWinOrFullBoardDrawNeverSchedulesAnAiMove` | game.view.BoardPanel | human Win Or Full Board Draw Never Schedules An Ai Move |
| `otherModesDoNotUseTheDelayClockOrScheduleAi` | game.view.BoardPanel | other Modes Do Not Use The Delay Clock Or Schedule Ai |

### test/game/BoardVisibilityTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `realModalGameFlowPaintsGridAndStoneInChildJvm` | BoardPanel; MainFrame (qua BoardDisplayProbe) | real Modal Game Flow Paints Grid And Stone In Child Jvm |

### test/game/BoardWinTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `testSanitySetupPatternNormal` | game.model.Board | Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô |
| `testSanitySetupPatternAllO` | game.model.Board | Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X |
| `testSanitySetupPatternInverted` | game.model.Board | Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O |
| `testT01` | game.model.Board | T01: . X X X X X* . -> THẮNG |
| `testT02` | game.model.Board | T02: O X X X X* X . -> THẮNG |
| `testT03` | game.model.Board | T03: . X X* X X X O -> THẮNG |
| `testT04` | game.model.Board | T04: O X* X X X X O -> KHÔNG THẮNG |
| `testT05` | game.model.Board | T05: # X X X X X* . -> THẮNG |
| `testT06` | game.model.Board | T06: # X X X X X* O -> THẮNG |
| `testT07` | game.model.Board | T07: O X X X X X* # -> THẮNG |
| `testT08` | game.model.Board | T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG |
| `testT09` | game.model.Board | T09: O . X X X X X* . O (O không liền kề) -> THẮNG |
| `testT10` | game.model.Board | T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG |
| `testT11` | game.model.Board | T11: . X X X X X X* . -> THẮNG |
| `testT12` | game.model.Board | T12: O X X X X X X* . -> THẮNG |
| `testT13` | game.model.Board | T13: O X X X X X X* O -> KHÔNG THẮNG |
| `testT14` | game.model.Board | T14: # X X X X X X* . -> THẮNG |
| `testT15` | game.model.Board | T15: # X X X X X X* O -> THẮNG |
| `testT16` | game.model.Board | T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG |
| `testT17` | game.model.Board | T17: . X X X X X X X* . -> THẮNG |
| `testT18` | game.model.Board | T18: O X X X X X X X* O -> KHÔNG THẮNG |
| `testT19` | game.model.Board | T19: Hàng 9 quân mở 2 đầu -> THẮNG |
| `testT20` | game.model.Board | T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG |
| `testT21` | game.model.Board | T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG |
| `testT22` | game.model.Board | T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG |
| `testT23` | game.model.Board | T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG |
| `testT24` | game.model.Board | T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG |
| `testT25` | game.model.Board | T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG |
| `testT26` | game.model.Board | T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG |
| `testT27` | game.model.Board | T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG |
| `testT28` | game.model.Board | T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG |
| `testT29` | game.model.Board | T29: . X X X O X* X . -> KHÔNG THẮNG |
| `testT30` | game.model.Board | T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG |
| `testT31` | game.model.Board | T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG |
| `testT32` | game.model.Board | T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG |
| `testT33` | game.model.Board | T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG |
| `testT34` | game.model.Board | T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG |
| `testT35` | game.model.Board | T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG |
| `testT36` | game.model.Board | T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) |
| `testT37` | game.model.Board | T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG |
| `testT38` | game.model.Board | T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG |
| `testT39` | game.model.Board | T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG |
| `testT40` | game.model.Board | T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG |
| `testT41` | game.model.Board | T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG |
| `testT42` | game.model.Board | T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG |
| `testT43` | game.model.Board | T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG |
| `testT44` | game.model.Board | T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG |
| `testT45` | game.model.Board | T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG |
| `testT46` | game.model.Board | T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG |
| `testT47` | game.model.Board | T47: . O O O O O* . -> O THẮNG |
| `testT48` | game.model.Board | T48: X O O O O O* X -> KHÔNG THẮNG |
| `testT49` | game.model.Board | T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA |
| `testT50` | game.model.Board | T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) |
| `testT51` | game.model.Board | Từ chối ô đã có quân; giữ Piece.X và getMoveCount. Không có kiểm lượt trong Board. |
| `testT52` | game.model.Board | T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ |

### test/game/F1ProtocolTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `invalidPlyOrColorDoesNotChangeBoard` | game.view.BoardPanel | invalid Ply Or Color Does Not Change Board |
| `wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored` | game.view.BoardPanel | wrong Round Duplicate Wrong Turn And Occupied Square Are Ignored |
| `queuedWireMoveKeepsOldRoundAfterReset` | game.network.NetworkManager; game.view.BoardPanel | queued Wire Move Keeps Old Round After Reset |
| `simultaneousSameBaseRestartAndStaleRequestResetOnce` | game.network.NetworkManager | simultaneous Same Base Restart And Stale Request Reset Once |
| `clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof` | game.network.NetworkManager | client Sends One Request Accepts Only Matching Base And Clears Wait On Eof |
| `startTimeoutClosesSession` | game.network.NetworkManager | start Timeout Closes Session |
| `mismatchedVersionClosesSession` | game.network.NetworkManager | mismatched Version Closes Session |
| `garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect` | game.network.NetworkManager | garbage Missing Fields Oversized And Wrong Color Do Not Disconnect |
| `waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult` | game.view.MainFrame; game.view.BoardPanel; game.network.NetworkManager | waiting Locks Moves And Round Change Closes Popup Rejecting Old Result |

### test/game/F2MoveOrderTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `finalMoveIsSentBeforeResultAndResultIsProcessedOnce` | BoardPanel; ScoreTracker; NetworkManager | final Move Is Sent Before Result And Result Is Processed Once |

### test/game/F3HeuristicTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `blockedSixIsNotFive` | game.ai.HeuristicEvaluator; game.model.Board | blocked Six Is Not Five |
| `singleThreeIsNotDoubleThree` | game.ai.HeuristicEvaluator; game.model.Board | single Three Is Not Double Three |
| `actualCrossDoubleThreeIsRecognized` | game.ai.HeuristicEvaluator; game.model.Board | actual Cross Double Three Is Recognized |
| `openFourIsThreatButNotTerminal` | game.ai.HeuristicEvaluator; game.model.Board | open Four Is Threat But Not Terminal |
| `edgeFiveMatchesBoardAndDominatesOtherThreats` | game.ai.HeuristicEvaluator; game.model.Board | edge Five Matches Board And Dominates Other Threats |
| `openSixMatchesSelectedRule` | game.ai.HeuristicEvaluator; game.model.Board | open Six Matches Selected Rule |
| `nonTerminalThreatsNeverOutrankWin` | game.ai.HeuristicEvaluator; game.model.Board | non Terminal Threats Never Outrank Win |

### test/game/F4LayoutTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `directStartupBoundsAndRender` | MainFrame; BoardPanel | direct Startup Bounds And Render |
| `allTransitionsKeepControlsInsideParents` | MainFrame; BoardPanel | all Transitions Keep Controls Inside Parents |

### test/game/I18nSourceScanTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `i1a_noIsVICall` | src/game/view/*.java (quet ma nguon) | I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java) |
| `i1b_noVietnameseInStringLiterals` | src/game/view/*.java (quet ma nguon) | I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.java) |

### test/game/LangPurityTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `a_ptNoVietnameseChars` | game.view.Lang | A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...) |
| `b_enAsciiOnly` | game.view.Lang | B – EN chỉ chứa ASCII (0x00-0x7F) |
| `c_sharedThemeContrast` | game.view.Theme; game.view.ThemedButton | C – Shared theme/button contrast >= 4.5 (WCAG AA) |

### test/game/NetworkStabilityTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `cancelPendingHostReleasesPortWithoutError` | game.network.NetworkManager | cancel Pending Host Releases Port Without Error |
| `hostAndClientExchangeMovesRestartAndDisconnect` | game.network.NetworkManager | host And Client Exchange Moves Restart And Disconnect |
| `malformedMoveDoesNotKillConnectionAndOrderIsPreserved` | game.network.NetworkManager | malformed Move Does Not Kill Connection And Order Is Preserved |
| `callbacksAlreadyQueuedAreDiscardedAfterCancel` | game.network.NetworkManager | callbacks Already Queued Are Discarded After Cancel |
| `reconnectCannotBeClosedOrNotifiedByPreviousSession` | game.network.NetworkManager | reconnect Cannot Be Closed Or Notified By Previous Session |
| `staleErrorIsDiscardedWhenNewSessionStarts` | game.network.NetworkManager | stale Error Is Discarded When New Session Starts |
| `occupiedPortReportsTypedHostError` | game.network.NetworkManager | occupied Port Reports Typed Host Error |

### test/game/P1RegressionTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `languageChangePreservesActiveGame` | game.view.MainFrame; game.view.BoardPanel; game.view.Lang | language Change Preserves Active Game |
| `languageChangePreservesFinishedGameAndTranslatesResult` | game.view.MainFrame; game.view.BoardPanel; game.view.Lang | language Change Preserves Finished Game And Translates Result |
| `pvpUndoAfterWinRestoresWinningPlayerTurn` | game.view.BoardPanel; game.model.Board | pvp Undo After Win Restores Winning Player Turn |
| `pvpUndoAfterDrawRestoresLastPlayerTurn` | game.view.BoardPanel; game.model.Board | pvp Undo After Draw Restores Last Player Turn |
| `onlineRejectsOpponentMoveOnLocalTurn` | game.view.BoardPanel; game.model.Board | online Rejects Opponent Move On Local Turn |
| `onlineRejectsMoveAfterSwitchingMode` | game.view.BoardPanel; game.model.Board | online Rejects Move After Switching Mode |
| `onlineAcceptsOnlyOneMoveFromCorrectOpponent` | game.view.BoardPanel; game.model.Board | online Accepts Only One Move From Correct Opponent |
| `onlineRejectsQueuedMoveAfterNewGame` | game.view.BoardPanel; game.model.Board | online Rejects Queued Move After New Game |
| `onlineRejectsInvalidSquaresAndMovesAfterGameOver` | game.view.BoardPanel; game.model.Board | online Rejects Invalid Squares And Moves After Game Over |

### test/game/P2LifecycleTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `disposedFrameDoesNotReceiveLanguageEvents` | game.view.MainFrame; game.view.Lang | disposed Frame Does Not Receive Language Events |
| `disposingFrameCancelsItsWorkerAndPreservesBoard` | game.view.MainFrame; game.view.BoardPanel | disposing Frame Cancels Its Worker And Preserves Board |
| `closingDialogCancelsPendingHostAndIgnoresLateResults` | game.view.NetworkDialog; game.network.NetworkManager | closing Dialog Cancels Pending Host And Ignores Late Results |
| `successfulDialogDisposalKeepsConnection` | game.view.NetworkDialog; game.network.NetworkManager | successful Dialog Disposal Keeps Connection |
| `cancellingOnlineSelectionRestoresModeWithoutResettingGame` | game.view.MainFrame; game.view.BoardPanel; game.view.NetworkDialog | cancelling Online Selection Restores Mode Without Resetting Game |
| `receivedMoveIsAppliedBeforeNextEdtNetworkCallback` | game.view.BoardPanel | received Move Is Applied Before Next Edt Network Callback |
| `networkErrorsAreTranslated` | game.view.Lang | network Errors Are Translated |
| `interruptedAiAbortsWithoutMutatingBoard` | game.ai.MinimaxAI; game.model.Board | interrupted Ai Aborts Without Mutating Board |
| `cancellationInsideRecursiveSearchUnwindsAllMoves` | game.ai.MinimaxAI; game.model.Board | cancellation Inside Recursive Search Unwinds All Moves |

### test/game/PlayerPieceBadgeTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `pureMappingAssignsBlackToXAndWhiteToO` | game.view.PieceBadgeIcon | pure Mapping Assigns Black To XAnd White To O |
| `badgePixelsUseBoardPaletteAndAntialiasedWhiteOutline` | game.view.PieceBadgeIcon | badge Pixels Use Board Palette And Antialiased White Outline |
| `realCardsFollowModeSideAndLanguageWithoutChangingScoresOrTurnBorder` | game.view.PlayerCard; game.view.MainFrame; game.view.PieceBadgeIcon | real Cards Follow Mode Side And Language Without Changing Scores Or Turn Border |
| `longNamesEllipsizeInsideBothCardsWithoutPushingBadgesOut` | game.view.PlayerCard; game.view.MainFrame | long Names Ellipsize Inside Both Cards Without Pushing Badges Out |

### test/game/SoundResourceTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `closesClipAfterPlaybackStops` | game.view.SoundManager | closes Clip After Playback Stops |
| `closesClipWhenOpenFails` | game.view.SoundManager | closes Clip When Open Fails |
| `closesClipWhenStartFails` | game.view.SoundManager | closes Clip When Start Fails |
| `staleResultClosesClipWithoutStarting` | game.view.SoundManager | stale Result Closes Clip Without Starting |
| `queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice` | game.view.SoundManager | queued Piece Sound Checks Game Validity Before Opening Any Audio Device |

### test/game/Task4LayoutTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `allThreeCardsFitAllLocalesOnStartupAndAfterTransitions` | MainFrame; StartScreen; MainMenu; BoardPanel | all Three Cards Fit All Locales On Startup And After Transitions |

### test/game/Task4LifecycleTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame` | MainFrame; BoardPanel; NetworkManager | menu Cancels Actual Ai Worker And Its Completion Cannot Change Next Game |
| `menuClosesRealLanSocketAndAllConnectionDialogs` | MainFrame; BoardPanel; NetworkManager | menu Closes Real Lan Socket And All Connection Dialogs |
| `menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart` | MainFrame; BoardPanel; NetworkManager | menu Closes Pending Network Dialog And Cannot Restore Old Mode After New Start |
| `queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart` | MainFrame; BoardPanel; NetworkManager | queued Network Ui Callback Cannot Run Even After Menu And New Start |
| `menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged` | MainFrame; BoardPanel; NetworkManager | menu Rejects Board Clicks Already Queued Before The Card Changed |
| `menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning` | MainFrame; BoardPanel; NetworkManager | menu Interrupts AWorker Whose Background Body Is Already Running |
| `queuedOpponentMoveCannotChangeBoardAfterMenu` | MainFrame; BoardPanel; NetworkManager | queued Opponent Move Cannot Change Board After Menu |
| `menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult` | MainFrame; BoardPanel; NetworkManager | menu Closes Result Popup Preserves Earned Score And Rejects Late Result |
| `undoRemainsAvailableAfterWin` | MainFrame; BoardPanel; NetworkManager | undo Remains Available After Win |

### test/game/Task4PreferencesTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `profileAndLanguageRoundTripThroughInMemoryNode` | AppSettings; MainFrame; PlayerProfile; Lang | profile And Language Round Trip Through In Memory Node |

### test/game/Task4StartScreenTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `titleMenuSetupAndGameUseTheSameFrame` | MainFrame; StartScreen; AppSettings; ProfileDialog; SettingsDialog | title Menu Setup And Game Use The Same Frame |
| `emptyProfileUsesTranslatedDefaultWithoutPersistingIt` | MainFrame; StartScreen; AppSettings; ProfileDialog; SettingsDialog | empty Profile Uses Translated Default Without Persisting It |
| `normalizeUnicodeWhitespaceAndLongNames` | game.view.AppSettings | normalize Unicode Whitespace And Long Names |
| `profileFieldLimitsUnicodeAndNamesRemainPlainText` | MainFrame; StartScreen; AppSettings; ProfileDialog; SettingsDialog | profile Field Limits Unicode And Names Remain Plain Text |
| `settingsApplyLanguageImmediatelyWithoutChangingProfileDraft` | MainFrame; StartScreen; AppSettings; ProfileDialog; SettingsDialog | settings Apply Language Immediately Without Changing Profile Draft |
| `profileChangesAndMenuKeepExistingSessionTotals` | MainFrame; StartScreen; AppSettings; ProfileDialog; SettingsDialog | profile Changes And Menu Keep Existing Session Totals |

### test/game/Task4bFlowTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `lanMenuJoinUsesRealSocketAndStartsTheExistingOnlineGame` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | lan Menu Join Uses Real Socket And Starts The Existing Online Game |
| `rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | rules Have All Six Supplied Lines And Open From Both Menu And Game |
| `titleAcceptsMouseAndAnyKeyAndStopsItsTimer` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | title Accepts Mouse And Any Key And Stops Its Timer |
| `enterCommitsProfileAndEscapeCancelsEveryDialogDraft` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | enter Commits Profile And Escape Cancels Every Dialog Draft |
| `setupRemembersDifficultyAndPlayerTwoOnlyAfterStart` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | setup Remembers Difficulty And Player Two Only After Start |
| `allCardsAndDialogsFitBothLooksWithStableFrameSize` | MainFrame; StartScreen; SetupDialog; ProfileDialog; SettingsDialog; RulesDialog; NetworkManager | all Cards And Dialogs Fit Both Looks With Stable Frame Size |

### test/game/Task4bFoundationTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `profileNames` | game.view.PlayerProfile | profile Names |
| `invalidAvatarsDefaultToZero` | game.view.PlayerProfile | invalid Avatars Default To Zero |
| `profileRoundTripKeepsLegacyKeyAndImplicitDefault` | game.view.AppSettings; game.view.PlayerProfile | profile Round Trip Keeps Legacy Key And Implicit Default |
| `missingImagesRenderAndCoverCacheTracksDevicePixels` | game.view.BackgroundPanel; game.view.AvatarIcon | missing Images Render And Cover Cache Tracks Device Pixels |
| `buttonsPaintFourDistinctStatesUnderBothLooks` | game.view.ThemedButton | buttons Paint Four Distinct States Under Both Looks |

### test/game/Task4bGameplayTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel` | MainFrame; BoardPanel; SetupDialog; SettingsDialog; ScoreTracker | computer And Setup Use Translated Difficulty Instead Of Enum Label |
| `realGameResultsUpdateBothCardsAndDrawCount` | MainFrame; BoardPanel; SetupDialog; SettingsDialog; ScoreTracker | real Game Results Update Both Cards And Draw Count |
| `scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu` | MainFrame; BoardPanel; SetupDialog; SettingsDialog; ScoreTracker | scores Reset Only On Mode Or Difficulty Change And Keep On Profile And Menu |
| `resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame` | MainFrame; BoardPanel; SetupDialog; SettingsDialog; ScoreTracker | reset Score Confirmation Clears Numbers Without Changing Current Game |
| `resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain` | game.model.ScoreTracker | resetting Numbers Does Not Allow Undo To Record An Old Result Again |
| `boardIsCentredAndMouseUsesUnchangedCoordinates` | MainFrame; BoardPanel; SetupDialog; SettingsDialog; ScoreTracker | board Is Centred And Mouse Uses Unchanged Coordinates |

### test/game/Task4bPresentationTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `buttonsRenderFourStatesAndActivateWithEnterAndSpace` | game.view.ThemedButton | buttons Render Four States And Activate With Enter And Space |
| `renderAllSixViewsAndMeasureDisplayedFrame` | ThemedButton; MainFrame; ProfileEditor; Lang; ResourceImages | render All Six Views And Measure Displayed Frame |
| `translatedButtonAndLabelTextFitsIncludingTransitions` | ThemedButton; MainFrame; ProfileEditor; Lang; ResourceImages | translated Button And Label Text Fits Including Transitions |
| `profileClearCounterAndFocusSelectAllUseActualEditor` | game.view.ProfileEditor | profile Clear Counter And Focus Select All Use Actual Editor |
| `suppliedRulesKeysAreVerbatimAndPortugueseIsPure` | game.view.Lang | supplied Rules Keys Are Verbatim And Portuguese Is Pure |
| `runtimeResourcePackagingAndImageLoadingWorkOnBinClasspath` | game.view.ResourceImages | runtime Resource Packaging And Image Loading Work On Bin Classpath |

### test/game/TwoPlayerSetupDialogTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `bothPlayersHaveSymmetricHeadersAndNameLabels` | SetupDialog; ProfileEditor; MainFrame | both Players Have Symmetric Headers And Name Labels |
| `duplicateWarningUpdatesImmediatelyAndBlocksButtonAndEnter` | SetupDialog; ProfileEditor; MainFrame | duplicate Warning Updates Immediately And Blocks Button And Enter |
| `avatarLocksFollowBothChoicesAndLockedIconsAreDimmed` | SetupDialog; ProfileEditor; MainFrame | avatar Locks Follow Both Choices And Locked Icons Are Dimmed |
| `startCommitsBothDraftsAndCancelDiscardsChanges` | SetupDialog; ProfileEditor; MainFrame | start Commits Both Drafts And Cancel Discards Changes |

### test/game/TwoPlayerSetupValidationTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `resolvesBlankAndTrimmedNames` | game.view.TwoPlayerSetupValidation | resolves Blank And Trimmed Names |
| `comparesEffectiveNamesAfterTrimIgnoringCase` | game.view.TwoPlayerSetupValidation | compares Effective Names After Trim Ignoring Case |
| `defaultsAreExplicitAndLocaleIndependent` | game.view.TwoPlayerSetupValidation | defaults Are Explicit And Locale Independent |
| `locksTheOtherPlayersAvatarInBothDirections` | game.view.TwoPlayerSetupValidation | locks The Other Players Avatar In Both Directions |
| `keepsAnAvailableSecondAvatar` | game.view.TwoPlayerSetupValidation | keeps An Available Second Avatar |
| `resolvesOnlyTheInitialSecondAvatarConflict` | game.view.TwoPlayerSetupValidation | resolves Only The Initial Second Avatar Conflict |
| `duplicateAvatarsCannotStartEvenWithDistinctNames` | game.view.TwoPlayerSetupValidation | duplicate Avatars Cannot Start Even With Distinct Names |

### test/game/UILangSoundTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `c1_langAllKeysNotNullNotEmpty` | game.view.Lang | C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist) |
| `c1b_connectingMsgContainsIp` | game.view.Lang | C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ |
| `c2_sharedButtonPaintAndContrast` | game.view.ThemedButton; game.view.Theme | C2 – Shared custom button painting and contrast >= 4.5 |
| `b2_settingsSoundToggle` | game.view.SettingsDialog; game.view.SoundManager | B2 – Click btnSound toggle SoundManager.isSoundEnabled() |
| `b3_settingsSoundTooltipByLang` | game.view.SettingsDialog; game.view.Lang | B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off |
| `c3_menuAndSettingsUpdateImmediately` | game.view.MainFrame; game.view.SettingsDialog; game.view.Lang | C3 – Menu and settings update VI/EN/PT immediately |
| `d1_appSettingsRoundTrip` | game.view.AppSettings | D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → VI |
| `d2_inMemoryNodeIsDifferentFromProduction` | test helper PreferencesIsolation | D2 – Preferences test node khác node production |
| `i2_speakerIconVisual` | game.view.SpeakerIcon; game.view.SoundManager | I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager |

### test/game/UndoParityTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `testP1_XWin_UndoOneMove` | BoardPanel; Board; ScoreTracker | P1: PV_AI, X thắng ở nước thứ 9, Undo chỉ rút đúng 1 nước của X |
| `testP1b_PlayAgainAfterUndo_ScoreUnchanged` | BoardPanel; Board; ScoreTracker | P1b: Tiếp P1, người chơi đi lại (7,11): ván kết thúc lần 2, điểm vẫn không đổi |
| `testP2_OnlyOneMove_Undo` | BoardPanel; Board; ScoreTracker | P2: X(7,7), undoMove(): size 0, lượt X |
| `testP3_XAndO_UndoTwoMoves` | BoardPanel; Board; ScoreTracker | P3: X(7,7) O(0,0) (AI vừa đi), undoMove(): size 0, lượt X |
| `testP4_DrawAtMove225_UndoOneMove` | BoardPanel; Board; ScoreTracker | P4: Hòa ở nước thứ 225 (nước cuối là của X), Undo chỉ rút 1 nước của X |
| `testP5_AlternatingPieceCountAfterUndo` | BoardPanel; Board; ScoreTracker | P5: Kiểm tra số quân trên bàn luân phiên đúng sau Undo ở P1 |

### test/game/UndoScoreTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `testU01_AiWinRecorded` | BoardPanel; Board; ScoreTracker | U01: AI thắng, ghi nhận thành tích 0-1 (Thắng: 0, Thua: 1, Hòa: 0) |
| `testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse` | BoardPanel; Board; ScoreTracker | U02: Undo xong, thành tích vẫn 0-1, isGameOver = false |
| `testU03_PlayAgainAndFinish_NoDuplicateScore` | BoardPanel; Board; ScoreTracker | U03: Chơi tiếp rồi kết thúc lần nữa trong cùng ván: không ghi trùng điểm |
| `testU04_UndoWhenHistoryEmpty_NoException` | BoardPanel; Board; ScoreTracker | U04 / 6a: Undo khi chưa có nước đi không lỗi, không đổi gì, canUndo = false |
| `testU05_UndoMultipleTimesUntilEmpty_RestoresStateCorrectly` | BoardPanel; Board; ScoreTracker | U05 / 6b: Undo nhiều lần liên tiếp đến hết lịch sử, bàn cờ và lượt khôi phục đúng |
| `testU06_ResetGame_NewGameId_ScoresPreserved_NewResultRecorded` | BoardPanel; Board; ScoreTracker | U06 / 6d: Reset/ván mới tạo gameId mới, kết quả ván mới được ghi, dữ liệu cũ giữ nguyên |
| `testU07_DrawGame_Undo_DrawScorePreserved` | BoardPanel; Board; ScoreTracker | U07: Kết thúc bằng hòa rồi Undo: thành tích hòa không bị xóa, không ghi trùng |
| `testU08_AiWorkerTokenDiscardedOnResetOrUndo` | BoardPanel; Board; ScoreTracker | resetGame tăng gameId và tắt isAiThinking; ca này không gọi undoMove. |
| `test6e_PvpUndoOneMove_PvAiUndoTwoMoves_OnlineUndoForbidden` | BoardPanel; Board; ScoreTracker | 6e: PvP undo 1 nước, PvAI undo tối đa 2 nước, Online cấm Undo (canUndo = false) |
| `test7_OnlineMoveIgnoredWhenGameOver` | BoardPanel; Board; ScoreTracker | 7: Online - Gói MOVE đến khi isGameOver = true phải bị bỏ qua |
| `test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01` | BoardPanel; Board; ScoreTracker | 8 & 9 (End-to-End): AI thắng (0-1) -> Undo -> người chơi thắng cùng ván -> điểm vẫn 0-1, bàn cờ và lượt đúng |

### test/game/ai/AITacticsTest.java

| Phương thức | Lớp được kiểm | Điều được kiểm |
|---|---|---|
| `winsImmediatelyWithOneOpenEnd` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | wins Immediately With One Open End |
| `blocksImmediateLossWithOneOpenEnd` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | blocks Immediate Loss With One Open End |
| `takesWinInsteadOfBlockingOpponentWin` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | takes Win Instead Of Blocking Opponent Win |
| `blocksOpponentOpenThree` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | blocks Opponent Open Three |
| `createsOpenFourWhenSafe` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | creates Open Four When Safe |
| `hardCreatesCrossDoubleThree` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | hard Creates Cross Double Three |
| `hardPreventsOpponentCrossDoubleThree` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | hard Prevents Opponent Cross Double Three |
| `blockedFiveIsNotAWinAndRealThreatRemains` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | blocked Five Is Not AWin And Real Threat Remains |
| `completedSearchPreservesEveryCellCountAndRule` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | completed Search Preserves Every Cell Count And Rule |
| `easyAlwaysReturnsLegalMoveOnNearlyFullBoard` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | easy Always Returns Legal Move On Nearly Full Board |
| `emptyBoardReturnsCentreWithoutMutation` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | empty Board Returns Centre Without Mutation |
| `fullDrawBoardReturnsNullWithoutMutation` | game.ai.MinimaxAI (va Board/HeuristicEvaluator de xac minh the co) | full Draw Board Returns Null Without Mutation |

## Helper / probe cũ không có @Test

- `test/game/BoardDisplayProbe.java`: probe vẽ Swing trong JVM con, có main; GUI, không chạy.
- `test/game/MemoryPreferences.java`: Preferences trong bộ nhớ, hỗ trợ test khác.
- `test/game/PreferencesIsolation.java`: cô lập Preferences, hỗ trợ test khác.

## Bốn chương trình kiểm thử ngoài JUnit trong src/game

- `RuleAndDrawTest.java`: `testBlockedEndsRule` có 8 kiểm tra (5 mở; 5 chặn một đầu; 5 chặn hai đầu; 5 sát mép mở; mép + một đầu chặn; chéo chặn hai đầu; chéo chặn một đầu; 6 chặn hai đầu). `testBoardFullAndDraw` có 9 kiểm tra (224 chưa đầy; ô cuối hợp lệ; moveCount=225; bàn đầy; nước cuối không thắng; không đánh thêm ô đã kín; bàn thắng có 224 quân; nước 225 thắng; bàn thắng đầy). Dùng main/custom assert, không phải JUnit.
- `FullGomokuTestSuite.java`: runGroup1 T01–T10 (hàng 5); runGroup2 T11–T16 (hàng 6 A/B); runGroup3 T17–T20 (7–15 quân); runGroup4 T21–T24 (nối đoạn); runGroup5 T25–T30 (thiếu/đứt/ngắt); runGroup6 T31–T38 (nhiều hướng); runGroup7 T39–T46 (dọc/chéo/biên); runGroup8 T47–T48 (đảo màu); runGroup9 T49–T52 (đầy/thắng cuối/ô kín/ngoài biên), T53–T54 dùng BoardPanel/Swing; T55–T57 pending trong chương trình. Không tách một phần main để chạy và không tính vào số JUnit.
- `ConsoleTest.java`: main in kết quả trước/sau quân X thứ 5 ngang, ba O chéo cộng một O lệch, rồi in bàn; không có assertion tự động hay @Test.
- `AIBenchmarkTest.java`: main dựng 7 quân và in thời gian/nodes/pruning/nước chọn ở ba mức; benchmark không có assertion hay @Test.
- Không chạy các main ngoài JUnit trong lượt yêu cầu; không tính chúng vào PASS/FAIL hoặc số phương thức JUnit bị loại.

## Bước 2 — chỉ các khoảng trống cần bổ sung

Phân biệt đã có assertion trong test GUI với chưa có ca thuần logic được phép chạy. Không thêm lại thắng bốn hướng, biên RULE_B, hòa bàn đầy, ô đã có quân, tính hợp lệ nước AI, thắng/chặn tức thì MEDIUM/HARD, bảo toàn bàn hay ba mức: các ca đó đã có trong BoardWinTest/F3HeuristicTest/AITacticsTest.

| Khoảng trống | Bằng chứng / cách bổ sung |
|---|---|
| Board.makeMove ngoài biên: kết quả false, bảo toàn mọi ô và moveCount ở cả bốn phía | testT52 chỉ assert isValidMove và không ném lỗi; main suite chỉ kiểm một phần. Thêm ca tham số hóa -1/15 ở hàng/cột. |
| Board.undoMove thuần model: chỉ xóa đúng ô, giảm moveCount, giữ các ô khác | UndoParityTest/UndoScoreTest gọi BoardPanel trong setup. Thêm một ca Board trực tiếp. Khôi phục lượt: CHƯA XÁC NHẬN bằng Board vì không có API/trạng thái lượt. |
| ScoreTracker ghi thắng/thua/hòa và ba getter trong test thuần model, cả humanPiece X/O | Test cũ chủ yếu qua BoardPanel/MainFrame; ca thuần resettingNumbers chỉ kiểm reset humanWins và chống ghi lại. Thêm từng loại kết quả, từ chối ID trùng trước reset và reset cả ba bộ đếm. |
| EASY không chạy Minimax | Test cũ chưa assert getNodesEvaluated/getPruningCount=0. Test mới seed Random=20261103, thế cờ đơn giản. |
| Heuristic: so trực tiếp thế thắng / open-four với thế thường | F3HeuristicTest chủ yếu so với các hằng ngưỡng. Thêm quan hệ > giữa điểm các Board, không assert giá trị tuyệt đối. |
| NetworkManager kiểm thông điệp không socket | Test giao thức cũ dùng socket/GUI. Thêm MOVE, RESTART_REQUEST, RESTART qua processLine/sendMove/requestRestart với Session trong bộ nhớ. |
| START không có điểm gọi độc lập khỏi socket | **không kiểm được nếu không sửa mã**: cả tạo và parse START nằm trong connectAndListen(Session), gọi/đợi Socket thật. Bỏ qua, giữ nguyên mã. |

## Phạm vi lượt chạy

Người dùng chọn phương án 1: chọn lớp/phương thức, không sửa/xóa test cũ. Chỉ 75 phương thức JUnit cũ thuộc phạm vi được chọn; 142 phương thức cũ bị loại. Danh sách từng mục và lý do được đưa vào mục H của REPORT.md sau lượt chạy.

Phân loại: "kịch bản ván cờ" là replay diễn biến ván theo lượt, không chỉ dựng fixture rồi kiểm một thao tác. Các test được chọn hiện có đều là hành vi đơn lẻ trên fixture/thế cờ, kể cả các fixture bàn đầy và AI chiến thuật. Danh sách từng lượt tham số hóa sẽ lấy từ XML thật sau chạy.

Lưu ý test AI cũ: AITacticsTest tạo EASY với Random nội bộ chưa seed (nhất là 50 lần ở mỗi ca nearly-full), nhưng chỉ assert tính hợp lệ/bảo toàn, không đoán ô ngẫu nhiên. Giữ nguyên theo yêu cầu; chỉ test EASY mới được seed bằng reflection. Không thể tuyên bố toàn bộ test AI cũ đã có seed.
