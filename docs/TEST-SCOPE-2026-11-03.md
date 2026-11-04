# Phạm vi và số liệu kiểm thử — 03/11/2026

Báo cáo chỉ đọc bằng chứng đã có; không chạy Java, javac, JUnit hoặc bất kỳ bộ test nào. Tệp mới duy nhất được tạo là báo cáo này. Không commit, không đổi nhánh, không sửa log/XML/kiểm kê/mã nguồn.

Git tại lúc đối chiếu: nhánh **task4-start-screen**, HEAD **a21ce2d6adba07e3d5d7a344a54c2b55a5ffa2d7**, cây sạch trước khi tạo báo cáo. HEAD đúng commit sửa lỗi đã nhập vào nhánh; không có merge commit riêng tại HEAD. Log và ba XML cuối trùng byte với các tệp được lưu trong commit này. Số liệu dưới đây kế thừa lượt chạy bản sửa đã lưu, không phải một lượt chạy mới trên task4-start-screen sau merge.

Nguồn chính: [log cuối](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log>), [XML cuối](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-after-fix-score-undo/xml/TEST-junit-jupiter.xml>), [INVENTORY gốc](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/INVENTORY.md>), [EXECUTED-TESTS gốc](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/EXECUTED-TESTS.md>), [REPORT gốc](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/REPORT.md>). Đối chiếu máy đọc thêm existing-tests.json/executed-tests.json, XML 158 gốc và mã nguồn fixture để xác định tên, nguồn gốc và phạm vi; không dùng tổng trong báo cáo cũ thay cho việc đếm XML.

## A. Tổng số lượt chạy / PASS / FAIL / ERROR / SKIPPED

| Lượt JUnit thực thi test | PASS | FAIL | ERROR | SKIPPED | Aborted |
|---:|---:|---:|---:|---:|---:|
| **190** | **190** | **0** | **0** | **0** | **0** |

190 là số lượt testcase (bao gồm từng lượt tham số hóa), tương ứng **118 phương thức test**, trong **một lần JUnit execute** của log cuối. Không cộng container, lần biên dịch, lượt chứng minh assertion cũ FAIL hoặc các lượt test ở log khác.

Bằng chứng từ [log, dòng 644](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log:644>):

```text
[       190 tests found           ]
[         0 tests skipped         ]
[       190 tests started         ]
[         0 tests aborted         ]
[       190 tests successful      ]
[         0 tests failed          ]
```

XML Jupiter: `tests="190" skipped="0" failures="0" errors="0"`; đếm đủ 190 phần tử testcase có định danh duy nhất, không có phần tử failure/error/skipped. XML Vintage và Platform Suite đều 0 test. Tổng ba XML vẫn 190, không nhân ba. [Thuộc tính XML Jupiter](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-after-fix-score-undo/xml/TEST-junit-jupiter.xml:2>).

Log ghi `TEST_EXECUTION_NUMBER: 1`, exit code JUnit 0, bắt đầu `2026-11-03T13:03:25.4097523+07:00`, kết thúc `2026-11-03T13:03:30.7548484+07:00`. Những phương thức không được chọn tại mục D không phải SKIPPED của JUnit.

## B. Theo lớp được kiểm

Mỗi lượt được gán đúng một lớp/nhóm chính để cộng khớp 190. Board và Heuristic có thể được dùng làm fixture trong test AI; không cộng lại các lời gọi phụ. Hai lớp Undo được đặt thành nhóm riêng, không cộng thêm vào Board hay ScoreTracker.

| Lớp/nhóm chính | Các lớp JUnit nguồn (lượt trong XML) | Đã chạy | PASS |
|---|---|---:|---:|
| game.model.Board | BoardWinTest (55) + BoardModelContractTest (5) | 60 | 60 |
| game.model.ScoreTracker | ScoreTrackerContractTest (10) + **một phương thức** thuần logic Task4bGameplayTest (1) + ScoreTrackerRevokeTest (15) | 26 | 26 |
| game.ai.MinimaxAI | AITacticsTest (32) + MinimaxEasyContractTest (1) | 33 | 33 |
| game.ai.HeuristicEvaluator | F3HeuristicTest (28) + HeuristicOrderingTest (4) | 32 | 32 |
| game.network.NetworkManager | NetworkMessageTest (22), xử lý/tạo thông điệp trong bộ nhớ | 22 | 22 |
| luồng Undo/tỉ số qua fixture | UndoScoreTest (11) + UndoParityTest (6), dùng BoardPanel thật và listener test | 17 | 17 |
| **Tổng** | **12 lớp JUnit có testcase được chạy** | **190** | **190** |

