package org.a8043.simpleScript;

import com.formdev.flatlaf.FlatLightLaf;
import org.a8043.docsViewer.DocsViewer;
import org.a8043.simpleUtil.util.ConsoleColor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static final String VERSION = "2.0";
    public static final List<Class<?>> LIBRARIES_RUNNER_LIST = new ArrayList<>();

    static {
        Thread.currentThread().setName("MainThread");
    }

    public static void main(String @NotNull [] args) {
        if (args.length == 0) {
            System.out.println("Simple Script");
            System.out.println("Version: " + VERSION);
            System.exit(0);
        }

        if (args[0].equals("-docs")) {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }
            DocsViewer docsViewer = new DocsViewer("SimpleScript文档", new URL[]{
                Main.class.getResource("/docs/introduce.md"),
                Main.class.getResource("/docs/grammar.md"),
                Main.class.getResource("/docs/canRun.md")
            });
            docsViewer.getMainFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                BufferedImage image =
                    ImageIO.read(Objects.requireNonNull(Main.class.getResource("/icon.png")));
                docsViewer.getMainFrame().setIconImage(image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            docsViewer.open();
            return;
        }

        File scriptFile = new File(args[0]);
        if (!scriptFile.exists()) {
            System.err.println(ConsoleColor.coloring("找不到脚本文件: " + scriptFile.getAbsolutePath(),
                ConsoleColor.RED));
            System.exit(1);
        }
        if (!scriptFile.getName().substring(scriptFile.getName().lastIndexOf('.') + 1)
            .equals("ss")) {
            System.err.println(ConsoleColor.coloring("不是脚本: " + scriptFile.getAbsolutePath(),
                ConsoleColor.RED));
            System.exit(1);
        }

        Script script = new Script(scriptFile);
        script.runMain(Arrays.copyOfRange(args, 1, args.length));
    }
}
