package org.a8043.simpleScript;

import org.a8043.simpleScript.exceptions.WrongTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.a8043.simpleScript.Main.LIBRARIES_RUNNER_LIST;

public class ScriptSentence {
    private final Script script;
    private final String run;
    private final List<Object> args = new ArrayList<>();

    @Contract(pure = true)
    public ScriptSentence(Script script, @NotNull String run, @NotNull String argsString) {
        this.script = script;

        while (true) {
            if (run.startsWith(" ")) {
                run = run.substring(1);
            } else {
                break;
            }
        }
        this.run = run;

        if (!argsString.isEmpty()) {
            List<String> argsStringList = new ArrayList<>();
            {
                int argStart = 0;
                boolean isInString = false;
                boolean isInSonScript = false;
                int charIndex = 0;
                for (String aChar : argsString.split("")) {
                    if (aChar.equals("\"")) {
                        isInString = !isInString;
                    }
                    if (aChar.equals("{") && !isInString) {
                        isInSonScript = true;
                    }
                    if (aChar.equals("}") && !isInString) {
                        isInSonScript = false;
                    }
                    if (aChar.equals(",") && !isInString && !isInSonScript) {
                        argsStringList.add(argsString.substring(argStart, charIndex));
                        argStart = charIndex + 1;
                    }
                    charIndex++;
                }
                argsStringList.add(argsString.substring(argStart, charIndex));
            }

            argsStringList.forEach(arg -> {
                String[] argsChars = arg.split("");
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
                            args.add(arg.substring(argStart, charIndex));
                        } else if (aChar.equals("}")) {
                            args.add(new Script(arg.substring(sonScriptStart, charIndex)));
                        } else if (aChar.equals("0") || aChar.equals("1") || aChar.equals("2")
                                   || aChar.equals("3") || aChar.equals("4") || aChar.equals("5")
                                   || aChar.equals("6") || aChar.equals("7") || aChar.equals("8")
                                   || aChar.equals("9")) {
                            try {
                                args.add(Double.parseDouble(arg));
                            } catch (NumberFormatException e) {
                                throw new WrongTypeException("不正确的类型: " + arg);
                            }
                        } else {
                            switch (arg) {
                                case "true" -> args.add(true);
                                case "false" -> args.add(false);
                                case "null" -> args.add(null);
                                default -> args.add(script.getVariable(arg));
                            }
                        }
                        argStart = charIndex + 1;
                    }
                    charIndex++;
                }
            });
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
        if (run.startsWith("*")) {
            String newRun = run.substring(1);
            String[] runArray = newRun.split("\\.");
            String className = newRun.substring(0, newRun.length() - runArray[runArray.length - 1].length() - 1);
            String methodName = runArray[runArray.length - 1];

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            Method method = null;
            for (Method aMethod : clazz.getMethods()) {
                if (aMethod.getName().equals(methodName)) {
                    method = aMethod;
                }
            }
            if (method == null) {
                throw new RuntimeException("找不到方法: " + methodName);
            }

            try {
                method.invoke(args.get(0), args.subList(1, args.size()).toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            String newRun = run.replace(".", "_");
            if (!isFinishRun.get()) {
                if (Arrays.stream(ScriptRunner.class.getMethods()).anyMatch(method -> method.getName().equals(newRun))) {
                    try {
                        Method method = ScriptRunner.class.getMethod(newRun, Object[].class);
                        method.invoke(script.getRunner(), (Object) args.toArray());
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    AtomicBoolean isFinishRunLibrary = new AtomicBoolean(false);
                    LIBRARIES_RUNNER_LIST.forEach(library -> {
                        if (Arrays.stream(library.getMethods()).anyMatch(method -> method.getName().equals(newRun))) {
                            try {
                                Method method = library.getMethod(newRun, Object[].class);
                                Object instance = library.getDeclaredConstructor(Script.class).newInstance(script);
                                method.invoke(instance, (Object) args.toArray());
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                                     IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            isFinishRunLibrary.set(true);
                        }
                    });
                    if (!isFinishRunLibrary.get()) {
                        throw new RuntimeException(new NoSuchMethodException("找不到方法: " + newRun));
                    }
                }
            }
        }
    }
}