Phương thức Task4bGameplayTest duy nhất là `resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain`; không có nghĩa cả lớp GUI này đã chạy. Các số 55/5/10/1/15/32/1/28/4/22/11/6 được đếm trực tiếp theo thuộc tính classname trong XML cuối, khớp selectors trong lệnh log dòng 31.

## C. Nguồn gốc 190 lượt

| Nguồn gốc, tách rời nhau | Số phương thức | Lượt chạy | PASS |
|---|---:|---:|---:|
| Bộ test cũ giữ nguyên đã nằm trong lượt 158 gốc | 75 | 116 | 116 |
| Phương thức mới của đợt kiểm thử 03/11 | 18 | 42 | 42 |
| Phương thức mới của fix-score-undo | 8 | 15 | 15 |
| Fixture cũ được thêm vào lượt cuối (UndoScoreTest/UndoParityTest) | 17 | 17 | 17 |
| **Tổng** | **118** | **190** | **190** |

**116 + 42 = 158; 116 + 42 + 15 + 17 = 190.** Tập 158 định danh testcase (classname, name/tham số) từ XML gốc là tập con nguyên vẹn của XML cuối. Nhóm fixture không bị tính vào 116 ca cũ giữ nguyên.

Chi tiết 18 phương thức / 42 lượt: BoardModelContractTest **2/5**, ScoreTrackerContractTest **5/10**, MinimaxEasyContractTest **1/1**, HeuristicOrderingTest **2/4**, NetworkMessageTest **8/22**. Tổng phương thức **2+5+1+2+8=18**, tổng lượt **5+10+1+4+22=42**; đối chiếu NEW-TESTS/REPORT gốc và tên thực tế trong XML cuối.

Chi tiết 8 phương thức / 15 lượt của ScoreTrackerRevokeTest:

| Phương thức | Lượt / PASS |
|---|---:|
| `drawThenRevokeThenWinRemovesDraw` | 2 / 2 |
| `duplicateIdIsRejectedWithoutRevoke` | 1 / 1 |
| `humanWinThenRevokeThenOpponentWinReplacesResult` | 2 / 2 |
| `repeatedRevokeDoesNotSubtractTwice` | 1 / 1 |
| `revokingEachResultRestoresPreviousTotals` | 6 / 6 |
| `revokingOneGamePreservesOtherGames` | 1 / 1 |
| `revokingResultClearedByResetDoesNotSubtractAnotherGame` | 1 / 1 |
| `revokingUnknownIdLeavesExistingResultsUnchanged` | 1 / 1 |

17 fixture là **test có sẵn được đưa thêm vào phạm vi chạy**, không phải 17 phương thức mới viết: 13 thân test giữ nguyên; 4 ca U02, test8_And_9, P1, P4 đổi tên/assertion theo hành vi thu hồi. Hai setUp được thêm callback. Vì vậy không gọi toàn bộ 17 ca là “giữ nguyên mã test”. Ánh xạ bốn tên cũ để so với kiểm kê gốc:

| Tên cũ | Tên hiện tại đã chạy |
|---|---|
| `testU02_UndoAfterAiWin_ScoreUnchanged_GameOverFalse` | `testU02_UndoAfterAiWin_RevokesAiResult_ReopensGame` |
| `test8_And_9_EndToEnd_AiWin_Undo_PlayerWin_ScoreStays01` | `test8_And_9_AiWin_Undo_PlayerWin_ReplacesResult` |
| `testP1_XWin_UndoOneMove` | `testP1_XWin_UndoRevokesHumanResult` |
| `testP4_DrawAtMove225_UndoOneMove` | `testP4_DrawAtMove225_UndoRevokesDrawResult` |

## D. Phương thức bị loại khỏi lượt cuối

**125 phương thức**, giảm **17** so với 142 trước đây: chỉ bỏ khỏi danh sách loại trừ toàn bộ 11 ca UndoScoreTest và 6 ca UndoParityTest vì chúng đã được chọn trong lượt cuối. Các phương thức mới của cả hai đợt đều được chạy, không thêm phần tử loại trừ.

