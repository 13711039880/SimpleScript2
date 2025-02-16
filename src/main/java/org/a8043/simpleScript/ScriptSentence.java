package org.a8043.simpleScript;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptSentence {
    private final Script script;
    private final String run;
    private final String argsString;
    private final List<Object> args = new ArrayList<>();

    @Contract(pure = true)
    public ScriptSentence(Script script, @NotNull String run, @NotNull String argsString) {
        this.script = script;
        this.argsString = argsString;

        while (true) {
            if (run.startsWith(" ")) {
                run = run.substring(1);
            } else {
                break;
            }
        }
        this.run = run;

        if (!argsString.equals("")) {
            String[] argsChars = argsString.split("");
            boolean isInString = false;
            boolean isInSonScript = false;
            int argStart = 1;
            int charIndex = 0;
            int sonScriptStart = 0;
            for (String aChar : argsChars) {
                if (aChar.equals("\"")) {
                    isInString = !isInString;
                }
                if (aChar.equals("{") && !isInString) {
                    sonScriptStart = charIndex + 1;
                    isInSonScript = true;
                }
                if (aChar.equals("}") && !isInString) {
                    isInSonScript = false;
                }
                if ((charIndex + 1 == argsChars.length || argsChars[charIndex + 1].equals(","))
                    && !isInString && !isInSonScript) {
                    if (aChar.equals("\"")) {
                        args.add(argsString.substring(argStart, charIndex));
                    } else if (aChar.equals("}")) {
                        args.add(new Script(argsString.substring(sonScriptStart, charIndex)));
                    } else {
                        String substring = argsString.substring(argStart - 1, charIndex + 1);
                        switch (substring) {
                            case "true" -> args.add(true);
                            case "false" -> args.add(false);
                            case "null" -> args.add(null);
                            default -> {
                                try {
                                    args.add(Double.parseDouble(substring));
                                } catch (NumberFormatException e) {
                                    throw new WrongTypeException("不正确的类型: %s".formatted(substring));
                                }
                            }
                        }
                    }
                    argStart = charIndex + 1;
                }
                charIndex++;
            }
        }
    }

    public void run() {
        AtomicBoolean isFinishRun = new AtomicBoolean(false);
        script.getMethodList().forEach(method -> {
            if (method.getName().equals(run)) {
                isFinishRun.set(true);
                method.run(args.toArray());
            }
        });
        if (!isFinishRun.get()) {
            try {
                Method method = ScriptRunner.class.getMethod(run, Object[].class);
                method.invoke(script.getRunner(), (Object) args.toArray());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("找不到方法: %s(%s)".formatted(run, e.getMessage()));
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("运行方法失败: %s(%s)".formatted(run, e.getMessage()));
            }
        }
    }
}
