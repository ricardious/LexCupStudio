package edu.usac.olc1.olc1_proyecto1.language.nodes;

import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

/** Represents the call: desc(Name); */
public class DescNode implements Statement {
    private final String name;

    public DescNode(String name) {
        this.name = name;
    }

    @Override
    public void run(Interpreter interpreter) {
        String out = interpreter.describe(name);
        System.out.println(out);
    }
}