package io.lexcupstudio.plugin.example;

import io.lexcupstudio.ui.api.LanguageRuntimePlugin;
import io.lexcupstudio.ui.api.DiagnosticType;
import io.lexcupstudio.ui.api.SourceDiagnostic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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

    @Override
    public List<SourceDiagnostic> analyze(String sourceText, Path projectDir, Consumer<String> log) {
        String text = sourceText == null ? "" : sourceText;
        List<SourceDiagnostic> diagnostics = new ArrayList<>();
        Deque<Bracket> stack = new ArrayDeque<>();

        int line = 1;
        int col = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                line++;
                col = 1;
                continue;
            }

            if (c == '@') {
                diagnostics.add(new SourceDiagnostic(
                        DiagnosticType.LEXICAL, line, col, 1, "Caracter no reconocido: '@'"
                ));
            }

            if (c == '{' || c == '(' || c == '[') {
                stack.push(new Bracket(c, line, col));
            } else if (c == '}' || c == ')' || c == ']') {
                if (stack.isEmpty()) {
                    diagnostics.add(new SourceDiagnostic(
                            DiagnosticType.SYNTAX, line, col, 1, "Cierre sin apertura: '" + c + "'"
                    ));
                } else {
                    Bracket open = stack.pop();
                    if (!matches(open.value(), c)) {
                        diagnostics.add(new SourceDiagnostic(
                                DiagnosticType.SYNTAX,
                                line,
                                col,
                                1,
                                "Se esperaba cierre para '" + open.value() + "' y se obtuvo '" + c + "'"
                        ));
                    }
                }
            }

            col++;
        }

        while (!stack.isEmpty()) {
            Bracket pending = stack.pop();
            diagnostics.add(new SourceDiagnostic(
                    DiagnosticType.SYNTAX,
                    pending.line(),
                    pending.column(),
                    1,
                    "Apertura sin cierre: '" + pending.value() + "'"
            ));
        }

        if (log != null) {
            if (diagnostics.isEmpty()) {
                log.accept("Analisis completado sin errores.");
            } else {
                log.accept("Analisis completado con " + diagnostics.size() + " error(es).");
            }
        }
        return diagnostics;
    }

    private static boolean matches(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '{' && close == '}')
                || (open == '[' && close == ']');
    }

    private record Bracket(char value, int line, int column) {}
}
