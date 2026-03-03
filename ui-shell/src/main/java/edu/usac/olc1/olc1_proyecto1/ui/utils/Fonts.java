package edu.usac.olc1.olc1_proyecto1.ui.utils;

import java.util.Objects;

public final class Fonts {
    public static String ttf(String name) {
        String fullName = name.endsWith(".ttf") ? name : name + ".ttf";
        return Objects.requireNonNull(
                Fonts.class.getResource("/edu/usac/olc1/olc1_proyecto1/fonts/ttf/" + fullName)
        ).toExternalForm();
    }
}