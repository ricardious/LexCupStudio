package edu.usac.olc1.olc1_proyecto1.ui.components;


import edu.usac.olc1.olc1_proyecto1.ui.components.commands.*;
import edu.usac.olc1.olc1_proyecto1.ui.utils.BannerGenerator;
import edu.usac.olc1.olc1_proyecto1.ui.utils.BrandingConfig;
import edu.usac.olc1.olc1_proyecto1.ui.utils.Fonts;
import edu.usac.olc1.olc1_proyecto1.ui.utils.Styles;
import edu.usac.olc1.olc1_proyecto1.ui.utils.TerminalTheme;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Terminal extends ScrollPane {

    private final VBox contentBox;
    private HBox currentLine;
    private final TextFlow promptFlow;
    private final TextField inputField;
    private final List<String> commandHistory;
    private int historyIndex = 0;
    private final Map<String, CommandHandler> commandHandlers;
    private final TerminalTheme theme;
    private final String userHomeDir;
    private String currentDir;
    private final Map<String, String> env;
    private static final int MAX_HISTORY_SIZE = 1000;

    public Terminal() {
        // Initialize theme
        theme = new TerminalTheme();

        // Initialize environment
        env = new HashMap<>();
        userHomeDir = System.getProperty("user.home");
        currentDir = userHomeDir + "/proyectos/" + appSlug();
        env.put("HOME", userHomeDir);
        env.put("PWD", currentDir);

        // Setup terminal UI
        this.getStyleClass().add("terminal-component");
        this.setFitToWidth(true);
        this.setFitToHeight(true);

        // Style the scroll pane
        this.getStylesheets().add(Styles.forName("terminal.css"));

        // Initialize container
        contentBox = new VBox(5);
        contentBox.setPadding(new Insets(10));
        contentBox.getStyleClass().add("terminal-content");

        // Load font
        try {
            Font.loadFont(Fonts.ttf("JetBrainsMono-Regular.ttf"), 14);
        } catch (Exception ignored) {
        }

        // Register command handlers
        commandHandlers = new HashMap<>();
        commandHandlers.put("help", new HelpCommandHandler());
        commandHandlers.put("clear", new ClearCommandHandler(this));
        commandHandlers.put("ls", new ListFilesCommandHandler());
        commandHandlers.put("cd", new ChangeDirectoryCommandHandler(this));
        commandHandlers.put("banner", new BannerCommandHandler(this));
        commandHandlers.put("exit", new ExitCommandHandler());
        commandHandlers.put("echo", new EchoCommandHandler());
        commandHandlers.put("cat", new CatCommandHandler());
        commandHandlers.put("pwd", new PwdCommandHandler(this));

        // Display welcome banner
        showBanner();

        // Setup command line
        currentLine = new HBox(5);
        currentLine.setAlignment(Pos.BASELINE_LEFT);

        // Create prompt
        promptFlow = new TextFlow();
        updatePrompt();

        // Create input field
        inputField = new TextField();
        inputField.getStyleClass().add("terminal-input");
        inputField.setFocusTraversable(true);

        // Make input field take remaining space
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // Add prompt and input to current line
        currentLine.getChildren().addAll(promptFlow, inputField);
        contentBox.getChildren().add(currentLine);

        // Initialize command history
        commandHistory = new ArrayList<>();
        loadCommandHistory();

        // Setup auto-scroll
        contentBox.heightProperty().addListener((obs, oldVal, newVal) -> this.setVvalue(1.0));

        // Set content and event handlers
        this.setContent(contentBox);
        inputField.setOnKeyPressed(this::handleKeyPress);

        // Initial focus
        Platform.runLater(() -> inputField.requestFocus());
    }

    public void registerCommand(String name, CommandHandler handler) {
        commandHandlers.put(name.toLowerCase(), handler);
    }

    private String abbreviatePath(String fullPath) {
        // Reemplaza el directorio home por "~"
        if (fullPath.startsWith(userHomeDir)) {
            fullPath = "~" + fullPath.substring(userHomeDir.length());
        }

        // Si la ruta es muy larga, muestra solo los dos últimos directorios
        String separator = File.separator;
        String[] parts = fullPath.split(Pattern.quote(separator));
        if (parts.length > 3) { // Ajusta el número según lo que necesites
            // Ejemplo: ~/.../penultimo/ultimo
            return parts[0] + separator + "..." + separator + parts[parts.length - 2] + separator + parts[parts.length - 1];
        }
        return fullPath;
    }


    public void updatePrompt() {
        promptFlow.getChildren().clear();

        // Create prompt components
        Text arrowPart = new Text("➜ ");
        arrowPart.setFill(theme.getPromptColor());
        arrowPart.setFont(Font.font("JetBrains Mono", 14));

        // Format current directory
        String displayDir = abbreviatePath(currentDir);
        Text dirPart = new Text(displayDir + " ");
        dirPart.setFill(theme.getDirColor());
        dirPart.setFont(Font.font("JetBrains Mono", 14));

        Tooltip tooltip = new Tooltip(currentDir);
        Tooltip.install(dirPart, tooltip);

        // Add git branch if in a git repo
        File gitDir = new File(currentDir + "/.git");
        if (gitDir.exists() && gitDir.isDirectory()) {
            String branch = getGitBranch();
            Text gitPart = new Text("(" + branch + ") ");
            gitPart.setFill(theme.getGitColor());
            gitPart.setFont(Font.font("JetBrains Mono", 14));
            promptFlow.getChildren().addAll(arrowPart, dirPart, gitPart);
        } else {
            promptFlow.getChildren().addAll(arrowPart, dirPart);
        }
    }

    private String getGitBranch() {
        try {
            File headFile = new File(currentDir + "/.git/HEAD");
            if (headFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(headFile));
                String headContent = reader.readLine();
                reader.close();

                if (headContent.startsWith("ref: refs/heads/")) {
                    return headContent.substring("ref: refs/heads/".length());
                }
                return headContent.substring(0, 7); // Detached HEAD state, show commit hash
            }
        } catch (IOException ignored) {
        }
        return "main"; // Default fallback
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // Get command and update history
            String command = inputField.getText().trim();
            if (!command.isEmpty()) {
                if (commandHistory.isEmpty() || !command.equals(commandHistory.get(commandHistory.size() - 1))) {
                    if (commandHistory.size() >= MAX_HISTORY_SIZE) {
                        commandHistory.remove(0);
                    }
                    commandHistory.add(command);
                    saveCommandHistory();
                }
                historyIndex = commandHistory.size();

                // Display executed command
                displayExecutedCommand(command);

                // Process command
                processCommand(command);
            } else {
                // Just add a new prompt line if empty command
                displayOutput("");
            }

            // Clear input field
            inputField.clear();
            event.consume();
        } else if (event.getCode() == KeyCode.UP && !commandHistory.isEmpty()) {
            // Navigate history up
            if (historyIndex > 0) {
                historyIndex--;
                inputField.setText(commandHistory.get(historyIndex));
                inputField.positionCaret(inputField.getText().length());
            }
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN && !commandHistory.isEmpty()) {
            // Navigate history down
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                inputField.setText(commandHistory.get(historyIndex));
                inputField.positionCaret(inputField.getText().length());
            } else if (historyIndex == commandHistory.size() - 1) {
                historyIndex++;
                inputField.clear();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.TAB) {
            // Handle tab completion
            String currentText = inputField.getText();
            List<String> suggestions = getTabCompletionSuggestions(currentText);

            if (suggestions.size() == 1) {
                // Single match - autocomplete
                inputField.setText(suggestions.get(0));
                inputField.positionCaret(inputField.getText().length());
            } else if (suggestions.size() > 1) {
                // Multiple matches - show options
                displayOutput("\nPosibles completaciones:");
                StringBuilder sb = new StringBuilder();
                for (String suggestion : suggestions) {
                    sb.append(suggestion).append("  ");
                }
                displayOutput(sb.toString());
                displayPrompt();
                inputField.setText(currentText);
                inputField.positionCaret(currentText.length());
            }

            event.consume();
        }
    }

    private List<String> getTabCompletionSuggestions(String partial) {
        List<String> suggestions = new ArrayList<>();

        // Check for command completion
        if (!partial.contains(" ")) {
            for (String cmd : commandHandlers.keySet()) {
                if (cmd.startsWith(partial)) {
                    suggestions.add(cmd);
                }
            }
        } else {
            // Path completion
            String[] parts = partial.split("\\s+", 2);
            String command = parts[0];
            String path = parts.length > 1 ? parts[1] : "";

            if (Arrays.asList("cd", "ls", "cat").contains(command)) {
                File dir;
                String prefix = "";

                if (path.contains("/")) {
                    String dirPath = path.substring(0, path.lastIndexOf('/') + 1);
                    prefix = path.substring(path.lastIndexOf('/') + 1);

                    if (dirPath.startsWith("~")) {
                        dirPath = userHomeDir + dirPath.substring(1);
                    } else if (!dirPath.startsWith("/")) {
                        dirPath = currentDir + "/" + dirPath;
                    }

                    dir = new File(dirPath);
                } else {
                    dir = new File(currentDir);
                    prefix = path;
                }

                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().startsWith(prefix)) {
                                String fullPath = path.contains("/")
                                        ? path.substring(0, path.lastIndexOf('/') + 1) + file.getName()
                                        : file.getName();

                                if (file.isDirectory()) {
                                    fullPath += "/";
                                }
                                suggestions.add(command + " " + fullPath);
                            }
                        }
                    }
                }
            }
        }

        return suggestions;
    }

    private void displayExecutedCommand(String command) {
        TextFlow commandLine = new TextFlow();

        // Copy prompt elements
        for (int i = 0; i < promptFlow.getChildren().size(); i++) {
            Text original = (Text) promptFlow.getChildren().get(i);
            Text copy = new Text(original.getText());
            copy.setFill(original.getFill());
            copy.setFont(original.getFont());
            commandLine.getChildren().add(copy);
        }

        // Add command text
        Text commandText = new Text(command + "\n");
        commandText.setFill(theme.getTextColor());
        commandText.setFont(Font.font("JetBrains Mono", 14));
        commandLine.getChildren().add(commandText);

        // Add to terminal output
        contentBox.getChildren().add(contentBox.getChildren().size() - 1, commandLine);
    }

    public void processCommand(String command) {
        if (command.isEmpty()) {
            return;
        }

        // Parse command and arguments
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String[] args = parts.length > 1
                ? parts[1].split("\\s+")
                : new String[0];

        // Check for environment variable expansion
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("$")) {
                String varName = args[i].substring(1);
                args[i] = env.getOrDefault(varName, "");
            }
        }

        // Execute command
        String output;
        CommandHandler handler = commandHandlers.get(cmd);

        if (handler != null) {
            output = handler.execute(args);

            // Special handling for certain commands
            if (cmd.equals("clear")) {
                contentBox.getChildren().clear();
                contentBox.getChildren().add(currentLine);
                return;
            } else if (cmd.equals("exit")) {
                // Graceful shutdown
                Platform.exit();
                return;
            }
        } else {
            output = "comando no encontrado: " + cmd + "\n";
        }

        // Display command output
        displayOutput(output);
    }

    public void executeCommandFromUi(String command) {
        if (command == null) {
            return;
        }
        String normalized = command.trim();
        if (normalized.isEmpty()) {
            return;
        }
        displayExecutedCommand(normalized);
        processCommand(normalized);
        inputField.clear();
        Platform.runLater(() -> inputField.requestFocus());
    }

    public void displayOutput(String output) {
        if (output == null || output.isEmpty()) {
            return;
        }

        TextFlow outputFlow = new TextFlow();
        Text outputText = new Text(output + "\n");
        outputText.setFill(theme.getTextColor());
        outputText.setFont(Font.font("JetBrains Mono", 14));
        outputFlow.getChildren().add(outputText);

        contentBox.getChildren().add(contentBox.getChildren().size() - 1, outputFlow);
    }

    public void displayPrompt() {
        // Create a new command line
        currentLine = new HBox(5);
        currentLine.setAlignment(Pos.BASELINE_LEFT);

        // Update prompt
        updatePrompt();

        // Recreate input field
        inputField.clear();

        // Add to UI
        currentLine.getChildren().addAll(promptFlow, inputField);
        contentBox.getChildren().add(currentLine);

        // Focus on input
        Platform.runLater(() -> inputField.requestFocus());
    }

    public void showBanner() {
        BannerGenerator bannerGen = new BannerGenerator();

        TextFlow bannerFlow = bannerGen.getFormattedBanner("OLC1");

        Text welcomeText = new Text("\n" + appName() + " Terminal v1.0.0\n" +
                "Type 'help' to see available commands\n\n");
        welcomeText.setFill(theme.getTextColor());
        welcomeText.setFont(Font.font("JetBrains Mono", 12));
        bannerFlow.getChildren().add(welcomeText);

        contentBox.getChildren().add(bannerFlow);
    }

    private void loadCommandHistory() {
        try {
            File historyFile = historyFile();
            if (historyFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(historyFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    commandHistory.add(line);
                }
                reader.close();
                historyIndex = commandHistory.size();
            }
        } catch (IOException ignored) {
        }
    }

    private void saveCommandHistory() {
        try {
            File historyFile = historyFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile));
            for (String cmd : commandHistory) {
                writer.write(cmd);
                writer.newLine();
            }
            writer.close();
        } catch (IOException ignored) {
        }
    }

    private String appName() {
        return BrandingConfig.getAppName();
    }

    private String appSlug() {
        String slug = appName().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "lexcupstudio" : slug;
    }

    private File historyFile() {
        return new File(userHomeDir + "/." + appSlug().replace('-', '_') + "_history");
    }

    // Getters and setters
    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String dir) {
        this.currentDir = dir;
        env.put("PWD", dir);
        updatePrompt();
    }

    public void setTheme(TerminalTheme newTheme) {
        // Update theme
    }

    public void clear() {
        contentBox.getChildren().clear();
        contentBox.getChildren().add(currentLine);
    }
}
