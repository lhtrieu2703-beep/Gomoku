package game;

import game.ai.MinimaxAI;
import game.model.Piece;
import game.view.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class PlayerPieceBadgeTest {
    static Stream<Arguments> assignments() {
        return Stream.of(Arguments.of(BoardPanel.GameMode.PVP,Piece.X,Piece.X,Piece.O),
                Arguments.of(BoardPanel.GameMode.PVP,Piece.O,Piece.X,Piece.O),
                Arguments.of(BoardPanel.GameMode.PV_AI,Piece.X,Piece.X,Piece.O),
                Arguments.of(BoardPanel.GameMode.PV_AI,Piece.O,Piece.X,Piece.O),
                Arguments.of(BoardPanel.GameMode.ONLINE,Piece.X,Piece.X,Piece.O),
                Arguments.of(BoardPanel.GameMode.ONLINE,Piece.O,Piece.O,Piece.X));
    }
    @ParameterizedTest @MethodSource("assignments")
    void pureMappingAssignsBlackToXAndWhiteToO(BoardPanel.GameMode mode,Piece local,Piece left,Piece right) {
        assertEquals(left,PieceBadgeIcon.pieceForCard(mode,local,true));
        assertEquals(right,PieceBadgeIcon.pieceForCard(mode,local,false));
    }
    @ParameterizedTest @EnumSource(value=Piece.class,names={"X","O"})
    void badgePixelsUseBoardPaletteAndAntialiasedWhiteOutline(Piece piece) {
        for(double scale:new double[]{1.0,1.5}) {
            PieceBadgeIcon icon=new PieceBadgeIcon(piece);
            BufferedImage image=new BufferedImage((int)(PieceBadgeIcon.SIZE*scale),(int)(PieceBadgeIcon.SIZE*scale),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=image.createGraphics();try { g.scale(scale,scale);icon.paintIcon(null,g,0,0); } finally { g.dispose(); }
            assertEquals((piece==Piece.X?Theme.BLACK_STONE:Theme.WHITE_STONE).getRGB(),image.getRGB((int)(9*scale),(int)(9*scale)));
            boolean antialiased=false,whiteEdge=false;
            for(int y=0;y<image.getHeight();y++) for(int x=0;x<image.getWidth();x++) {
                int pixel=image.getRGB(x,y),alpha=pixel>>>24;
                antialiased|=alpha>0&&alpha<255;
                Color c=new Color(pixel,true);
                whiteEdge|=alpha>0&&Math.abs(c.getRed()-Theme.WHITE_EDGE.getRed())<20
                        &&Math.abs(c.getGreen()-Theme.WHITE_EDGE.getGreen())<20&&Math.abs(c.getBlue()-Theme.WHITE_EDGE.getBlue())<20;
            }
            assertTrue(antialiased,"Circle edge must be antialiased");
            if(piece==Piece.O) assertTrue(whiteEdge,"White piece must have a contrasting thin outline");
        }
    }
    static Stream<Arguments> gameSides() {
        return Stream.of(Arguments.of(BoardPanel.GameMode.PVP,Piece.X),Arguments.of(BoardPanel.GameMode.PV_AI,Piece.X),
                Arguments.of(BoardPanel.GameMode.ONLINE,Piece.X),Arguments.of(BoardPanel.GameMode.ONLINE,Piece.O));
    }
    @ParameterizedTest @MethodSource("gameSides")
    void realCardsFollowModeSideAndLanguageWithoutChangingScoresOrTurnBorder(BoardPanel.GameMode mode,Piece local) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            Lang.Language before=Lang.get();boolean sound=SoundManager.isSoundEnabled();MainFrame f=null;
            try {
                f=Task4StartScreenTest.frame(Lang.Language.VI);f.beginGame(mode,MinimaxAI.Difficulty.MEDIUM);
                if(mode==BoardPanel.GameMode.ONLINE) f.getBoardPanel().setupOnlineGame(null,local==Piece.X);
                f.getScoreTracker().recordResult(-1,Piece.X,Piece.X);
                f.selectLanguage(Lang.Language.PT); // Refresh the seeded score; every loop iteration then changes locale.
                for(Lang.Language language:Lang.Language.values()) {
                    f.selectLanguage(language);
                    Piece left=mode==BoardPanel.GameMode.ONLINE?local:Piece.X;
                    checkBadge(f.getLeftPlayerCard(),left);checkBadge(f.getRightPlayerCard(),left.opposite());
                    assertEquals(left==Piece.X,f.getLeftPlayerCard().isHighlighted());
                    assertEquals(left==Piece.O,f.getRightPlayerCard().isHighlighted());
                    assertEquals(1,f.getScoreTracker().getHumanWins());
                    assertEquals(Lang.wins()+": 1",f.getLeftPlayerCard().getWinsLabel().getText());
                    if(mode==BoardPanel.GameMode.PVP) assertEquals("",f.getStatusText(),"Removed floating turn/name label must stay empty");
                }
            } finally { if(f!=null)f.dispose();Lang.set(before);SoundManager.setSoundEnabled(sound); }
        });
    }
    static void checkBadge(PlayerCard card,Piece piece) {
        JLabel badge=card.getPieceBadge();
        assertEquals(piece,((PieceBadgeIcon)badge.getIcon()).getPiece());
        assertEquals(Lang.pieceName(piece),badge.getToolTipText());
        assertEquals(Lang.pieceName(piece),badge.getAccessibleContext().getAccessibleName());
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void longNamesEllipsizeInsideBothCardsWithoutPushingBadgesOut(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            Lang.Language before=Lang.get();boolean sound=SoundManager.isSoundEnabled();MainFrame f=null;
            try {
                f=Task4StartScreenTest.frame(language);f.updateProfile(new PlayerProfile("W".repeat(16),0));
                f.setSecondProfile(new PlayerProfile("M".repeat(16),1));f.beginGame(BoardPanel.GameMode.PVP,MinimaxAI.Difficulty.MEDIUM);
                F4LayoutTest.layout(f);
                for(PlayerCard card:new PlayerCard[]{f.getLeftPlayerCard(),f.getRightPlayerCard()}) {
                    JLabel name=card.getNameLabel(),badge=card.getPieceBadge();
                    assertTrue(name.getText().endsWith("..."));assertEquals(card.getFullName(),name.getToolTipText());
                    assertTrue(name.getFontMetrics(name.getFont()).stringWidth(name.getText())<=name.getWidth());
                    Rectangle badgeBounds=SwingUtilities.convertRectangle(badge.getParent(),badge.getBounds(),card);
                    assertTrue(new Rectangle(0,0,card.getWidth(),card.getHeight()).contains(badgeBounds));
                    assertTrue(badge.getWidth()>=PieceBadgeIcon.SIZE);assertTrue(badge.getHeight()>=PieceBadgeIcon.SIZE);
                }
                F4LayoutTest.check(f.getContentPane());
            } finally { if(f!=null)f.dispose();Lang.set(before);SoundManager.setSoundEnabled(sound); }
        });
    }
}
