package org.a8043.simpleScript;

import cn.hutool.core.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class ScriptRunner {
    private final Script script;

    @Contract(pure = true)
    public ScriptRunner(Script script) {
        this.script = script;
    }

    public void println(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        System.out.println(args[0]);
    }

    public void print(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        System.out.print(args[0]);
    }

    public void newThread(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof Script script)) {
            throw new WrongTypeException("参数类型不正确");
        }
        new Thread(() -> script.run("run")).start();
    }

    public void sleep(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        ThreadUtil.sleep(Double.parseDouble(args[0].toString()));
    }

    public void math_addition(Object @NotNull ... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        Object[] addCount = Arrays.copyOfRange(args, 1, args.length);
        int i = 0;
        for (Object count : addCount) {
            i += Integer.parseInt(count.toString());
        }
    }
}
