package edu.usac.olc1.olc1_proyecto1.reports;

import edu.usac.olc1.olc1_proyecto1.automata.*;
import edu.usac.olc1.olc1_proyecto1.compiler.CompilerError;
import edu.usac.olc1.olc1_proyecto1.compiler.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ProjectReports {

    private final Path projectDir; // carpeta del archivo ejecutado
    public ProjectReports(Path projectDir) { this.projectDir = projectDir; }

    private Path reportsRoot() throws IOException { return GraphVizGenerator.reportsRoot(projectDir); }

    public Path writeTokensCsv(List<Token> tokens) throws IOException {
        Path out = reportsRoot().resolve("tokens.csv");
        List<String> lines = new ArrayList<>();
        lines.add("Index,Lexeme,Type,Line,Column");
        int i = 1;
        for (Token t : tokens) {
            lines.add(String.format(Locale.ROOT, "%d,%s,%d,%d,%d",
                    i++, csv(t.getLexeme()), t.getType(), t.getLine(), t.getColumn()));
        }
        Files.write(out, lines, StandardCharsets.UTF_8);
        return out;
    }

    public Path writeErrorsCsv(List<CompilerError> errors) throws IOException {
        Path out = reportsRoot().resolve("errors.csv");
        List<String> lines = new ArrayList<>();
        lines.add("Index,Type,Description,Line,Column");
        int i = 1;
        for (CompilerError e : errors) {
            lines.add(String.format(Locale.ROOT, "%d,%s,%s,%d,%d",
                    i++, e.getType(), csv(e.getDescription()), e.getLine(), e.getColumn()));
        }
        Files.write(out, lines, StandardCharsets.UTF_8);
        return out;
    }

    public Path graphAutomatonToPng(Automaton a) throws Exception {
        Path dot = GraphVizGenerator.generateAutomatonGraph(a, projectDir);
        return DotRunner.runDotToPng(dot);
    }

    public Path graphAFDStepsToPng(AFD afd, String input) throws Exception {
        Automaton.ValidationResult vr = afd.validate(input);
        Path dot = GraphVizGenerator.generateAFDStepsGraph(afd, input, vr.steps, vr.accepted, vr.finalState, projectDir);
        return DotRunner.runDotToPng(dot);
    }

    public Path graphAPStepsToPng(AP ap, String input) throws Exception {
        Automaton.ValidationResult vr = ap.validate(input);
        Path dot = GraphVizGenerator.generateAPStepsGraph(ap, input, vr.steps, vr.accepted, vr.finalState, projectDir);
        return DotRunner.runDotToPng(dot);
    }

    private static String csv(String s) {
        if (s == null) return "";
        boolean q = s.contains(",") || s.contains("\"") || s.contains("\n");
        return q ? "\"" + s.replace("\"","\"\"") + "\"" : s;
    }
}
