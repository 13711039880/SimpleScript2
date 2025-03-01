package org.a8043.simpleScript.exceptions;

public class NonexistentVariable extends RuntimeException {
    public NonexistentVariable(String name) {
        super("不存在的变量: " + name);
    }
}
