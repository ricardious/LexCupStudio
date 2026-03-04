package edu.usac.olc1.olc1_proyecto1.ui.controllers;

import edu.usac.olc1.olc1_proyecto1.ui.components.FileTreeCell;
import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;
import edu.usac.olc1.olc1_proyecto1.ui.managers.AutoCompletionManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.FileManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.SyntaxHighlightingManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.TabManager;
import edu.usac.olc1.olc1_proyecto1.ui.utils.BrandingConfig;
import edu.usac.olc1.olc1_proyecto1.ui.utils.DialogStyler;
import edu.usac.olc1.olc1_proyecto1.ui.utils.ResourceManager;
import edu.usac.olc1.olc1_proyecto1.ui.utils.WindowDragger;
import io.lexcupstudio.ui.api.DiagnosticType;
import io.lexcupstudio.ui.api.LanguageRuntimePlugin;
import io.lexcupstudio.ui.api.SourceDiagnostic;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditorController implements Initializable {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button runBtn;

    @FXML
    private Button btnOpenFolder;

    @FXML
    private Button btnClose, btnMin, btnRest;

    @FXML
    private HBox titleBar;

    @FXML
    private VBox emptyExplorerMessage;

    @FXML
    private TreeView<String> explorer;

    @FXML
    private AnchorPane explorerPanel;

    @FXML
    private AnchorPane searchPanel;

    @FXML
    private TextField searchInput;

    @FXML
    private ListView<SearchResultEntry> searchResultsList;

    @FXML
    private AnchorPane sourceControlPanel;

    @FXML
    private ListView<SourceControlEntry> sourceControlList;


    @FXML
    private Button explorerBtn;

    @FXML
    private Button searchBtn;

    @FXML
    private Button toolsBtn;

    @FXML
    private SplitPane leftSplitPane;

    @FXML
    private CodeArea codeEditor;

    @FXML
    private TabPane editorTabPane;

    @FXML
    private Tab welcomeTab;

    @FXML
    private StackPane tabContentArea;

    @FXML
    private ToggleGroup tabGroup;

    @FXML
    private ListView<ProblemEntry> problemsPanel;

    @FXML
    private TextArea outputPanel, debugConsolePanel, reportsPanel;

    @FXML
    private Terminal terminal;

    @FXML
    private AnchorPane terminalPanel;

    @FXML
    private SplitPane rightSplitPane;

    @FXML
    private Button btnMinimizePanel;

    @FXML
    private Button btnMaximizePanel;

    @FXML
    private Button btnCloneRepository;

    @FXML
    private Label welcomeAppNameLabel;

    @FXML
    private Label appTitleLabel;

    @FXML
    private ImageView welcomeLogoImageView;

    @FXML
    private ImageView titleBarLogoImageView;

    private boolean isMaximized = false;
    private double lastDividerPosition;
    private File selectedDirectory;

    private final FileManager fileManager = new FileManager();
    private TabManager tabManager;
    private final AutoCompletionManager autoCompletionManager = new AutoCompletionManager();
    private final SyntaxHighlightingManager syntaxHighlightingManager = new SyntaxHighlightingManager();
    private final LanguageRuntimePlugin languagePlugin = resolveLanguagePlugin();
    private final List<ProblemEntry> currentProblems = new ArrayList<>();
    private final PauseTransition diagnosticsDebounce = new PauseTransition(Duration.millis(350));
    private static final Logger LOGGER = Logger.getLogger(EditorController.class.getName());
    private boolean isPanelExpanded = true;
    private double originalDividerPosition;
    private CodeArea trackedCodeArea;
    private final ChangeListener<String> diagnosticsTextListener = (obs, oldText, newText) -> diagnosticsDebounce.playFromStart();
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(250));
    private Task<List<SearchResultEntry>> activeSearchTask;
    private Task<List<SourceControlEntry>> activeSourceControlTask;
    private edu.usac.olc1.olc1_proyecto1.ui.components.commands.RicardiousCommandHandler runCommandHandler;

    /**
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new WindowDragger().makeDraggable(titleBar);
        setupWindowControls();

        if (welcomeTab == null && !editorTabPane.getTabs().isEmpty()) {
            welcomeTab = editorTabPane.getTabs().get(0);
        }
        javafx.scene.Node welcomeContent = welcomeTab.getContent();
        welcomeTab.setContent(null);

        tabManager = new TabManager(editorTabPane, tabContentArea);

        tabManager.registerTab(welcomeTab, welcomeContent);
        applyBranding();

        codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
        syntaxHighlightingManager.bind(codeEditor);

        autoCompletionManager.setupAutoCompletion(codeEditor);

        double minWidth = 180;

        leftSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
            double leftPanelWidth = leftSplitPane.getWidth() * newPos.doubleValue();

            if (leftPanelWidth < minWidth) {
                leftSplitPane.setDividerPositions(0.0);
            }
        });

        leftSplitPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double currentDividerPosition = leftSplitPane.getDividers().get(0).getPosition();
            double leftPanelWidth = newWidth.doubleValue() * currentDividerPosition;

            if (leftPanelWidth < minWidth && currentDividerPosition > 0) {
                leftSplitPane.setDividerPositions(0.0);
            } else if (leftPanelWidth >= minWidth && currentDividerPosition == 0) {
                leftSplitPane.setDividerPositions(0.23);
            }
        });

        checkExplorerState();
        lastDividerPosition = leftSplitPane.getDividerPositions()[0];

        addDoubleClickHandler(explorerBtn);
        addDoubleClickHandler(searchBtn);
        addDoubleClickHandler(toolsBtn);
        explorer.setOnMouseClicked(this::handleTreeViewClick);

        setupTabSwitching();
        setupProblemsPanel();
        setupDiagnosticsTracking();
        setupSearchPanel();
        setupSourceControlPanel();

        hideTerminalPanel();

        Platform.runLater(this::initializePanelButtons);

        runCommandHandler = new edu.usac.olc1.olc1_proyecto1.ui.components.commands.RicardiousCommandHandler(
                terminal,
                () -> {
                    java.io.File f = null;
                    try {
                        f = tabManager.getActiveFile();
                    } catch (Exception ignore) {}
                    return f;
                }
        );
        terminal.registerCommand("ricardious", runCommandHandler);
        terminal.registerCommand("run", runCommandHandler);


    }

    private void applyBranding() {
        String appName = BrandingConfig.getAppName();
        Image logo = BrandingConfig.getLogoImage();

        if (welcomeAppNameLabel != null) {
            welcomeAppNameLabel.setText(appName);
        }

        if (appTitleLabel != null) {
            appTitleLabel.setText(appName);
        }

        if (logo != null) {
            if (welcomeLogoImageView != null) {
                welcomeLogoImageView.setImage(logo);
            }
            if (titleBarLogoImageView != null) {
                titleBarLogoImageView.setImage(logo);
            }
        }
    }

    private void initializePanelButtons() {
        if (rightSplitPane.getDividers() != null && !rightSplitPane.getDividers().isEmpty()) {
            originalDividerPosition = rightSplitPane.getDividerPositions()[0];

            btnMaximizePanel.setVisible(true);
            btnMaximizePanel.setManaged(true);
            btnMinimizePanel.setVisible(false);
            btnMinimizePanel.setManaged(false);

            rightSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() < 0.05) {
                    btnMaximizePanel.setVisible(false);
                    btnMaximizePanel.setManaged(false);
                    btnMinimizePanel.setVisible(true);
                    btnMinimizePanel.setManaged(true);
                } else {
                    btnMaximizePanel.setVisible(true);
                    btnMaximizePanel.setManaged(true);
                    btnMinimizePanel.setVisible(false);
                    btnMinimizePanel.setManaged(false);
                }
            });
        }
    }

    private void runAllTabs() {
        CodeArea activeEditor = tabManager.getActiveCodeArea();
        if (activeEditor == null) {
            terminal.displayOutput("No hay un editor de código activo para ejecutar.");
            return;
        }

        File activeFile = tabManager.getActiveFile();
        if (activeFile != null) {
            terminal.executeCommandFromUi("run " + activeFile.getAbsolutePath());
        } else {
            terminal.executeCommandFromUi("run");
        }
    }

    private void setupTabSwitching() {
        tabGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                String tabText = ((ToggleButton) newToggle).getText();

                problemsPanel.setVisible(false);
                outputPanel.setVisible(false);
                debugConsolePanel.setVisible(false);
                terminal.setVisible(false);
                reportsPanel.setVisible(false);

                switch (tabText) {
                    case "PROBLEMS":
                        problemsPanel.setVisible(true);
                        break;
                    case "OUTPUT":
                        outputPanel.setVisible(true);
                        break;
                    case "DEBUG CONSOLE":
                        debugConsolePanel.setVisible(true);
                        break;
                    case "TERMINAL":
                        terminal.setVisible(true);
                        break;
                    case "REPORTS":
                        reportsPanel.setVisible(true);
                        break;
                }

                for (Toggle toggle : tabGroup.getToggles()) {
                    ToggleButton tb = (ToggleButton) toggle;
                    if (tb == newToggle) {
                        tb.setStyle("-fx-background-color: transparent; -fx-text-fill: #f0f0f0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-border-width: 0 0 2 0; -fx-border-color: #007acc;");
                    } else {
                        tb.setStyle("-fx-background-color: transparent; -fx-text-fill: #8a8a8a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;");
                    }
                }
            }
        });

        if (!tabGroup.getToggles().isEmpty()) {
            for (Toggle toggle : tabGroup.getToggles()) {
                ToggleButton tb = (ToggleButton) toggle;
                if ("TERMINAL".equals(tb.getText())) {
                    tabGroup.selectToggle(tb);
                    break;
                }
            }

            if (tabGroup.getSelectedToggle() == null && !tabGroup.getToggles().isEmpty()) {
                tabGroup.selectToggle(tabGroup.getToggles().get(0));
            }
        }
    }

    private void setupProblemsPanel() {
        problemsPanel.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ProblemEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.prettyText());
            }
        });

        problemsPanel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                ProblemEntry selected = problemsPanel.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    goToProblem(selected);
                }
            }
        });

        problemsPanel.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ProblemEntry selected = problemsPanel.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    goToProblem(selected);
                }
            }
        });
    }

    private void setupSearchPanel() {
        searchResultsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SearchResultEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.prettyText());
            }
        });

        searchResultsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SearchResultEntry selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openSearchResult(selected);
                }
            }
        });

        searchResultsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SearchResultEntry selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openSearchResult(selected);
                }
            }
        });

        searchDebounce.setOnFinished(e -> performSearch(searchInput.getText()));
        searchInput.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
    }

    private void setupSourceControlPanel() {
        sourceControlList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SourceControlEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.prettyText());
            }
        });

        sourceControlList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SourceControlEntry selected = sourceControlList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openSourceControlEntry(selected);
                }
            }
        });

        sourceControlList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SourceControlEntry selected = sourceControlList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openSourceControlEntry(selected);
                }
            }
        });
    }

    @FXML
    private void handleRefreshSourceControl(ActionEvent event) {
        refreshSourceControlStatus();
    }

    @FXML
    private void handleStageAllChanges(ActionEvent event) {
        Path repoRoot = detectGitRepoRoot();
        if (repoRoot == null) {
            showInfoDialog("Source Control", "No Git repository detected.");
            return;
        }

        try {
            runGitCommand(repoRoot, "add", "-A");
            appendOutput("Git: stage all completed.");
            refreshSourceControlStatus();
        } catch (Exception ex) {
            showErrorDialog("Git Error", "Failed to stage changes: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCommitChanges(ActionEvent event) {
        Path repoRoot = detectGitRepoRoot();
        if (repoRoot == null) {
            showInfoDialog("Source Control", "No Git repository detected.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        DialogStyler.apply(dialog);
        dialog.setTitle("Commit Changes");
        dialog.setHeaderText("Create commit");
        dialog.setContentText("Message:");
        Optional<String> messageResult = dialog.showAndWait();

        if (messageResult.isEmpty() || messageResult.get().isBlank()) {
            return;
        }

        String message = messageResult.get().trim();
        try {
            runGitCommand(repoRoot, "add", "-A");
            runGitCommand(repoRoot, "commit", "-m", message);
            appendOutput("Git: commit created.");
            refreshSourceControlStatus();
        } catch (Exception ex) {
            showErrorDialog("Git Error", "Failed to commit changes: " + ex.getMessage());
        }
    }

    private void refreshSourceControlStatus() {
        Path repoRoot = detectGitRepoRoot();
        if (repoRoot == null) {
            if (selectedDirectory == null) {
                sourceControlList.getItems().setAll(new SourceControlEntry("..", "No folder opened.", null));
            } else {
                sourceControlList.getItems().setAll(new SourceControlEntry("!!", "No Git repository detected in opened folder.", null));
            }
            return;
        }

        if (activeSourceControlTask != null && activeSourceControlTask.isRunning()) {
            activeSourceControlTask.cancel();
        }

        activeSourceControlTask = new Task<>() {
            @Override
            protected List<SourceControlEntry> call() throws Exception {
                String output = runGitCommand(repoRoot, "status", "--porcelain");
                List<SourceControlEntry> entries = new ArrayList<>();
                if (output.isBlank()) {
                    entries.add(new SourceControlEntry("OK", "Working tree clean", repoRoot));
                    return entries;
                }

                String[] lines = output.split("\\R");
                for (String line : lines) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String status = line.length() >= 2 ? line.substring(0, 2).trim() : "??";
                    String path = line.length() > 3 ? line.substring(3).trim() : line.trim();
                    int renameArrow = path.indexOf(" -> ");
                    if (renameArrow >= 0) {
                        path = path.substring(renameArrow + 4).trim();
                    }
                    entries.add(new SourceControlEntry(status.isEmpty() ? "M" : status, path, repoRoot));
                }
                return entries;
            }
        };

        activeSourceControlTask.setOnSucceeded(e -> sourceControlList.getItems().setAll(activeSourceControlTask.getValue()));
        activeSourceControlTask.setOnFailed(e -> {
            Throwable ex = activeSourceControlTask.getException();
            String msg = ex == null ? "Unknown error" : ex.getMessage();
            sourceControlList.getItems().setAll(new SourceControlEntry("!!", "Error: " + msg, repoRoot));
        });

        Thread t = new Thread(activeSourceControlTask, "source-control-status");
        t.setDaemon(true);
        t.start();
    }

    private Path detectGitRepoRoot() {
        if (selectedDirectory == null || !selectedDirectory.isDirectory()) {
            return null;
        }
        Path base = selectedDirectory.toPath();
        try {
            String top = runGitCommand(base, "rev-parse", "--show-toplevel").trim();
            if (!top.isEmpty()) {
                return Path.of(top);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void openSourceControlEntry(SourceControlEntry entry) {
        if (entry == null || entry.repoRoot() == null || entry.path() == null) {
            return;
        }
        if ("Working tree clean".equals(entry.path()) || entry.path().startsWith("No Git") || entry.path().startsWith("Error:")) {
            return;
        }
        File file = entry.repoRoot().resolve(entry.path()).toFile();
        if (file.exists() && file.isFile()) {
            tabManager.openFile(file, fileManager);
        }
    }

    private String runGitCommand(Path workDir, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("-C");
        command.add(workDir.toString());
        command.addAll(List.of(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        String stdout;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
            stdout = out.toString();
        }

        String stderr;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
            stderr = out.toString().trim();
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException(stderr.isBlank() ? ("git exited with code " + exit) : stderr);
        }
        return stdout;
    }

    private void performSearch(String query) {
        if (query == null || query.isBlank()) {
            searchResultsList.getItems().clear();
            return;
        }

        if (selectedDirectory == null || !selectedDirectory.isDirectory()) {
            searchResultsList.getItems().setAll(new SearchResultEntry(
                    null, -1, "Open a folder first to search.", false
            ));
            return;
        }

        if (activeSearchTask != null && activeSearchTask.isRunning()) {
            activeSearchTask.cancel();
        }

        final String term = query.trim().toLowerCase();
        activeSearchTask = new Task<>() {
            @Override
            protected List<SearchResultEntry> call() {
                List<SearchResultEntry> results = new ArrayList<>();
                walkAndSearch(selectedDirectory, term, results, 200);
                return results;
            }
        };

        activeSearchTask.setOnSucceeded(e -> {
            List<SearchResultEntry> results = activeSearchTask.getValue();
            if (results == null || results.isEmpty()) {
                searchResultsList.getItems().setAll(new SearchResultEntry(
                        null, -1, "No results found.", false
                ));
            } else {
                searchResultsList.getItems().setAll(results);
            }
        });

        activeSearchTask.setOnFailed(e -> searchResultsList.getItems().setAll(
                new SearchResultEntry(null, -1, "Search failed: " + activeSearchTask.getException().getMessage(), false)
        ));

        Thread searchThread = new Thread(activeSearchTask, "search-task");
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void walkAndSearch(File dir, String term, List<SearchResultEntry> results, int limit) {
        if (dir == null || results.size() >= limit) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (activeSearchTask != null && activeSearchTask.isCancelled()) {
                return;
            }

            if (file.isDirectory()) {
                walkAndSearch(file, term, results, limit);
                if (results.size() >= limit) {
                    return;
                }
                continue;
            }

            if (file.getName().toLowerCase().contains(term)) {
                results.add(new SearchResultEntry(file, 1, "File name match", false));
                if (results.size() >= limit) {
                    return;
                }
            }

            scanFileContent(file, term, results, limit);
            if (results.size() >= limit) {
                return;
            }
        }
    }

    private void scanFileContent(File file, String term, List<SearchResultEntry> results, int limit) {
        if (limit <= results.size()) {
            return;
        }
        if (file.length() > 1024 * 1024) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.toLowerCase().contains(term)) {
                    String snippet = line.trim();
                    if (snippet.length() > 120) {
                        snippet = snippet.substring(0, 120) + "...";
                    }
                    results.add(new SearchResultEntry(file, lineNumber, snippet, true));
                    if (results.size() >= limit) {
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void openSearchResult(SearchResultEntry entry) {
        if (entry == null || entry.file() == null) {
            return;
        }

        tabManager.openFile(entry.file(), fileManager);
        Platform.runLater(() -> {
            CodeArea activeEditor = tabManager.getActiveCodeArea();
            if (activeEditor == null) {
                return;
            }
            int offset = offsetForLineColumn(activeEditor.getText(), Math.max(1, entry.line()), 1);
            activeEditor.requestFocus();
            activeEditor.moveTo(offset);
            activeEditor.requestFollowCaret();
        });
    }

    private void setupDiagnosticsTracking() {
        diagnosticsDebounce.setOnFinished(event -> {
            CodeArea activeEditor = tabManager.getActiveCodeArea();
            if (activeEditor != null) {
                analyzeEditor(activeEditor, false);
            }
        });

        editorTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> bindActiveEditorTracking());
        Platform.runLater(this::bindActiveEditorTracking);
    }

    private void bindActiveEditorTracking() {
        if (trackedCodeArea != null) {
            trackedCodeArea.textProperty().removeListener(diagnosticsTextListener);
        }

        trackedCodeArea = tabManager.getActiveCodeArea();
        if (trackedCodeArea == null) {
            currentProblems.clear();
            problemsPanel.getItems().clear();
            return;
        }

        trackedCodeArea.textProperty().addListener(diagnosticsTextListener);
        diagnosticsDebounce.playFromStart();
    }

    private void analyzeEditor(CodeArea editor, boolean logToOutput) {
        if (languagePlugin == null) {
            syntaxHighlightingManager.clearDiagnostics(editor);
            currentProblems.clear();
            problemsPanel.getItems().setAll(currentProblems);
            return;
        }

        Path projectDir = resolveProjectDirectory();
        List<SourceDiagnostic> diagnostics = languagePlugin.analyze(
                editor.getText(),
                projectDir,
                logToOutput ? this::appendOutput : msg -> {}
        );
        if (diagnostics == null) {
            diagnostics = List.of();
        }

        currentProblems.clear();
        for (SourceDiagnostic diagnostic : diagnostics) {
            currentProblems.add(new ProblemEntry(diagnostic));
        }

        problemsPanel.getItems().setAll(currentProblems);
        syntaxHighlightingManager.setDiagnostics(editor, diagnostics);
        if (!currentProblems.isEmpty()) {
            selectBottomTab("PROBLEMS");
        }
    }

    private void selectBottomTab(String tabName) {
        for (Toggle toggle : tabGroup.getToggles()) {
            if (toggle instanceof ToggleButton button && tabName.equalsIgnoreCase(button.getText())) {
                tabGroup.selectToggle(toggle);
                return;
            }
        }
    }

    private void goToProblem(ProblemEntry problem) {
        CodeArea editor = tabManager.getActiveCodeArea();
        if (editor == null) {
            return;
        }
        SourceDiagnostic diagnostic = problem.diagnostic();
        int offset = offsetForLineColumn(editor.getText(), diagnostic.getLine(), diagnostic.getColumn());
        editor.requestFocus();
        editor.moveTo(offset);
        editor.requestFollowCaret();
    }

    private int offsetForLineColumn(String text, int line, int column) {
        int targetLine = Math.max(1, line);
        int targetColumn = Math.max(1, column);

        int currentLine = 1;
        int currentColumn = 1;
        for (int i = 0; i < text.length(); i++) {
            if (currentLine == targetLine && currentColumn == targetColumn) {
                return i;
            }
            char c = text.charAt(i);
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
        }
        return text.length();
    }

    private Path resolveProjectDirectory() {
        File activeFile = tabManager.getActiveFile();
        if (activeFile != null && activeFile.getParentFile() != null) {
            return activeFile.getParentFile().toPath();
        }
        if (selectedDirectory != null) {
            return selectedDirectory.toPath();
        }
        return Path.of(System.getProperty("user.dir"));
    }

    private void appendOutput(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        outputPanel.appendText(message.endsWith("\n") ? message : message + "\n");
    }

    private static LanguageRuntimePlugin resolveLanguagePlugin() {
        ServiceLoader<LanguageRuntimePlugin> loader = ServiceLoader.load(LanguageRuntimePlugin.class);
        Optional<LanguageRuntimePlugin> maybe = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(plugin -> "ricardious".equalsIgnoreCase(plugin.commandName()))
                .findFirst();
        return maybe.orElseGet(() -> loader.findFirst().orElse(null));
    }

    private void setupWindowControls() {
        btnClose.setOnAction(event -> closeWindow());
        btnMin.setOnAction(event -> minimizeWindow());
        btnRest.setOnAction(event -> restoreMaximizeWindow());
    }

    public void hideTerminalPanel() {
        terminalPanel.setVisible(false);
        if (!rightSplitPane.getDividers().isEmpty()) {
            rightSplitPane.setDividerPosition(0, 1.0);
        }
    }

    private void showTerminalPanelWithAnimation() {
        if (!terminalPanel.isVisible()) {
            terminalPanel.setVisible(true);

            terminalPanel.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), terminalPanel);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            if (!rightSplitPane.getDividers().isEmpty()) {
                Timeline timeline = new Timeline();
                KeyValue kv = new KeyValue(rightSplitPane.getDividers().get(0).positionProperty(), 0.6368);
                KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
                timeline.getKeyFrames().add(kf);
                timeline.play();
            }
        }
    }

    private void updateToggleButtons(boolean isExpanded) {
        btnMinimizePanel.setVisible(isExpanded);
        btnMaximizePanel.setVisible(!isExpanded);
    }

    private void closeWindow() {
        if (!confirmSaveBeforeAction("salir")) {
            return;
        }
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private boolean confirmSaveBeforeAction(String actionText) {
        List<Tab> dirtyTabs = tabManager.getDirtyTabs();
        if (dirtyTabs.isEmpty()) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        DialogStyler.apply(alert);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Hay archivos sin guardar");

        StringBuilder content = new StringBuilder("Se detectaron cambios en:\n");
        for (int i = 0; i < dirtyTabs.size(); i++) {
            if (i >= 6) {
                content.append("... y ").append(dirtyTabs.size() - i).append(" archivo(s) más.\n");
                break;
            }
            content.append("• ").append(tabManager.getTabDisplayName(dirtyTabs.get(i))).append('\n');
        }
        content.append("\n¿Deseas guardar antes de ").append(actionText).append("?");
        alert.setContentText(content.toString());

        ButtonType saveButton = new ButtonType("Guardar y salir");
        ButtonType dontSaveButton = new ButtonType("Salir sin guardar");
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancelButton) {
            return false;
        }

        if (result.get() == saveButton) {
            for (Tab tab : dirtyTabs) {
                tabManager.saveFile(tab);
            }
        }

        return true;
    }

    private void minimizeWindow() {
        Stage stage = (Stage) btnMin.getScene().getWindow();
        stage.setIconified(true);
    }

    private void restoreMaximizeWindow() {
        Stage stage = (Stage) btnRest.getScene().getWindow();
        isMaximized = !isMaximized;
        stage.setMaximized(isMaximized);

        Image iconToUse = isMaximized
                ? ResourceManager.getInstance().getRestoreIcon()
                : ResourceManager.getInstance().getMaximizeIcon();
        if (iconToUse != null) {
            btnRest.setGraphic(new ImageView(iconToUse));
        }
    }

    @FXML
    private void handleOpenFolder() {
        openFolderFromChooser();
    }

    @FXML
    private void handleMenuOpenFolder(ActionEvent event) {
        openFolderFromChooser();
    }

    @FXML
    private void handleMenuCloseFolder(ActionEvent event) {
        closeCurrentProject();
    }

    private void openFolderFromChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        Stage stage = (Stage) btnOpenFolder.getScene().getWindow();
        File chosenDirectory = directoryChooser.showDialog(stage);
        if (chosenDirectory == null) {
            return;
        }
        if (!confirmSaveBeforeAction("abrir otra carpeta")) {
            return;
        }
        switchToProject(chosenDirectory);
    }

    private void switchToProject(File directory) {
        closeAllProjectTabs();
        selectedDirectory = directory;
        explorer.setRoot(null);
        loadDirectoryWithProgress(directory);
        terminal.setCurrentDir(directory.getAbsolutePath());
        refreshSourceControlStatus();
    }

    private void closeCurrentProject() {
        if (selectedDirectory == null && (explorer.getRoot() == null || explorer.getRoot().getChildren().isEmpty())) {
            return;
        }
        if (!confirmSaveBeforeAction("cerrar la carpeta")) {
            return;
        }
        closeAllProjectTabs();
        selectedDirectory = null;
        explorer.setRoot(null);
        checkExplorerState();
        searchInput.clear();
        searchResultsList.getItems().clear();
        sourceControlList.getItems().setAll(new SourceControlEntry("..", "No folder opened.", null));
        terminal.setCurrentDir(System.getProperty("user.home"));
    }

    private void closeAllProjectTabs() {
        List<Tab> toClose = new ArrayList<>();
        for (Tab tab : editorTabPane.getTabs()) {
            if (tab != welcomeTab) {
                toClose.add(tab);
            }
        }
        for (Tab tab : toClose) {
            tabManager.closeTab(tab);
        }
        if (welcomeTab != null) {
            editorTabPane.getSelectionModel().select(welcomeTab);
        }
    }


    private void loadDirectoryWithProgress(File directory) {
        Pair<Stage, ProgressBar> progressDialog = createProgressDialog();
        Stage progressStage = progressDialog.getKey();
        ProgressBar progressBar = progressDialog.getValue();
        progressStage.show();

        Task<TreeItem<String>> task = fileManager.buildFileTreeTask(directory);
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            TreeItem<String> root = task.getValue();
            if (root != null) {
                explorer.setCellFactory(tv -> new FileTreeCell(selectedDirectory));
                explorer.setRoot(root);
                root.setExpanded(true);
            }
            checkExplorerState();
            progressBar.progressProperty().unbind();
            progressStage.close();

            showTerminalPanelWithAnimation();
        });

        task.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressStage.close();
            Throwable exception = task.getException();
            if (exception != null) {
                LOGGER.log(Level.SEVERE, "Error loading directory", exception);
            }
        });

        new Thread(task).start();
    }

    private void checkExplorerState() {
        if (explorer.getRoot() == null || explorer.getRoot().getChildren().isEmpty()) {
            emptyExplorerMessage.setVisible(true);
            explorer.setVisible(false);
        } else {
            emptyExplorerMessage.setVisible(false);
            explorer.setVisible(true);
        }
    }

    @FXML
    private void handleButtonClick(ActionEvent event) {
        explorerPanel.setVisible(false);
        searchPanel.setVisible(false);
        sourceControlPanel.setVisible(false);

        if (event.getSource() == explorerBtn) {
            explorerPanel.setVisible(true);
        } else if (event.getSource() == searchBtn) {
            searchPanel.setVisible(true);
        } else if (event.getSource() == toolsBtn) {
            sourceControlPanel.setVisible(true);
            refreshSourceControlStatus();
        }
    }

    private void addDoubleClickHandler(Button button) {
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                if (leftSplitPane.getDividerPositions()[0] > 0.05) {
                    lastDividerPosition = leftSplitPane.getDividerPositions()[0];
                    leftSplitPane.setDividerPositions(0.0);
                } else {
                    leftSplitPane.setDividerPositions(lastDividerPosition);
                }
                event.consume();
            }
        });
    }

    private void handleTreeViewClick(MouseEvent event) {
        TreeItem<String> selectedItem = explorer.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        File file = fileManager.getFileFromTreeItem(selectedItem, selectedDirectory);
        if (file == null || !file.isFile()) return;

        if (event.getClickCount() == 1) {
            tabManager.showPreview(file, fileManager);
        } else if (event.getClickCount() == 2) {
            tabManager.openFile(file, fileManager);
        }
    }

    private Pair<Stage, ProgressBar> createProgressDialog() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);

        Label label = new Label("Loading files...");

        VBox root = new VBox(10, label, progressBar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 250, 150);
        progressBar.getStyleClass().add("custom-progress-bar");

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        return new Pair<>(dialog, progressBar);
    }

    @FXML
    private void handleRunAction(ActionEvent event) {
        runAllTabs();
    }

    @FXML
    private void handleRunAllFilesAction(ActionEvent event) {
        List<File> openFiles = tabManager.getOpenFiles();
        if (openFiles.isEmpty()) {
            terminal.displayOutput("No hay archivos abiertos para ejecutar.");
            return;
        }

        terminal.displayOutput("Ejecutando run sobre " + openFiles.size() + " archivo(s) abiertos...");
        for (File file : openFiles) {
            terminal.executeCommandFromUi("run " + file.getAbsolutePath());
        }
    }

    @FXML
    private void handleRunProjectAction(ActionEvent event) {
        if (selectedDirectory == null || !selectedDirectory.isDirectory()) {
            terminal.displayOutput("No hay una carpeta de proyecto abierta.");
            return;
        }
        terminal.executeCommandFromUi("run " + selectedDirectory.getAbsolutePath());
    }

    @FXML
    private void handleDebugAction(ActionEvent event) {
        terminal.displayOutput("Debug aún no está implementado. Ejecutando run del archivo activo.");
        runAllTabs();
    }

    private record ProblemEntry(SourceDiagnostic diagnostic) {
        String prettyText() {
            return "[" + diagnostic.getType() + "] L" + diagnostic.getLine() + ":C" + diagnostic.getColumn()
                    + " - " + diagnostic.getMessage();
        }
    }

    private record SearchResultEntry(File file, int line, String snippet, boolean contentMatch) {
        String prettyText() {
            if (file == null) {
                return snippet;
            }
            String name = file.getName();
            if (contentMatch) {
                return name + "  |  L" + line + ": " + snippet;
            }
            return name + "  |  " + snippet;
        }
    }

    private record SourceControlEntry(String status, String path, Path repoRoot) {
        String prettyText() {
            return String.format("%-2s | %s", status == null ? "??" : status, path == null ? "" : path);
        }
    }

    @FXML
    private void handleMaximizePanel() {
        originalDividerPosition = rightSplitPane.getDividerPositions()[0];

        rightSplitPane.setDividerPosition(0, 0.0);

        btnMaximizePanel.setVisible(false);
        btnMaximizePanel.setManaged(false);
        btnMinimizePanel.setVisible(true);
        btnMinimizePanel.setManaged(true);
    }

    @FXML
    private void handleMinimizePanel() {
        rightSplitPane.setDividerPosition(0, originalDividerPosition);

        btnMaximizePanel.setVisible(true);
        btnMaximizePanel.setManaged(true);
        btnMinimizePanel.setVisible(false);
        btnMinimizePanel.setManaged(false);
    }

    @FXML
    private void handleClosePanel() {
        originalDividerPosition = rightSplitPane.getDividerPositions()[0];

        rightSplitPane.setDividerPosition(0, 1.0);

        rightSplitPane.getDividers().get(0).setPosition(1.0);

        btnMaximizePanel.setVisible(true);
        btnMinimizePanel.setVisible(false);
    }

    @FXML
    private void handleCloneRepository() {
        Dialog<String> dialog = new Dialog<>();
        DialogStyler.apply(dialog);
        dialog.setTitle("Clone Repository");
        dialog.setHeaderText("Enter the repository URL to clone");

        ButtonType cloneButtonType = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cloneButtonType, ButtonType.CANCEL);

        TextField urlField = new TextField();
        urlField.setPromptText("https://github.com/user/repository.git");
        urlField.setPrefWidth(400);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Repository URL:"),
                urlField
        );
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);

        Platform.runLater(() -> urlField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cloneButtonType) {
                return urlField.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(url -> {
            if (!url.isEmpty()) {
                cloneRepository(url);
            }
        });
    }

    private void cloneRepository(String repositoryUrl) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination folder");
        Stage stage = (Stage) btnCloneRepository.getScene().getWindow();
        File destinationDir = directoryChooser.showDialog(stage);

        if (destinationDir != null) {
            String projectName = extractProjectNameFromUrl(repositoryUrl);
            File projectDir = new File(destinationDir, projectName);

            if (projectDir.exists()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                DialogStyler.apply(confirmAlert);
                confirmAlert.setTitle("Directory Exists");
                confirmAlert.setHeaderText("The directory '" + projectName + "' already exists");
                confirmAlert.setContentText("Do you want to continue? This may overwrite existing files.");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.get() != ButtonType.OK) {
                    return;
                }
            }

            Pair<Stage, ProgressBar> progressDialog = createProgressDialog("Cloning repository...");
            Stage progressStage = progressDialog.getKey();
            ProgressBar progressBar = progressDialog.getValue();
            progressStage.show();

            Task<Void> cloneTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Cloning repository...");
                    updateProgress(-1, 1);

                    try {
                        cloneWithGitCommand(repositoryUrl, projectDir);

                        Platform.runLater(() -> {
                            selectedDirectory = projectDir;
                            loadDirectoryWithProgress(projectDir);
                            terminal.setCurrentDir(projectDir.getAbsolutePath());
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showErrorDialog("Clone Error",
                                    "Failed to clone repository: " + e.getMessage());
                        });
                        throw e;
                    }

                    return null;
                }
            };

            cloneTask.setOnSucceeded(e -> {
                progressStage.close();
                showInfoDialog("Success",
                        "Repository cloned successfully to: " + projectDir.getAbsolutePath());
            });

            cloneTask.setOnFailed(e -> {
                progressStage.close();
                LOGGER.log(Level.SEVERE, "Error cloning repository", cloneTask.getException());
            });

            new Thread(cloneTask).start();
        }
    }

    private String extractProjectNameFromUrl(String repositoryUrl) {
        String cleanUrl = repositoryUrl.replaceAll("https?://", "")
                .replaceAll("\\.git$", "");

        String[] parts = cleanUrl.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }

        return "cloned-repository";
    }

    private void cloneWithGitCommand(String repositoryUrl, File projectDir) throws Exception {
        File parentDir = projectDir.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "git", "clone", repositoryUrl, projectDir.getAbsolutePath()
        );

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                StringBuilder error = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new RuntimeException("Git clone failed: " + error.toString());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogStyler.apply(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogStyler.apply(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Pair<Stage, ProgressBar> createProgressDialog(String message) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);

        Label label = new Label(message);

        VBox root = new VBox(10, label, progressBar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 300, 150);
        progressBar.getStyleClass().add("custom-progress-bar");

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        return new Pair<>(dialog, progressBar);
    }
}
