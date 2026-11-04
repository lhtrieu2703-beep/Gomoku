# Từng ca đã chạy — lấy từ XML thực tế

158 lượt JUnit, gồm 93 phương thức (75 cũ + 18 mới). Mỗi lượt tham số hóa có một hàng riêng. Tất cả là **hành vi đơn lẻ**: dựng fixture/thế cờ rồi kiểm một hành vi; **0 kịch bản replay ván cờ theo lượt**. Việc gọi makeMove để dựng fixture không đồng nghĩa kiểm diễn biến một ván chơi. Không có Swing/GUI/socket thật trong các ca được chọn hoặc hook của chúng.

| # | Test / lượt tham số | Hiển thị tham số | Lớp chính được kiểm | Kết quả | Loại |
|---|---|---|---|---|---|
| 1 | `game.BoardWinTest#testSanitySetupPatternAllO()` | Sanity 2: Kiểm tra setupPattern('. O O O O O* .') chỉ có quân O, không có X | game.model.Board | PASS | Hành vi đơn lẻ |
| 2 | `game.BoardWinTest#testSanitySetupPatternInverted()` | Sanity 3: Kiểm tra invertColors=true đảo màu đúng cả 2 nhánh X và O | game.model.Board | PASS | Hành vi đơn lẻ |
| 3 | `game.BoardWinTest#testSanitySetupPatternNormal()` | Sanity 1: Kiểm tra setupPattern('O X X X X* X O') đặt đúng quân ở từng ô | game.model.Board | PASS | Hành vi đơn lẻ |
| 4 | `game.BoardWinTest#testT01()` | T01: . X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 5 | `game.BoardWinTest#testT02()` | T02: O X X X X* X . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 6 | `game.BoardWinTest#testT03()` | T03: . X X* X X X O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 7 | `game.BoardWinTest#testT04()` | T04: O X* X X X X O -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 8 | `game.BoardWinTest#testT05()` | T05: # X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 9 | `game.BoardWinTest#testT06()` | T06: # X X X X X* O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 10 | `game.BoardWinTest#testT07()` | T07: O X X X X X* # -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 11 | `game.BoardWinTest#testT08()` | T08: # X X X X X* # (chéo 5 ô góc chạm 2 mép) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 12 | `game.BoardWinTest#testT09()` | T09: O . X X X X X* . O (O không liền kề) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 13 | `game.BoardWinTest#testT10()` | T10: O X X X X X* . O (chỉ xét ô liền kề) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 14 | `game.BoardWinTest#testT11()` | T11: . X X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 15 | `game.BoardWinTest#testT12()` | T12: O X X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 16 | `game.BoardWinTest#testT13()` | T13: O X X X X X X* O -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 17 | `game.BoardWinTest#testT14()` | T14: # X X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 18 | `game.BoardWinTest#testT15()` | T15: # X X X X X X* O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 19 | `game.BoardWinTest#testT16()` | T16: # X X X X X X* # (chéo 6 ô góc) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 20 | `game.BoardWinTest#testT17()` | T17: . X X X X X X X* . -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 21 | `game.BoardWinTest#testT18()` | T18: O X X X X X X X* O -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 22 | `game.BoardWinTest#testT19()` | T19: Hàng 9 quân mở 2 đầu -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 23 | `game.BoardWinTest#testT20()` | T20: Cả hàng 15 ô đều là X, 2 đầu là mép -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 24 | `game.BoardWinTest#testT21()` | T21: . X X X* X X . (nối 2+2 thành 5) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 25 | `game.BoardWinTest#testT22()` | T22: . X X X X* X X X . (nối 3+3 thành 7) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 26 | `game.BoardWinTest#testT23()` | T23: . X X X* X X X . (nối 3+2 thành 6) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 27 | `game.BoardWinTest#testT24()` | T24: O X X X* X X O (nối thành 5, O chặn 2 đầu) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 28 | `game.BoardWinTest#testT25()` | T25: . X X X X* . (chỉ 4 quân) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 29 | `game.BoardWinTest#testT26()` | T26: . X X . X* X X . (có ô trống ở giữa) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 30 | `game.BoardWinTest#testT27()` | T27: . X X O X* X X . (bị O ngắt giữa) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 31 | `game.BoardWinTest#testT28()` | T28: . X X X* . X X . (còn ô trống) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 32 | `game.BoardWinTest#testT29()` | T29: . X X X O X* X . -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 33 | `game.BoardWinTest#testT30()` | T30: Hàng 5 bị chặn có sẵn, đặt chỗ khác không tạo hàng mới -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 34 | `game.BoardWinTest#testT31()` | T31: Ngang 5 chặn 2 đầu + Dọc 5 mở -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 35 | `game.BoardWinTest#testT32()` | T32: Ngang 5 chặn 2 đầu + Chéo 5 chặn 2 đầu -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 36 | `game.BoardWinTest#testT33()` | T33: Ngang 5 hợp lệ + Dọc 5 hợp lệ -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 37 | `game.BoardWinTest#testT34()` | T34: Ngang 6 hợp lệ + Dọc 5 hợp lệ -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 38 | `game.BoardWinTest#testT35()` | T35: Ngang 6 + Dọc 6 (đều mở) -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 39 | `game.BoardWinTest#testT36()` | T36: Ngang 6 mở + Dọc 5 chặn 2 đầu -> THẮNG (nhờ hàng ngang) | game.model.Board | PASS | Hành vi đơn lẻ |
| 40 | `game.BoardWinTest#testT37()` | T37: Ngang 7 chặn 2 đầu + Dọc 5 chặn 2 đầu -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 41 | `game.BoardWinTest#testT38()` | T38: Ngang 4 + Dọc 4 + Chéo 4 (đều mở) -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 42 | `game.BoardWinTest#testT39()` | T39: Hàng dọc 5, đặt ở giữa bàn, 2 đầu trống -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 43 | `game.BoardWinTest#testT40()` | T40: Chéo chính (↘) 5 quân, 2 đầu trống -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 44 | `game.BoardWinTest#testT41()` | T41: Chéo phụ (↙) 5 quân, 2 đầu trống -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 45 | `game.BoardWinTest#testT42()` | T42: Ngang 5 quân ở cột 0-4, quân vừa đặt ở cột 0, cột 5 trống -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 46 | `game.BoardWinTest#testT43()` | T43: Ngang 5 quân ở cột 10-14, quân vừa đặt ở cột 14, cột 9 là O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 47 | `game.BoardWinTest#testT44()` | T44: Quân vừa đặt ở góc (0,0), chéo (0,0)-(4,4) là X, (5,5) là O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 48 | `game.BoardWinTest#testT45()` | T45: Quân vừa đặt ở góc (14,14), chéo (10,10)-(14,14) là X, (9,9) trống -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 49 | `game.BoardWinTest#testT46()` | T46: Chéo từ (0,0) đến (5,5) là X (6 quân), (6,6) là O -> THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 50 | `game.BoardWinTest#testT47()` | T47: . O O O O O* . -> O THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 51 | `game.BoardWinTest#testT48()` | T48: X O O O O O* X -> KHÔNG THẮNG | game.model.Board | PASS | Hành vi đơn lẻ |
| 52 | `game.BoardWinTest#testT49()` | T49: Bàn cờ đã đầy, nước cuối không tạo hàng hợp lệ -> HÒA | game.model.Board | PASS | Hành vi đơn lẻ |
| 53 | `game.BoardWinTest#testT50()` | T50: Bàn cờ đã đầy, nước cuối tạo hàng hợp lệ -> THẮNG (thắng ưu tiên hơn hòa) | game.model.Board | PASS | Hành vi đơn lẻ |
| 54 | `game.BoardWinTest#testT51()` | T51: Đặt vào ô đã có quân -> Từ chối, bàn cờ và lượt giữ nguyên | game.model.Board | PASS | Hành vi đơn lẻ |
| 55 | `game.BoardWinTest#testT52()` | T52: Đặt ngoài bàn cờ -> Từ chối, không văng lỗi ngoại lệ | game.model.Board | PASS | Hành vi đơn lẻ |
| 56 | `game.F3HeuristicTest#actualCrossDoubleThreeIsRecognized(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 57 | `game.F3HeuristicTest#actualCrossDoubleThreeIsRecognized(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 58 | `game.F3HeuristicTest#actualCrossDoubleThreeIsRecognized(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 59 | `game.F3HeuristicTest#actualCrossDoubleThreeIsRecognized(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 60 | `game.F3HeuristicTest#blockedSixIsNotFive(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 61 | `game.F3HeuristicTest#blockedSixIsNotFive(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 62 | `game.F3HeuristicTest#blockedSixIsNotFive(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 63 | `game.F3HeuristicTest#blockedSixIsNotFive(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 64 | `game.F3HeuristicTest#edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 65 | `game.F3HeuristicTest#edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 66 | `game.F3HeuristicTest#edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 67 | `game.F3HeuristicTest#edgeFiveMatchesBoardAndDominatesOtherThreats(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 68 | `game.F3HeuristicTest#nonTerminalThreatsNeverOutrankWin(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 69 | `game.F3HeuristicTest#nonTerminalThreatsNeverOutrankWin(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 70 | `game.F3HeuristicTest#nonTerminalThreatsNeverOutrankWin(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 71 | `game.F3HeuristicTest#nonTerminalThreatsNeverOutrankWin(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 72 | `game.F3HeuristicTest#openFourIsThreatButNotTerminal(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 73 | `game.F3HeuristicTest#openFourIsThreatButNotTerminal(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 74 | `game.F3HeuristicTest#openFourIsThreatButNotTerminal(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 75 | `game.F3HeuristicTest#openFourIsThreatButNotTerminal(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 76 | `game.F3HeuristicTest#openSixMatchesSelectedRule(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 77 | `game.F3HeuristicTest#openSixMatchesSelectedRule(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 78 | `game.F3HeuristicTest#openSixMatchesSelectedRule(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 79 | `game.F3HeuristicTest#openSixMatchesSelectedRule(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 80 | `game.F3HeuristicTest#singleThreeIsNotDoubleThree(WinRule, Piece)[1]` | [1] RULE_A, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 81 | `game.F3HeuristicTest#singleThreeIsNotDoubleThree(WinRule, Piece)[2]` | [2] RULE_A, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 82 | `game.F3HeuristicTest#singleThreeIsNotDoubleThree(WinRule, Piece)[3]` | [3] RULE_B, X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 83 | `game.F3HeuristicTest#singleThreeIsNotDoubleThree(WinRule, Piece)[4]` | [4] RULE_B, O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 84 | `game.Task4bGameplayTest#resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain()` | resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain() | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 85 | `game.ai.AITacticsTest#blockedFiveIsNotAWinAndRealThreatRemains(Difficulty)[1]` | [1] Dễ | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 86 | `game.ai.AITacticsTest#blockedFiveIsNotAWinAndRealThreatRemains(Difficulty)[2]` | [2] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 87 | `game.ai.AITacticsTest#blockedFiveIsNotAWinAndRealThreatRemains(Difficulty)[3]` | [3] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 88 | `game.ai.AITacticsTest#blocksImmediateLossWithOneOpenEnd(Difficulty)[1]` | [1] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 89 | `game.ai.AITacticsTest#blocksImmediateLossWithOneOpenEnd(Difficulty)[2]` | [2] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 90 | `game.ai.AITacticsTest#blocksOpponentOpenThree(Difficulty)[1]` | [1] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 91 | `game.ai.AITacticsTest#blocksOpponentOpenThree(Difficulty)[2]` | [2] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 92 | `game.ai.AITacticsTest#completedSearchPreservesEveryCellCountAndRule(Difficulty)[1]` | [1] Dễ | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 93 | `game.ai.AITacticsTest#completedSearchPreservesEveryCellCountAndRule(Difficulty)[2]` | [2] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 94 | `game.ai.AITacticsTest#completedSearchPreservesEveryCellCountAndRule(Difficulty)[3]` | [3] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 95 | `game.ai.AITacticsTest#createsOpenFourWhenSafe(Difficulty)[1]` | [1] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 96 | `game.ai.AITacticsTest#createsOpenFourWhenSafe(Difficulty)[2]` | [2] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 97 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[1]` | [1] 1, X | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 98 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[2]` | [2] 1, O | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 99 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[3]` | [3] 2, X | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 100 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[4]` | [4] 2, O | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 101 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[5]` | [5] 5, X | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 102 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[6]` | [6] 5, O | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 103 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[7]` | [7] 10, X | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 104 | `game.ai.AITacticsTest#easyAlwaysReturnsLegalMoveOnNearlyFullBoard(int, Piece)[8]` | [8] 10, O | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 105 | `game.ai.AITacticsTest#emptyBoardReturnsCentreWithoutMutation(Difficulty)[1]` | [1] Dễ | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 106 | `game.ai.AITacticsTest#emptyBoardReturnsCentreWithoutMutation(Difficulty)[2]` | [2] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 107 | `game.ai.AITacticsTest#emptyBoardReturnsCentreWithoutMutation(Difficulty)[3]` | [3] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 108 | `game.ai.AITacticsTest#fullDrawBoardReturnsNullWithoutMutation(Difficulty)[1]` | [1] Dễ | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 109 | `game.ai.AITacticsTest#fullDrawBoardReturnsNullWithoutMutation(Difficulty)[2]` | [2] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 110 | `game.ai.AITacticsTest#fullDrawBoardReturnsNullWithoutMutation(Difficulty)[3]` | [3] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 111 | `game.ai.AITacticsTest#hardCreatesCrossDoubleThree()` | hardCreatesCrossDoubleThree() | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 112 | `game.ai.AITacticsTest#hardPreventsOpponentCrossDoubleThree()` | hardPreventsOpponentCrossDoubleThree() | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 113 | `game.ai.AITacticsTest#takesWinInsteadOfBlockingOpponentWin(Difficulty)[1]` | [1] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 114 | `game.ai.AITacticsTest#takesWinInsteadOfBlockingOpponentWin(Difficulty)[2]` | [2] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 115 | `game.ai.AITacticsTest#winsImmediatelyWithOneOpenEnd(Difficulty)[1]` | [1] Trung bình | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 116 | `game.ai.AITacticsTest#winsImmediatelyWithOneOpenEnd(Difficulty)[2]` | [2] Khó | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 117 | `game.ai.HeuristicOrderingTest#openFourOutranksOrdinaryPosition(Piece)[1]` | [1] X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 118 | `game.ai.HeuristicOrderingTest#openFourOutranksOrdinaryPosition(Piece)[2]` | [2] O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 119 | `game.ai.HeuristicOrderingTest#winningPositionOutranksOrdinaryPosition(Piece)[1]` | [1] X | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 120 | `game.ai.HeuristicOrderingTest#winningPositionOutranksOrdinaryPosition(Piece)[2]` | [2] O | game.ai.HeuristicEvaluator | PASS | Hành vi đơn lẻ |
| 121 | `game.ai.MinimaxEasyContractTest#easyChoosesMoveWithoutEvaluatingMinimaxNodes()` | easyChoosesMoveWithoutEvaluatingMinimaxNodes() | game.ai.MinimaxAI | PASS | Hành vi đơn lẻ |
| 122 | `game.model.BoardModelContractTest#outOfBoundsPlacementIsRejectedWithoutChangingBoard(int, int)[1]` | row=-1, col=7 | game.model.Board | PASS | Hành vi đơn lẻ |
| 123 | `game.model.BoardModelContractTest#outOfBoundsPlacementIsRejectedWithoutChangingBoard(int, int)[2]` | row=15, col=7 | game.model.Board | PASS | Hành vi đơn lẻ |
| 124 | `game.model.BoardModelContractTest#outOfBoundsPlacementIsRejectedWithoutChangingBoard(int, int)[3]` | row=7, col=-1 | game.model.Board | PASS | Hành vi đơn lẻ |
| 125 | `game.model.BoardModelContractTest#outOfBoundsPlacementIsRejectedWithoutChangingBoard(int, int)[4]` | row=7, col=15 | game.model.Board | PASS | Hành vi đơn lẻ |
| 126 | `game.model.BoardModelContractTest#undoRemovesOnlyRequestedStoneAndDecrementsMoveCount()` | undoRemovesOnlyRequestedStoneAndDecrementsMoveCount() | game.model.Board | PASS | Hành vi đơn lẻ |
| 127 | `game.model.ScoreTrackerContractTest#drawIsRecordedForEitherHumanPiece(Piece)[1]` | [1] X | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 128 | `game.model.ScoreTrackerContractTest#drawIsRecordedForEitherHumanPiece(Piece)[2]` | [2] O | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 129 | `game.model.ScoreTrackerContractTest#duplicateGameIdRejectsSecondResultAndPreservesFirst(Piece, Piece)[1]` | first=X, second=O | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 130 | `game.model.ScoreTrackerContractTest#duplicateGameIdRejectsSecondResultAndPreservesFirst(Piece, Piece)[2]` | first=O, second=X | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 131 | `game.model.ScoreTrackerContractTest#duplicateGameIdRejectsSecondResultAndPreservesFirst(Piece, Piece)[3]` | first=NONE, second=X | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 132 | `game.model.ScoreTrackerContractTest#humanWinIsRecordedForEitherHumanPiece(Piece)[1]` | [1] X | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 133 | `game.model.ScoreTrackerContractTest#humanWinIsRecordedForEitherHumanPiece(Piece)[2]` | [2] O | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 134 | `game.model.ScoreTrackerContractTest#opponentWinIsRecordedForEitherHumanPiece(Piece)[1]` | [1] X | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 135 | `game.model.ScoreTrackerContractTest#opponentWinIsRecordedForEitherHumanPiece(Piece)[2]` | [2] O | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 136 | `game.model.ScoreTrackerContractTest#resetScoresClearsAllThreeCounters()` | resetScoresClearsAllThreeCounters() | game.model.ScoreTracker | PASS | Hành vi đơn lẻ |
| 137 | `game.network.NetworkMessageTest#clientRestartRequestFormatsTheBaseRoundMessage()` | clientRestartRequestFormatsTheBaseRoundMessage() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 138 | `game.network.NetworkMessageTest#hostRestartFormatsBaseAndNewRoundMessage()` | hostRestartFormatsBaseAndNewRoundMessage() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 139 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[10]` | [10] MOVE not-a-uuid 1 7 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 140 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[11]` | [11] MOVE 00000000-0000-0000-0000-000000000001 2 7 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 141 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[12]` | [12] MOVE 00000000-0000-0000-0000-000000000001 1 7 7 extra | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 142 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[1]` | [1] MOVE | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 143 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[2]` | [2] MOVE 00000000-0000-0000-0000-000000000001 1 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 144 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[3]` | [3] MOVE 00000000-0000-0000-0000-000000000001 a 7 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 145 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[4]` | [4] MOVE 00000000-0000-0000-0000-000000000001 0 7 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 146 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[5]` | [5] MOVE 00000000-0000-0000-0000-000000000001 226 7 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 147 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[6]` | [6] MOVE 00000000-0000-0000-0000-000000000001 1 -1 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 148 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[7]` | [7] MOVE 00000000-0000-0000-0000-000000000001 1 15 7 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 149 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[8]` | [8] MOVE 00000000-0000-0000-0000-000000000001 1 7 -1 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 150 | `game.network.NetworkMessageTest#invalidMoveLineDoesNotDispatchAMove(String)[9]` | [9] MOVE 00000000-0000-0000-0000-000000000001 1 7 15 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 151 | `game.network.NetworkMessageTest#moveLineDispatchesPeerRoundPlyAndCoordinates(boolean, int, int, int)[1]` | host=false, ply=1, row=0, col=14 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 152 | `game.network.NetworkMessageTest#moveLineDispatchesPeerRoundPlyAndCoordinates(boolean, int, int, int)[2]` | host=true, ply=2, row=14, col=0 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 153 | `game.network.NetworkMessageTest#moveLineDispatchesPeerRoundPlyAndCoordinates(boolean, int, int, int)[3]` | host=false, ply=225, row=14, col=14 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 154 | `game.network.NetworkMessageTest#moveLineDispatchesPeerRoundPlyAndCoordinates(boolean, int, int, int)[4]` | host=true, ply=224, row=0, col=0 | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 155 | `game.network.NetworkMessageTest#restartLineForDifferentBaseDoesNotChangeRound()` | restartLineForDifferentBaseDoesNotChangeRound() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 156 | `game.network.NetworkMessageTest#restartLineOnClientReplacesTheMatchingRound()` | restartLineOnClientReplacesTheMatchingRound() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 157 | `game.network.NetworkMessageTest#restartRequestLineOnHostCommitsANewRound()` | restartRequestLineOnHostCommitsANewRound() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |
| 158 | `game.network.NetworkMessageTest#sendMoveFormatsTheCurrentRoundMessage()` | sendMoveFormatsTheCurrentRoundMessage() | game.network.NetworkManager | PASS | Hành vi đơn lẻ |

Nguồn XML: [TEST-junit-jupiter.xml](<C:/VNUK/Năm 3 - Kì 1 - 2026_2027/Basic Project/Gomoku/test/review-2026-11-03/xml/TEST-junit-jupiter.xml>). Tên/phân loại không được suy ra từ kết quả benchmark hoặc log cũ.
