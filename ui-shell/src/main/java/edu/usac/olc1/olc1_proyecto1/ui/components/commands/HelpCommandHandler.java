package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

/**
 * Handler for the 'help' command that displays help for the available commands.
 */
public class HelpCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        StringBuilder helpText = new StringBuilder();
        helpText.append("Available commands:\n");
        helpText.append("  help           - Displays this help\n");
        helpText.append("  clear          - Clears the terminal\n");
        helpText.append("  ls [path]      - Lists files in the specified path or in the current directory\n");
        helpText.append("  cd [path]      - Changes the current directory\n");
        helpText.append("  pwd            - Displays the current directory path\n");
        helpText.append("  banner [style] - Displays the banner with the specified style\n");
        helpText.append("  cat [file]     - Shows the content of a file\n");
        helpText.append("  echo [text]    - Prints text on the terminal\n");
        helpText.append("  exit           - Exits the application\n");
        helpText.append("\n");
        helpText.append("Special features:\n");
        helpText.append("  - Use the up/down arrow keys to navigate through the command history\n");
        helpText.append("  - Use the TAB key to autocomplete commands and paths\n");
        helpText.append("  - The symbol ~ represents your home directory\n");

        return helpText.toString();
    }
}
