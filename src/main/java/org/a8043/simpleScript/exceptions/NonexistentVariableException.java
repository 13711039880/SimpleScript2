package org.a8043.simpleScript.exceptions;

public class NonexistentVariableException extends RuntimeException {
    public NonexistentVariableException(String name) {
        super("不存在的变量: " + name);
    }
}
