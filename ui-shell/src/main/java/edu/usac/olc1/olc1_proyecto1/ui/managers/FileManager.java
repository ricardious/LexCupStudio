package edu.usac.olc1.olc1_proyecto1.ui.managers;

import javafx.concurrent.Task;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileManager {

    public void loadFilesIntoTreeView(File directory, TreeView<String> explorer) {
        TreeItem<String> root = new TreeItem<>(directory.getName());
        addFilesRecursively(root, directory);
        explorer.setRoot(root);
    }

    public File createNewFile(File directory) {
        TextInputDialog dialog = new TextInputDialog("new_file.txt");
        dialog.setTitle("Create New File");
        dialog.setHeaderText("Enter the file name:");
        dialog.setContentText("File name:");
        System.out.println("Trying to create file in: " + directory.getAbsolutePath());
        return dialog.showAndWait().map(fileName -> {
            File newFile = new File(directory, fileName);
            try {
                if (newFile.createNewFile()) {
                    System.out.println("File created: " + newFile.getAbsolutePath());
                    return newFile;
                } else {
                    System.out.println("File already exists.");
                    return null;
                }
            } catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
                return null;
            }
        }).orElse(null);
    }

    public File createNewFolder(File directory) {
        TextInputDialog dialog = new TextInputDialog("New Folder");
        dialog.setTitle("Create New Folder");
        dialog.setHeaderText("Enter the folder name:");
        dialog.setContentText("Folder name:");
        System.out.println("Trying to create folder in: " + directory.getAbsolutePath());
        return dialog.showAndWait().map(folderName -> {
            File newFolder = new File(directory, folderName);
            if (newFolder.mkdir()) {
                System.out.println("Folder created: " + newFolder.getAbsolutePath());
                return newFolder;
            } else {
                System.out.println("Failed to create folder. It may already exist.");
                return null;
            }
        }).orElse(null);
    }

    public void addFilesRecursively(TreeItem<String> parent, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            List<File> directories = new ArrayList<>();
            List<File> regularFiles = new ArrayList<>();

            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else {
                    regularFiles.add(file);
                }
            }

            directories.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            regularFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File dir : directories) {
                TreeItem<String> newItem = new TreeItem<>(dir.getName());
                addFilesRecursively(newItem, dir);
                parent.getChildren().add(newItem);
            }

            // Then add files
            for (File file : regularFiles) {
                TreeItem<String> newItem = new TreeItem<>(file.getName());
                parent.getChildren().add(newItem);
            }
        }
    }

    public String readFileContent(File file) {
        if (!file.exists() || !file.isFile()) {
            return "Error: The file does not exist or is not a valid file.";
        }
        if (!file.canRead()) {
            return "Error: Cannot read the file. Check permissions.";
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            return "Error reading the file: " + file.getName() + "\nDetails: " + e.getMessage();
        }
        return content.toString();
    }

    public File getFileFromTreeItem(TreeItem<String> item, File baseDirectory) {
        if (item == null) return null;
        if (item.getParent() == null) {
            return baseDirectory;
        }
        String relativePath = buildRelativePath(item);
        return new File(baseDirectory, relativePath);
    }

    private String buildRelativePath(TreeItem<String> item) {
        StringBuilder pathBuilder = new StringBuilder(item.getValue());
        TreeItem<String> parent = item.getParent();
        while (parent != null && parent.getParent() != null) {
            pathBuilder.insert(0, parent.getValue() + File.separator);
            parent = parent.getParent();
        }
        return pathBuilder.toString();
    }

    public static void saveFile(File file, CodeArea codeArea) {
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(codeArea.getText());
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
        codeArea.getUndoManager().mark();
    }

    public boolean rename(File file, String newName) {
        if (file == null || newName == null || newName.trim().isEmpty()) return false;
        File newFile = new File(file.getParentFile(), newName);
        return file.renameTo(newFile);
    }

    public boolean delete(File file) {
        if (file == null) return false;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!delete(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }


    public Task<TreeItem<String>> buildFileTreeTask(File directory) {
        return new Task<>() {
            @Override
            protected TreeItem<String> call() throws Exception {
                int total = countFilesRecursively(directory);
                int[] processed = new int[]{0};
                TreeItem<String> root = new TreeItem<>(directory.getName());
                addFilesRecursivelyWithProgress(root, directory, total, processed);
                return root;
            }

            private void addFilesRecursivelyWithProgress(TreeItem<String> parent, File directory, int total, int[] processed) {
                File[] files = directory.listFiles();
                if (files != null) {
                    java.util.List<File> directories = new java.util.ArrayList<>();
                    java.util.List<File> regularFiles = new java.util.ArrayList<>();
                    for (File file : files) {
                        if (file.isDirectory()) {
                            directories.add(file);
                        } else {
                            regularFiles.add(file);
                        }
                    }
                    directories.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
                    regularFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

                    for (File dir : directories) {
                        TreeItem<String> newItem = new TreeItem<>(dir.getName());
                        parent.getChildren().add(newItem);
                        processed[0]++;
                        updateProgress(processed[0], total);
                        addFilesRecursivelyWithProgress(newItem, dir, total, processed);
                    }
                    for (File file : regularFiles) {
                        TreeItem<String> newItem = new TreeItem<>(file.getName());
                        parent.getChildren().add(newItem);
                        processed[0]++;
                        updateProgress(processed[0], total);
                    }
                }
            }

            private int countFilesRecursively(File directory) {
                int count = 0;
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        count++;
                        if (file.isDirectory()) {
                            count += countFilesRecursively(file);
                        }
                    }
                }
                return count;
            }
        };
    }
}
