package io.lexcupstudio.plugin.example;

import edu.usac.olc1.olc1_proyecto1.compiler.CompilerPipeline;
import io.lexcupstudio.ui.api.LanguageRuntimePlugin;

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
        return CompilerPipeline.run(sourceText, projectDir, log);
    }
}
