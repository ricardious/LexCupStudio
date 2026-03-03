package edu.usac.olc1.olc1_proyecto1.language.nodes;

/** Optional source position holder for better error messages. */
public class Position {
    public final int line;
    public final int column;

    public Position(int line, int column) {
        this.line = Math.max(1, line);
        this.column = Math.max(1, column);
    }
}
