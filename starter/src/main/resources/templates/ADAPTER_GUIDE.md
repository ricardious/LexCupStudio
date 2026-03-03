# Adapter guide

Implementa dos clases en tu proyecto consumidor:

1. `MyLexerAdapter implements LexerAdapter<MyToken>`
- Internamente llama al lexer generado por JFlex.
- Convierte tokens a tu tipo `MyToken`.
- Reporta errores en `getMessages()`.

2. `MyParserAdapter implements ParserAdapter<MyToken, MyAst>`
- Consume la lista de tokens o usa tu parser generado por CUP.
- Devuelve AST.
- Reporta errores en `getMessages()`.

Luego ejecuta:

```java
LanguageFrontend<MyToken, MyAst> frontend = new LanguageFrontend<>(
    new MyLexerAdapter(),
    new MyParserAdapter()
);
FrontendResult<MyToken, MyAst> result = frontend.run(sourceCode);
```
