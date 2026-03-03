package ${package};

import io.lexcupstudio.core.FrontendResult;
import io.lexcupstudio.core.LanguageFrontend;
import java_cup.runtime.Symbol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppIntegrationTest {

    @Test
    void parsesExpression() throws Exception {
        LanguageFrontend<Symbol, Integer> frontend = new LanguageFrontend<>(
                new ExampleLexerAdapter(),
                new ExampleParserAdapter()
        );

        FrontendResult<Symbol, Integer> result = frontend.run("5 + (3 - 1)");
        assertFalse(result.hasErrors());
        assertEquals(7, result.ast());
    }
}