| Lý do theo kiểm kê/REPORT gốc | Trước | Nay | Thay đổi |
|---|---:|---:|---:|
| GUI/Swing (trong thân test hoặc hook của lớp) | 102 | 85 | -17 |
| GUI + socket thật | 6 | 6 | 0 |
| socket thật | 12 | 12 | 0 |
| ngoài phạm vi 5 lớp (không gán nhầm là GUI/socket) | 22 | 22 | 0 |
| **Tổng** | **142** | **125** | **−17** |

Đếm theo **phương thức**, không phải lượt tham số hóa: 217 phương thức cũ trong existing-tests/INVENTORY + 18 phương thức mới đợt đầu + 8 phương thức mới bản sửa = **243**; XML cuối có **118** phương thức đã chạy; **243 − 118 = 125**. Đã đối chiếu danh tính với các khai báo @Test/@ParameterizedTest hiện có trong test/ bằng đọc tĩnh, không chạy JUnit discovery.

Danh sách dưới đây giữ nguyên lý do gốc. Nhóm GUI/Swing có thể do hook của cả lớp; không kết luận mỗi thân phương thức tự nó bắt buộc mở cửa sổ. “Ngoài phạm vi” là ngoài phạm vi năm lớp của lượt gốc, không phải khẳng định không thể kiểm headless.

### D.1. GUI/Swing — 85 phương thức

