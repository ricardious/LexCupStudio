package edu.usac.olc1.olc1_proyecto1.language.nodes;


import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

/** Represents the call: verAutomatas(); */
public class VerAutomatasNode implements Statement {
    @Override
    public void run(Interpreter interpreter) {
        // Typically you'd print to console or collect output elsewhere
        String out = interpreter.listAutomata();
        // For now we just print; your UI can capture/redirect this
        System.out.print(out);
    }
}