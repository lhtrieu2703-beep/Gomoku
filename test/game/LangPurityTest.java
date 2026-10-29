package game;

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
 * Test độ thuần khiết ngôn ngữ của Lang.java:
 *   CA A – bản PT không chứa ký tự tiếng Việt đặc trưng
 *   CA B – bản PT không trùng EN (trừ allowlist tường minh)
 *   CA C – contrast ratio combo >= 4.5 (ô display tối trên nền header)
 *
 * Chạy test này TRƯỚC khi sửa nguồn để xác nhận đỏ/xanh.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LangPurityTest {

    private static Lang.Language origLang;
    private static boolean origSound;
    private static java.util.Map<String, String> productionBefore;

    static final String TEST_NODE = "/test/task3/langPurity-" + java.util.UUID.randomUUID();

    // Tập ký tự tiếng Việt đặc trưng KHÔNG có trong tiếng Bồ Đào Nha:
    // ă, đ, ơ, ư và các chữ có dấu thanh chồng
    static final String VI_ONLY_CHARS =
        "ăĂđĐơƠưƯ" +
        "ạảấầẩẫậẠẢẤẦẨẪẬ" +
        "ắằẳẵặẮẰẲẴẶ" +
        "ẹẻẽếềểễệẸẺẼẾỀỂỄỆ" +
        "ỉịỈỊ" +
        "ọỏốồổỗộớờởỡợỌỎỐỒỔỖỘỚỜỞỠỢ" +
        "ụủứừửữựỤỦỨỪỬỮỰ" +
        "ỳỵỷỹỲỴỶỸ";

    @BeforeAll
    static void setup() throws Exception {
        productionBefore = PreferencesIsolation.snapshot();
        origLang  = Lang.get();
        origSound = SoundManager.isSoundEnabled();
    }

    @AfterEach
    void restore() throws Exception {
        Lang.set(origLang);
        SoundManager.setSoundEnabled(origSound);
        PreferencesIsolation.cleanup(TEST_NODE, productionBefore);
    }

    @AfterAll
    static void cleanup() throws Exception {
        Lang.set(origLang);
        SoundManager.setSoundEnabled(origSound);
        PreferencesIsolation.cleanup(TEST_NODE, productionBefore);
    }

    // =========================================================================
    // CA A – bản PT không chứa ký tự tiếng Việt đặc trưng
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("A – PT không chứa ký tự tiếng Việt đặc trưng (ă đ ơ ư ...)")
    void a_ptNoVietnameseChars() throws Exception {
        List<Method> methods = getNoArgStringMethods();
        System.out.println("[A] Kiểm " + methods.size() + " methods với bản PT");

        List<String> failures = new ArrayList<>();
        for (Method m : methods) {
            Lang.set(Lang.Language.PT);
            String pt = (String) m.invoke(null);
            for (char c : VI_ONLY_CHARS.toCharArray()) {
                if (pt.indexOf(c) >= 0) {
                    failures.add("PT." + m.getName() + "() chứa ký tự VI '" + c
                        + "': \"" + pt + "\"");
                }
            }
        }

        // Cũng kiểm connectingMsg
        Lang.set(Lang.Language.PT);
        String ptConn = Lang.connectingMsg("1.2.3.4");
        for (char c : VI_ONLY_CHARS.toCharArray()) {
            if (ptConn.indexOf(c) >= 0) {
                failures.add("PT.connectingMsg() chứa ký tự VI '" + c
                    + "': \"" + ptConn + "\"");
            }
        }

        if (!failures.isEmpty()) {
            fail("[A] FAILURES:\n" + String.join("\n", failures));
        }
        System.out.println("[A] PASS – không có ký tự VI trong bản PT");
    }

    // =========================================================================
    // CA B – bản EN chỉ chứa ASCII
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("B – EN chỉ chứa ASCII (0x00-0x7F)")
    void b_enAsciiOnly() throws Exception {
        List<Method> methods = getNoArgStringMethods();
        System.out.println("[B] Kiểm " + methods.size() + " methods với bản EN");

        List<String> failures = new ArrayList<>();
        for (Method m : methods) {
            Lang.set(Lang.Language.EN);
            String en = (String) m.invoke(null);
            for (int i = 0; i < en.length(); i++) {
                char c = en.charAt(i);
                if (c > 0x7F) {
                    failures.add("EN." + m.getName() + "() chứa non-ASCII U+"
                        + String.format("%04X", (int) c) + " '" + c
                        + "': \"" + en + "\"");
                    break; // chỉ báo 1 lần mỗi method
                }
            }
        }

        // connectingMsg
        Lang.set(Lang.Language.EN);
        String enConn = Lang.connectingMsg("1.2.3.4");
        for (int i = 0; i < enConn.length(); i++) {
            char c = enConn.charAt(i);
            if (c > 0x7F) {
                failures.add("EN.connectingMsg() chứa non-ASCII U+"
                    + String.format("%04X", (int) c) + ": \"" + enConn + "\"");
                break;
            }
        }

        if (!failures.isEmpty()) {
            fail("[B] FAILURES:\n" + String.join("\n", failures));
        }
        System.out.println("[B] PASS – toàn bộ EN là ASCII thuần");
    }

    // =========================================================================
    // CA C – contrast ratio combo display field >= 4.5 (WCAG AA)
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("C – Shared theme/button contrast >= 4.5 (WCAG AA)")
    void c_comboContrastRatio() throws Exception {
        double normal = contrastRatio(Theme.DARK, Theme.CREAM);
        double primary = contrastRatio(Theme.BRASS, Theme.DARK);
        assertTrue(normal >= 4.5); assertTrue(primary >= 4.5);
        ThemedButton button = new ThemedButton(Lang.startGame(), ThemedButton.Style.PRIMARY);
        assertEquals(Theme.BRASS, button.fillColor());
        System.out.println("[C] Theme normal="+normal+" primary="+primary+" PASS");
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private List<Method> getNoArgStringMethods() {
        List<Method> result = new ArrayList<>();
        for (Method m : Lang.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())
                    && Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == String.class
                    && m.getParameterCount() == 0) {
                result.add(m);
            }
        }
        return result;
    }

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
}
