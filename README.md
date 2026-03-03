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
./run-ui.sh
```

La UI consume implementaciones de `LanguageRuntimePlugin` vía `ServiceLoader`.
Para integrar otra lógica, crea un módulo que implemente `ui-api` y registra el provider en `META-INF/services`.

### Plugin propio (resumen)

1. Crea un módulo nuevo (ej. `plugin-mi-lenguaje`).
2. Agrega dependencia a `lexcupstudio-ui-api`.
3. Implementa `LanguageRuntimePlugin`.
4. Registra la implementación en:
   `src/main/resources/META-INF/services/io.lexcupstudio.ui.api.LanguageRuntimePlugin`
5. Agrega tu módulo al `pom.xml` padre y ejecuta `./run-ui.sh`.

## Branding rapido (nombre y logo)

Edita:

`ui-shell/src/main/resources/edu/usac/olc1/olc1_proyecto1/branding.properties`

```properties
app.name=LexCupStudio
app.logo.path=/edu/usac/olc1/olc1_proyecto1/icons/logo.png
```

- `app.name`: nombre que aparece en la ventana principal, portada y mensajes de salida.
- `app.logo.path`: ruta absoluta dentro de recursos del `ui-shell`.

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

## Documentación de plugins

Guía completa: [docs/PLUGIN_GUIDE.md](/home/ricardious/IdeaProjects/LexCupStudio/docs/PLUGIN_GUIDE.md)
