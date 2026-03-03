package edu.usac.olc1.olc1_proyecto1.ui.theme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IconThemeManager {
    private static final Logger LOGGER = Logger.getLogger(IconThemeManager.class.getName());
    private static IconTheme theme;

    public static IconTheme getTheme() {
        if (theme == null) {
            loadTheme();
        }
        return theme;
    }

    private static void loadTheme() {
        try {
            InputStream is = IconThemeManager.class.getResourceAsStream(
                    "/edu/usac/olc1/olc1_proyecto1/theme/default-icon-theme.json"
            );
            if (is == null) {
                LOGGER.warning("Icon theme file not found: /edu/usac/olc1/olc1_proyecto1/theme/default-icon-theme.json");
                return;
            }
            Gson gson = new GsonBuilder().create();
            theme = gson.fromJson(new InputStreamReader(is), IconTheme.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading icon theme", e);
        }
    }
}
