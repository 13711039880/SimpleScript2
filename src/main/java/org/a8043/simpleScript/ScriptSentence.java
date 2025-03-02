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
import java.util.concurrent.atomic.AtomicReference;

import static org.a8043.simpleScript.Main.LIBRARIES_RUNNER_LIST;

public class ScriptSentence {
    private Script script;
    private String run;
    private List<Object> args = new ArrayList<>();

    public void init(Script script, @NotNull String run, @NotNull String argsString) {
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
                List<Boolean> isInSonSentence = new ArrayList<>();
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
                    if (aChar.equals("(") && !isInString && !isInSonScript) {
                        isInSonSentence.add(true);
                    }
                    if (aChar.equals(")") && !isInString && !isInSonScript) {
                        isInSonSentence.remove(isInSonSentence.size() - 1);
                    }
                    if (aChar.equals(",") && !isInString && !isInSonScript && isInSonSentence.isEmpty()) {
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
                List<Boolean> isInSonSentence = new ArrayList<>();
                int argStart = 1;
                int charIndex = 0;
                int sonScriptStart = 0;
                List<Integer> sonSentenceStart = new ArrayList<>();
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
                    if (aChar.equals("(") && !isInString && !isInSonScript) {
                        sonSentenceStart.add(charIndex + 1);
                        isInSonSentence.add(true);
                    }
                    if (aChar.equals(")") && !isInString && !isInSonScript) {
                        isInSonSentence.remove(isInSonSentence.size() - 1);
                    }
                    if ((charIndex + 1 == argsChars.length || argsChars[charIndex + 1].equals(","))
                        && !isInString && !isInSonScript && isInSonSentence.isEmpty()) {
                        if (aChar.equals("\"")) {
                            args.add(arg.substring(argStart, charIndex));
                        } else if (aChar.equals("}")) {
                            args.add(new Script(arg.substring(sonScriptStart, charIndex)));
                        } else if (aChar.equals(")")) {
                            String sentence = arg.substring(sonSentenceStart.get(0), charIndex);
                            args.add(new ScriptSentence(script, sentence));
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

    public ScriptSentence(Script script, @NotNull String sentence) {
        String[] sentenceChars = sentence.split("");
        boolean isInString = false;
        boolean isInSonScript = false;
        List<Boolean> isInSonSentence = new ArrayList<>();
        int argsStart = 0;

        int charIndex = 0;
        for (String aChar : sentenceChars) {
            if (aChar.equals("\"")) {
                isInString = !isInString;
            }
            if (aChar.equals("{") && !isInString) {
                isInSonScript = true;
            }
            if (aChar.equals("}") && !isInString) {
                isInSonScript = false;
            }
            if (aChar.equals("(") && !isInString && !isInSonScript) {
                isInSonSentence.add(true);
            }
            if (aChar.equals(")") && !isInString && !isInSonScript) {
                isInSonSentence.remove(isInSonSentence.size() - 1);
            }
            if (aChar.equals("(") && !isInString && !isInSonScript && isInSonSentence.size() == 1) {
                argsStart = charIndex + 1;
            } else if (aChar.equals(")") && !isInString && !isInSonScript && isInSonSentence.isEmpty()) {
                String argsString = sentence.substring(argsStart, charIndex);
                init(script, sentence.substring(0, argsStart - 1), argsString);
            }
            charIndex++;
        }
    }

    public Object run() {
        List<Object> newArgs = new ArrayList<>();
        args.forEach(arg -> {
            if (arg instanceof ScriptSentence sentence) {
                newArgs.add(sentence.run());
            } else {
                newArgs.add(arg);
            }
        });
        args = newArgs;

        AtomicReference<Object> returnValue = new AtomicReference<>();
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
            Method method = null;
            String methodName;
            if (runArray.length == 1) {
                methodName = runArray[0];
                Object obj = args.get(0);
                Class<?> clazz = obj.getClass();
                for (Method aMethod : clazz.getMethods()) {
                    if (aMethod.getName().equals(methodName)) {
                        method = aMethod;
                    }
                }
            } else {
                String className = newRun.substring(0, newRun.length() - runArray[runArray.length - 1].length() - 1);
                methodName = runArray[runArray.length - 1];
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                for (Method aMethod : clazz.getMethods()) {
                    if (aMethod.getName().equals(methodName)) {
                        method = aMethod;
                    }
                }
            }
            if (method == null) {
                throw new RuntimeException("找不到方法: " + methodName);
            }
            try {
                returnValue.set(method.invoke(args.get(0), args.subList(1, args.size()).toArray()));
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
                                returnValue.set(method.invoke(instance, (Object) args.toArray()));
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

        return returnValue.get();
    }
}
