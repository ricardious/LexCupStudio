%%
%class MyLexer
%unicode
%cup
%line
%column

WHITESPACE = [ \t\r\n]+
ID         = [a-zA-Z_][a-zA-Z_0-9]*
NUMBER     = [0-9]+

%%

{WHITESPACE}     { /* skip */ }
"+"              { return symbol(sym.PLUS); }
"-"              { return symbol(sym.MINUS); }
{NUMBER}         { return symbol(sym.NUMBER, yytext()); }
{ID}             { return symbol(sym.ID, yytext()); }

.                { return symbol(sym.error, yytext()); }
