package edu.usac.olc1.olc1_proyecto1.language.nodes;

import edu.usac.olc1.olc1_proyecto1.automata.AFD;
import edu.usac.olc1.olc1_proyecto1.automata.TransitionAFD;
import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

import java.util.List;
import java.util.Set;

/** Represents an AFD definition: states, alphabet, initial, accept, transitions. */
public class DefinitionAFDNode implements Statement {

    private final String name;
    private final Set<String> states;
    private final Set<String> alphabet;
    private final String initial;
    private final Set<String> accept;
    private final List<TransitionAFD> transitions;
    private final Position pos; // optional

    public DefinitionAFDNode(String name,
                             Set<String> states,
                             Set<String> alphabet,
                             String initial,
                             Set<String> accept,
                             List<TransitionAFD> transitions,
                             Position pos) {
        this.name = name;
        this.states = states;
        this.alphabet = alphabet;
        this.initial = initial;
        this.accept = accept;
        this.transitions = transitions;
        this.pos = pos;
    }

    @Override
    public void run(Interpreter interpreter) {
        AFD afd = new AFD(name);
        if (states != null) states.forEach(afd::addState);
        if (alphabet != null) alphabet.forEach(afd::addSymbol);
        afd.setInitial(initial);
        if (accept != null) accept.forEach(afd::addAccept);
        if (transitions != null) {
            for (TransitionAFD t : transitions) {
                afd.addTransition(t.from(), t.symbol(), t.to());
            }
        }
        interpreter.registerAFD(afd);
    }
}
