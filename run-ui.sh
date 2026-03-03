#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER="$ROOT_DIR/mvnw"

"$WRAPPER" -q -f "$ROOT_DIR/pom.xml" -pl ui-api,plugin-example,ui-shell -am install -DskipTests
"$WRAPPER" -f "$ROOT_DIR/ui-shell/pom.xml" org.openjfx:javafx-maven-plugin:0.0.8:run
