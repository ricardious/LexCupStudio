package edu.usac.olc1.olc1_proyecto1.language.nodes;

import edu.usac.olc1.olc1_proyecto1.automata.Automaton;
import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

/** Represents the call: AutomatonName("input"); */
public class ValidateCallNode implements Statement {
    private final String automatonName;
    private final String input;

    public ValidateCallNode(String automatonName, String input) {
        this.automatonName = automatonName;
        this.input = input;
    }

    @Override
    public void run(Interpreter interpreter) {
        Automaton.ValidationResult res = interpreter.validate(automatonName, input);
        System.out.println(
                automatonName + "  " + input + "  " + (res.accepted ? "Cadena Válida" : "Cadena Inválida")
        );
        // You can also iterate res.steps to produce a steps report
        // res.steps.forEach(System.out::println);
    }
}