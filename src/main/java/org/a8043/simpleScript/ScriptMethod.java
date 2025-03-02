package org.a8043.simpleScript;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScriptMethod {
    private final Script script;
    private final String methodDescribe;
    @Getter
    private final String name;
    private final String body;
    private final String[] bodyChars;
    private final List<String> sentenceStringList = new ArrayList<>();
    private final String[] args;

    public ScriptMethod(Script script, @NotNull String methodDescribe, @NotNull String body) {
        this.script = script;
        this.methodDescribe = methodDescribe;
        this.body = body;
        bodyChars = body.split("");
        String[] nameAndArgs = methodDescribe.split(" ", 2);
        name = nameAndArgs[0];
        if (nameAndArgs.length == 1) {
            args = new String[0];
        } else {
            args = nameAndArgs[1].split(",");
        }

        boolean isInString = false;
        boolean isInSonScript = false;
        int sentenceStart = 0;
        int charIndex = 0;
        for (String aChar : bodyChars) {
            if (aChar.equals("\"")) {
                isInString = !isInString;
            }
            if (aChar.equals("{") && !isInString) {
                isInSonScript = true;
            }
            if (aChar.equals("}") && !isInString) {
                isInSonScript = false;
            }
            if (aChar.equals(";") && !isInString && !isInSonScript) {
                sentenceStringList.add(body.substring(sentenceStart, charIndex));
                sentenceStart = charIndex + 1;
            }
            charIndex++;
        }
    }

    public void run(Object @NotNull ... runArgs) {
        int i = 0;
        for (Object arg : runArgs) {
            script.addVariable(args[i]);
            script.setVariable(args[i], arg);
            i++;
        }
        List<ScriptSentence> sentenceList = new ArrayList<>();
        sentenceStringList.forEach(sentence -> sentenceList.add(new ScriptSentence(script, sentence)));
        sentenceList.forEach(ScriptSentence::run);
    }
}
