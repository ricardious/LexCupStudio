package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;

public class BannerCommandHandler implements CommandHandler {
    private final Terminal terminal;

    public BannerCommandHandler(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public String execute(String[] args) {
        terminal.showBanner();
        return "";
    }
}