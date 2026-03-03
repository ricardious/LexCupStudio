package ${package};

import io.lexcupstudio.core.FrontendResult;
import io.lexcupstudio.core.LanguageFrontend;
import java_cup.runtime.Symbol;

public final class App {

    private App() {}

    public static void main(String[] args) throws Exception {
        String source = args.length > 0 ? args[0] : "10 + (2 - 1)";

        LanguageFrontend<Symbol, Integer> frontend = new LanguageFrontend<>(
                new ExampleLexerAdapter(),
                new ExampleParserAdapter()
        );

        FrontendResult<Symbol, Integer> result = frontend.run(source);

        if (result.hasErrors()) {
            System.out.println("Errors:");
            result.messages().forEach(m ->
                    System.out.println(m.type() + " " + m.line() + ":" + m.column() + " -> " + m.message())
            );
            return;
        }

        System.out.println("Result: " + result.ast());
    }
}
