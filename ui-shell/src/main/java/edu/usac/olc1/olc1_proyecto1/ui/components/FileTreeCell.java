package edu.usac.olc1.olc1_proyecto1.ui.components;

import edu.usac.olc1.olc1_proyecto1.ui.managers.FileManager;
import edu.usac.olc1.olc1_proyecto1.ui.theme.IconProvider;
import edu.usac.olc1.olc1_proyecto1.ui.utils.SVGLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.File;
import java.net.URL;

public class FileTreeCell extends TreeCell<String> {

    private final HBox container;
    private final Label textLabel;
    private final ImageView iconView;
    private final HBox actionButtons;
    private final File baseDirectory;
    private final FileManager fileManager;
    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
    private static final String MAIN_STYLESHEET = "/edu/usac/olc1/olc1_proyecto1/css/main.css";
    private ContextMenu contextMenu;

    public FileTreeCell(File baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.fileManager = new FileManager();
        this.iconView = new ImageView();
        textLabel = new Label();
        textLabel.getStyleClass().add("tree-cell-label");

        actionButtons = new HBox(5);
        actionButtons.setVisible(false);
        actionButtons.getStyleClass().add("action-buttons");

        Button createFileBtn = new Button();
        Button createFolderBtn = new Button();
        Button collapseBtn = new Button();

        createFileBtn.getStyleClass().add("action-button");
        createFolderBtn.getStyleClass().add("action-button");
        collapseBtn.getStyleClass().add("action-button");

        createFileBtn.setGraphic(SVGLoader.loadSVGIcon("file-plus-2.svg", 16, 16));
        createFolderBtn.setGraphic(SVGLoader.loadSVGIcon("folder-plus.svg", 16, 16));
        collapseBtn.setGraphic(SVGLoader.loadSVGIcon("copy-minus.svg", 16, 16));

        Tooltip.install(createFileBtn, new Tooltip("Create new file"));
        Tooltip.install(createFolderBtn, new Tooltip("Create new folder"));
        Tooltip.install(collapseBtn, new Tooltip("Collapse"));

        createFileBtn.setOnAction(e -> {
            TreeView<String> treeView = getTreeView();
            if (treeView == null) return;

            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) return;

            File selectedFile = fileManager.getFileFromTreeItem(selectedItem, baseDirectory);
            System.out.println("Selected item: " + selectedItem.getValue());
            System.out.println("Selected file path: " + (selectedFile != null ? selectedFile.getAbsolutePath() : "null"));

            if (selectedFile != null) {
                File targetDir = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
                System.out.println("Target directory: " + targetDir.getAbsolutePath());

                File newFile = fileManager.createNewFile(targetDir);
                if (newFile != null) {
                    TreeItem<String> parentItem = selectedFile.isDirectory() ? selectedItem : selectedItem.getParent();
                    if (parentItem != null) {
                        TreeItem<String> newFileItem = new TreeItem<>(newFile.getName());
                        parentItem.getChildren().add(newFileItem);
                        parentItem.setExpanded(true);
                    }
                }
            }
        });

