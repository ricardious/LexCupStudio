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
- `ui-api/`: contrato para conectar lógica de lenguaje a la UI.
- `plugin-example/`: implementación mock de referencia del contrato de UI.
- `ui-shell/`: interfaz JavaFX desacoplada de la lógica.

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

## Ejecutar UI con plugin

```bash
mvn -pl ui-shell -am test -DskipTests
mvn -pl ui-shell -am org.openjfx:javafx-maven-plugin:0.0.8:run
```

La UI consume implementaciones de `LanguageRuntimePlugin` vía `ServiceLoader`.
Para integrar otra lógica, crea un módulo que implemente `ui-api` y registra el provider en `META-INF/services`.

## Generar proyecto desde archetype

```bash
mvn install -DskipTests
mvn archetype:generate \
  -DarchetypeGroupId=io.lexcupstudio \
  -DarchetypeArtifactId=lexcupstudio-example-language-archetype \
  -DarchetypeVersion=0.1.0 \
  -DgroupId=com.myteam \
  -DartifactId=my-language \
  -Dversion=0.1.0 \
  -Dpackage=com.myteam.language \
  -DlexcupstudioVersion=0.1.0 \
  -DinteractiveMode=false
```
