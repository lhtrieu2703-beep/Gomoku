package game;
import game.view.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Executable render/size evidence; no production Preferences are constructed. */
class Task4bPresentationTest {
    @ParameterizedTest @ValueSource(booleans={false,true})
    void buttonsRenderFourStatesAndActivateWithEnterAndSpace(boolean nativeLook) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String previous=UIManager.getLookAndFeel().getClass().getName();
            try {
                UIManager.setLookAndFeel(nativeLook?UIManager.getSystemLookAndFeelClassName():UIManager.getCrossPlatformLookAndFeelClassName());
                for(ThemedButton.Style style:ThemedButton.Style.values()) {
                    ThemedButton button=new ThemedButton(Lang.done(),style); button.setSize(180,48);
                    java.util.Set<Integer> pixels=new java.util.HashSet<>();
                    for(int state=0;state<4;state++) {
                        button.setEnabled(state!=3); button.getModel().setRollover(state==1); button.getModel().setArmed(state==2); button.getModel().setPressed(state==2);
                        BufferedImage image=new BufferedImage(180,48,BufferedImage.TYPE_INT_RGB); Graphics2D g=image.createGraphics();
                        g.setColor(Theme.DARK); g.fillRect(0,0,180,48); button.paint(g); g.dispose(); pixels.add(image.getRGB(15,15));
                    }
                    assertEquals(4,pixels.size()); button.setEnabled(true); button.getModel().setPressed(false); button.getModel().setArmed(false);
                    int[] clicks={0}; button.addActionListener(e -> clicks[0]++);
                    for(String key:new String[]{"ENTER","pressed SPACE","released SPACE"}) {
                        Object action=button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(key)); assertNotNull(action);
                        button.getActionMap().get(action).actionPerformed(new java.awt.event.ActionEvent(button,0,key));
                    }
                    assertEquals(2,clicks[0]); assertTrue(button.isFocusable());
                    assertTrue(button.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS).contains(AWTKeyStroke.getAWTKeyStroke("TAB")));
                }
            } catch(Exception e) { throw new AssertionError(e); }
            finally { try { UIManager.setLookAndFeel(previous); } catch(Exception e) { throw new AssertionError(e); } }
        });
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void renderAllSixViewsAndMeasureDisplayedFrame(Lang.Language language) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String previous=UIManager.getLookAndFeel().getClass().getName(); MainFrame f=null;
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); f=Task4StartScreenTest.frame(language); f.setVisible(true);
                Dimension outer=f.getSize();
                var nativeScale=f.getGraphicsConfiguration().getDefaultTransform();
                if(nativeScale.getScaleX()==1.25&&nativeScale.getScaleY()==1.25)
                    assertEquals(new Dimension(774,795),outer,"Native Windows frame at DPI 125%");
                for(MainFrame.Screen screen:MainFrame.Screen.values()) {
                    if(screen==MainFrame.Screen.MENU) f.showMenu(); else if(screen==MainFrame.Screen.GAME) f.startGame();
                    f.validate(); F4LayoutTest.layout(f); assertEquals(outer,f.getSize());
                    assertEquals(new Dimension(760,758),f.getContentPane().getSize());
                    System.out.println("SIZE "+language+" "+screen+" frame.getSize()="+f.getWidth()+"x"+f.getHeight()+" content.getSize()="+f.getContentPane().getWidth()+"x"+f.getContentPane().getHeight()+" transform="+f.getGraphicsConfiguration().getDefaultTransform());
                    render(f,screen.name().toLowerCase(java.util.Locale.ROOT)+"-"+language);
                }
                LocalizedDialog[] dialogs={new ProfileDialog(f),new SettingsDialog(f),new RulesDialog(f)};
                String[] names={"profile","settings","rules"};
                for(int i=0;i<dialogs.length;i++) { F4LayoutTest.layout(dialogs[i]); F4LayoutTest.check(dialogs[i].getContentPane()); render(dialogs[i],names[i]+"-"+language); dialogs[i].dispose(); }
            } catch(Exception e) { throw new AssertionError(e); }
            finally { if(f!=null) f.dispose(); try { UIManager.setLookAndFeel(previous); } catch(Exception e) { throw new AssertionError(e); } }
        });
    }
    private static void render(RootPaneContainer window,String name) throws Exception {
        Container content=window.getContentPane(); F4LayoutTest.layout(content); F4LayoutTest.check(content);
        GraphicsConfiguration configuration=((Window)window).getGraphicsConfiguration(); var scale=configuration.getDefaultTransform();
        BufferedImage image=new BufferedImage((int)Math.ceil(content.getWidth()*scale.getScaleX()),(int)Math.ceil(content.getHeight()*scale.getScaleY()),BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics(); g.scale(scale.getScaleX(),scale.getScaleY()); content.printAll(g); g.dispose();
        Path directory=Paths.get("docs","task4b-render"); Files.createDirectories(directory); ImageIO.write(image,"png",directory.resolve(name+".png").toFile());
        System.out.println("RENDER "+name+" "+image.getWidth()+"x"+image.getHeight());
    }
    @ParameterizedTest @MethodSource("game.Task4LayoutTest#languagesAndLooks")
    void translatedButtonAndLabelTextFitsIncludingTransitions(Lang.Language language,boolean nativeLook) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String previous=UIManager.getLookAndFeel().getClass().getName(); MainFrame f=null;
            try {
                UIManager.setLookAndFeel(nativeLook?UIManager.getSystemLookAndFeelClassName():UIManager.getCrossPlatformLookAndFeelClassName()); f=Task4StartScreenTest.frame(language);
                f.updateProfile(new PlayerProfile("WWWWWWWWWWWWWWWW",7));
                for(Lang.Language next:Lang.Language.values()) {
                    f.selectLanguage(next); f.showMenu(); F4LayoutTest.layout(f); textFits(f.getContentPane());
                    f.startGame(); F4LayoutTest.layout(f); textFits(f.getContentPane());
                    assertEquals(new Dimension(40,40),f.getSettingsButton().getSize()); assertEquals(new Dimension(40,40),f.getRulesButton().getSize());
                    assertEquals(SwingUtilities.convertPoint(f.getMainMenu().getSettingsButton(),0,0,f.getContentPane()).x,SwingUtilities.convertPoint(f.getSettingsButton(),0,0,f.getContentPane()).x);
                    assertEquals(SwingUtilities.convertPoint(f.getMainMenu().getRulesButton(),0,0,f.getContentPane()).x,SwingUtilities.convertPoint(f.getRulesButton(),0,0,f.getContentPane()).x);
                    f.returnToMenu();
                    for(LocalizedDialog d:new LocalizedDialog[]{new ProfileDialog(f),new SettingsDialog(f),new RulesDialog(f),new SetupDialog(f,true),new SetupDialog(f,false)}) {
                        F4LayoutTest.layout(d); textFits(d.getContentPane()); d.dispose();
                    }
                }
            } catch(Exception e) { throw new AssertionError(e); }
            finally { if(f!=null) f.dispose(); try { UIManager.setLookAndFeel(previous); } catch(Exception e) { throw new AssertionError(e); } }
        });
    }
    static void textFits(Container container) {
        for(Component c:container.getComponents()) {
            if(!c.isVisible()) continue;
            if(c instanceof JLabel label&&label.getText()!=null&&!label.getText().isEmpty())
                assertTrue(label.getFontMetrics(label.getFont()).stringWidth(label.getText())<=label.getWidth(),label.getText()+" clipped at "+label.getSize());
            if(c instanceof ThemedButton button) {
                String text=button.getText()==null?"":button.getText(); int width=button.getFontMetrics(button.getFont()).stringWidth(text);
                if(button.getIcon()!=null) width+=button.getIcon().getIconWidth()+(text.isEmpty()?0:button.getIconTextGap());
                assertTrue(width<=button.getWidth()-8,text+" clipped at "+button.getSize());
            }
            if(c instanceof JComboBox<?> combo&&combo.getSelectedItem()!=null) {
                @SuppressWarnings({"rawtypes","unchecked"})
                Component rendered=((ListCellRenderer)combo.getRenderer()).getListCellRendererComponent(new JList<>(),combo.getSelectedItem(),-1,false,false);
                if(rendered instanceof JLabel label) {
                    int width=label.getFontMetrics(label.getFont()).stringWidth(label.getText());
                    assertTrue(width<=combo.getWidth()-30,label.getText()+" clipped in combo "+combo.getSize());
                }
            }
            if(c instanceof Container child) textFits(child);
        }
    }
    @Test void profileClearCounterAndFocusSelectAllUseActualEditor() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f=Task4StartScreenTest.frame(Lang.Language.PT); ProfileDialog dialog=new ProfileDialog(f);
            try {
                ProfileEditor editor=dialog.getEditor(); JTextField field=editor.getNameField(); field.setText("  abcdefghijklmnop  ");
                assertEquals("abcdefghijklmnop",editor.value().name()); assertEquals("16/16",editor.getCountLabel().getText());
                for(var listener:field.getFocusListeners()) listener.focusGained(new java.awt.event.FocusEvent(field,java.awt.event.FocusEvent.FOCUS_GAINED));
                assertEquals(field.getText(),field.getSelectedText()); editor.getClearButton().doClick(); assertEquals("",field.getText()); assertEquals("0/16",editor.getCountLabel().getText());
            } finally { dialog.dispose(); f.dispose(); }
        });
    }
    @ParameterizedTest @EnumSource(Lang.Language.class)
    void suppliedRulesKeysAreVerbatimAndPortugueseIsPure(Lang.Language language) throws Exception {
        Properties approved=new Properties(); try(var reader=Files.newBufferedReader(Paths.get("test","resources","task4b-rules",language+".properties"))) { approved.load(reader); }
        Lang.Language before=Lang.get();
        try {
            Lang.set(language); assertEquals(8,approved.size());
            for(String key:approved.stringPropertyNames()) {
                assertEquals(approved.getProperty(key),Lang.rules(key),key);
                if(language==Lang.Language.PT) for(char vi:LangPurityTest.VI_ONLY_CHARS.toCharArray()) assertTrue(Lang.rules(key).indexOf(vi)<0,key);
            }
        } finally { Lang.set(before); }
    }
    @Test void runtimeResourcePackagingAndImageLoadingWorkOnBinClasspath() throws Exception {
        assertNotNull(ResourceImages.class.getResource("/README.md"),"build must copy resources to bin");
        Path folder=Paths.get("bin_test","task4b-fixtures"); Files.createDirectories(folder); Path fixture=folder.resolve("image.png");
        try {
            BufferedImage image=new BufferedImage(5,7,BufferedImage.TYPE_INT_RGB); image.setRGB(2,3,Theme.BRASS.getRGB()); ImageIO.write(image,"png",fixture.toFile());
            BufferedImage loaded=ResourceImages.load("/task4b-fixtures/image.png"); assertNotNull(loaded); assertEquals(5,loaded.getWidth()); assertEquals(7,loaded.getHeight());
            assertEquals(Theme.BRASS.getRGB(),loaded.getRGB(2,3)); assertNull(ResourceImages.load("/task4b-fixtures/missing.png"));
        } finally { Files.deleteIfExists(fixture); Files.deleteIfExists(folder); }
    }
}
