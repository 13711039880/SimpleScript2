package org.a8043.simpleScript;

import cn.hutool.core.io.FileUtil;
import lombok.Getter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Script {
    private String scriptString;
    @Getter
    private final List<ScriptMethod> methodList = new ArrayList<>();
    @Getter
    private ScriptRunner runner;
    private final List<ScriptVariable> variableList = new ArrayList<>();

    public Script(File file) {
        String originalString = FileUtil.readString(file, StandardCharsets.UTF_8);
        scriptString = originalString.replace("\r\n", "");
        scriptString = originalString.replace("\n", "");
        init();
    }

    public Script(String scriptString) {
        this.scriptString = scriptString;
        init();
    }

    private void init() {
        String[] scriptStringChars = scriptString.split("");

        boolean isInString = false;
        boolean isMethodDescribe = false;
        boolean isInMethod = false;
        boolean isInSonScript = false;
        int inMethodStart = 0;
        int methodDescribeStart = 0;

        int charIndex = 0;
        for (String aChar : scriptString.split("")) {
            if (!isInString) {
                if (aChar.equals("\"")) {
                    isInString = true;
                } else {
                    if (aChar.equals("{")) {
                        isInSonScript = true;
                    }
                    if (aChar.equals("}")) {
                        isInSonScript = false;
                    }
                    if (!isInSonScript) {
                        if (!isInMethod) {
                            if (aChar.equals("#")) {
                                if (isMethodDescribe) {
                                    isInMethod = true;
                                    inMethodStart = charIndex + 1;
                                    isMethodDescribe = false;
                                } else {
                                    methodDescribeStart = charIndex + 1;
                                    isMethodDescribe = true;
                                }
                            }
                        } else {
                            if (charIndex == scriptStringChars.length - 1
                                || scriptStringChars[charIndex + 1].equals("#")) {
                                ScriptMethod method =
                                    new ScriptMethod(this,
                                        scriptString.substring(methodDescribeStart, inMethodStart - 1),
                                        scriptString.substring(inMethodStart, charIndex + 1));
                                methodList.add(method);
                                isInMethod = false;
                            }
                        }
                    }
                }
            } else {
                if (aChar.equals("\"")) {
                    isInString = false;
                }
            }

            charIndex++;
        }

        runner = new ScriptRunner(this);
    }

    public void runMain(String[] args) {
        methodList.forEach(method -> {
            if (method.getName().equals("main")) {
                method.run((Object) args);
            }
        });
    }

    public void run(String methodName, Object... args) {
        methodList.forEach(method -> {
            if (method.getName().equals(methodName)) {
                method.run((Object) args);
            }
        });
    }

    public void addVariable(String name) {
        variableList.add(new ScriptVariable(name));
    }

    public void setVariable(String name, Object value) {
        variableList.forEach(variable -> {
            if (variable.getName().equals(name)) {
                variable.setValue(value);
            }
        });
    }
}
