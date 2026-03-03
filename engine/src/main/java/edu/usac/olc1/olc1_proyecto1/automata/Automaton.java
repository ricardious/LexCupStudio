package edu.usac.olc1.olc1_proyecto1.automata;

import java.util.List;
import java.util.Objects;

public abstract class Automaton {
    private final String name;

    protected Automaton(String name) {
        this.name = Objects.requireNonNull(name, "name == null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Automaton name cannot be empty");
        }
    }

    public String getName() {
        return name;
    }

    public abstract String getType();

    public abstract String describe();

    public abstract ValidationResult validate(String input);

    public static class ValidationResult {
        public final boolean accepted;
        public final List<String> steps;
        public final String finalState;
        public final String message;

        public ValidationResult(boolean accepted, List<String> steps) {
            this(accepted, steps, null, null);
        }

        public ValidationResult(boolean accepted, List<String> steps, String finalState, String message) {
            this.accepted = accepted;
            this.steps = List.copyOf(Objects.requireNonNull(steps, "steps == null"));
            this.finalState = finalState;
            this.message = message;
        }

        public static ValidationResult accept(List<String> steps, String finalState) {
            return new ValidationResult(true, steps, finalState, null);
        }

        public static ValidationResult reject(List<String> steps, String finalState, String message) {
            return new ValidationResult(false, steps, finalState, message);
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "accepted=" + accepted +
                    ", finalState=" + finalState +
                    ", steps=" + steps.size() +
                    (message != null ? ", message=" + message : "") +
                    '}';
        }
    }
}