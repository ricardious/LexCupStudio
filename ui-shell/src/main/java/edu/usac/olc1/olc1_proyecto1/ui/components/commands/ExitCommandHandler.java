package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import edu.usac.olc1.olc1_proyecto1.ui.utils.BrandingConfig;
import javafx.application.Platform;

public class ExitCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        String message = "Cerrando " + BrandingConfig.getAppName() + "...\n";
        Platform.runLater(() -> Platform.exit());
        return message;
    }
}
