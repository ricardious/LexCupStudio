package edu.usac.olc1.olc1_proyecto1.compiler;

import edu.usac.olc1.olc1_proyecto1.automata.*;
import edu.usac.olc1.olc1_proyecto1.language.Interpreter;
import edu.usac.olc1.olc1_proyecto1.language.nodes.Statement;
import edu.usac.olc1.olc1_proyecto1.reports.ProjectReports;
import java_cup.runtime.Symbol;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class CompilerPipeline {

    /**
     * Ejecuta: Léxico -> Sintaxis -> (Interpreter sin semántico) -> reportes locales.
     * @param srcText  Código fuente a compilar
     * @param projectDir Carpeta base del archivo (se crearán output/ aquí)
     * @param log Salida de log (se imprime en la terminal)
     * @return true si parseó sin errores sintácticos (y se generaron reportes), false si hubo errores.
     */
    public static boolean run(String srcText, Path projectDir, Consumer<String> log) {
        try {
            ProjectReports pr = new ProjectReports(projectDir);

            // === 1) LÉXICO
            Lexer lex1 = new Lexer(new StringReader(srcText));
            drain(lex1);
            List<Token> tokens = lex1.getTokens();
            List<CompilerError> lexErrs = lex1.getErrors();
            pr.writeTokensCsv(tokens);
            if (!lexErrs.isEmpty()) {
                pr.writeErrorsCsv(lexErrs);
                log.accept("Errores LÉXICOS detectados. Ver: " + projectDir.resolve("output/tokens.csv") + " y errors.csv");
                for (CompilerError e : lexErrs) log.accept(e.toString());
                return false;
            }
            log.accept("Léxico OK. Tokens: " + tokens.size());

            // === 2) PARSER
            Lexer lex2 = new Lexer(new StringReader(srcText));
            Parser parser = new Parser(lex2);
            Symbol r = null;
            try { r = parser.parse(); } catch (Exception ignored) {}
            List<CompilerError> synErrs = parser.getErrors();
            if (!synErrs.isEmpty()) {
                pr.writeErrorsCsv(synErrs);
                log.accept("Errores SINTÁCTICOS detectados. Ver: " + projectDir.resolve("output/errors.csv"));
                for (CompilerError e : synErrs) log.accept(e.toString());
                return false;
            }
            log.accept("Sintaxis OK.");

            // === 3) INTÉRPRETE (sin semántico)
            CompilationContext ctx = new CompilationContext();
            Interpreter interp = new Interpreter(ctx.getRepository(), ctx.getErrors());
            List<Statement> program = extractProgram(r, parser);
            if (!program.isEmpty()) interp.execute(program);
            AutomataRepository repo = ctx.getRepository();

            if (repo.size() == 0) {
                log.accept("Aviso: No se registraron autómatas. Verifica acciones semánticas del parser o el Interpreter.");
            }

            // === 4) REPORTES locales (en <dir>/output)
            for (Automaton a : repo.all()) {
                var png = pr.graphAutomatonToPng(a);
                log.accept("PNG autómata: " + png.toAbsolutePath());
                if (a instanceof AFD afd) {
                    var ps = pr.graphAFDStepsToPng(afd, "101");
                    log.accept("PNG pasos AFD: " + ps.toAbsolutePath());
                } else if (a instanceof AP ap) {
                    var ps = pr.graphAPStepsToPng(ap, "aabb");
                    log.accept("PNG pasos AP: " + ps.toAbsolutePath());
                }
            }

            log.accept("Listo. Reportes en: " + projectDir.resolve("output").toAbsolutePath());
            return true;
        } catch (Exception ex) {
            log.accept("Error inesperado en pipeline: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private static void drain(Lexer lx) throws Exception {
        java_cup.runtime.Symbol tok;
        do { tok = lx.next_token(); } while (tok != null && tok.sym != ParserSym.EOF);
    }

    @SuppressWarnings("unchecked")
    private static List<Statement> extractProgram(Symbol parseResult, Parser parser) {
        try {
            if (parseResult != null && parseResult.value instanceof List<?> l
                    && (l.isEmpty() || l.get(0) instanceof Statement)) {
                return (List<Statement>) l;
            }
            Method m = parser.getClass().getMethod("getProgram");
            Object v = m.invoke(parser);
            if (v instanceof List<?> l && (l.isEmpty() || l.get(0) instanceof Statement)) {
                return (List<Statement>) l;
            }
        } catch (ReflectiveOperationException ignore) {}
        return List.of();
    }
}
