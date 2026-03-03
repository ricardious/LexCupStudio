package edu.usac.olc1.olc1_proyecto1.ui.controllers;

import edu.usac.olc1.olc1_proyecto1.ui.components.FileTreeCell;
import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;
import edu.usac.olc1.olc1_proyecto1.ui.managers.AutoCompletionManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.FileManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.SyntaxHighlightingManager;
import edu.usac.olc1.olc1_proyecto1.ui.managers.TabManager;
import edu.usac.olc1.olc1_proyecto1.ui.utils.BrandingConfig;
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
    private AnchorPane sourceControlPanel;


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

    /**
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new WindowDragger().makeDraggable(titleBar);
        setupWindowControls();

        Tab welcomeTab = editorTabPane.getTabs().get(0);
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

        hideTerminalPanel();

        Platform.runLater(this::initializePanelButtons);

        terminal.registerCommand(
                "ricardious",
                new edu.usac.olc1.olc1_proyecto1.ui.components.commands.RicardiousCommandHandler(
                        terminal,
                        () -> {
                            java.io.File f = null;
                            try {
                                f = tabManager.getActiveFile();
                            } catch (Exception ignore) {}
                            return f;
                        }
                )
        );


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
        } else {
            System.err.println("Warning: SplitPane does not have any dividers yet");
        }
    }

    private void runAllTabs() {
        CodeArea activeEditor = tabManager.getActiveCodeArea();
        if (activeEditor == null) {
            appendOutput("No hay un editor de código activo para ejecutar.");
            return;
        }

        analyzeEditor(activeEditor, true);

        if (languagePlugin == null) {
            appendOutput("No hay plugin de lenguaje cargado (LanguageRuntimePlugin).");
            return;
        }

        Path projectDir = resolveProjectDirectory();
        boolean ok = languagePlugin.run(
                activeEditor.getText(),
                projectDir,
                this::appendOutput
        );

        if (ok) {
            appendOutput("Ejecución completada. Reportes en: " + projectDir.resolve(languagePlugin.reportsDirectoryName()));
        } else {
            appendOutput("Ejecución finalizada con errores.");
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
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        Stage stage = (Stage) btnOpenFolder.getScene().getWindow();
        selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            explorer.setRoot(null);
            loadDirectoryWithProgress(selectedDirectory);
            terminal.setCurrentDir(selectedDirectory.getAbsolutePath());
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

    private record ProblemEntry(SourceDiagnostic diagnostic) {
        String prettyText() {
            return "[" + diagnostic.getType() + "] L" + diagnostic.getLine() + ":C" + diagnostic.getColumn()
                    + " - " + diagnostic.getMessage();
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