        createFolderBtn.setOnAction(e -> {
            TreeView<String> treeView = getTreeView();
            if (treeView == null) return;

            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) return;

            File selectedFile = fileManager.getFileFromTreeItem(selectedItem, baseDirectory);
            System.out.println("Selected item: " + selectedItem.getValue());
            System.out.println("Selected file path: " + (selectedFile != null ? selectedFile.getAbsolutePath() : "null"));

            if (selectedFile != null) {
                File targetDir = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
                System.out.println("Target directory: " + targetDir.getAbsolutePath());

                File newFolder = fileManager.createNewFolder(targetDir);
                if (newFolder != null) {
                    TreeItem<String> parentItem = selectedFile.isDirectory() ? selectedItem : selectedItem.getParent();
                    if (parentItem != null) {
                        TreeItem<String> newFolderItem = new TreeItem<>(newFolder.getName());
                        parentItem.getChildren().add(newFolderItem);
                        parentItem.setExpanded(true);
                    }
                }
            }
        });

        collapseBtn.setOnAction(e -> {
            if (getTreeItem() != null) {
                getTreeItem().setExpanded(false);
            }
        });

        actionButtons.getChildren().addAll(createFileBtn, createFolderBtn, collapseBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container = new HBox(5, iconView, textLabel, spacer, actionButtons);

        container.setOnMouseEntered(event -> actionButtons.setVisible(true));
        container.setOnMouseExited(event -> actionButtons.setVisible(false));

        setOnDragDetected(this::handleDragDetected);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragDone(this::handleDragDone);

        setOnContextMenuRequested(this::showContextMenu);

        contextMenu = new ContextMenu();
    }


    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            textLabel.setText(item);
            TreeItem<String> treeItem = getTreeItem();

            File file = fileManager.getFileFromTreeItem(treeItem, baseDirectory);

            boolean isExpanded = treeItem != null && treeItem.isExpanded();

            Node icon = IconProvider.getIconForFile(file, isExpanded);

            if (icon instanceof ImageView) {
                iconView.setImage(((ImageView) icon).getImage());
            } else {
                container.getChildren().set(0, icon);
            }

            if (treeItem != null && treeItem.getParent() == null) {
                setGraphic(container);
                setText(null);
            } else {
                HBox simpleContainer = new HBox(5, iconView, textLabel);
                setGraphic(simpleContainer);
                setText(null);
            }
        }
    }

    private void handleDragDetected(MouseEvent event) {
        if (getItem() == null || getTreeItem() == null || getTreeItem().getParent() == null) {
            return;
        }
        Dragboard db = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(JAVA_FORMAT, getTreeItem().getValue());
        db.setContent(content);
        event.consume();
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasContent(JAVA_FORMAT) && getTreeItem() != null) {
            File targetFile = fileManager.getFileFromTreeItem(getTreeItem(), baseDirectory);
            if (targetFile != null && targetFile.isDirectory()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasContent(JAVA_FORMAT) && getTreeItem() != null) {
            String draggedFileName = (String) db.getContent(JAVA_FORMAT);
            TreeItem<String> sourceItem = findTreeItemByName(getTreeView().getRoot(), draggedFileName);
            if (sourceItem != null && sourceItem.getParent() != null) {
                File sourceFile = fileManager.getFileFromTreeItem(sourceItem, baseDirectory);
                File targetDirectory = fileManager.getFileFromTreeItem(getTreeItem(), baseDirectory);
                if (sourceFile != null && targetDirectory != null && targetDirectory.isDirectory()) {
                    File destinationFile = new File(targetDirectory, sourceFile.getName());
                    boolean moved = sourceFile.renameTo(destinationFile);
                    if (moved) {
                        sourceItem.getParent().getChildren().remove(sourceItem);
                        getTreeItem().getChildren().add(sourceItem);
                        getTreeItem().setExpanded(true);
                        success = true;
                    }
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        event.consume();
    }

    private TreeItem<String> findTreeItemByName(TreeItem<String> root, String name) {
        if (root == null) return null;
        if (root.getValue().equals(name)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = findTreeItemByName(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void showContextMenu(ContextMenuEvent event) {
        if (getItem() == null || getTreeItem() == null) return;

        if (contextMenu != null) {
            contextMenu.hide();
        }

        contextMenu = new ContextMenu();

        File file = fileManager.getFileFromTreeItem(getTreeItem(), baseDirectory);
        if (file != null) {
            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(e -> renameFileOrFolder(file, getTreeItem()));

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> deleteFileOrFolder(file, getTreeItem()));

            contextMenu.getItems().addAll(renameItem, deleteItem);

            if (file.isDirectory()) {
                MenuItem newFileItem = new MenuItem("New File");
                MenuItem newFolderItem = new MenuItem("New Folder");

                handleNewFileAction(file, newFileItem, newFolderItem);

                contextMenu.getItems().addAll(new SeparatorMenuItem(), newFileItem, newFolderItem);
            }

            if (getTreeItem().getParent() == null) {
                contextMenu.getItems().clear();
                MenuItem newFileItem = new MenuItem("New File");
                MenuItem newFolderItem = new MenuItem("New Folder");
                MenuItem refreshItem = new MenuItem("Refresh");

                handleNewFileAction(file, newFileItem, newFolderItem);

                refreshItem.setOnAction(e -> refreshTreeView());
                contextMenu.getItems().addAll(newFileItem, newFolderItem, refreshItem);
            }
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        }
    }

    private void handleNewFileAction(File file, MenuItem newFileItem, MenuItem newFolderItem) {
        newFileItem.setOnAction(e -> {
            File newFile = fileManager.createNewFile(file);
            if (newFile != null) {
                TreeItem<String> newFileItem2 = new TreeItem<>(newFile.getName());
                getTreeItem().getChildren().add(newFileItem2);
                getTreeItem().setExpanded(true);
            }
        });

        newFolderItem.setOnAction(e -> {
            File newFolder = fileManager.createNewFolder(file);
            if (newFolder != null) {
                TreeItem<String> newFolderItem2 = new TreeItem<>(newFolder.getName());
                getTreeItem().getChildren().add(newFolderItem2);
                getTreeItem().setExpanded(true);
            }
        });
    }

    private void renameFileOrFolder(File file, TreeItem<String> item) {
        TextInputDialog dialog = new TextInputDialog(file.getName());
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename " + (file.isDirectory() ? "folder" : "file"));
        dialog.setContentText("New name:");
        styleDialog(dialog, "rename-dialog");
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                boolean renamed = fileManager.rename(file, newName);
                if (renamed) {
                    item.setValue(newName);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error renaming");
                    alert.setContentText("Could not rename the " +
                            (file.isDirectory() ? "directory" : "file") +
                            ". Check permissions or if an item with that name already exists.");
                    styleDialog(alert, "rename-dialog");
                    alert.showAndWait();
                }
            }
        });
    }

    private void styleDialog(Dialog<?> dialog, String styleClass) {
        DialogPane pane = dialog.getDialogPane();
        URL cssUrl = FileTreeCell.class.getResource(MAIN_STYLESHEET);
        if (cssUrl != null && pane.getStylesheets().stream().noneMatch(s -> s.equals(cssUrl.toExternalForm()))) {
            pane.getStylesheets().add(cssUrl.toExternalForm());
        }
        if (!pane.getStyleClass().contains(styleClass)) {
            pane.getStyleClass().add(styleClass);
        }
    }

    private void deleteFileOrFolder(File file, TreeItem<String> item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm deletion");
        confirmation.setHeaderText("Are you sure you want to delete the " +
                (file.isDirectory() ? "folder" : "file") +
                " \"" + file.getName() + "\"?");
        if (file.isDirectory()) {
            confirmation.setContentText("This action will delete the folder and all its contents.");
        }
        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean deleted = fileManager.delete(file);
                if (deleted) {
                    if (item.getParent() != null) {
                        item.getParent().getChildren().remove(item);
                    }
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Error deleting");
                    error.setContentText("Could not delete the " +
                            (file.isDirectory() ? "folder" : "file") +
                            ". Check permissions.");
                    error.showAndWait();
                }
            }
        });
    }

    private void refreshTreeView() {
        TreeView<String> tree = getTreeView();
        if (tree != null) {
            TreeItem<String> root = tree.getRoot();
            if (root != null) {
                root.getChildren().clear();
                fileManager.addFilesRecursively(root, baseDirectory);
                root.setExpanded(true);
            }
        }
    }
}
