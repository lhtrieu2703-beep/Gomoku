package game;

import game.ai.MinimaxAI;
import game.view.*;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tự động cho Task 3 (UI sound, language, color contrast, persistence).
 * Chạy headless=false để tạo Swing component.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UILangSoundTest {

    // Lưu trạng thái gốc để khôi phục sau mỗi test
    private static Lang.Language origLang;
    private static boolean origSound;
    private static java.util.Map<String, String> productionBefore;

    // Node Preferences riêng cho test — KHÔNG phải node thật
    static final String TEST_PREF_NODE = "/test/task3/uiLangSound-" + java.util.UUID.randomUUID();

    @BeforeAll
    static void setup() throws Exception {
        System.setProperty("java.awt.headless", "false");
        productionBefore = PreferencesIsolation.snapshot();
        origLang  = Lang.get();
        origSound = SoundManager.isSoundEnabled();
    }

    @AfterEach
    void restore() throws Exception {
        // Khôi phục trạng thái tĩnh sau mỗi test
        Lang.set(origLang);
        SoundManager.setSoundEnabled(origSound);
        PreferencesIsolation.cleanup(TEST_PREF_NODE, productionBefore);
    }

    @AfterAll
    static void cleanup() throws Exception {
        PreferencesIsolation.cleanup(TEST_PREF_NODE, productionBefore);
        // Khôi phục lần cuối
        Lang.set(origLang);
        SoundManager.setSoundEnabled(origSound);
    }

    // =========================================================================
    // C1 – Lang reflection test: mọi key VI/EN/PT không null, không rỗng
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("C1 – Lang: mọi key VI/EN/PT không null, không rỗng, PT khác EN (trừ allowlist)")
    void c1_langAllKeysNotNullNotEmpty() throws Exception {
        // allowlistMethods: tên method được phép PT==EN
        // Chỉ còn "online" — "Online (LAN)" là thuật ngữ quốc tế, PT==EN là đúng
        // hostBtn: "Criar Sala (Host - Preto)" != "Create Room (Host - Black)" → không allowlist
        // joinBtn: "Entrar na Sala (Join - Branco)" != "Join Room (Join - White)" → không allowlist
        Set<String> allowlistMethods = Set.of(
            "online"   // "Online (LAN)" — từ quốc tế, PT==VI==EN
        );

        List<Method> stringMethods = new ArrayList<>();
        for (Method m : Lang.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())
                    && Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == String.class
                    && m.getParameterCount() == 0) {
                stringMethods.add(m);
            }
        }

        System.out.println("[C1] Tổng hàm String không tham số: " + stringMethods.size());

        List<String> failures = new ArrayList<>();
        for (Method m : stringMethods) {
            for (Lang.Language lang : Lang.Language.values()) {
                Lang.set(lang);
                String val = (String) m.invoke(null);
                if (val == null || val.isEmpty()) {
                    failures.add(lang + "." + m.getName() + "() = null/empty");
                }
                if (val != null && val.equals(m.getName())) {
                    failures.add(lang + "." + m.getName() + "() == tên method (quên sửa?)");
                }
            }

            // PT không được trùng EN trừ allowlist
            Lang.set(Lang.Language.EN);
            String en = (String) m.invoke(null);
            Lang.set(Lang.Language.PT);
            String pt = (String) m.invoke(null);

            if (en.equals(pt) && !allowlistMethods.contains(m.getName())) {
                System.out.println("[C1] CẢNH BÁO: PT == EN cho " + m.getName()
                    + "() = \"" + en + "\"");
                failures.add("PT==EN cho " + m.getName() + "() = \"" + en + "\"");
            }
        }

        System.out.println("[C1] Allowlist methods (PT có thể == EN): " + allowlistMethods);
        if (!failures.isEmpty()) {
            fail("C1 failures:\n" + String.join("\n", failures));
        }
        System.out.println("[C1] PASS — " + stringMethods.size() + " methods × 3 languages");
    }

    // =========================================================================
    // C1b – hàm có tham số connectingMsg(String ip)
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("C1b – Lang.connectingMsg(ip) chứa ip trong mọi ngôn ngữ")
    void c1b_connectingMsgContainsIp() {
        String ip = "192.168.1.5";
        for (Lang.Language lang : Lang.Language.values()) {
            Lang.set(lang);
            String msg = Lang.connectingMsg(ip);
            assertNotNull(msg, lang + ": null");
            assertFalse(msg.isEmpty(), lang + ": empty");
            assertTrue(msg.contains(ip), lang + ": không chứa ip. msg=" + msg);
        }
        System.out.println("[C1b] PASS");
    }

    // =========================================================================
    // C2 – Renderer combo contrast (modeCombo / diffCombo)
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("C2 – modeCombo/diffCombo renderer: bg tối, fg trắng khi không selected; contrast sel >= 3.0")
    void c2_comboRendererContrast() throws Exception {
        Lang.set(Lang.Language.VI);
        MainFrame frame = new MainFrame(new AppSettings(
                Preferences.userRoot().node(TEST_PREF_NODE + "/c2")));
        try {
            // modeCombo
            JComboBox<BoardPanel.GameMode> modeCombo = frame.getModeCombo();
            assertRendererContrast(modeCombo, "modeCombo");

            // diffCombo
            JComboBox<MinimaxAI.Difficulty> diffCombo = frame.getDiffCombo();
            assertRendererContrast(diffCombo, "diffCombo");

            System.out.println("[C2] PASS");
        } finally {
            frame.dispose();
            cleanup_pref(TEST_PREF_NODE + "/c2");
        }
    }

    private void assertRendererContrast(JComboBox<?> combo, String name) {
        JList<?> dummyList = new JList<>();
        // Thiết lập dummyList với màu tối để renderer không lấy màu hệ thống
        dummyList.setBackground(new Color(55, 58, 65));
        dummyList.setForeground(Color.WHITE);
        Object item = combo.getItemAt(0);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ListCellRenderer renderer = combo.getRenderer();
        System.out.printf("[C2] %s renderer class: %s%n", name, renderer.getClass().getName());

        // index=-1: ô display — kiểm tra qua paintCurrentValue (BasicComboBoxUI)
        // Thay vì gọi renderer trực tiếp, vẽ combo ra ảnh và kiểm pixel
        int w = Math.max(combo.getWidth(), 100);
        int h = Math.max(combo.getHeight(), 22);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(34, 36, 40)); // nền header
        g2.fillRect(0, 0, w, h);
        combo.paint(g2);
        g2.dispose();

        // Lấy pixel ở giữa vùng display (loại bỏ 20px arrow button bên phải)
        int displayW = w - 22;
        int midY = h / 2;
        int sumR=0, sumG=0, sumB=0, cnt=0;
        for (int x = 4; x < displayW - 4; x += 3) {
            int rgb = img.getRGB(x, midY);
            sumR += (rgb>>16)&0xFF; sumG += (rgb>>8)&0xFF; sumB += rgb&0xFF; cnt++;
        }
        int avgR = sumR/cnt, avgG = sumG/cnt, avgB = sumB/cnt;
        System.out.printf("[C2] %s painted field avg pixel: RGB(%d,%d,%d)%n", name, avgR, avgG, avgB);
        assertTrue(avgR + avgG + avgB < 400,
            name + " display field bg không đủ tối (painted): RGB(" + avgR+","+avgG+","+avgB+")");

        // index=0, isSelected=true: popup selected — kiểm qua renderer
        @SuppressWarnings({"unchecked", "rawtypes"})
        Component cSel = ((ListCellRenderer)renderer).getListCellRendererComponent(dummyList, item, 0, true, false);
        Color bgS = cSel.getBackground();
        Color fgS = cSel.getForeground();
        System.out.printf("[C2] %s selected: bg=RGB(%d,%d,%d) fg=RGB(%d,%d,%d)%n",
            name, bgS.getRed(), bgS.getGreen(), bgS.getBlue(),
            fgS.getRed(), fgS.getGreen(), fgS.getBlue());
        double cr = contrastRatio(bgS, fgS);
        System.out.printf("[C2] %s contrast ratio selected: %.2f%n", name, cr);
        assertTrue(cr >= 3.0, name + " contrast ratio selected < 3.0: " + cr);
    }

    /** Tính contrast ratio theo WCAG 2.x */
    private double contrastRatio(Color c1, Color c2) {
        double l1 = relativeLuminance(c1);
        double l2 = relativeLuminance(c2);
        double lighter = Math.max(l1, l2);
        double darker  = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private double relativeLuminance(Color c) {
        double r = srgb(c.getRed()   / 255.0);
        double g = srgb(c.getGreen() / 255.0);
        double b = srgb(c.getBlue()  / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double srgb(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    // =========================================================================
    // B2 – SoundManager flag toggle qua btnSound
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("B2 – Click btnSound toggle SoundManager.isSoundEnabled()")
    void b2_soundButtonToggle() throws Exception {
        SoundManager.setSoundEnabled(true);
        MainFrame frame = new MainFrame(new AppSettings(
                Preferences.userRoot().node(TEST_PREF_NODE + "/b2")));
        try {
            JButton btn = frame.getBtnSound();
            assertNotNull(btn, "btnSound null");

            assertTrue(SoundManager.isSoundEnabled(), "ban đầu phải bật");
            btn.doClick();
            assertFalse(SoundManager.isSoundEnabled(), "sau click 1: phải tắt");
            btn.doClick();
            assertTrue(SoundManager.isSoundEnabled(), "sau click 2: phải bật");

            System.out.println("[B2] PASS");
        } finally {
            frame.dispose();
            cleanup_pref(TEST_PREF_NODE + "/b2");
        }
    }

    // =========================================================================
    // B3 – Tooltip nút sound đúng theo ngôn ngữ và trạng thái
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("B3 – Tooltip btnSound đúng theo ngôn ngữ và trạng thái on/off")
    void b3_soundTooltipByLang() throws Exception {
        Lang.set(Lang.Language.VI);
        SoundManager.setSoundEnabled(true);
        MainFrame frame = new MainFrame(new AppSettings(
                Preferences.userRoot().node(TEST_PREF_NODE + "/b3")));
        try {
            JButton btn = frame.getBtnSound();

            // VI + on
            assertEquals(Lang.soundTooltipOn(), btn.getToolTipText(),
                "VI+on: tooltip sai");

            // Tắt sound → tooltip đổi
            btn.doClick(); // tắt
            Lang.set(Lang.Language.VI); // trigger updateLanguage via listener
            assertEquals(Lang.soundTooltipOff(), btn.getToolTipText(),
                "VI+off: tooltip sai");

            // Đổi sang EN
            Lang.set(Lang.Language.EN);
            // updateLanguage được gọi qua listener → tooltip cập nhật
            assertEquals(Lang.soundTooltipOff(), btn.getToolTipText(),
                "EN+off: tooltip sai");

            btn.doClick(); // bật lại
            Lang.set(Lang.Language.EN);
            assertEquals(Lang.soundTooltipOn(), btn.getToolTipText(),
                "EN+on: tooltip sai");

            System.out.println("[B3] PASS");
        } finally {
            frame.dispose();
            cleanup_pref(TEST_PREF_NODE + "/b3");
        }
    }

    // =========================================================================
    // C3 – MainFrame labels cập nhật sau Lang.set()
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("C3 – MainFrame labels cập nhật đúng sau đổi ngôn ngữ VI→EN→PT")
    void c3_mainFrameLabelsUpdate() throws Exception {
        Lang.set(Lang.Language.VI);
        MainFrame frame = new MainFrame(new AppSettings(
                Preferences.userRoot().node(TEST_PREF_NODE + "/c3")));
        try {
            // VI
            assertEquals(Lang.mode(), frame.getLblMode().getText(), "VI: lblMode sai");

            // EN
            Lang.set(Lang.Language.EN);
            assertEquals(Lang.mode(), frame.getLblMode().getText(), "EN: lblMode sai");
            assertEquals("Mode:", frame.getLblMode().getText(), "EN: phải là 'Mode:'");

            // PT
            Lang.set(Lang.Language.PT);
            assertEquals(Lang.mode(), frame.getLblMode().getText(), "PT: lblMode sai");
            assertEquals("Modo:", frame.getLblMode().getText(), "PT: phải là 'Modo:'");

            // LangCombo selected item
            assertEquals(Lang.Language.PT, frame.getLangCombo().getSelectedItem(),
                "langCombo phải chọn PT");

            System.out.println("[C3] PASS");
        } finally {
            frame.dispose();
            cleanup_pref(TEST_PREF_NODE + "/c3");
        }
    }

    // =========================================================================
    // D1 – AppSettings round-trip
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("D1 – AppSettings round-trip: lưu/đọc lang + sound; fallback mặc định; invalid → VI")
    void d1_appSettingsRoundTrip() throws Exception {
        Preferences testNode = Preferences.userRoot().node(TEST_PREF_NODE + "/d1");
        testNode.clear();
        AppSettings s = new AppSettings(testNode);

        // Lưu EN + tắt âm thanh
        s.saveLang(Lang.Language.EN);
        s.saveSound(false);

        // Đọc lại
        assertEquals(Lang.Language.EN, s.loadLang(), "loadLang EN");
        assertFalse(s.loadSound(), "loadSound false");

        // Lưu PT
        s.saveLang(Lang.Language.PT);
        assertEquals(Lang.Language.PT, s.loadLang(), "loadLang PT");

        // Node rỗng → mặc định VI + bật
        testNode.clear();
        assertEquals(Lang.Language.VI, s.loadLang(), "empty→VI");
        assertTrue(s.loadSound(), "empty→sound bật");

        // Giá trị lang không hợp lệ → fallback VI, không ném exception
        testNode.put("language", "INVALID_LANG");
        assertDoesNotThrow(() -> s.loadLang(), "invalid không được ném exception");
        assertEquals(Lang.Language.VI, s.loadLang(), "invalid→fallback VI");

        System.out.println("[D1] PASS");

        // Cleanup
        testNode.removeNode();
    }

    // =========================================================================
    // D2 – Preferences node test KHÁC node thật
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("D2 – Preferences test node khác node production")
    void d2_testNodeDifferentFromProduction() {
        AppSettings prod = new AppSettings();
        String prodPath = prod.getPrefs().absolutePath();

        Preferences testNode = Preferences.userRoot().node(TEST_PREF_NODE);
        assertNotEquals(prodPath, testNode.absolutePath(),
            "Test node phải khác production node");

        System.out.println("[D2] prod path: " + prodPath);
        System.out.println("[D2] test path: " + testNode.absolutePath());
        System.out.println("[D2] PASS");
    }

    // =========================================================================
    // I2 – SpeakerIcon visual: on ≠ off, off có pixel đỏ
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("I2 – SpeakerIcon: on≠off, off có pixel đỏ, đọc từ SoundManager")
    void i2_speakerIconVisual() {
        SpeakerIcon icon = new SpeakerIcon(20);

        // Vẽ ON
        SoundManager.setSoundEnabled(true);
        BufferedImage imgOn = renderIcon(icon, 20, 20);

        // Vẽ OFF
        SoundManager.setSoundEnabled(false);
        BufferedImage imgOff = renderIcon(icon, 20, 20);

        // Hai ảnh phải khác nhau
        assertFalse(imagesEqual(imgOn, imgOff), "Icon on == off — phải khác nhau");

        // Ảnh OFF phải có pixel đỏ (X-mark)
        boolean hasRed = hasRedPixel(imgOff);
        assertTrue(hasRed, "Icon OFF phải có pixel đỏ (dấu X)");

        // Ảnh ON không có pixel đỏ
        assertFalse(hasRedPixel(imgOn), "Icon ON không được có pixel đỏ");

        // SoundManager.isSoundEnabled() đổi → ảnh đổi
        SoundManager.setSoundEnabled(true);
        BufferedImage imgOn2 = renderIcon(icon, 20, 20);
        assertFalse(imagesEqual(imgOff, imgOn2), "Sau set(true): ảnh phải đổi về on");

        System.out.println("[I2] PASS");
    }

    private BufferedImage renderIcon(Icon icon, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(55, 58, 65)); // nền header tối
        g2.fillRect(0, 0, w, h);
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return img;
    }

    private boolean imagesEqual(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int x = 0; x < a.getWidth(); x++)
            for (int y = 0; y < a.getHeight(); y++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }

    private boolean hasRedPixel(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int a = (rgb >> 24) & 0xFF;
                if (a > 100 && r > 150 && g < 100 && b < 100) return true;
            }
        }
        return false;
    }

    private void cleanup_pref(String path) {
        try { Preferences.userRoot().node(path).removeNode(); } catch (Exception ignored) {}
    }
}
