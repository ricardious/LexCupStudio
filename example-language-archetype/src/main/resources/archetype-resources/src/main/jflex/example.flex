package ${package}.generated;

import io.lexcupstudio.core.FrontendMessage;
import java_cup.runtime.Symbol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

%%

%public
%class ExampleLexer
%unicode
%cup
%line
%column

%{
    private final List<FrontendMessage> messages = new ArrayList<>();

    public List<FrontendMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

WHITESPACE = [ \t\r\n]+
NUMBER     = [0-9]+

%%

{WHITESPACE} { }
"+"          { return symbol(ExampleSym.PLUS); }
"-"          { return symbol(ExampleSym.MINUS); }
"("          { return symbol(ExampleSym.LPAREN); }
")"          { return symbol(ExampleSym.RPAREN); }
{NUMBER}     { return symbol(ExampleSym.NUMBER, Integer.parseInt(yytext())); }
<<EOF>>      { return symbol(ExampleSym.EOF); }

.            {
                messages.add(FrontendMessage.error(
                    "Invalid character: '" + yytext() + "'",
                    yyline + 1,
                    yycolumn + 1
                ));
             }
