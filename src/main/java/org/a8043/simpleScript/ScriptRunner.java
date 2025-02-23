package org.a8043.simpleScript;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import org.a8043.simpleUtil.util.Config;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.a8043.simpleScript.Main.LIBRARIES_RUNNER_LIST;

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
}
