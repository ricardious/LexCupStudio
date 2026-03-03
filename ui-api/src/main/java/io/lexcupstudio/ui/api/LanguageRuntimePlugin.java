package io.lexcupstudio.ui.api;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface LanguageRuntimePlugin {
    String commandName();
    String reportsDirectoryName();
    boolean run(String sourceText, Path projectDir, Consumer<String> log);

    default List<SourceDiagnostic> analyze(String sourceText, Path projectDir, Consumer<String> log) {
        return List.of();
    }
}
