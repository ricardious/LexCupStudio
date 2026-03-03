package io.lexcupstudio.core;

public record FrontendMessage(String type, String message, int line, int column) {
    public static FrontendMessage error(String message, int line, int column) {
        return new FrontendMessage("ERROR", message, line, column);
    }

    public static FrontendMessage warning(String message, int line, int column) {
        return new FrontendMessage("WARNING", message, line, column);
    }
}
