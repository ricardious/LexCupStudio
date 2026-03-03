package io.lexcupstudio.core;

import java.util.List;

public interface ParserAdapter<TToken, TAst> {
    TAst parse(List<TToken> tokens) throws Exception;
    List<FrontendMessage> getMessages();
}
