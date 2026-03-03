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
- `example-language/`: ejemplo funcional con generación JFlex + CUP y adapters reales.
- `example-language-archetype/`: generador Maven para crear un proyecto base listo.

## Uso rápido

1. Copia `starter/src/main/resources/templates/lexer.flex` y `parser.cup` a tu proyecto.
2. Genera código con JFlex/CUP según tu build.
3. Implementa adapters usando `core`.
4. Ejecuta `LanguageFrontend`.

## Build

```bash
mvn clean test
```

## Ejecutar ejemplo

```bash
mvn -pl example-language -am test
```

## Generar proyecto desde archetype

```bash
mvn install -DskipTests
mvn archetype:generate \
  -DarchetypeGroupId=io.lexcupstudio \
  -DarchetypeArtifactId=lexcupstudio-example-language-archetype \
  -DarchetypeVersion=0.1.0-SNAPSHOT \
  -DgroupId=com.myteam \
  -DartifactId=my-language \
  -Dversion=0.1.0-SNAPSHOT \
  -Dpackage=com.myteam.language \
  -DlexcupstudioVersion=0.1.0-SNAPSHOT \
  -DinteractiveMode=false
```
