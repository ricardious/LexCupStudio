package edu.usac.olc1.olc1_proyecto1.ui.theme;

import java.util.Map;

public class IconTheme {
    private String file;
    private String folder;
    private String folderExpanded;
    private Map<String, String> fileExtensions;
    private Map<String, String> folderNames;
    private Map<String, String> folderNamesExpanded;
    private Map<String, IconDefinition> iconDefinitions;

    public String getFile() {
        return file;
    }

    public String getFolder() {
        return folder;
    }

    public String getFolderExpanded() {
        return folderExpanded;
    }

    public Map<String, String> getFileExtensions() {
        return fileExtensions;
    }

    public Map<String, String> getFolderNames() {
        return folderNames;
    }

    public Map<String, String> getFolderNamesExpanded() {
        return folderNamesExpanded;
    }

    public Map<String, IconDefinition> getIconDefinitions() {
        return iconDefinitions;
    }
}
