package game;

import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class Task4bFoundationTest {
    @ParameterizedTest @CsvSource(value={"'',''","A,A","abcdefghijklmnop,abcdefghijklmnop","abcdefghijklmnopq,abcdefghijklmnop","'   ',''","' João ',João","' Đặng ',Đặng"})
    void profileNames(String raw,String expected) { assertEquals(expected,new PlayerProfile(raw,0).name()); }
    @ParameterizedTest @ValueSource(ints={-1,8,99,Integer.MAX_VALUE})
    void invalidAvatarsDefaultToZero(int index) { assertEquals(0,new PlayerProfile("A",index).avatarIndex()); }
    @Test void profileRoundTripKeepsLegacyKeyAndImplicitDefault() throws Exception {
        var node=new MemoryPreferences().node("profile"); var store=new AppSettings(node);
        store.saveProfile(new PlayerProfile(" João ",7));
        assertEquals("João",node.get("playerName",null));
        assertEquals(new PlayerProfile("João",7),new AppSettings(node).loadProfile());
        store.saveProfile(new PlayerProfile(" ",0)); assertNull(node.get("playerName",null));
        for(var language:Lang.Language.values()) { Lang.set(language); assertEquals(Lang.defaultPlayerName(),store.loadProfile().displayName()); }
        node.removeNode();
    }
    @Test void missingImagesRenderAndCoverCacheTracksDevicePixels() {
        BackgroundPanel fallback=new BackgroundPanel(null); fallback.setSize(200,100);
        var image=new BufferedImage(300,150,BufferedImage.TYPE_INT_RGB); Graphics2D g=image.createGraphics();
        assertDoesNotThrow(() -> fallback.paint(g)); assertFalse(fallback.hasImage());
        assertNotEquals(image.getRGB(0,0),image.getRGB(199,99));
        for(int index=0;index<8;index++) new AvatarIcon(index,"João",40).paintIcon(fallback,g,index*20,20);
        BackgroundPanel cover=new BackgroundPanel(new BufferedImage(80,200,BufferedImage.TYPE_INT_RGB)); cover.setSize(200,100);
        for(double scale:new double[]{1.25,1.5}) {
            Graphics2D scaled=(Graphics2D)g.create(); scaled.scale(scale,scale); cover.paint(scaled);
            assertEquals((int)(200*scale),cover.getCachedImage().getWidth());
            var cache=cover.getCachedImage(); cover.paint(scaled); assertSame(cache,cover.getCachedImage()); scaled.dispose();
        }
        g.dispose();
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void buttonsPaintFourDistinctStatesUnderBothLooks(boolean nativeLook) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String previous=UIManager.getLookAndFeel().getClass().getName();
            try {
                UIManager.setLookAndFeel(nativeLook?UIManager.getSystemLookAndFeelClassName():UIManager.getCrossPlatformLookAndFeelClassName());
                for(var style:ThemedButton.Style.values()) {
                    var button=new ThemedButton(Lang.startGame(),style); button.setSize(200,50);
                    var fills=new java.util.HashSet<Color>(); fills.add(button.fillColor());
                    button.getModel().setRollover(true); fills.add(button.fillColor());
                    button.getModel().setArmed(true); button.getModel().setPressed(true); fills.add(button.fillColor());
                    button.setEnabled(false); fills.add(button.fillColor()); assertEquals(4,fills.size());
                    button.paint(new BufferedImage(200,50,BufferedImage.TYPE_INT_RGB).createGraphics());
                    assertNotNull(button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("ENTER")));
                    assertNotNull(button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("pressed SPACE")));
                }
            } catch(Exception e) { throw new AssertionError(e); }
            finally { try { UIManager.setLookAndFeel(previous); } catch(Exception e) { throw new AssertionError(e); } }
        });
    }
}
