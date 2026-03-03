package edu.usac.olc1.olc1_proyecto1.ui.managers;

import edu.usac.olc1.olc1_proyecto1.ui.components.ImageViewer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabManager {

    private Tab currentPreviewTab;
    private final TabPane editorTabPane;
    private final StackPane tabContentArea;
    private final Map<Tab, File> fileMap = new HashMap<>();
    private final Map<Tab, VirtualizedScrollPane<CodeArea>> contentMap = new HashMap<>();
    private final Map<Tab, ImageViewer> imageViewerMap = new HashMap<>();
    private final Map<Tab, Node> nodeMap = new HashMap<>();
    private final Map<Tab, Subscription> highlightSubscriptionMap = new HashMap<>();
    private final SyntaxHighlightingManager syntaxHighlightingManager = new SyntaxHighlightingManager();

    public TabManager(TabPane editorTabPane, StackPane tabContentArea) {
        this.editorTabPane = editorTabPane;
        this.tabContentArea = tabContentArea;
        setupTabSelectionListener();
    }

    private void setupTabSelectionListener() {
        editorTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            tabContentArea.getChildren().clear();

            if (newTab != null) {
                if (nodeMap.containsKey(newTab)) {
                    Node content = nodeMap.get(newTab);
                    tabContentArea.getChildren().add(content);
                } else if (contentMap.containsKey(newTab)) {
                    tabContentArea.getChildren().add(contentMap.get(newTab));
                } else if (imageViewerMap.containsKey(newTab)) {
                    tabContentArea.getChildren().add(imageViewerMap.get(newTab));
                }
            }
        });
    }

    public Tab getCurrentPreviewTab() {
        return currentPreviewTab;
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg", ".tiff", ".webp"};
        return Arrays.stream(imageExtensions)
                .anyMatch(fileName.toLowerCase()::endsWith);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private VBox createImageViewer(File imageFile) {
        return new VBox();
    }

    public void showPreview(File file, FileManager fileManager) {
        if (currentPreviewTab != null) {
            unsubscribeHighlight(currentPreviewTab);
            editorTabPane.getTabs().remove(currentPreviewTab);
            contentMap.remove(currentPreviewTab);
            imageViewerMap.remove(currentPreviewTab);
        }
        currentPreviewTab = new Tab();

        Label tabLabel = new Label("_" + file.getName() + "_");
        tabLabel.getStyleClass().addAll("tab-label", "preview-tab-label");

        tabLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                openFile(file, fileManager);
            }
        });
        currentPreviewTab.setGraphic(tabLabel);

        if (isImageFile(file.getName())) {
            ImageViewer imageViewer = new ImageViewer(file);
            imageViewerMap.put(currentPreviewTab, imageViewer);
            // currentPreviewTab.setContent(imageViewer);
        } else {
            CodeArea previewEditor = new CodeArea();
            previewEditor.setParagraphGraphicFactory(LineNumberFactory.get(previewEditor));
            previewEditor.appendText(fileManager.readFileContent(file));
            previewEditor.setEditable(false);

            previewEditor.moveTo(0);
            previewEditor.requestFollowCaret();
            highlightSubscriptionMap.put(currentPreviewTab, syntaxHighlightingManager.bind(previewEditor));

            VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(previewEditor);
            contentMap.put(currentPreviewTab, scrollPane);
            // currentPreviewTab.setContent(scrollPane);
        }

        editorTabPane.getTabs().add(currentPreviewTab);
        editorTabPane.getSelectionModel().select(currentPreviewTab);
    }

    public void openFile(File file, FileManager fileManager) {
        for (Map.Entry<Tab, File> entry : fileMap.entrySet()) {
            if (entry.getValue().equals(file)) {
                editorTabPane.getSelectionModel().select(entry.getKey());
                return;
            }
        }

        if (currentPreviewTab != null) {
            unsubscribeHighlight(currentPreviewTab);
            editorTabPane.getTabs().remove(currentPreviewTab);
            contentMap.remove(currentPreviewTab);
            imageViewerMap.remove(currentPreviewTab);
            currentPreviewTab = null;
        }

        Tab tab = new Tab(file.getName());

        if (isImageFile(file.getName())) {
            ImageViewer imageViewer = new ImageViewer(file);
            imageViewerMap.put(tab, imageViewer);
            // tab.setContent(imageViewer);

            try {
                ImageView tabIcon = new ImageView(new Image(getClass().getResourceAsStream("/edu/usac/olc1/olc1_proyecto1/icons/image.png")));
                tabIcon.setFitWidth(16);
                tabIcon.setFitHeight(16);
                tab.setGraphic(tabIcon);
            } catch (Exception e) {

            }
        } else {
            CodeArea fileEditor = new CodeArea();
            fileEditor.setParagraphGraphicFactory(LineNumberFactory.get(fileEditor));
            fileEditor.appendText(fileManager.readFileContent(file));
            fileEditor.setEditable(true);

            fileEditor.moveTo(0);
            fileEditor.requestFollowCaret();

            fileEditor.getUndoManager().forgetHistory();
            fileEditor.getUndoManager().mark();

            addChangeListenerToCodeArea(fileEditor, tab, file.getName());
            highlightSubscriptionMap.put(tab, syntaxHighlightingManager.bind(fileEditor));

            VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(fileEditor);
            contentMap.put(tab, scrollPane);
            // tab.setContent(scrollPane);
        }

        editorTabPane.getTabs().add(tab);
        editorTabPane.getSelectionModel().select(tab);
        fileMap.put(tab, file);

    }

    private void addChangeListenerToCodeArea(CodeArea codeArea, Tab tab, String fileName) {
        codeArea.getUndoManager().mark();

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!tab.getText().endsWith(" ●")) {
                tab.setText(fileName + " ●");
            }
        });

        codeArea.getUndoManager().atMarkedPositionProperty().addListener((obs, wasMarked, isMarked) -> {
            if (isMarked) {
                tab.setText(fileName);
            }
        });

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                saveFile(tab);
                event.consume();
            }
        });
    }

    public void saveFile(Tab tab) {
        if (!fileMap.containsKey(tab) || !contentMap.containsKey(tab)) return;

        File file = fileMap.get(tab);
        VirtualizedScrollPane<CodeArea> scrollPane = contentMap.get(tab);
        CodeArea codeArea = scrollPane.getContent();

        FileManager.saveFile(file, codeArea);
        tab.setText(file.getName());
    }

    public void closeTab(Tab tab) {
        unsubscribeHighlight(tab);
        contentMap.remove(tab);
        fileMap.remove(tab);
        editorTabPane.getTabs().remove(tab);
    }

    private void unsubscribeHighlight(Tab tab) {
        Subscription subscription = highlightSubscriptionMap.remove(tab);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public File getActiveFile() {
        Tab selectedTab = editorTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return null;
        return fileMap.get(selectedTab);
    }

    public CodeArea getActiveCodeArea() {
        Tab selectedTab = editorTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return null;
        VirtualizedScrollPane<CodeArea> scrollPane = contentMap.get(selectedTab);
        if (scrollPane == null) return null;
        return scrollPane.getContent();
    }

    public void registerTab(Tab tab, Node content) {
        if (content != null) {
            nodeMap.put(tab, content);

            if (editorTabPane.getSelectionModel().getSelectedItem() == tab) {
                tabContentArea.getChildren().clear();
                tabContentArea.getChildren().add(content);
            }
        }
    }

    public List<Tab> getDirtyTabs() {
        List<Tab> dirtyTabs = new ArrayList<>();
        for (Tab tab : editorTabPane.getTabs()) {
            if (isDirtyTab(tab)) {
                dirtyTabs.add(tab);
            }
        }
        return dirtyTabs;
    }

    public boolean isDirtyTab(Tab tab) {
        if (tab == null) {
            return false;
        }
        return tab.getText() != null && tab.getText().endsWith(" ●");
    }

    public String getTabDisplayName(Tab tab) {
        if (tab == null || tab.getText() == null) {
            return "";
        }
        return tab.getText().replace(" ●", "");
    }
}
