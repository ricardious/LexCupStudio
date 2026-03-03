package edu.usac.olc1.olc1_proyecto1.reports;

import edu.usac.olc1.olc1_proyecto1.automata.AFD;
import edu.usac.olc1.olc1_proyecto1.automata.AP;
import edu.usac.olc1.olc1_proyecto1.automata.Automaton;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GraphVizGenerator {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /* ====== NUEVO: helpers con baseDir ====== */
    public static Path reportsRoot(Path baseDir) throws IOException {
        Path r = baseDir.resolve("output");
        Files.createDirectories(r);
        return r;
    }
    public static Path automataDir(Path baseDir) throws IOException {
        Path d = reportsRoot(baseDir).resolve("automata");
        Files.createDirectories(d);
        return d;
    }
    public static Path afdStepsDir(Path baseDir) throws IOException {
        Path d = reportsRoot(baseDir).resolve("steps").resolve("afd");
        Files.createDirectories(d);
        return d;
    }
    public static Path apStepsDir(Path baseDir) throws IOException {
        Path d = reportsRoot(baseDir).resolve("steps").resolve("ap");
        Files.createDirectories(d);
        return d;
    }

    /* ====== NUEVO: overloads que usan baseDir ====== */
    public static Path generateAFDGraph(AFD afd, Path baseDir) throws IOException {
        Path outputDir = automataDir(baseDir);
        Path dotFile = outputDir.resolve("AFD_" + afd.getName() + "_" + TS.format(LocalDateTime.now()) + ".dot");
        try (BufferedWriter w = Files.newBufferedWriter(dotFile)) {
            w.write("digraph " + afd.getName() + " {\n");
            w.write("  rankdir=LR;\n");
            w.write("  node [shape = circle];\n");
            w.write("  __start [shape=point];\n");
            w.write("  __start -> \"" + afd.getInitial() + "\";\n");
            for (String acc : afd.getAccept()) w.write("  \"" + acc + "\" [shape=doublecircle];\n");
            for (String from : afd.getDelta().keySet()) {
                for (var e : afd.getDelta().get(from).entrySet()) {
                    w.write("  \"" + from + "\" -> \"" + e.getValue() + "\" [label=\"" + e.getKey() + "\"];\n");
                }
            }
            w.write("}\n");
        }
        return dotFile;
    }

    public static Path generateAPGraph(AP ap, Path baseDir) throws IOException {
        Path outputDir = automataDir(baseDir);
        Path dotFile = outputDir.resolve("AP_" + ap.getName() + "_" + TS.format(LocalDateTime.now()) + ".dot");
        try (BufferedWriter w = Files.newBufferedWriter(dotFile)) {
            w.write("digraph " + ap.getName() + " {\n");
            w.write("  rankdir=LR;\n");
            w.write("  node [shape = circle];\n");
            w.write("  __start [shape=point];\n");
            w.write("  __start -> \"" + ap.getInitial() + "\";\n");
            for (String acc : ap.getAccept()) w.write("  \"" + acc + "\" [shape=doublecircle];\n");
            for (AP.TransitionAP t : ap.getTransitions()) {
                String label = String.format("(%s, %s/%s)",
                        "$".equals(t.input) ? "ε" : t.input,
                        "$".equals(t.pop) ? "ε" : t.pop,
                        "$".equals(t.push) ? "ε" : t.push);
                w.write("  \"" + t.from + "\" -> \"" + t.to + "\" [label=\"" + label + "\"];\n");
            }
            w.write("}\n");
        }
        return dotFile;
    }

    public static Path generateAutomatonGraph(Automaton automaton, Path baseDir) throws IOException {
        if (automaton instanceof AFD afd) return generateAFDGraph(afd, baseDir);
        if (automaton instanceof AP ap) return generateAPGraph(ap, baseDir);
        throw new IllegalArgumentException("Unsupported automaton type: " + automaton.getType());
    }

    public static Path generateAFDStepsGraph(AFD afd, String input,
                                             java.util.List<String> steps, boolean accepted, String finalState,
                                             Path baseDir) throws IOException {
        Path dir = afdStepsDir(baseDir);
        Path dotFile = dir.resolve("AFDSteps_" + afd.getName() + "_" + TS.format(LocalDateTime.now()) + ".dot");
        try (BufferedWriter w = Files.newBufferedWriter(dotFile)) {
            String status = accepted ? "ACCEPT" : "REJECT";
            w.write("digraph AFD_STEPS { rankdir=TB; node [shape=box];\n");
            w.write("title [shape=plaintext, label=<");
            w.write("<b>AFD Steps:</b> " + afd.getName() + "<br/>input: " + input + "<br/>result: " + status + ">];\n");
            String prev = "title";
            for (int i = 0; i < steps.size(); i++) {
                String id = "s" + i;
                w.write(id + " [label=\"" + steps.get(i).replace("\"","\\\"") + "\"];\n");
                w.write(prev + " -> " + id + " [style=dashed];\n");
                prev = id;
            }
            if (finalState != null) w.write(prev + " -> end [label=\"final=" + finalState + "\"];\n");
            w.write("end [shape=doublecircle, label=\"" + status + "\"];\n}\n");
        }
        return dotFile;
    }

    public static Path generateAPStepsGraph(AP ap, String input,
                                            java.util.List<String> steps, boolean accepted, String finalState,
                                            Path baseDir) throws IOException {
        Path dir = apStepsDir(baseDir);
        Path dotFile = dir.resolve("APSteps_" + ap.getName() + "_" + TS.format(LocalDateTime.now()) + ".dot");
        try (BufferedWriter w = Files.newBufferedWriter(dotFile)) {
            String status = accepted ? "ACCEPT" : "REJECT";
            w.write("digraph AP_STEPS { rankdir=TB; node [shape=box];\n");
            w.write("title [shape=plaintext, label=<");
            w.write("<b>AP Steps:</b> " + ap.getName() + "<br/>input: " + input + "<br/>result: " + status + ">];\n");
            String prev = "title";
            for (int i = 0; i < steps.size(); i++) {
                String id = "s" + i;
                w.write(id + " [label=\"" + steps.get(i).replace("\"","\\\"") + "\"];\n");
                w.write(prev + " -> " + id + " [style=dashed];\n");
                prev = id;
            }
            if (finalState != null) w.write(prev + " -> end [label=\"final=" + finalState + "\"];\n");
            w.write("end [shape=doublecircle, label=\"" + status + "\"];\n}\n");
        }
        return dotFile;
    }
}
