package org.a8043.simpleScript;

import java.util.HashMap;
import java.util.Map;

public class ScriptRecorder {
    private final Map<String, Object> map = new HashMap<>();

    public ScriptRecorder() {
    }

    public void add(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }
}
