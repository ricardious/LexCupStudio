package edu.usac.olc1.olc1_proyecto1.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collector for lexical/syntactic errors only. */
public class Errors {
    private final List<CompilerError> list = new ArrayList<>();

    public void addLexical(String msg, int line, int col) {
        list.add(new CompilerError("Lexical", msg, line, col));
    }

    public void addSyntax(String msg, int line, int col) {
        list.add(new CompilerError("Syntax", msg, line, col));
    }

    public boolean hasErrors() {
        return !list.isEmpty();
    }

    public List<CompilerError> getAll() {
        return Collections.unmodifiableList(list);
    }

    public void clear() {
        list.clear();
    }
}