| Tệp | Phương thức không được chọn |
|---|---|
| `test/game/AiMoveDelayTest.java` | `changingDifficultyRestartsSearchButOldTimerCannotTouchTheNewTimer` |
| `test/game/AiMoveDelayTest.java` | `changingModeCancelsThePendingAiMove` |
| `test/game/AiMoveDelayTest.java` | `disposingWindowCancelsThePendingMoveWithoutChangingTheBoard` |
| `test/game/AiMoveDelayTest.java` | `elapsedMinimumBeforeWorkerCompletionAddsNoTimer` |
| `test/game/AiMoveDelayTest.java` | `fastSearchKeepsThinkingAndInputLockedUntilTheOriginalDeadline` |
| `test/game/AiMoveDelayTest.java` | `humanWinOrFullBoardDrawNeverSchedulesAnAiMove` |
| `test/game/AiMoveDelayTest.java` | `languageChangeUpdatesThinkingTextInPlaceWithoutRebuildingTheGame` |
| `test/game/AiMoveDelayTest.java` | `menuCancelsQueuedMoveEvenAfterReturningToGame` |
| `test/game/AiMoveDelayTest.java` | `newGameButtonCancelsQueuedMoveBeforeStartingAnotherRound` |
| `test/game/AiMoveDelayTest.java` | `otherModesDoNotUseTheDelayClockOrScheduleAi` |
| `test/game/AiMoveDelayTest.java` | `rejectsNegativeDelayOrElapsedTime` |
| `test/game/AiMoveDelayTest.java` | `undoButtonDuringWaitCancelsQueuedMoveEvenAfterAnotherHumanMove` |
| `test/game/AiMoveDelayTest.java` | `waitsOnlyForTheMissingPart` |
| `test/game/AiMoveDelayTest.java` | `zeroDelayInjectionAppliesOnCompletionWithoutATimer` |
| `test/game/BoardVisibilityTest.java` | `realModalGameFlowPaintsGridAndStoneInChildJvm` |
| `test/game/F1ProtocolTest.java` | `invalidPlyOrColorDoesNotChangeBoard` |
| `test/game/F1ProtocolTest.java` | `waitingLocksMovesAndRoundChangeClosesPopupRejectingOldResult` |
| `test/game/F1ProtocolTest.java` | `wrongRoundDuplicateWrongTurnAndOccupiedSquareAreIgnored` |
| `test/game/F2MoveOrderTest.java` | `finalMoveIsSentBeforeResultAndResultIsProcessedOnce` |
| `test/game/F4LayoutTest.java` | `allTransitionsKeepControlsInsideParents` |
| `test/game/F4LayoutTest.java` | `directStartupBoundsAndRender` |
| `test/game/LangPurityTest.java` | `c_sharedThemeContrast` |
| `test/game/P1RegressionTest.java` | `languageChangePreservesActiveGame` |
| `test/game/P1RegressionTest.java` | `languageChangePreservesFinishedGameAndTranslatesResult` |
| `test/game/P1RegressionTest.java` | `onlineAcceptsOnlyOneMoveFromCorrectOpponent` |
| `test/game/P1RegressionTest.java` | `onlineRejectsInvalidSquaresAndMovesAfterGameOver` |
| `test/game/P1RegressionTest.java` | `onlineRejectsMoveAfterSwitchingMode` |
| `test/game/P1RegressionTest.java` | `onlineRejectsOpponentMoveOnLocalTurn` |
| `test/game/P1RegressionTest.java` | `onlineRejectsQueuedMoveAfterNewGame` |
| `test/game/P1RegressionTest.java` | `pvpUndoAfterDrawRestoresLastPlayerTurn` |
| `test/game/P1RegressionTest.java` | `pvpUndoAfterWinRestoresWinningPlayerTurn` |
| `test/game/P2LifecycleTest.java` | `cancellationInsideRecursiveSearchUnwindsAllMoves` |
| `test/game/P2LifecycleTest.java` | `cancellingOnlineSelectionRestoresModeWithoutResettingGame` |
| `test/game/P2LifecycleTest.java` | `disposedFrameDoesNotReceiveLanguageEvents` |
| `test/game/P2LifecycleTest.java` | `disposingFrameCancelsItsWorkerAndPreservesBoard` |
| `test/game/P2LifecycleTest.java` | `interruptedAiAbortsWithoutMutatingBoard` |
| `test/game/P2LifecycleTest.java` | `networkErrorsAreTranslated` |
| `test/game/P2LifecycleTest.java` | `receivedMoveIsAppliedBeforeNextEdtNetworkCallback` |
| `test/game/PlayerPieceBadgeTest.java` | `badgePixelsUseBoardPaletteAndAntialiasedWhiteOutline` |
| `test/game/PlayerPieceBadgeTest.java` | `longNamesEllipsizeInsideBothCardsWithoutPushingBadgesOut` |
| `test/game/PlayerPieceBadgeTest.java` | `realCardsFollowModeSideAndLanguageWithoutChangingScoresOrTurnBorder` |
| `test/game/Task4LayoutTest.java` | `allThreeCardsFitAllLocalesOnStartupAndAfterTransitions` |
| `test/game/Task4LifecycleTest.java` | `menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame` |
| `test/game/Task4LifecycleTest.java` | `menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult` |
| `test/game/Task4LifecycleTest.java` | `menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning` |
| `test/game/Task4LifecycleTest.java` | `menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged` |
| `test/game/Task4LifecycleTest.java` | `queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart` |
| `test/game/Task4LifecycleTest.java` | `queuedOpponentMoveCannotChangeBoardAfterMenu` |
| `test/game/Task4LifecycleTest.java` | `undoRemainsAvailableAfterWin` |
| `test/game/Task4PreferencesTest.java` | `profileAndLanguageRoundTripThroughInMemoryNode` |
| `test/game/Task4StartScreenTest.java` | `emptyProfileUsesTranslatedDefaultWithoutPersistingIt` |
| `test/game/Task4StartScreenTest.java` | `normalizeUnicodeWhitespaceAndLongNames` |
| `test/game/Task4StartScreenTest.java` | `profileChangesAndMenuKeepExistingSessionTotals` |
| `test/game/Task4StartScreenTest.java` | `profileFieldLimitsUnicodeAndNamesRemainPlainText` |
| `test/game/Task4StartScreenTest.java` | `settingsApplyLanguageImmediatelyWithoutChangingProfileDraft` |
| `test/game/Task4StartScreenTest.java` | `titleMenuSetupAndGameUseTheSameFrame` |
| `test/game/Task4bFlowTest.java` | `allCardsAndDialogsFitBothLooksWithStableFrameSize` |
| `test/game/Task4bFlowTest.java` | `enterCommitsProfileAndEscapeCancelsEveryDialogDraft` |
| `test/game/Task4bFlowTest.java` | `rulesHaveAllSixSuppliedLinesAndOpenFromBothMenuAndGame` |
| `test/game/Task4bFlowTest.java` | `setupRemembersDifficultyAndPlayerTwoOnlyAfterStart` |
| `test/game/Task4bFlowTest.java` | `titleAcceptsMouseAndAnyKeyAndStopsItsTimer` |
| `test/game/Task4bFoundationTest.java` | `buttonsPaintFourDistinctStatesUnderBothLooks` |
| `test/game/Task4bFoundationTest.java` | `missingImagesRenderAndCoverCacheTracksDevicePixels` |
| `test/game/Task4bGameplayTest.java` | `boardIsCentredAndMouseUsesUnchangedCoordinates` |
| `test/game/Task4bGameplayTest.java` | `computerAndSetupUseTranslatedDifficultyInsteadOfEnumLabel` |
| `test/game/Task4bGameplayTest.java` | `realGameResultsUpdateBothCardsAndDrawCount` |
| `test/game/Task4bGameplayTest.java` | `resetScoreConfirmationClearsNumbersWithoutChangingCurrentGame` |
| `test/game/Task4bGameplayTest.java` | `scoresResetOnlyOnModeOrDifficultyChangeAndKeepOnProfileAndMenu` |
| `test/game/Task4bPresentationTest.java` | `buttonsRenderFourStatesAndActivateWithEnterAndSpace` |
| `test/game/Task4bPresentationTest.java` | `profileClearCounterAndFocusSelectAllUseActualEditor` |
| `test/game/Task4bPresentationTest.java` | `renderAllSixViewsAndMeasureDisplayedFrame` |
| `test/game/Task4bPresentationTest.java` | `translatedButtonAndLabelTextFitsIncludingTransitions` |
| `test/game/TwoPlayerSetupDialogTest.java` | `avatarLocksFollowBothChoicesAndLockedIconsAreDimmed` |
| `test/game/TwoPlayerSetupDialogTest.java` | `bothPlayersHaveSymmetricHeadersAndNameLabels` |
| `test/game/TwoPlayerSetupDialogTest.java` | `duplicateWarningUpdatesImmediatelyAndBlocksButtonAndEnter` |
| `test/game/TwoPlayerSetupDialogTest.java` | `startCommitsBothDraftsAndCancelDiscardsChanges` |
| `test/game/UILangSoundTest.java` | `b2_settingsSoundToggle` |
| `test/game/UILangSoundTest.java` | `b3_settingsSoundTooltipByLang` |
| `test/game/UILangSoundTest.java` | `c1_langAllKeysNotNullNotEmpty` |
| `test/game/UILangSoundTest.java` | `c1b_connectingMsgContainsIp` |
| `test/game/UILangSoundTest.java` | `c2_sharedButtonPaintAndContrast` |
| `test/game/UILangSoundTest.java` | `c3_menuAndSettingsUpdateImmediately` |
| `test/game/UILangSoundTest.java` | `d1_appSettingsRoundTrip` |
| `test/game/UILangSoundTest.java` | `d2_inMemoryNodeIsDifferentFromProduction` |
| `test/game/UILangSoundTest.java` | `i2_speakerIconVisual` |

