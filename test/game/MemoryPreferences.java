package game;

import java.util.*;
import java.util.prefs.AbstractPreferences;

/** No calls to the platform Preferences factory or registry. */
final class MemoryPreferences extends AbstractPreferences {
    private final Map<String,String> values = new HashMap<>();
    private final Map<String,MemoryPreferences> children = new HashMap<>();
    MemoryPreferences() { super(null, ""); }
    private MemoryPreferences(MemoryPreferences parent, String name) { super(parent,name); }
    protected void putSpi(String key,String value) { values.put(key,value); }
    protected String getSpi(String key) { return values.get(key); }
    protected void removeSpi(String key) { values.remove(key); }
    protected void removeNodeSpi() {
        values.clear(); children.clear();
        if (parent() instanceof MemoryPreferences parent) parent.children.remove(name());
    }
    protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
    protected String[] childrenNamesSpi() { return children.keySet().toArray(String[]::new); }
    protected AbstractPreferences childSpi(String name) { return children.computeIfAbsent(name,n -> new MemoryPreferences(this,n)); }
    protected void syncSpi() {}
    protected void flushSpi() {}
}
