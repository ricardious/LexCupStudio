package io.lexcupstudio.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FrontendResult<TToken, TAst> {
    private final List<TToken> tokens;
    private final TAst ast;
    private final List<FrontendMessage> messages;

    public FrontendResult(List<TToken> tokens, TAst ast, List<FrontendMessage> messages) {
        this.tokens = tokens == null ? List.of() : List.copyOf(tokens);
        this.ast = ast;
        this.messages = messages == null ? List.of() : List.copyOf(messages);
    }

    public List<TToken> tokens() {
        return tokens;
    }

    public TAst ast() {
        return ast;
    }

    public List<FrontendMessage> messages() {
        return messages;
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(m -> "ERROR".equalsIgnoreCase(m.type()));
    }

    public static <TToken, TAst> FrontendResult<TToken, TAst> from(
            List<TToken> tokens,
            TAst ast,
            List<FrontendMessage> lexerMessages,
            List<FrontendMessage> parserMessages
    ) {
        List<FrontendMessage> merged = new ArrayList<>();
        if (lexerMessages != null) merged.addAll(lexerMessages);
        if (parserMessages != null) merged.addAll(parserMessages);
        return new FrontendResult<>(tokens, ast, Collections.unmodifiableList(merged));
    }
}
