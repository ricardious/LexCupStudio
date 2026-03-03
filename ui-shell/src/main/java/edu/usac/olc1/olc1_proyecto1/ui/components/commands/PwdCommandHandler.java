package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;

public class PwdCommandHandler implements CommandHandler {
    private final Terminal terminal;

    public PwdCommandHandler(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public String execute(String[] args) {
        return terminal.getCurrentDir() + "\n";
    }
}