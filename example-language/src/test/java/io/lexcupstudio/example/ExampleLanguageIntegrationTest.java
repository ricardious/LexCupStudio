package io.lexcupstudio.example;

import io.lexcupstudio.core.FrontendResult;
import io.lexcupstudio.core.LanguageFrontend;
import java_cup.runtime.Symbol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleLanguageIntegrationTest {

    @Test
    void shouldParseArithmeticExpression() throws Exception {
        LanguageFrontend<Symbol, Integer> frontend = new LanguageFrontend<>(
                new ExampleLexerAdapter(),
                new ExampleParserAdapter()
        );

        FrontendResult<Symbol, Integer> result = frontend.run("10 + (2 - 1)");

        assertFalse(result.hasErrors());
        assertEquals(11, result.ast());
    }

    @Test
    void shouldReportLexerErrorAndSkipParser() throws Exception {
        LanguageFrontend<Symbol, Integer> frontend = new LanguageFrontend<>(
                new ExampleLexerAdapter(),
                new ExampleParserAdapter()
        );

        FrontendResult<Symbol, Integer> result = frontend.run("10 + @");

        assertTrue(result.hasErrors());
        assertNull(result.ast());
    }
}
