package org.a8043.simpleScript;

import cn.hutool.core.thread.ThreadUtil;
import org.a8043.simpleScript.exceptions.WrongTypeException;
import org.a8043.simpleScript.runnerAnnotation.ArgLength;
import org.a8043.simpleScript.runnerAnnotation.ReplaceVariable;
import org.a8043.simpleUtil.util.Config;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static org.a8043.simpleScript.Main.LIBRARIES_RUNNER_LIST;

@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class ScriptRunner {
    private final Script script;

    @Contract(pure = true)
    public ScriptRunner(Script script) {
        this.script = script;
    }

    @ReplaceVariable("all")
    @ArgLength(1)
    public void println(Object @NotNull ... args) {
        System.out.println(args[0]);
    }

    @ReplaceVariable("all")
    @ArgLength(1)
    public void print(Object @NotNull ... args) {
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
        ThreadUtil.sleep(Integer.parseInt(args[0].toString()));
    }

    public void loadLibrary(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof String path)) {
            throw new WrongTypeException("参数类型不正确");
        }

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(path).toURI().toURL()});
             InputStream libraryYamlInputStream = classLoader.getResourceAsStream("library.yaml")) {
            Config libraryYaml = new Config(new Yaml().load(libraryYamlInputStream));
            List<Object> loadClassesList = libraryYaml.getList("loadClasses");
            if (loadClassesList != null) {
                loadClassesList.forEach(loadClass -> {
                    try {
                        classLoader.loadClass(loadClass.toString());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            libraryYaml.getList("runners").forEach(library -> {
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(library.toString());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                LIBRARIES_RUNNER_LIST.add(clazz);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void math_addition(Object @NotNull ... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        Object[] addCount = Arrays.copyOfRange(args, 1, args.length);
        int i = 0;
        for (Object count : addCount) {
            i += Integer.parseInt(count.toString());
        }
        variable.setValue(i);
    }

    public void newVariable(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof String name)) {
            throw new WrongTypeException("参数类型不正确");
        }
        script.addVariable(name);
    }

    public void frame_new(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        variable.setValue(new JFrame());
    }

    public void frame_setTitle(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof String title)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JFrame) variable.getValue()).setTitle(title);
    }

    public void frame_setSize(Object @NotNull ... args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof Integer width)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[2] instanceof Integer height)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JFrame) variable.getValue()).setSize(width, height);
    }

    public void frame_add(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof ScriptVariable componentVariable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JFrame) variable.getValue()).add((Component) componentVariable.getValue());
    }

    public void frame_setVisible(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof Boolean visible)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JFrame) variable.getValue()).setVisible(visible);
    }

    public void frame_button_new(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        variable.setValue(new JButton());
    }

    public void frame_button_setText(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof String text)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JButton) variable.getValue()).setText(text);
    }

    public void frame_button_addActionListener(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        if (!(args[1] instanceof Script actionListener)) {
            throw new WrongTypeException("参数类型不正确");
        }
        ((JButton) variable.getValue()).addActionListener(e ->
            actionListener.run("actionPerformed", e));
        new JPanel().setLayout(new FlowLayout());
    }

    public void frame_textField_new(Object @NotNull ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        variable.setValue(new JTextField());
    }

    public Object newObj(Object @NotNull ... args) {
        ScriptVariable variable;
        String className;
        if (args.length == 2) {
            if (!(args[0] instanceof ScriptVariable variable1)) {
                throw new WrongTypeException("参数类型不正确");
            }
            if (!(args[1] instanceof String className1)) {
                throw new WrongTypeException("参数类型不正确");
            }
            variable = variable1;
            className = className1;
        } else if (args.length == 1) {
            if (!(args[0] instanceof String className1)) {
                throw new WrongTypeException("参数类型不正确");
            }
            variable = new ScriptVariable("");
            className = className1;
        } else {
            throw new WrongTypeException("参数类型不正确");
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object obj;
        try {
            obj = clazz.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        variable.setValue(obj);

        return obj;
    }

    public void setVariable(Object @NotNull ... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("参数长度不正确");
        }
        if (!(args[0] instanceof ScriptVariable variable)) {
            throw new WrongTypeException("参数类型不正确");
        }
        variable.setValue(args[1]);
    }
}
