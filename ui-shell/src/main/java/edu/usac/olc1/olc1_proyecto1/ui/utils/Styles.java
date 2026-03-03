package edu.usac.olc1.olc1_proyecto1.ui.utils;

import java.util.Objects;

public final class Styles {
    public static String forName(String name) {
        String fullName = name.endsWith(".css") ? name : name + ".css";
        return Objects.requireNonNull(
                Styles.class.getResource("/edu/usac/olc1/olc1_proyecto1/css/" + fullName)
        ).toExternalForm();
    }
}