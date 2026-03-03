package edu.usac.olc1.olc1_proyecto1.compiler;


import edu.usac.olc1.olc1_proyecto1.automata.AutomataRepository;
import edu.usac.olc1.olc1_proyecto1.automata.Automaton;
import edu.usac.olc1.olc1_proyecto1.automata.AFD;
import edu.usac.olc1.olc1_proyecto1.automata.AP;

import java.util.Set;

public class CompilationContext {
    private final AutomataRepository repository = new AutomataRepository();
    private final Errors errors = new Errors();

    private String currentSourcePath;
    private final StringBuilder console = new StringBuilder();

    public AutomataRepository getRepository() {
        return repository;
    }

    public Errors getErrors() {
        return errors;
    }

    public String getCurrentSourcePath() {
        return currentSourcePath;
    }

    public void setCurrentSourcePath(String path) {
        this.currentSourcePath = path;
    }

    public void println(String msg) {
        console.append(msg).append('\n');
    }

    public void print(String msg) {
        console.append(msg);
    }

    public String getConsoleText() {
        return console.toString();
    }

    public void clearConsole() {
        console.setLength(0);
    }

    public boolean register(Automaton a) {
        if (a == null) return false;
        repository.put(a);
        return true;
    }

    public Set<String> automataNames() {
        return repository.names();
    }

    public void clearRepository() {
        repository.clear();
    }

    public int automataCount() {
        return repository.size();
    }

    private void errorsAddSemantic(String msg) {
        try {
            errors.getClass()
                    .getMethod("addSemantic", String.class, int.class, int.class)
                    .invoke(errors, msg, -1, -1);
        } catch (ReflectiveOperationException ignore) {
            try {
                errors.getClass()
                        .getMethod("add", String.class)
                        .invoke(errors, msg);
            } catch (ReflectiveOperationException ignore2) {
                println("[Semantic Error] " + msg);
            }
        }
    }
}
