package io.lexcupstudio.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LanguageFrontendTest {

    @Test
    void shouldParseWhenNoLexerErrors() throws Exception {
        LexerAdapter<String> lexer = new LexerAdapter<>() {
            @Override
            public List<String> tokenize(String source) {
                return List.of("A", "B");
            }

            @Override
            public List<FrontendMessage> getMessages() {
                return List.of();
            }
        };

        ParserAdapter<String, String> parser = new ParserAdapter<>() {
            @Override
            public String parse(List<String> tokens) {
                return String.join("-", tokens);
            }

            @Override
            public List<FrontendMessage> getMessages() {
                return List.of();
            }
        };

        LanguageFrontend<String, String> frontend = new LanguageFrontend<>(lexer, parser);
        FrontendResult<String, String> result = frontend.run("ab");

        assertEquals("A-B", result.ast());
        assertFalse(result.hasErrors());
    }

    @Test
    void shouldSkipParserWhenLexerHasErrors() throws Exception {
        LexerAdapter<String> lexer = new LexerAdapter<>() {
            @Override
            public List<String> tokenize(String source) {
                return List.of();
            }

            @Override
            public List<FrontendMessage> getMessages() {
                return List.of(FrontendMessage.error("bad token", 1, 1));
            }
        };

        List<String> calls = new ArrayList<>();
        ParserAdapter<String, String> parser = new ParserAdapter<>() {
            @Override
            public String parse(List<String> tokens) {
                calls.add("called");
                return "AST";
            }

            @Override
            public List<FrontendMessage> getMessages() {
                return List.of();
            }
        };

        LanguageFrontend<String, String> frontend = new LanguageFrontend<>(lexer, parser);
        FrontendResult<String, String> result = frontend.run("?");

        assertNull(result.ast());
        assertTrue(result.hasErrors());
        assertTrue(calls.isEmpty());
    }
}
