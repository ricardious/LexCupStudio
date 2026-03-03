package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

public class EchoCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}