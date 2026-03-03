/* ========= AutómataLab - Lexer (JFlex) ========= */
package edu.usac.olc1.olc1_proyecto1.compiler;

import java_cup.runtime.Symbol;
import java.util.ArrayList;

%%

%class Lexer
%unicode
%line
%column
%cup
%public

/* State for multi-line comments */
%state COMMENT_MULTI

%{
  private final ArrayList<Token> tokens = new ArrayList<>();
  private final ArrayList<CompilerError> errors = new ArrayList<>();
  private boolean inMultiLineComment = false;
  private int commentStartLine = -1;

  public ArrayList<Token> getTokens() { return tokens; }
  public ArrayList<CompilerError> getErrors() { return errors; }

  /** Create a symbol and add it to the token list */
  private Symbol symbol(int type) {
    tokens.add(new Token(yytext(), type, yyline + 1, yycolumn + 1));
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  /** Create a symbol with a custom value (used for literals) */
  private Symbol symbol(int type, Object value) {
    tokens.add(new Token(yytext(), type, yyline + 1, yycolumn + 1));
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }

  /** Register a lexical error */
  private void lexError(String msg) {
    errors.add(new CompilerError("Lexical", msg, yyline + 1, yycolumn + 1));
  }
%}

%eofval{
  if (inMultiLineComment) {
    lexError("Unclosed multi-line comment starting at line " + commentStartLine);
  }
  return new Symbol(ParserSym.EOF, yyline + 1, yycolumn + 1);
%eofval}

/* ====== Regular Expressions (macros) ====== */
WHITESPACE   = [ \t\r\n\f]+
DIGIT        = [0-9]
INTEGER      = {DIGIT}+
ID           = [A-Za-z_][A-Za-z0-9_]*
STRING       = \"([^\"\\]|\\.)*\"        /* Strings with escape support */
ARROW        = "->"
LTEQ         = "<="
GTEQ         = ">="
NEQ          = "!="
EQ           = "=="

/* Comment patterns */
LINE_COMMENT = "//"[^\n]*
ML_START     = "/*"
ML_END       = "*/"

%%

/* ====== Lexical Rules ====== */

/* Single-line comments (ignored) */
{LINE_COMMENT}                { /* ignore */ }

/* Multi-line comments */
{ML_START}                    {
                                inMultiLineComment = true;
                                commentStartLine = yyline + 1;
                                yybegin(COMMENT_MULTI);
                              }
<COMMENT_MULTI>{
  {ML_END}                    {
                                inMultiLineComment = false;
                                yybegin(YYINITIAL);
                              }
  [^*]+                       { /* consume non-asterisk chars */ }
  "*"                         { /* consume asterisks not followed by / */ }
  \n                          { /* consume newlines */ }
}

/* Whitespace (ignored) */
{WHITESPACE}                  { /* ignore */ }

/* ====== Tags for AFD/AP ====== */
/* Opening tags: <AFD Nombre="...">, <AP Nombre="..."> */
/* Closing tags: </AFD>, </AP> */

"</AFD>"                      { return symbol(ParserSym.AFD_CLOSE); }
"</AP>"                       { return symbol(ParserSym.AP_CLOSE); }

/* Tokenize opening tags as separate parts: '<', 'AFD'/'AP', attributes, '>' */
"<"                           { return symbol(ParserSym.LT); }
">"                           { return symbol(ParserSym.GT); }

/* Keywords for tags and attributes */
"AFD"                         { return symbol(ParserSym.AFD); }
"AP"                          { return symbol(ParserSym.AP); }
"Nombre"                      { return symbol(ParserSym.NOMBRE); }

/* ====== Definition Sections ====== */
"N" / {WHITESPACE}* "="       { return symbol(ParserSym.SECTION_N); }
"T" / {WHITESPACE}* "="       { return symbol(ParserSym.SECTION_T); }
"P" / {WHITESPACE}* "="       { return symbol(ParserSym.SECTION_P); }
"I" / {WHITESPACE}* "="       { return symbol(ParserSym.SECTION_I); }
"A" / {WHITESPACE}* "="       { return symbol(ParserSym.SECTION_A); }
"Transiciones" / {WHITESPACE}* ":" { return symbol(ParserSym.TRANSICIONES); }

/* ====== Language Functions ====== */
"verAutomatas"                { return symbol(ParserSym.VER_AUTOMATAS); }
"desc"                        { return symbol(ParserSym.DESC); }

/* ====== Operators and punctuation ====== */
{ARROW}                       { return symbol(ParserSym.ARROW); }
"|"                           { return symbol(ParserSym.PIPE); }
";"                           { return symbol(ParserSym.SEMI); }
","                           { return symbol(ParserSym.COMMA); }
":"                           { return symbol(ParserSym.COLON); }
"="                           { return symbol(ParserSym.EQUALS); }

"{"                           { return symbol(ParserSym.LBRACE); }
"}"                           { return symbol(ParserSym.RBRACE); }
"\("                          { return symbol(ParserSym.LPAREN); }    /* literal '(' */
"\)"                          { return symbol(ParserSym.RPAREN); }    /* literal ')' */
"\["                          { return symbol(ParserSym.LBRACKET); }  /* literal '[' */
"\]"                          { return symbol(ParserSym.RBRACKET); }  /* literal ']' */

/* Special symbols used in AP */
"\$"                          { return symbol(ParserSym.DOLLAR); }    /* lambda */
"#"                           { return symbol(ParserSym.HASH); }      /* stack base */

/* ====== Literals ====== */
{STRING}                      { return symbol(ParserSym.STRING, yytext()); }
{INTEGER}                     { return symbol(ParserSym.INTEGER, Integer.parseInt(yytext())); }

/* ====== Identifiers ====== */
/* Used for state names, automaton names, alphabet symbols (non-literals) */
{ID}                          { return symbol(ParserSym.ID); }

/* ====== Error handling ====== */
.                             {
                                 lexError("Unrecognized character: '" + yytext() + "'");
                                 /* continue scanning without throwing exception */
                               }
