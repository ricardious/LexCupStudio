package io.lexcupstudio.ui.api;

import java.util.Objects;

public final class SourceDiagnostic {
    private final DiagnosticType type;
    private final int line;
    private final int column;
    private final int length;
    private final String message;

    public SourceDiagnostic(DiagnosticType type, int line, int column, int length, String message) {
        this.type = Objects.requireNonNull(type, "type");
        this.line = Math.max(1, line);
        this.column = Math.max(1, column);
        this.length = Math.max(1, length);
        this.message = Objects.requireNonNullElse(message, "").trim();
    }

    public DiagnosticType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getLength() {
        return length;
    }

    public String getMessage() {
        return message;
    }
}
