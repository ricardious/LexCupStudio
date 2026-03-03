package io.lexcupstudio.core;

import java.util.List;

public final class LanguageFrontend<TToken, TAst> {

    private final LexerAdapter<TToken> lexer;
    private final ParserAdapter<TToken, TAst> parser;

    public LanguageFrontend(LexerAdapter<TToken> lexer, ParserAdapter<TToken, TAst> parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    public FrontendResult<TToken, TAst> run(String source) throws Exception {
        List<TToken> tokens = lexer.tokenize(source);
        TAst ast = null;

        if (lexer.getMessages().stream().noneMatch(m -> "ERROR".equalsIgnoreCase(m.type()))) {
            ast = parser.parse(tokens);
        }

        return FrontendResult.from(tokens, ast, lexer.getMessages(), parser.getMessages());
    }
}
