# Task 4b — đối chiếu test cũ (D1)

Mốc so sánh: `f61e2d5`. Không xóa lớp JUnit nào, không thêm `@Disabled`.
Các test còn giữ tên vẫn giữ hành vi hồi quy; chỉ thay điểm truy cập giao diện/fixture khi cần.
Số ca của mỗi lớp cũ giữ nguyên. Parameterized invocation được đếm thành từng ca trong output JUnit.

| Test cũ | Lý do thay đổi | Test thay thế / bằng chứng hiện tại |
|---|---|---|
| `UILangSoundTest.c1_langAllKeysNotNullNotEmpty` | Fixture Preferences chuyển sang RAM; giữ reflection VI/EN/PT | Cùng tên; quét đủ các hàm Lang hiện tại |
| `UILangSoundTest.c1b_connectingMsgContainsIp` | Fixture RAM, giữ kiểm tra IP | Cùng tên |
| `UILangSoundTest.c2_comboRendererContrast` | Hai combobox game đã bị loại bỏ theo C5 | `UILangSoundTest.c2_sharedButtonPaintAndContrast`; thêm `Task4bPresentationTest.buttonsRenderFourStatesAndActivateWithEnterAndSpace` |
| `UILangSoundTest.b2_soundButtonToggle` | Nút âm thanh chuyển vào Cài đặt | `UILangSoundTest.b2_settingsSoundToggle` |
| `UILangSoundTest.b3_soundTooltipByLang` | Tooltip được đọc từ nút trong Cài đặt | `UILangSoundTest.b3_settingsSoundTooltipByLang` |
| `UILangSoundTest.c3_mainFrameLabelsUpdate` | Nhãn chế độ/selector cũ bị loại | `UILangSoundTest.c3_menuAndSettingsUpdateImmediately` |
| `UILangSoundTest.d1_appSettingsRoundTrip` | Thay node registry riêng bằng Preferences RAM; giữ fallback/invalid/lang/sound | Cùng tên |
| `UILangSoundTest.d2_testNodeDifferentFromProduction` | Không tạo AppSettings production chỉ để lấy đường dẫn | `UILangSoundTest.d2_inMemoryNodeIsDifferentFromProduction`; đối chiếu node thật chỉ đọc trong `PreferencesIsolation` |
| `UILangSoundTest.i2_speakerIconVisual` | Fixture RAM và nền Theme; giữ kiểm pixel dấu X đỏ và bật/tắt | Cùng tên |
| `LangPurityTest.a_ptNoVietnameseChars` | Fixture RAM; giữ phép quét PT | Cùng tên; bổ sung kiểm rules/độ khó có tham số ở các lớp Task4b |
| `LangPurityTest.b_enAsciiOnly` | Fixture RAM; giữ phép quét các hàm không tham số | Cùng tên; nội dung luật EN có dấu nhân × được kiểm bằng fixture nguyên văn riêng |
| `LangPurityTest.c_comboContrastRatio` | Combobox game và màu tím cũ bị loại | `LangPurityTest.c_sharedThemeContrast`; kiểm màu nâu/kem/vàng thực tế |
| `Task4StartScreenTest.startAndMenuSwitchCardsInTheSameFrame` | Hai card cũ trở thành TITLE/MENU/GAME và thiết lập modal | `Task4StartScreenTest.titleMenuSetupAndGameUseTheSameFrame` |
| `Task4StartScreenTest.emptyNameUsesTranslatedDefaultAndRemainsImplicit` | Tên nhập trong hồ sơ; mặc định giờ là Player 1 | `Task4StartScreenTest.emptyProfileUsesTranslatedDefaultWithoutPersistingIt` (3 ngôn ngữ) |
| `Task4StartScreenTest.normalizeUnicodeWhitespaceAndLongNames` | Chỉ định dạng lại; giữ 10 bộ dữ liệu và cả hai assertion | Cùng tên; không giảm kiểm tra |
| `Task4StartScreenTest.fieldLimitsUnicodeWithoutSplittingEmojiAndDisplaysNamesAsPlainText` | Ô nhập chuyển sang ProfileDialog | `Task4StartScreenTest.profileFieldLimitsUnicodeAndNamesRemainPlainText` |
| `Task4StartScreenTest.languageChoiceOnMenuPreservesTypedNameAndSynchronizesBothSelectors` | Không còn hai selector; chỉ Cài đặt chọn ngôn ngữ | `Task4StartScreenTest.settingsApplyLanguageImmediatelyWithoutChangingProfileDraft` |
| `Task4StartScreenTest.achievementIconShowsExistingSessionTotalsAndMenuKeepsThem` | Nút cúp bị loại theo yêu cầu; giữ kiểm tỉ số | `Task4StartScreenTest.profileChangesAndMenuKeepExistingSessionTotals`; thêm `Task4bGameplayTest.realGameResultsUpdateBothCardsAndDrawCount` |
| `Task4PreferencesTest.nameAndLanguageRoundTripThroughInjectedNode` | Lưu thêm avatar; không ghi node test vào registry | `Task4PreferencesTest.profileAndLanguageRoundTripThroughInMemoryNode` (3 ngôn ngữ) |
| `Task4LayoutTest.bothCardsFitAllLocalesOnStartupAndAfterTransitions` | Kiểm cả ba card, hồ sơ thay ô nhập StartScreen, ngôn ngữ qua trạng thái | `Task4LayoutTest.allThreeCardsFitAllLocalesOnStartupAndAfterTransitions` (3×2); thêm `Task4bFlowTest.allCardsAndDialogsFitBothLooksWithStableFrameSize` |
| `F4LayoutTest.directStartupBoundsAndRender` | Helper render đọc nhãn thẻ trái; ảnh không ghi đè tư liệu Task 4 | Cùng tên; thêm `Task4bPresentationTest.renderAllSixViewsAndMeasureDisplayedFrame` |
| `F4LayoutTest.allTransitionsKeepControlsInsideParents` | Cùng thay đổi helper; giữ VI→PT và VI→EN→PT | Cùng tên; thêm `Task4bPresentationTest.translatedButtonAndLabelTextFitsIncludingTransitions` |
| `P1RegressionTest.languageChangePreservesActiveGame` | Nhãn chế độ cũ bị loại; trạng thái PvP nay chứa tên đang chơi | Cùng tên; kiểm nhãn menu và `Lang.namedTurn`, giữ board/gameId/lượt/worker/score |
| `P1RegressionTest.languageChangePreservesFinishedGameAndTranslatesResult` | Helper lấy trạng thái từ nguồn mới thay JLabel cũ | Cùng tên; giữ ba kết quả và board/gameId/history |
| `P2LifecycleTest.disposedFrameDoesNotReceiveLanguageEvents` | Theo dõi nút PvAI trên menu thay nhãn mode cũ | Cùng tên; vẫn kiểm frame đã dispose không nhận Lang event |
| `P2LifecycleTest.cancellingOnlineSelectionRestoresModeWithoutResettingGame` | Không dùng modeCombo; gọi điểm mở NetworkDialog và đọc selectedMode | Cùng tên; giữ gameId/history/Undo, chế độ PvP sau hủy |
| `Task4LifecycleTest.menuCancelsActualAiWorkerAndItsCompletionCannotChangeNextGame` | Chọn HARD qua trạng thái thay diffCombo | Cùng tên; vẫn hủy worker thật và gọi lại completion thật |
| `Task4LifecycleTest.menuClosesRealLanSocketAndAllConnectionDialogs` | Về menu nay giữ ONLINE; muốn PvAI phải chọn qua SetupDialog | Cùng tên; thêm assertion giữ ONLINE, rồi xác nhận thiết lập PvAI; vẫn kiểm EOF/socket/dialog/gameId |
| `Task4LifecycleTest.menuClosesPendingNetworkDialogAndCannotRestoreOldModeAfterNewStart` | Không modeCombo; về menu giữ PvP, không tự về PvAI | Cùng tên; vẫn kiểm session cũ đóng và không phục hồi state sau modal |
| `Task4LifecycleTest.menuClosesResultPopupPreservesEarnedScoreAndRejectsLateResult` | Nút cúp bị loại; dùng RulesDialog làm cửa sổ phụ đang mở | Cùng tên; vẫn kiểm đóng mọi owned dialog, giữ điểm và loại kết quả cũ |
| `Task4LifecycleTest.undoRemainsAvailableAfterWin` | Bắt đầu PvP qua nguồn trạng thái thay modeCombo | Cùng tên; vẫn bấm Undo sản phẩm sau thắng, còn 8 nước và điểm không đổi |

Các ca vòng đời còn lại không đổi nội dung assertion: `queuedNetworkUiCallbackCannotRunEvenAfterMenuAndNewStart`, `menuRejectsBoardClicksAlreadyQueuedBeforeTheCardChanged`, `menuInterruptsAWorkerWhoseBackgroundBodyIsAlreadyRunning`, `queuedOpponentMoveCannotChangeBoardAfterMenu`.

`PreferencesIsolation` là helper, không phải ca JUnit: các node dùng để ghi là RAM và được removeNode; snapshot node thật `/game/view` chỉ đọc, chỉ mở khi node đã tồn tại. `MemoryPreferences.removeNodeSpi` cũng xóa child khỏi bộ nhớ của parent, tránh trả lại node đã bị xóa.

Ca mới theo giai đoạn: A thêm `Task4bFoundationTest` 15 ca; B thêm `Task4bFlowTest` 12 ca; C thêm `Task4bGameplayTest` 12 ca; D thêm 1 ca LAN từ menu trong Flow và `Task4bPresentationTest` 16 ca. Tổng mới 56; nền 211 thành 267. Không có PENDING trong JUnit.
