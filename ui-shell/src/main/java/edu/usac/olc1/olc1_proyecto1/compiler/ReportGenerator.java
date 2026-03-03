package edu.usac.olc1.olc1_proyecto1.compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates simple CSV / TXT reports for tokens and compiler errors.
 * Filenames are uppercase as requested.
 */
public class ReportGenerator {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Writes a CSV with all tokens to the given directory. Filename like: TOKENS_YYYYMMDD_HHMMSS.CSV */
    public Path writeTokensCsv(List<Token> tokens, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        Path csv = outDir.resolve("TOKENS_" + TS.format(LocalDateTime.now()) + ".CSV");
        try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {
            w.write("LEXEME,TYPE,TYPE_NAME,LINE,COLUMN\n");
            for (Token t : tokens) {
                w.write(csvEscape(t.getLexeme())); w.write(",");
                w.write(Integer.toString(t.getType())); w.write(",");
                w.write(csvEscape(typeName(t.getType()))); w.write(",");
                w.write(Integer.toString(t.getLine())); w.write(",");
                w.write(Integer.toString(t.getColumn()));
                w.write("\n");
            }
        }
        return csv;
    }

    /** Writes a CSV with all errors to the given directory. Filename like: ERRORS_YYYYMMDD_HHMMSS.CSV */
    public Path writeErrorsCsv(List<CompilerError> errors, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        Path csv = outDir.resolve("ERRORS_" + TS.format(LocalDateTime.now()) + ".CSV");
        try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {
            w.write("TYPE,DESCRIPTION,LINE,COLUMN\n");
            for (CompilerError e : errors) {
                w.write(csvEscape(e.getType())); w.write(",");
                w.write(csvEscape(e.getDescription())); w.write(",");
                w.write(Integer.toString(e.getLine())); w.write(",");
                w.write(Integer.toString(e.getColumn()));
                w.write("\n");
            }
        }
        return csv;
    }

    /* ----------------- Legacy helpers (compatible con tu ejemplo anterior) ----------------- */

    /** Returns a simple plain-text report for tokens. */
    public static String generateTokenReport(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-8s %-20s %-6s %-6s%n", "LEXEME", "TYPE", "TYPE_NAME", "LINE", "COLUMN"));
        for (Token t : tokens) {
            sb.append(String.format("%-20s %-8d %-20s %-6d %-6d%n",
                    truncate(t.getLexeme(), 20),
                    t.getType(),
                    truncate(typeNameSafe(t.getType()), 20),
                    t.getLine(),
                    t.getColumn()));
        }
        return sb.toString();
    }

    /** Writes the plain-text token report to a specific path. */
    public static void exportTokenReport(List<Token> tokens, String exportPath) throws IOException {
        Path p = Paths.get(exportPath);
        Files.createDirectories(p.getParent() == null ? Paths.get(".") : p.getParent());
        Files.writeString(p, generateTokenReport(tokens), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /* ----------------- Internals ----------------- */

    private static String csvEscape(String s) {
        if (s == null) return "\"\"";
        String x = s.replace("\"", "\"\"");
        return "\"" + x + "\"";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + "…";
    }

    /**
     * Try to resolve token name via CUP symbols class (PARSERSYM or ParserSym).
     * Uses reflection to avoid compile-time dependency on either name.
     */
    private static String typeName(int typeIndex) {
        // Try PARSERSYM first (uppercase), then ParserSym (default)
        String[] candidates = {
                "edu.usac.olc1.olc1_proyecto1.compiler.PARSERSYM",
                "edu.usac.olc1.olc1_proyecto1.compiler.ParserSym"
        };
        for (String cn : candidates) {
            try {
                Class<?> c = Class.forName(cn);
                Field f = c.getField("terminalNames");
                String[] names = (String[]) f.get(null);
                if (typeIndex >= 0 && typeIndex < names.length && names[typeIndex] != null) {
                    return names[typeIndex];
                }
            } catch (Throwable ignore) {
                // try next candidate
            }
        }
        return Integer.toString(typeIndex);
    }

    private static String typeNameSafe(int t) {
        try { return typeName(t); } catch (Throwable e) { return Integer.toString(t); }
    }
}
