package game;

import game.model.*;
import game.network.NetworkManager;
import game.view.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.swing.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class F2MoveOrderTest {
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void finalMoveIsSentBeforeResultAndResultIsProcessedOnce(boolean draw) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SoundManager.setSoundEnabled(false);
            List<String> order = new ArrayList<>();
            NetworkManager manager = new NetworkManager(null) {
                @Override public void sendMove(String id, int ply, int r, int c) { order.add("send"); }
            };
            Board board = new Board(); BoardPanel panel = new BoardPanel(board); ScoreTracker score = new ScoreTracker();
            panel.setupOnlineGame(manager, true);
            if (draw) {
                for (int r = 0; r < 15; r++) for (int c = 0; c < 15; c++) {
                    if (r == 14 && c == 14) continue;
                    board.makeMove(r, c, (c + (r % 2 == 0 ? 0 : 2)) % 4 < 2 ? Piece.X : Piece.O);
                }
            } else {
                for (int c = 0; c < 4; c++) { panel.playMove(7, c); panel.applyOpponentMove(0, c * 2); }
            }
            panel.setGameStateListener(new BoardPanel.GameStateListener() {
                public void onTurnChanged(Piece p, boolean thinking) {}
                public void onGameOver(String message, Piece winner) {
                    order.add("result"); score.recordResult(panel.getGameId(), winner);
                    assertEquals(List.of("send", "result"), order); // modal UI may only open here
                }
                public void onAiStatsUpdated(long t, long n, long p) {}
                public void onHistoryChanged(boolean b) {}
            });
            order.clear();
            assertTrue(panel.playMove(draw ? 14 : 7, draw ? 14 : 4));
            assertTrue(panel.isGameOver());
            assertFalse(panel.playMove(3, 3));
            assertEquals(List.of("send", "result"), order);
            assertEquals(1, draw ? score.getDraws() : score.getHumanWins());
        });
    }
}
