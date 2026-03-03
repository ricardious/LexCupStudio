package io.lexcupstudio.example;

import io.lexcupstudio.core.FrontendMessage;
import io.lexcupstudio.core.ParserAdapter;
import io.lexcupstudio.example.generated.ExampleParser;
import java_cup.runtime.Symbol;

import java.util.ArrayList;
import java.util.List;

public final class ExampleParserAdapter implements ParserAdapter<Symbol, Integer> {

    private List<FrontendMessage> messages = List.of();

    @Override
    public Integer parse(List<Symbol> tokens) throws Exception {
        ExampleParser parser = new ExampleParser(new SymbolListScanner(tokens));
        Symbol result = parser.parse();

        this.messages = new ArrayList<>(parser.getMessages());
        if (result == null || result.value == null) {
            return null;
        }
        return (Integer) result.value;
    }

    @Override
    public List<FrontendMessage> getMessages() {
        return messages;
    }
}
