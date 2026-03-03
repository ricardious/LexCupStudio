package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This class is responsible for generating ASCII banners for the terminal.
 * It supports multiple banner styles and can apply color gradients to the banners.
 */
public class BannerGenerator {
    private static final String[] BANNER_STYLES = {
            "OLC1"
    };

    private final Map<String, String> bannerTemplates;
    private final Random random;

    /**
     * Constructs a BannerGenerator and initializes all banner templates.
     */
    public BannerGenerator() {
        bannerTemplates = new HashMap<>();
        random = new Random();

        initializeBannerTemplates();
    }

    /**
     * Initializes all available banner templates.
     */
    private void initializeBannerTemplates() {
        // Standard banner
        bannerTemplates.put("Standard",
                "  _____  _ _                                ______                    \n" +
                        " |  __ \\(_) |                              |  ____|                   \n" +
                        " | |  | |_| | ___ _ __ ___  _ __ ___   __ _| |__ ___  _ __ __ _  ___ \n" +
                        " | |  | | | |/ _ \\ '_ ` _ \\| '_ ` _ \\ / _` |  __/ _ \\| '__/ _` |/ _ \\\n" +
                        " | |__| | | |  __/ | | | | | | | | | | (_| | | | (_) | | | (_| |  __/\n" +
                        " |_____/|_|_|\\___|_| |_| |_|_| |_| |_|\\__,_|_|  \\___/|_|  \\__, |\\___|\n" +
                        "                                                           __/ |     \n" +
                        "                                                          |___/      \n");

        // Fire banner
        bannerTemplates.put("Fire",
                "    ____  _ __                                ______                     \n" +
                        "   / __ \\(_) /__  ____ ___  ____ ___  ____ _/ ____/___  _________ ____ \n" +
                        "  / / / / / / _ \\/ __ `__ \\/ __ `__ \\/ __ `/ /_  / __ \\/ ___/ __ `/ _ \\\n" +
                        " / /_/ / / /  __/ / / / / / / / / / / /_/ / __/ / /_/ / /  / /_/ /  __/\n" +
                        "/_____/_/_/\\___/_/ /_/ /_/_/ /_/ /_/\\__,_/_/    \\____/_/   \\__, /\\___/ \n" +
                        "                                                          /____/       \n");

        // Digital banner
        bannerTemplates.put("Digital",
                "  _____  _____ _      ______ __  __ __  __          \n" +
                        " |  __ \\|_   _| |    |  ____|  \\/  |  \\/  |   /\\    \n" +
                        " | |  | | | | | |    | |__  | \\  / | \\  / |  /  \\   \n" +
                        " | |  | | | | | |    |  __| | |\\/| | |\\/| | / /\\ \\  \n" +
                        " | |__| |_| |_| |____| |____| |  | | |  | |/ ____ \\ \n" +
                        " |_____/|_____|______|______|_|  |_|_|  |_/_/    \\_\\\n" +
                        "  ______                                            \n" +
                        " |  ____|                                           \n" +
                        " | |__ ___  _ __ __ _  ___                          \n" +
                        " |  __/ _ \\| '__/ _` |/ _ \\                         \n" +
                        " | | | (_) | | | (_| |  __/                         \n" +
                        " |_|  \\___/|_|  \\__, |\\___|                         \n" +
                        "                 __/ |                              \n" +
                        "                |___/                               \n");

        // Poison banner
        bannerTemplates.put("Poison",
                "  ___    _   _                                  ___                       \n" +
                        " |   \\  (_) | |  ___   _ __    _ __    __ _    | __|  ___   _ _   __ _   ___\n" +
                        " | |) | | | | | / -_) | '  \\  | '  \\  / _` |   | _|  / _ \\ | '_| / _` | / -_)\n" +
                        " |___/  |_| |_| \\___| |_|_|_| |_|_|_| \\__,_|   |_|   \\___/ |_|   \\__, | \\___|\n" +
                        "                                                                 |___/       \n");

        // OLC1 banner
        bannerTemplates.put("OLC1",
                "_______/\\\\\\\\\\_______/\\\\\\____________________/\\\\\\\\\\\\\\\\\\______/\\\\\\_       \n" +
                        " _____/\\\\\\///\\\\\\____\\/\\\\\\_________________/\\\\\\////////___/\\\\\\\\\\\\\\_      \n" +
                        "  ___/\\\\\\/__\\///\\\\\\__\\/\\\\\\_______________/\\\\\\/___________\\/////\\\\\\_     \n" +
                        "   __/\\\\\\______\\//\\\\\\_\\/\\\\\\______________/\\\\\\_________________\\/\\\\\\_    \n" +
                        "    _\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\_____________\\/\\\\\\_________________\\/\\\\\\_   \n" +
                        "     _\\//\\\\\\______/\\\\\\__\\/\\\\\\_____________\\//\\\\\\________________\\/\\\\\\_  \n" +
                        "      __\\///\\\\\\__/\\\\\\____\\/\\\\\\______________\\///\\\\\\______________\\/\\\\\\_ \n" +
                        "       ____\\///\\\\\\\\\\/_____\\/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\____\\////\\\\\\\\\\\\\\\\\\_____\\/\\\\\\_\n" +
                        "        ______\\/////_______\\///////////////________\\/////////______\\///_\n");
    }

    /**
     * Generates a banner with a horizontal gradient effect.
     *
     * @param style The style of the banner.
     * @return The banner as a string with ANSI escape codes for a gradient effect.
     */
    public String getGradientBanner(String style) {
        String banner = bannerTemplates.getOrDefault(style, "");
        if (banner.isEmpty()) {
            return "Banner style not found";
        }

        String[] lines = banner.split("\n");
        StringBuilder gradientBanner = new StringBuilder();

        for (String line : lines) {
            gradientBanner.append(applyHorizontalGradient(line)).append("\n");
        }

        return gradientBanner.toString();
    }

