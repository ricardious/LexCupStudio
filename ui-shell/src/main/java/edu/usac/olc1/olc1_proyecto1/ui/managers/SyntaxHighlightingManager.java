package edu.usac.olc1.olc1_proyecto1.ui.managers;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SyntaxHighlightingManager {

    private static final String[] KEYWORDS = new String[]{
            "afd", "ap", "states", "alphabet", "stack", "initial", "accept",
            "transitions", "desc", "validate", "run", "with", "input", "steps",
            "show", "ver", "automatas", "true", "false", "null"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\\n]*|/\\*(.|\\R)*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+(?:\\.\\d+)?\\b";
    private static final String OPERATOR_PATTERN = "->|==|!=|<=|>=|\\+|-|\\*|/|=|:|;|,|\\.";
    private static final String DELIMITER_PATTERN = "[(){}\\[\\]]";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
                    + "|(?<DELIMITER>" + DELIMITER_PATTERN + ")"
    );

    public Subscription bind(CodeArea codeArea) {
        applyHighlighting(codeArea);
        return codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(120))
                .subscribe(ignore -> applyHighlighting(codeArea));
    }

    private void applyHighlighting(CodeArea codeArea) {
        String text = codeArea.getText();
        codeArea.setStyleSpans(0, computeHighlighting(text));
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "syntax-keyword" :
                    matcher.group("STRING") != null ? "syntax-string" :
                    matcher.group("COMMENT") != null ? "syntax-comment" :
                    matcher.group("NUMBER") != null ? "syntax-number" :
                    matcher.group("OPERATOR") != null ? "syntax-operator" :
                    matcher.group("DELIMITER") != null ? "syntax-delimiter" :
                    null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }
}
