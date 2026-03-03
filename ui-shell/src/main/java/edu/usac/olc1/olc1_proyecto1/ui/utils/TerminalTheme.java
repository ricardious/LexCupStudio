package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.paint.Color;

public class TerminalTheme {
    private Color promptColor = Color.rgb(152, 195, 121);
    private Color dirColor = Color.rgb(97, 175, 239);
    private Color gitColor = Color.rgb(229, 192, 123);
    private Color textColor = Color.rgb(171, 178, 191);
    private Color errorColor = Color.rgb(224, 108, 117);
    private Color successColor = Color.rgb(152, 195, 121);
    private Color warningColor = Color.rgb(229, 192, 123);
    private Color bannerTitleColor = Color.rgb(229, 192, 123);
    private Color backgroundColor = Color.rgb(40, 44, 52);

    private String themeName = "dark";


    public TerminalTheme() {
    }

    public void setTheme(String name) {
        this.themeName = name;

        if ("light".equals(name)) {
            backgroundColor = Color.rgb(240, 240, 240);
            textColor = Color.rgb(30, 30, 30);
            promptColor = Color.rgb(0, 128, 0);
            dirColor = Color.rgb(0, 0, 255);
            gitColor = Color.rgb(128, 96, 0);
            errorColor = Color.rgb(200, 0, 0);
            successColor = Color.rgb(0, 128, 0);
            warningColor = Color.rgb(200, 150, 0);
            bannerTitleColor = Color.rgb(128, 96, 0);
        } else {
            backgroundColor = Color.rgb(40, 44, 52);
            textColor = Color.rgb(171, 178, 191);
            promptColor = Color.rgb(152, 195, 121);
            dirColor = Color.rgb(97, 175, 239);
            gitColor = Color.rgb(229, 192, 123);
            errorColor = Color.rgb(224, 108, 117);
            successColor = Color.rgb(152, 195, 121);
            warningColor = Color.rgb(229, 192, 123);
            bannerTitleColor = Color.rgb(229, 192, 123);
        }
    }

    public Color getPromptColor() {
        return promptColor;
    }

    public Color getDirColor() {
        return dirColor;
    }

    public Color getGitColor() {
        return gitColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public Color getSuccessColor() {
        return successColor;
    }

    public Color getWarningColor() {
        return warningColor;
    }

    public Color getBannerTitleColor() {
        return bannerTitleColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public String getThemeName() {
        return themeName;
    }
}