package game.view;

import java.util.prefs.Preferences;

/**
 * Lưu/đọc cài đặt người dùng (ngôn ngữ, âm thanh) qua java.util.prefs.Preferences.
 * Nhận Preferences node qua constructor để test có thể inject node riêng.
 */
public class AppSettings {
    private static final String KEY_LANG  = "language";
    private static final String KEY_SOUND = "soundEnabled";

    private final Preferences prefs;

    /** Constructor sản xuất — dùng node thật của ứng dụng. */
    public AppSettings() {
        this(Preferences.userNodeForPackage(AppSettings.class));
    }

    /** Constructor test — truyền node tùy ý để cách ly. */
    public AppSettings(Preferences prefs) {
        this.prefs = prefs;
    }

    public void saveLang(Lang.Language lang) {
        try { prefs.put(KEY_LANG, lang.name()); } catch (Exception ignored) {}
    }

    public Lang.Language loadLang() {
        try {
            String v = prefs.get(KEY_LANG, Lang.Language.VI.name());
            return Lang.Language.valueOf(v);
        } catch (Exception ignored) {
            return Lang.Language.VI;
        }
    }

    public void saveSound(boolean enabled) {
        try { prefs.putBoolean(KEY_SOUND, enabled); } catch (Exception ignored) {}
    }

    public boolean loadSound() {
        try { return prefs.getBoolean(KEY_SOUND, true); } catch (Exception ignored) { return true; }
    }

    /** Trả về node Preferences đang dùng (để test kiểm tra path). */
    public Preferences getPrefs() { return prefs; }
}
