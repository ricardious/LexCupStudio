package edu.usac.olc1.olc1_proyecto1.ui.components.commands;

import edu.usac.olc1.olc1_proyecto1.compiler.CompilerPipeline;
import edu.usac.olc1.olc1_proyecto1.ui.components.ActiveFileAccessor;
import edu.usac.olc1.olc1_proyecto1.ui.components.Terminal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * ricardious [ruta/opcional]
 * - Si no se da ruta, toma el archivo abierto en la TAB ACTUAL (vía ActiveFileAccessor).
 * - Ejecuta el pipeline y deja reportes en <carpetaDelArchivo>/output
 * - Imprime todo en la terminal.
 */
public class RicardiousCommandHandler implements CommandHandler {

    private final Terminal terminal;
    private final ActiveFileAccessor activeFileAccessor;

    public RicardiousCommandHandler(Terminal terminal, ActiveFileAccessor accessor) {
        this.terminal = Objects.requireNonNull(terminal);
        this.activeFileAccessor = Objects.requireNonNull(accessor);
    }

    @Override
    public String execute(String[] args) {
        try {
            File file;
            if (args.length == 0) {
                file = activeFileAccessor.getActiveFile();
                if (file == null) return "No hay archivo activo en la pestaña actual.\nUso: ricardious <archivo>\n";
            } else {
                // ruta relativa al PWD de la terminal
                String path = args[0];
                if (!path.startsWith("/") && !path.startsWith("~")) {
                    path = terminal.getCurrentDir() + File.separator + path;
                }
                file = new File(path.replace("~", System.getProperty("user.home")));
            }

            if (!file.exists() || !file.isFile()) {
                return "Archivo no encontrado: " + file + "\n";
            }

            String src = Files.readString(file.toPath());
            Path baseDir = file.getParentFile().toPath();

            terminal.displayOutput("Ejecutando ricardious sobre: " + file.getAbsolutePath());
            boolean ok = CompilerPipeline.run(
                    src,
                    baseDir,
                    (msg) -> terminal.displayOutput(msg)
            );

            return ok
                    ? "Ejecución completada. Revisa: " + baseDir.resolve("output") + "\n"
                    : "Ejecución con errores. Revisa: " + baseDir.resolve("output") + "\n";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error al ejecutar ricardious: " + ex.getMessage() + "\n";
        }
    }
}
