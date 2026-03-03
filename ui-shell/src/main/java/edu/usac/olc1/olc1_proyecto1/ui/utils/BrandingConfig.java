package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Properties;

public final class BrandingConfig {
    private static final String BRANDING_FILE = "/edu/usac/olc1/olc1_proyecto1/branding.properties";
    private static final String DEFAULT_APP_NAME = "LexCupStudio";
    private static final String DEFAULT_LOGO_PATH = "/edu/usac/olc1/olc1_proyecto1/icons/logo.png";

    private static final Properties PROPERTIES = load();

    private BrandingConfig() {
    }

    public static String getAppName() {
        String appName = PROPERTIES.getProperty("app.name");
        if (appName == null || appName.isBlank()) {
            return DEFAULT_APP_NAME;
        }
        return appName.trim();
    }

    public static String getLogoPath() {
        String logoPath = PROPERTIES.getProperty("app.logo.path");
        if (logoPath == null || logoPath.isBlank()) {
            return DEFAULT_LOGO_PATH;
        }
        return logoPath.trim();
    }

    public static Image getLogoImage() {
        String configuredPath = getLogoPath();
        InputStream configuredStream = BrandingConfig.class.getResourceAsStream(configuredPath);
        if (configuredStream != null) {
            return new Image(configuredStream);
        }

        InputStream defaultStream = BrandingConfig.class.getResourceAsStream(DEFAULT_LOGO_PATH);
        if (defaultStream != null) {
            return new Image(defaultStream);
        }

        return null;
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = BrandingConfig.class.getResourceAsStream(BRANDING_FILE)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (Exception ignored) {
        }
        return properties;
    }
}
