package io.lexcupstudio.example;

import io.lexcupstudio.example.generated.ExampleSym;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;

import java.util.List;

public final class SymbolListScanner implements Scanner {

    private final List<Symbol> symbols;
    private int index = 0;

    public SymbolListScanner(List<Symbol> symbols) {
        this.symbols = symbols;
    }

    @Override
    public Symbol next_token() {
        if (index < symbols.size()) {
            return symbols.get(index++);
        }
        return new Symbol(ExampleSym.EOF);
    }
}