### D.2. GUI kèm socket thật — 6 phương thức

| Tệp | Phương thức không được chọn |
|---|---|
| `test/game/F1ProtocolTest.java` | `queuedWireMoveKeepsOldRoundAfterReset` |
| `test/game/P2LifecycleTest.java` | `closingDialogCancelsPendingHostAndIgnoresLateResults` |
| `test/game/P2LifecycleTest.java` | `successfulDialogDisposalKeepsConnection` |
| `test/game/Task4LifecycleTest.java` | `menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart` |
| `test/game/Task4LifecycleTest.java` | `menuClosesRealLanSocketAndAllConnectionDialogs` |
| `test/game/Task4bFlowTest.java` | `lanMenuJoinUsesRealSocketAndStartsTheExistingOnlineGame` |

### D.3. Socket thật — 12 phương thức

| Tệp | Phương thức không được chọn |
|---|---|
| `test/game/F1ProtocolTest.java` | `clientSendsOneRequestAcceptsOnlyMatchingBaseAndClearsWaitOnEof` |
| `test/game/F1ProtocolTest.java` | `garbageMissingFieldsOversizedAndWrongColorDoNotDisconnect` |
| `test/game/F1ProtocolTest.java` | `mismatchedVersionClosesSession` |
| `test/game/F1ProtocolTest.java` | `simultaneousSameBaseRestartAndStaleRequestResetOnce` |
| `test/game/F1ProtocolTest.java` | `startTimeoutClosesSession` |
| `test/game/NetworkStabilityTest.java` | `callbacksAlreadyQueuedAreDiscardedAfterCancel` |
| `test/game/NetworkStabilityTest.java` | `cancelPendingHostReleasesPortWithoutError` |
| `test/game/NetworkStabilityTest.java` | `hostAndClientExchangeMovesRestartAndDisconnect` |
| `test/game/NetworkStabilityTest.java` | `malformedMoveDoesNotKillConnectionAndOrderIsPreserved` |
| `test/game/NetworkStabilityTest.java` | `occupiedPortReportsTypedHostError` |
| `test/game/NetworkStabilityTest.java` | `reconnectCannotBeClosedOrNotifiedByPreviousSession` |
| `test/game/NetworkStabilityTest.java` | `staleErrorIsDiscardedWhenNewSessionStarts` |

