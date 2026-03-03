package edu.usac.olc1.olc1_proyecto1.compiler;

public class Token {
    private String lexeme;
    private int type;
    private int line;
    private int column;

    public Token(String lexeme, int type, int line, int column) {
        this.lexeme = lexeme;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getType() {
        return type;
    }


    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Token{" +
                "lexeme='" + lexeme + '\'' +
                ", type=" + type +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}