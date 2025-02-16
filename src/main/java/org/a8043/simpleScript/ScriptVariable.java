package org.a8043.simpleScript;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

public class ScriptVariable {
    @Getter
    @Setter
    private Object value;
    @Getter
    private final String name;

    @Contract(pure = true)
    public ScriptVariable(String name) {
        this.name = name;
    }
}
