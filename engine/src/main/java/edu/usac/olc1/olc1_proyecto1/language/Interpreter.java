package edu.usac.olc1.olc1_proyecto1.language;


import edu.usac.olc1.olc1_proyecto1.automata.*;
import edu.usac.olc1.olc1_proyecto1.compiler.Errors;
import edu.usac.olc1.olc1_proyecto1.language.nodes.*;

import java.util.List;

/**
 * Executes high-level language constructs (definitions and calls)
 * produced by the parser over the repository of automatons.
 */
public class Interpreter {

    private final AutomataRepository repo;
    private final Errors errors;

    public Interpreter(AutomataRepository repo, Errors errors) {
        this.repo = repo;
        this.errors = errors;
    }

    public void execute(List<Statement> program) {
        if (program == null) return;
        for (Statement s : program) {
            if (s != null) s.run(this);
        }
    }

    /* ========= API used by AST nodes ========= */
    public void registerAFD(AFD afd) {
        if (afd == null) return;
        repo.put(afd);
    }

    public void registerAP(AP ap) {
        if (ap == null) return;
        repo.put(ap);
    }

    public String listAutomata() {
        StringBuilder sb = new StringBuilder();
        for (Automaton a : repo.all()) {
            sb.append(a.getName()).append("  ").append(a.getType()).append("\n");
        }
        return sb.toString();
    }

    public String describe(String name) {
        Automaton a = repo.get(name);
        if (a == null) return "";
        return a.describe();
    }

    public Automaton.ValidationResult validate(String name, String input) {
        Automaton a = repo.get(name);
        if (a == null) {
            return new Automaton.ValidationResult(
                    false,
                    List.of("Automaton not found: " + name)
            );
        }
        return a.validate(input);
    }

    public AutomataRepository getRepository() { return repo; }
    public Errors getErrors() { return errors; }
}
