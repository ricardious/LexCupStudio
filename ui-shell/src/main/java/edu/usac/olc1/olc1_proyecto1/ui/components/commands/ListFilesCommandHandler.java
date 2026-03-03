package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ListFilesCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        String path = ".";
        boolean detailedList = false;

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.contains("l")) {
                    detailedList = true;
                }
            } else {
                path = arg;
            }
        }

        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }

        File dir = new File(path);

        if (!dir.exists()) {
            return "ls: No se puede acceder a '" + path + "': No existe el archivo o directorio\n";
        }

        if (!dir.isDirectory()) {
            return dir.getName() + "\n";
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return "ls: No se puede acceder a '" + path + "': Permiso denegado\n";
        }

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        StringBuilder result = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm");

        for (File file : files) {
            if (detailedList) {
                String permissions = (file.isDirectory() ? "d" : "-") +
                        (file.canRead() ? "r" : "-") +
                        (file.canWrite() ? "w" : "-") +
                        (file.canExecute() ? "x" : "-") +
                        "------";

                String size = String.format("%8d", file.length());
                String date = dateFormat.format(new Date(file.lastModified()));
                String name = file.getName() + (file.isDirectory() ? "/" : "");

                result.append(permissions).append(" ")
                        .append(size).append(" ")
                        .append(date).append(" ")
                        .append(name).append("\n");
            } else {
                result.append(file.getName())
                        .append(file.isDirectory() ? "/" : "")
                        .append("  ");
            }
        }

        if (!detailedList && files.length > 0) {
            result.append("\n");
        }

        return result.toString();
    }
}