### D.4. Ngoài phạm vi — 22 phương thức

| Tệp | Phương thức không được chọn |
|---|---|
| `test/game/I18nSourceScanTest.java` | `i1a_noIsVICall` |
| `test/game/I18nSourceScanTest.java` | `i1b_noVietnameseInStringLiterals` |
| `test/game/LangPurityTest.java` | `a_ptNoVietnameseChars` |
| `test/game/LangPurityTest.java` | `b_enAsciiOnly` |
| `test/game/PlayerPieceBadgeTest.java` | `pureMappingAssignsBlackToXAndWhiteToO` |
| `test/game/SoundResourceTest.java` | `closesClipAfterPlaybackStops` |
| `test/game/SoundResourceTest.java` | `closesClipWhenOpenFails` |
| `test/game/SoundResourceTest.java` | `closesClipWhenStartFails` |
| `test/game/SoundResourceTest.java` | `queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice` |
| `test/game/SoundResourceTest.java` | `staleResultClosesClipWithoutStarting` |
| `test/game/Task4bFoundationTest.java` | `invalidAvatarsDefaultToZero` |
| `test/game/Task4bFoundationTest.java` | `profileNames` |
| `test/game/Task4bFoundationTest.java` | `profileRoundTripKeepsLegacyKeyAndImplicitDefault` |
| `test/game/Task4bPresentationTest.java` | `runtimeResourcePackagingAndImageLoadingWorkOnBinClasspath` |
| `test/game/Task4bPresentationTest.java` | `suppliedRulesKeysAreVerbatimAndPortugueseIsPure` |
| `test/game/TwoPlayerSetupValidationTest.java` | `comparesEffectiveNamesAfterTrimIgnoringCase` |
| `test/game/TwoPlayerSetupValidationTest.java` | `defaultsAreExplicitAndLocaleIndependent` |
| `test/game/TwoPlayerSetupValidationTest.java` | `duplicateAvatarsCannotStartEvenWithDistinctNames` |
| `test/game/TwoPlayerSetupValidationTest.java` | `keepsAnAvailableSecondAvatar` |
| `test/game/TwoPlayerSetupValidationTest.java` | `locksTheOtherPlayersAvatarInBothDirections` |
| `test/game/TwoPlayerSetupValidationTest.java` | `resolvesBlankAndTrimmedNames` |
| `test/game/TwoPlayerSetupValidationTest.java` | `resolvesOnlyTheInitialSecondAvatarConflict` |

Các main/probe không phải JUnit (ví dụ BoardDisplayProbe.main, RuleAndDrawTest.main, ConsoleTest.main, AIBenchmarkTest.main, FullGomokuTestSuite.main theo INVENTORY) không nằm trong 243/118/125 và không được tính PASS/FAIL. Không suy từ số phương thức bị loại sang số lượt tham số hóa chưa chạy.

## E. Fixture BoardPanel: bằng chứng headless và phạm vi thật

**Có: cả 17 ca đã chạy thành công với chế độ headless, không cần cửa sổ thật.** Bằng chứng độc lập:

1. Lệnh Java thực tế trong [log dòng 31](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log:31>) có `"-Djava.awt.headless=true"`, `"--select-class" "game.UndoScoreTest"` và `"--select-class" "game.UndoParityTest"`.
2. [XML dòng 6](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-after-fix-score-undo/xml/TEST-junit-jupiter.xml:6>) ghi `<property name="java.awt.headless" value="true"/>`.
3. [Cây kết quả UndoScoreTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log:594>) và [UndoParityTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/docs/junit-run-after-fix-score-undo.log:606>) đều có dấu thành công; đếm XML thấy tương ứng 11 và 6 testcase PASS, không skipped/failure/error.

