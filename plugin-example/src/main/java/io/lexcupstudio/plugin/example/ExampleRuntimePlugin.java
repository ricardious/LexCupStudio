package io.lexcupstudio.plugin.example;

import io.lexcupstudio.ui.api.LanguageRuntimePlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class ExampleRuntimePlugin implements LanguageRuntimePlugin {

    @Override
    public String commandName() {
        return "ricardious";
    }

    @Override
    public String reportsDirectoryName() {
        return "output";
    }

    @Override
    public boolean run(String sourceText, Path projectDir, Consumer<String> log) {
        Path outDir = projectDir.resolve(reportsDirectoryName());
        try {
            Files.createDirectories(outDir);
            Path outFile = outDir.resolve("plugin-example-report.txt");
            String content = "LexCupStudio plugin-example executed.\n"
                    + "Input length: " + (sourceText == null ? 0 : sourceText.length()) + "\n";
            Files.writeString(outFile, content, StandardCharsets.UTF_8);
            log.accept("Plugin example ejecutado correctamente.");
            log.accept("Reporte generado en: " + outFile.toAbsolutePath());
            return true;
        } catch (IOException ex) {
            log.accept("Error en plugin-example: " + ex.getMessage());
            return false;
        }
    }
}
