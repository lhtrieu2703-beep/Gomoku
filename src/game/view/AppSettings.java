package game.view;

import java.util.prefs.Preferences;
import java.text.Normalizer;

/**
 * Lưu/đọc cài đặt người dùng (ngôn ngữ, âm thanh, tên) qua java.util.prefs.Preferences.
 * Nhận Preferences node qua constructor để test có thể inject node riêng.
 */
public class AppSettings {
    private static final String KEY_LANG  = "language";
    private static final String KEY_SOUND = "soundEnabled";
    private static final String KEY_PLAYER_NAME = "playerName";
    public static final int MAX_PLAYER_NAME_LENGTH = 16;

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

    /** Empty means use Lang.defaultPlayerName() at display time, in the current locale. */
    public void savePlayerName(String name) {
        try {
            String normalized = normalizePlayerName(name);
            if (normalized.isEmpty()) prefs.remove(KEY_PLAYER_NAME);
            else prefs.put(KEY_PLAYER_NAME, normalized);
        } catch (Exception ignored) {}
    }

    public String loadPlayerName() {
        try { return normalizePlayerName(prefs.get(KEY_PLAYER_NAME, "")); }
        catch (Exception ignored) { return ""; }
    }

    /** Count Unicode code points after NFC normalization; never split a surrogate pair. */
    public static String normalizePlayerName(String name) {
        if (name == null) return "";
        StringBuilder singleLine = new StringBuilder();
        name.codePoints().forEach(cp -> singleLine.appendCodePoint(
                Character.isWhitespace(cp) || Character.isSpaceChar(cp) || Character.isISOControl(cp) ? ' ' : cp));
        String normalized = Normalizer.normalize(singleLine.toString().strip(), Normalizer.Form.NFC);
        if (normalized.codePointCount(0, normalized.length()) > MAX_PLAYER_NAME_LENGTH)
            normalized = normalized.substring(0, normalized.offsetByCodePoints(0, MAX_PLAYER_NAME_LENGTH)).stripTrailing();
        return normalized;
    }

    /** Trả về node Preferences đang dùng (để test kiểm tra path). */
    public Preferences getPrefs() { return prefs; }
}
