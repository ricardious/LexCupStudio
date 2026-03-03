package edu.usac.olc1.olc1_proyecto1.ui.managers;

import io.lexcupstudio.ui.api.SourceDiagnostic;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SyntaxHighlightingManager {
    private static final String ERROR_STYLE = "syntax-error";

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
    private final Map<CodeArea, List<ErrorRange>> errorRanges = new IdentityHashMap<>();

    public Subscription bind(CodeArea codeArea) {
        applyHighlighting(codeArea);
        return codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(120))
                .subscribe(ignore -> applyHighlighting(codeArea));
    }

    public void setDiagnostics(CodeArea codeArea, List<SourceDiagnostic> diagnostics) {
        if (codeArea == null) {
            return;
        }
        String text = codeArea.getText();
        List<ErrorRange> ranges = new ArrayList<>();
        if (diagnostics != null) {
            for (SourceDiagnostic diagnostic : diagnostics) {
                int start = offsetForLineColumn(text, diagnostic.getLine(), diagnostic.getColumn());
                int length = Math.max(1, diagnostic.getLength());
                if (start >= text.length()) {
                    continue;
                }
                int safeLength = Math.min(length, text.length() - start);
                ranges.add(new ErrorRange(start, safeLength));
            }
        }
        errorRanges.put(codeArea, ranges);
        applyHighlighting(codeArea);
    }

    public void clearDiagnostics(CodeArea codeArea) {
        if (codeArea == null) {
            return;
        }
        errorRanges.remove(codeArea);
        applyHighlighting(codeArea);
    }

    private void applyHighlighting(CodeArea codeArea) {
        String text = codeArea.getText();
        List<ErrorRange> ranges = errorRanges.getOrDefault(codeArea, List.of());
        codeArea.setStyleSpans(0, computeHighlighting(text, ranges));
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text, List<ErrorRange> ranges) {
        if (text.isEmpty()) {
            return new StyleSpansBuilder<Collection<String>>().add(List.of(), 0).create();
        }

        List<Set<String>> stylesByChar = new ArrayList<>(text.length());
        for (int i = 0; i < text.length(); i++) {
            stylesByChar.add(new HashSet<>());
        }

        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "syntax-keyword" :
                    matcher.group("STRING") != null ? "syntax-string" :
                    matcher.group("COMMENT") != null ? "syntax-comment" :
                    matcher.group("NUMBER") != null ? "syntax-number" :
                    matcher.group("OPERATOR") != null ? "syntax-operator" :
                    matcher.group("DELIMITER") != null ? "syntax-delimiter" :
                    null;
            if (styleClass == null) {
                continue;
            }
            for (int i = matcher.start(); i < matcher.end(); i++) {
                stylesByChar.get(i).add(styleClass);
            }
        }

        for (ErrorRange range : ranges) {
            int start = Math.max(0, range.start());
            int end = Math.min(text.length(), range.start() + range.length());
            for (int i = start; i < end; i++) {
                stylesByChar.get(i).add(ERROR_STYLE);
            }
        }

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        Set<String> current = stylesByChar.getFirst();
        int spanStart = 0;
        for (int i = 1; i < stylesByChar.size(); i++) {
            Set<String> next = stylesByChar.get(i);
            if (!current.equals(next)) {
                spansBuilder.add(toCollection(current), i - spanStart);
                current = next;
                spanStart = i;
            }
        }
        spansBuilder.add(toCollection(current), stylesByChar.size() - spanStart);
        return spansBuilder.create();
    }

    private Collection<String> toCollection(Set<String> styles) {
        if (styles == null || styles.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(styles);
    }

    private int offsetForLineColumn(String text, int line, int column) {
        int targetLine = Math.max(1, line);
        int targetColumn = Math.max(1, column);

        int currentLine = 1;
        int currentColumn = 1;
        for (int i = 0; i < text.length(); i++) {
            if (currentLine == targetLine && currentColumn == targetColumn) {
                return i;
            }
            char c = text.charAt(i);
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
        }
        return text.length();
    }

    private record ErrorRange(int start, int length) {
    }
}
