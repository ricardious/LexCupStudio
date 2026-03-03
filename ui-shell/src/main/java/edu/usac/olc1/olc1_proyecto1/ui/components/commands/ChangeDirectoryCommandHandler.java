package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;

import java.io.File;

/**
 * Handler for the 'cd' command that changes the current directory.
 */
public class ChangeDirectoryCommandHandler implements CommandHandler {
    private final Terminal terminal;

    /**
     * Constructor that receives a reference to the terminal.
     *
     * @param terminal Reference to the terminal.
     */
    public ChangeDirectoryCommandHandler(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public String execute(String[] args) {
        // Determine path
        String path = (args.length > 0) ? args[0] : System.getProperty("user.home");

        // Special case: no arguments or ~ (go to home)
        if (path.equals("~")) {
            path = System.getProperty("user.home");
        }

        // Special case: .. (go to parent directory)
        if (path.equals("..")) {
            File current = new File(terminal.getCurrentDir());
            path = current.getParent();
            if (path == null) {
                // Already at the root
                return "";
            }
        }

        // Expand ~ in the path
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }

        // If the path is not absolute, make it relative to the current directory
        if (!path.startsWith("/") && !path.contains(":")) {
            path = terminal.getCurrentDir() + "/" + path;
        }

        // Normalize the path (remove ./ and resolve ../)
        File dir = new File(path);
        try {
            path = dir.getCanonicalPath();
        } catch (Exception e) {
            return "cd: Error resolving path: " + e.getMessage() + "\n";
        }

        // Check if it exists and is a directory
        dir = new File(path);
        if (!dir.exists()) {
            return "cd: " + path + ": File or directory does not exist\n";
        }

        if (!dir.isDirectory()) {
            return "cd: " + path + ": Not a directory\n";
        }

        // Change directory
        terminal.setCurrentDir(path);
        return "";
    }
}
