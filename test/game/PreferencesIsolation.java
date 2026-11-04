package game;

import game.view.AppSettings;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import static org.junit.jupiter.api.Assertions.*;

/** Reads production settings without writing; cleanup failures must fail the test. */
final class PreferencesIsolation {
    static final Preferences testRoot = new MemoryPreferences();
    static Map<String, String> snapshot() throws Exception {
        if (!Preferences.userRoot().nodeExists("/game/view")) return Map.of();
        Preferences node = Preferences.userRoot().node("/game/view");
        node.sync();
        Map<String, String> values = new TreeMap<>();
        for (String key : node.keys()) values.put(key, node.get(key, null));
        return values;
    }

    static void cleanup(String path, Map<String, String> before) throws Exception {
        Preferences root = testRoot;
        if (root.nodeExists(path)) root.node(path).removeNode();
        root.flush();
        assertFalse(root.nodeExists(path), "Test node must be removed");
        assertEquals(before, snapshot(), "Production preferences must remain unchanged");
        System.out.println("[Preferences] " + path
                + " RAM node removed; production /game/view unchanged: " + before);
    }
}
