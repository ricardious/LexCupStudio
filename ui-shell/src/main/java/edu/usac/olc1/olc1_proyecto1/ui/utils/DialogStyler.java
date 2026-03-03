package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.control.Dialog;

import java.net.URL;

public final class DialogStyler {
    private static final String MAIN_STYLESHEET = "/edu/usac/olc1/olc1_proyecto1/css/main.css";

    private DialogStyler() {
    }

    public static void apply(Dialog<?> dialog) {
        if (dialog == null || dialog.getDialogPane() == null) {
            return;
        }

        URL cssUrl = DialogStyler.class.getResource(MAIN_STYLESHEET);
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!dialog.getDialogPane().getStylesheets().contains(css)) {
                dialog.getDialogPane().getStylesheets().add(css);
            }
        }

        if (!dialog.getDialogPane().getStyleClass().contains("app-dialog")) {
            dialog.getDialogPane().getStyleClass().add("app-dialog");
        }
    }
}
