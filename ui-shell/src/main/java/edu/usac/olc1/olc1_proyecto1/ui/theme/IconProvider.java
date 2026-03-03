package edu.usac.olc1.olc1_proyecto1.ui.theme;

import edu.usac.olc1.olc1_proyecto1.ui.utils.SVGLoader;
import javafx.scene.Node;

import java.io.File;

public class IconProvider {

    private static final String THEME_BASE_FOLDER = "theme/simple-icons-plus/";

    public static Node getIconForFile(File file, boolean expanded) {
        IconTheme theme = IconThemeManager.getTheme();
        if (theme == null) {
            return SVGLoader.loadSVGIcon("ghost.svg", 16, 16);
        }

        if (file.isDirectory()) {
            String folderName = file.getName().toLowerCase();
            String iconKey = null;

            if (expanded && theme.getFolderNamesExpanded() != null &&
                    theme.getFolderNamesExpanded().containsKey(folderName)) {
                iconKey = theme.getFolderNamesExpanded().get(folderName);
            } else if (theme.getFolderNames() != null &&
                    theme.getFolderNames().containsKey(folderName)) {
                iconKey = theme.getFolderNames().get(folderName);
            }

            if (iconKey == null) {
                iconKey = expanded ? theme.getFolderExpanded() : theme.getFolder();
            }

            IconDefinition def = theme.getIconDefinitions().get(iconKey);
            if (def != null) {
                String iconFileName = extractFileName(def.getIconPath());
                return SVGLoader.loadSVGIconFromFolder(THEME_BASE_FOLDER, iconFileName, 16, 16);
            } else {
                return SVGLoader.loadSVGIcon("ghost.svg", 16, 16);
            }
        } else {
            String fileName = file.getName();
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = fileName.substring(dotIndex + 1).toLowerCase();
            }
            String iconKey = null;
            if (theme.getFileExtensions() != null && theme.getFileExtensions().containsKey(extension)) {
                iconKey = theme.getFileExtensions().get(extension);
            }
            if (iconKey == null) {
                iconKey = theme.getFile();
            }
            IconDefinition def = theme.getIconDefinitions().get(iconKey);
            if (def != null) {
                String iconFileName = extractFileName(def.getIconPath());
                return SVGLoader.loadSVGIconFromFolder(THEME_BASE_FOLDER, iconFileName, 16, 16);
            } else {
                return SVGLoader.loadSVGIcon("ghost.svg", 16, 16);
            }
        }
    }

    private static String extractFileName(String path) {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
