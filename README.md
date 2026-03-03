# LexCupStudio

Base reutilizable para crear lenguajes con **JFlex + Java CUP** sin rearmar infraestructura cada vez.

## Objetivo

Que cualquier proyecto nuevo pueda:

1. Definir su `lexer.flex` y `parser.cup`.
2. Implementar dos adapters (`LexerAdapter` y `ParserAdapter`).
3. Ejecutar un frontend consistente con manejo de mensajes/errores.

## Estructura

- `core/`: contratos y pipeline reutilizable.
- `starter/`: plantillas `.flex` / `.cup` y utilidades de arranque.

## Uso rápido

1. Copia `starter/src/main/resources/templates/lexer.flex` y `parser.cup` a tu proyecto.
2. Genera código con JFlex/CUP según tu build.
3. Implementa adapters usando `core`.
4. Ejecuta `LanguageFrontend`.

## Build

```bash
mvn clean test
```

## Siguiente paso recomendado

Agregar un módulo `example-language` con una gramática mínima funcional para validar flujo end-to-end.
