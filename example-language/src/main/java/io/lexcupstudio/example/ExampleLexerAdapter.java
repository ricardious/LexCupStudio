package io.lexcupstudio.example;

import io.lexcupstudio.core.FrontendMessage;
import io.lexcupstudio.core.LexerAdapter;
import io.lexcupstudio.example.generated.ExampleLexer;
import io.lexcupstudio.example.generated.ExampleSym;
import java_cup.runtime.Symbol;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public final class ExampleLexerAdapter implements LexerAdapter<Symbol> {

    private List<FrontendMessage> messages = List.of();

    @Override
    public List<Symbol> tokenize(String source) throws Exception {
        ExampleLexer lexer = new ExampleLexer(new StringReader(source));
        List<Symbol> tokens = new ArrayList<>();

        Symbol token;
        do {
            token = lexer.next_token();
            if (token.sym != ExampleSym.EOF) {
                tokens.add(token);
            }
        } while (token.sym != ExampleSym.EOF);

        this.messages = lexer.getMessages();
        return tokens;
    }

    @Override
    public List<FrontendMessage> getMessages() {
        return messages;
    }
}