Đối tượng được kiểm là **game.view.BoardPanel thật, kế thừa JPanel**, không mock hoặc dựng lại UI: [khai báo lớp](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/src/game/view/BoardPanel.java:26>). Mỗi setUp tạo một `new Board()`, đặt RULE_B, truyền vào `new BoardPanel(board)` và tạo `new ScoreTracker()` riêng. UndoParityTest còn đặt mode PV_AI; UndoScoreTest đặt mode theo từng ca. [setUp UndoScoreTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/UndoScoreTest.java:36>), [setUp UndoParityTest](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/game/UndoParityTest.java:36>).

Fixture cài anonymous GameStateListener trực tiếp: onGameOver gọi tracker.recordResult; onGameReopened gọi tracker.revokeResult; callback khác rỗng. Không tạo MainFrame, JFrame, JDialog, không gọi setVisible. Tên “giống MainFrame” trong comment chỉ mô tả mô phỏng cách nối, không phải bằng chứng gọi MainFrame.

Các test gọi API BoardPanel trực tiếp như executeMoveForTest, playMove, undoMove, resetGame, canUndo, applyOpponentMove rồi kiểm Board/lịch sử/lượt/gameId/cờ kết thúc và ScoreTracker. executeMoveForTest đưa quân chỉ định vào đường xử lý nước đi mà không kích hoạt AI thật; các tên người/AI trong kịch bản không chứng minh AI tự chọn nước. Hai ca chuyển ONLINE/chặn MOVE kiểm trạng thái bằng lời gọi trong bộ nhớ, không mở kết nối LAN thật.

Giới hạn cụ thể từ mã: U07 tự record hòa vào tracker nhưng panel chưa kết thúc; Undo ở ca đó không phải Undo từ trạng thái hòa thật. U08 chỉ resetGame và kiểm gameId/isAiThinking; không chứng minh đua luồng SwingWorker thực tế dù tên/DisplayName cũ rộng hơn. Không suy phạm vi chỉ từ tên test.

**Không có MainFrame thật trong nhóm fixture đã chạy.** Vì vậy kết quả này chưa kiểm tự động callback MainFrame, refresh hai thẻ người chơi/nhãn hòa, hộp kết quả, thao tác nút, bố cục/render hoặc hành vi người dùng trên màn hình.

## F. CHƯA XÁC NHẬN

- **CHƯA XÁC NHẬN** bằng kiểm thử tự động: luồng MainFrame thật và hiển thị tỉ số sau A thắng → Undo → B thắng, kể cả việc hai thẻ/nhãn hòa cập nhật. Cần kiểm tay; các fixture mô phỏng listener không thay thế việc này.
- **CHƯA XÁC NHẬN** kết quả của 125 phương thức không được chọn, cũng như số lượt tham số hóa mà chúng sẽ sinh ra. Chúng không được tính là PASS, FAIL hay SKIPPED.
- **CHƯA XÁC NHẬN** khả năng chạy headless của từng ca bị loại nếu tách riêng hook; báo cáo chỉ kế thừa phân loại gốc và xác nhận chúng không xuất hiện trong XML cuối.
- Log cuối không in tên nhánh hoặc mã commit. **CHƯA XÁC NHẬN từ riêng log/XML** rằng tiến trình đã chạy trên task4-start-screen sau merge. Git hiện tại xác nhận nhánh ở đúng a21ce2d và log/XML là bằng chứng được lưu trong commit đó; không có lượt chạy mới sau merge trong công việc này.
- Không xác nhận GUI/LAN thật hoặc phần mềm được kiểm toàn diện chỉ vì biên dịch toàn bộ nguồn. Phạm vi được chọn để thực thi nằm trong lệnh log và các testcase XML; phân nhóm lớp chính không phải số đo bao phủ mã.

## G. Câu mô tả phạm vi dùng trong báo cáo

> Bộ kiểm thử chạy headless đạt 190/190 lượt: 173 lượt thuần logic cho Board, ScoreTracker, MinimaxAI, HeuristicEvaluator và thông điệp NetworkManager trong bộ nhớ, cùng 17 lượt luồng Undo/tỉ số trên BoardPanel thật qua listener fixture; không mở cửa sổ hay socket thật, chưa kiểm luồng MainFrame và hiển thị tỉ số thực tế.

173 = 60 + 26 + 33 + 32 + 22; 173 + 17 = 190. Có dùng thành phần Swing BoardPanel, nên không dùng câu “không dùng GUI” để mô tả toàn bộ bộ test.
