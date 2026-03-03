package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import javafx.application.Platform;

public class ExitCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        String message = "Cerrando AutómataLab...\n";
        Platform.runLater(() -> Platform.exit());
        return message;
    }
}