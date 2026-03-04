# Plugin Guide (UI Shell)

Esta guía explica cómo usar `LexCupStudio` para que otras personas conecten su lógica sobre la UI **sin tocar la UI**.

## Qué se mantiene fijo

- `ui-shell`: interfaz visual JavaFX.
- `ui-api`: contrato que la UI consume.

## Qué implementa cada persona

- Un módulo plugin propio (por ejemplo `plugin-mi-lenguaje`) que implemente `LanguageRuntimePlugin`.

## Flujo rápido

1. Crear módulo Maven nuevo en el repo.
2. Agregar dependencia a `lexcupstudio-ui-api`.
3. Implementar `LanguageRuntimePlugin`.
4. Registrar la clase en `META-INF/services`.
5. Agregar módulo al `pom.xml` padre.
6. Ejecutar `./run-ui.sh`.

No hace falta crear un `main` de UI.

## 1) Crear módulo plugin

Ejemplo de estructura:

```text
plugin-mi-lenguaje/
  pom.xml
  src/main/java/com/miempresa/plugin/MiRuntimePlugin.java
  src/main/resources/META-INF/services/io.lexcupstudio.ui.api.LanguageRuntimePlugin
```

## 2) `pom.xml` mínimo

```xml
<project>
  <parent>
    <groupId>io.lexcupstudio</groupId>
    <artifactId>lexcupstudio-parent</artifactId>
    <version>0.1.0</version>
  </parent>

  <artifactId>plugin-mi-lenguaje</artifactId>

  <dependencies>
    <dependency>
      <groupId>io.lexcupstudio</groupId>
      <artifactId>lexcupstudio-ui-api</artifactId>
      <version>${project.version}</version>
    </dependency>

    <!-- Tus deps de parser/lexer/lógica -->
  </dependencies>
</project>
```

## 3) Implementar el contrato

Contrato actual: [LanguageRuntimePlugin.java](/home/ricardious/IdeaProjects/LexCupStudio/ui-api/src/main/java/io/lexcupstudio/ui/api/LanguageRuntimePlugin.java)

Ejemplo:

```java
package com.miempresa.plugin;

import io.lexcupstudio.ui.api.LanguageRuntimePlugin;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class MiRuntimePlugin implements LanguageRuntimePlugin {

    @Override
    public String commandName() {
        return "ricardious"; // comando que invoca la UI/terminal
    }

    @Override
    public String reportsDirectoryName() {
        return "output";
    }

    @Override
    public boolean run(String sourceText, Path projectDir, Consumer<String> log) {
        // 1) Ejecuta tu lexer/parser/semántica
        // 2) Genera archivos de salida si aplica
        // 3) Reporta progreso a la UI con log.accept(...)
        // 4) retorna true/false según éxito

        log.accept("Plugin ejecutado");
        return true;
    }

    @Override
    public List<SourceDiagnostic> analyze(String sourceText, Path projectDir, Consumer<String> log) {
        // Opcional: devolver errores léxicos/sintácticos para panel PROBLEMS.
        return List.of();
    }
}
```

Notas:
- `analyze(...)` es opcional (método default en la interfaz), pero recomendado.
- La terminal soporta `run` y `ricardious` como alias del mismo flujo.

## 4) Registrar plugin (ServiceLoader)

Archivo:

```text
src/main/resources/META-INF/services/io.lexcupstudio.ui.api.LanguageRuntimePlugin
```

Contenido del archivo (una línea):

```text
com.miempresa.plugin.MiRuntimePlugin
```

## 5) Agregar módulo en el parent

En `LexCupStudio/pom.xml`, en `<modules>`, agrega:

```xml
<module>plugin-mi-lenguaje</module>
```

## 6) Ejecutar la UI

Desde raíz de `LexCupStudio`:

```bash
./run-ui.sh
```

La UI cargará plugins por `ServiceLoader` automáticamente.

## Cómo probar rápido

1. Abrir un archivo de entrada en la UI.
2. Ejecutar el comando (`run` o `ricardious`).
3. Ver logs en terminal integrada.
4. Revisar carpeta de salida (`output/` u otra según `reportsDirectoryName()`).

## IntelliJ

- No crear nuevo `main` para UI.
- Solo crear módulo plugin y su provider file.
- Correr UI con `./run-ui.sh` (Terminal) o una configuración de Shell Script.

## Troubleshooting

- `No hay plugin de lenguaje cargado`:
  - Faltó el archivo en `META-INF/services` o el módulo plugin no está en `<modules>`.
- No encuentra clases del plugin:
  - Verifica dependencias del plugin y compilación Maven.
- La UI no arranca:
  - Ejecuta `./run-ui.sh` desde raíz del repo para instalar módulos previos.