    /**
     * Applies a horizontal gradient to a line of text.
     *
     * @param line The input line.
     * @return The line with ANSI escape codes applied for a gradient effect.
     */
    private String applyHorizontalGradient(String line) {
        StringBuilder coloredLine = new StringBuilder();

        // Gradient colors (red -> orange -> yellow -> green -> cyan -> blue)
        int[] colors = {31, 33, 32, 36, 34, 35};

        char[] chars = line.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            // Calculate the color index based on the character's relative position
            int colorIndex = (int) (i * (colors.length - 1.0) / (chars.length - 1));
            colorIndex = Math.min(colorIndex, colors.length - 1);

            // Apply the ANSI color code
            coloredLine.append("\u001B[").append(colors[colorIndex]).append("m")
                    .append(chars[i])
                    .append("\u001B[0m"); // Reset color
        }

        return coloredLine.toString();
    }

    /**
     * Generates a TextFlow containing the banner with a smooth color gradient.
     * Each character is rendered as a separate Text node with its corresponding color.
     *
     * @param style The style of the banner.
     * @return A TextFlow with the formatted banner using a rainbow gradient.
     */
    public TextFlow getFormattedBanner(String style) {
        String banner = bannerTemplates.getOrDefault(style, "Banner not found");

        // Create an empty TextFlow
        TextFlow bannerFlow = new TextFlow();

        // Process each line of the banner
        String[] lines = banner.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Create a Text node for each character with its color
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                Text charText = new Text(String.valueOf(c));

                // Apply color to all characters except spaces
                if (c != ' ') {
                    // Calculate the color based on the horizontal position
                    double ratio = (double) j / Math.max(1, line.length() - 1);
                    Color color = calculateRainbowGradient(ratio);

                    // Adjust brightness based on the type of character (no special case for underscores)
                    color = adjustBrightnessForChar(color, c);

                    charText.setFill(color);
                } else {
                    // Spaces are rendered with a subtle transparent color
                    charText.setFill(Color.rgb(60, 60, 60, 0));
                }

                charText.setFont(Font.font("JetBrains Mono", 12));
                bannerFlow.getChildren().add(charText);
            }

            // Add a newline after each line
            bannerFlow.getChildren().add(new Text("\n"));
        }

        return bannerFlow;
    }

    /**
     * Calculates a color from a rainbow gradient.
     * Uses the HSB color space for a smoother gradient.
     *
     * @param ratio A value between 0.0 and 1.0 indicating the position in the gradient.
     * @return The calculated color.
     */
    private Color calculateRainbowGradient(double ratio) {
        // Hue: 0° = red, 60° = yellow, 120° = green, 180° = cyan, 240° = blue, 300° = magenta.
        double hue = 300 - (ratio * 300);
        return Color.hsb(hue, 0.9, 1.0);
    }

    /**
     * Adjusts the brightness of a color based on the character type.
     *
     * @param baseColor The base color.
     * @param c         The character to evaluate.
     * @return The adjusted color.
     */
    private Color adjustBrightnessForChar(Color baseColor, char c) {
        if (c == '\\' || c == '/') {
            // Increase brightness for diagonal strokes
            return baseColor.deriveColor(0, 1.0, 1.2, 1.0);
        } else if (c == '|') {
            // Slightly increase brightness for vertical strokes
            return baseColor.deriveColor(0, 1.0, 1.1, 1.0);
        } else {
            return baseColor;
        }
    }

    /**
     * An alternative method that calculates a smooth RGB gradient.
     *
     * @param ratio A value between 0.0 and 1.0 indicating the position in the gradient.
     * @return The calculated color.
     */
    private Color calculateSmoothRGBGradient(double ratio) {
        // Define control points for the gradient
        Color[] gradientPoints = {
                Color.rgb(255, 30, 0),    // Red
                Color.rgb(255, 150, 0),   // Orange
                Color.rgb(255, 255, 0),   // Yellow
                Color.rgb(0, 255, 30),    // Light Green
                Color.rgb(0, 255, 200),   // Teal
                Color.rgb(0, 150, 255),   // Light Blue
                Color.rgb(0, 30, 255),    // Blue
                Color.rgb(130, 0, 255)    // Violet
        };

        int segments = gradientPoints.length - 1;
        double segmentWidth = 1.0 / segments;

        // Determine which segment of the gradient the ratio falls in
        int segment = (int) (ratio / segmentWidth);
        segment = Math.min(segment, segments - 1);

        // Calculate the position within the segment
        double segmentRatio = (ratio - segment * segmentWidth) / segmentWidth;

        // Interpolate between the two segment colors
        Color startColor = gradientPoints[segment];
        Color endColor = gradientPoints[segment + 1];

        return startColor.interpolate(endColor, segmentRatio);
    }

    /**
     * Returns a random banner as a string.
     *
     * @return A banner string selected at random.
     */
    public String getRandomBanner() {
        String style = BANNER_STYLES[random.nextInt(BANNER_STYLES.length)];
        return bannerTemplates.get(style);
    }

    /**
     * Retrieves a specific banner by style.
     *
     * @param style The style of the banner to retrieve.
     * @return The banner as a string.
     */
    public String getBanner(String style) {
        return bannerTemplates.getOrDefault(style, bannerTemplates.get("Standard"));
    }

    /**
     * Returns the names of the available banner styles.
     *
     * @return An array of banner style names.
     */
    public String[] getAvailableStyles() {
        return BANNER_STYLES;
    }
}
