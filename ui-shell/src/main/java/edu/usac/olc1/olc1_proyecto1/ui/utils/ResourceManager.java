package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.image.Image;

import java.util.Objects;

public class ResourceManager {
    private static ResourceManager instance;
    private Image maximizeIcon;
    private Image restoreIcon;

    private ResourceManager() {
        loadResources();
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    private void loadResources() {
        try {
            maximizeIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/edu/usac/olc1/olc1_proyecto1/icons/maximize.png")));
            restoreIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/edu/usac/olc1/olc1_proyecto1/icons/restore.png")));
        } catch (Exception e) {
            System.err.println("Failed to load icons: " + e.getMessage());
        }
    }

    public Image getMaximizeIcon() {
        return maximizeIcon;
    }

    public Image getRestoreIcon() {
        return restoreIcon;
    }
}
