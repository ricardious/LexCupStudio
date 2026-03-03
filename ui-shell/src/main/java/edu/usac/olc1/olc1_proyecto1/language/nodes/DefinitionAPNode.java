package edu.usac.olc1.olc1_proyecto1.language.nodes;

import edu.usac.olc1.olc1_proyecto1.automata.AP;
import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

import java.util.List;
import java.util.Set;

/** Represents a Pushdown Automaton (AP) definition. */
public class DefinitionAPNode implements Statement {

    private final String name;
    private final Set<String> states;
    private final Set<String> alphabet;
    private final Set<String> stackSymbols;
    private final String initial;
    private final Set<String> accept;
    private final List<AP.TransitionAP> transitions;
    private final Position pos; // optional

    public DefinitionAPNode(String name,
                            Set<String> states,
                            Set<String> alphabet,
                            Set<String> stackSymbols,
                            String initial,
                            Set<String> accept,
                            List<AP.TransitionAP> transitions,
                            Position pos) {
        this.name = name;
        this.states = states;
        this.alphabet = alphabet;
        this.stackSymbols = stackSymbols;
        this.initial = initial;
        this.accept = accept;
        this.transitions = transitions;
        this.pos = pos;
    }

    @Override
    public void run(Interpreter interpreter) {
        AP ap = new AP(name);
        if (states != null) states.forEach(ap::addState);
        if (alphabet != null) alphabet.forEach(ap::addSymbol);
        if (stackSymbols != null) stackSymbols.forEach(ap::addStackSymbol);
        ap.setInitial(initial);
        if (accept != null) accept.forEach(ap::addAccept);
        if (transitions != null) transitions.forEach(ap::addTransition);
        interpreter.registerAP(ap);
    }
}