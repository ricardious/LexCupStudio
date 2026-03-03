package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiColorParser {
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[(\\d+)(;\\d+)*m");

    private static final Map<Integer, Color> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(30, Color.BLACK);
        COLOR_MAP.put(31, Color.RED);
        COLOR_MAP.put(32, Color.GREEN);
        COLOR_MAP.put(33, Color.YELLOW);
        COLOR_MAP.put(34, Color.BLUE);
        COLOR_MAP.put(35, Color.MAGENTA);
        COLOR_MAP.put(36, Color.CYAN);
        COLOR_MAP.put(37, Color.WHITE);
        COLOR_MAP.put(90, Color.GRAY);
        COLOR_MAP.put(91, Color.LIGHTPINK);
        COLOR_MAP.put(92, Color.LIGHTGREEN);
        COLOR_MAP.put(93, Color.LIGHTYELLOW);
        COLOR_MAP.put(94, Color.LIGHTBLUE);
        COLOR_MAP.put(95, Color.LIGHTPINK);
        COLOR_MAP.put(96, Color.LIGHTCYAN);
        COLOR_MAP.put(97, Color.WHITE);
    }

    public static TextFlow parseAnsiString(String ansiText, Color defaultColor) {
        TextFlow textFlow = new TextFlow();
        List<Text> textSegments = new ArrayList<>();

        Color currentColor = defaultColor;
        boolean isBold = false;
        boolean isItalic = false;

        Matcher matcher = ANSI_PATTERN.matcher(ansiText);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textSegment = ansiText.substring(lastEnd, matcher.start());
                Text text = new Text(textSegment);
                text.setFill(currentColor);
                if (isBold) {
                    text.setStyle("-fx-font-weight: bold;");
                }
                if (isItalic) {
                    text.setStyle(text.getStyle() + "-fx-font-style: italic;");
                }
                textSegments.add(text);
            }

            String codeStr = matcher.group(1);
            int code = Integer.parseInt(codeStr);

            if (code == 0) {
                currentColor = defaultColor;
                isBold = false;
                isItalic = false;
            } else if (code == 1) {
                isBold = true;
            } else if (code == 3) {
                isItalic = true;
            } else if (code >= 30 && code <= 37) {
                currentColor = COLOR_MAP.get(code);
            } else if (code >= 90 && code <= 97) {
                currentColor = COLOR_MAP.get(code);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < ansiText.length()) {
            String textSegment = ansiText.substring(lastEnd);
            Text text = new Text(textSegment);
            text.setFill(currentColor);
            if (isBold) {
                text.setStyle("-fx-font-weight: bold;");
            }
            if (isItalic) {
                text.setStyle(text.getStyle() + "-fx-font-style: italic;");
            }
            textSegments.add(text);
        }

        textFlow.getChildren().addAll(textSegments);
        return textFlow;
    }
}