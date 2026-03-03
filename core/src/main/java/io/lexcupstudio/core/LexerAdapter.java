package io.lexcupstudio.core;

import java.util.List;

public interface LexerAdapter<TToken> {
    List<TToken> tokenize(String source) throws Exception;
    List<FrontendMessage> getMessages();
}
