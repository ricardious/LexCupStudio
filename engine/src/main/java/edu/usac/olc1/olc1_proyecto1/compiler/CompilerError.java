package edu.usac.olc1.olc1_proyecto1.compiler;

public class CompilerError {
    private final String type;        // e.g. "Lexical", "Syntax"
    private final String description; // Human-readable message
    private final int line;           // Line number (1-based)
    private final int column;         // Column number (1-based)

    public CompilerError(String type, String description, int line, int column) {
        this.type = type;
        this.description = description;
        this.line = line;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("%s Error at line %d, column %d: %s",
                type, line, column, description);
    }
}

