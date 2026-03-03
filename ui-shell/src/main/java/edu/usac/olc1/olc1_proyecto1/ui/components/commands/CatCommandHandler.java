package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CatCommandHandler implements CommandHandler {
    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            return "Uso: cat [archivo]";
        }

        String filePath = args[0];

        // Handle special paths
        if (filePath.startsWith("~")) {
            filePath = System.getProperty("user.home") + filePath.substring(1);
        }

        File file = new File(filePath);

        if (!file.exists()) {
            return "cat: " + args[0] + ": No existe el archivo";
        }

        if (file.isDirectory()) {
            return "cat: " + args[0] + ": Es un directorio";
        }

        try {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();
            return content.toString();
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        }
    }
}