package edu.usac.olc1.olc1_proyecto1.language.nodes;

import edu.usac.olc1.olc1_proyecto1.language.Interpreter;

/** Base node for AST elements. */
public interface AstNode {
    void run(Interpreter interpreter);
}
