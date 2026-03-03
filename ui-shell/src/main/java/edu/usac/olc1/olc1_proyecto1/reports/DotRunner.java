package edu.usac.olc1.olc1_proyecto1.reports;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class DotRunner {
    private DotRunner() {}

    /**
     * Ejecuta: dot -Tpng input.dot -o output.png
     * Retorna la ruta del PNG creado.
     */
    public static Path runDotToPng(Path dotPath) throws IOException, InterruptedException {
        if (dotPath == null || !Files.exists(dotPath)) {
            throw new FileNotFoundException("DOT file not found: " + dotPath);
        }
        Path pngPath = replaceExt(dotPath, ".png");

        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng",
                dotPath.toString(), "-o", pngPath.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) { /* drenar salida */ }
        }
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException("Graphviz dot failed with exit code " + code + " for " + dotPath);
        }
        return pngPath;
    }

    private static Path replaceExt(Path path, String newExt) {
        String name = path.getFileName().toString();
        int i = name.lastIndexOf('.');
        String base = (i < 0) ? name : name.substring(0, i);
        return path.getParent().resolve(base + newExt);
    }
}
