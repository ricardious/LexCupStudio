package edu.usac.olc1.olc1_proyecto1.automata;

import java.util.*;

public class AFD extends Automaton {

    private final Set<String> states = new LinkedHashSet<>();
    private final Set<String> alphabet = new LinkedHashSet<>();
    private String initial;
    private final Set<String> accept = new LinkedHashSet<>();
    // δ: (state -> (symbol -> nextState))
    private final Map<String, Map<String, String>> delta = new LinkedHashMap<>();

    public AFD(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "AFD";
    }

    public void addState(String s) {
        requireNonBlank(s, "state");
        states.add(s);
    }

    public void addSymbol(String a) {
        requireNonBlank(a, "symbol");
        alphabet.add(a);
    }

    public void setInitial(String s) {
        requireNonBlank(s, "initial");
        initial = s;
        states.add(s);
    }

    public void addAccept(String s) {
        requireNonBlank(s, "accept state");
        accept.add(s);
        states.add(s);
    }

    public void addTransition(String from, String symbol, String to) {
        requireNonBlank(from, "from");
        requireNonBlank(symbol, "symbol");
        requireNonBlank(to, "to");
        if (!states.contains(from)) {
            throw new IllegalArgumentException("Transition 'from' state not in states: " + from);
        }
        if (!states.contains(to)) {
            throw new IllegalArgumentException("Transition 'to' state not in states: " + to);
        }
        if (!alphabet.contains(symbol)) {
            throw new IllegalArgumentException("Symbol not in alphabet: " + symbol);
        }
        delta.computeIfAbsent(from, k -> new LinkedHashMap<>());
        String prev = delta.get(from).put(symbol, to);
        if (prev != null && !prev.equals(to)) {
            throw new IllegalStateException("Determinism violated at (" + from + ", " + symbol +
                    "): was " + prev + ", new " + to);
        }
    }

    public Set<String> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public Set<String> getAlphabet() {
        return Collections.unmodifiableSet(alphabet);
    }

    public String getInitial() {
        return initial;
    }

    public Set<String> getAccept() {
        return Collections.unmodifiableSet(accept);
    }

    public Map<String, Map<String, String>> getDelta() {
        Map<String, Map<String, String>> copy = new LinkedHashMap<>();
        for (var e : delta.entrySet()) {
            copy.put(e.getKey(), Collections.unmodifiableMap(e.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<String> checkWellFormed() {
        List<String> issues = new ArrayList<>();
        if (initial == null || initial.isBlank()) {
            issues.add("Initial state is not set.");
        } else if (!states.contains(initial)) {
            issues.add("Initial state '" + initial + "' is not in the states set.");
        }
        for (String f : delta.keySet()) {
            if (!states.contains(f)) issues.add("Transition 'from' state not declared: " + f);
            for (var e : delta.get(f).entrySet()) {
                String sym = e.getKey();
                String to = e.getValue();
                if (!alphabet.contains(sym)) issues.add("Transition symbol not in alphabet: " + sym);
                if (!states.contains(to)) issues.add("Transition 'to' state not declared: " + to);
            }
        }
        for (String a : accept) {
            if (!states.contains(a)) issues.add("Accept state not declared in states: " + a);
        }
        return issues;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName())
                .append("\nType: AFD")
                .append("\nStates: ").append(states)
                .append("\nAlphabet: ").append(alphabet)
                .append("\nInitial: ").append(initial)
                .append("\nAccept: ").append(accept)
                .append("\nTransitions:");
        for (var from : delta.keySet()) {
            for (var e : delta.get(from).entrySet()) {
                sb.append("\n  ").append(from)
                        .append(" --").append(e.getKey())
                        .append("--> ").append(e.getValue());
            }
        }
        return sb.toString();
    }

    @Override
    public ValidationResult validate(String input) {
        List<String> steps = new ArrayList<>();
        if (initial == null || initial.isBlank()) {
            steps.add("Initial state not set.");
            return ValidationResult.reject(steps, null, "AFD has no initial state");
        }
        List<String> wf = checkWellFormed();
        if (!wf.isEmpty()) {
            steps.addAll(wf);
            return ValidationResult.reject(steps, null, "AFD is not well-formed");
        }

        String current = initial;
        steps.add("Start at " + current);

        for (int i = 0; i < input.length(); i++) {
            String sym = String.valueOf(input.charAt(i));

            if (!alphabet.contains(sym)) {
                steps.add("Symbol '" + sym + "' not in alphabet Σ");
                return ValidationResult.reject(steps, current,
                        "Unknown symbol '" + sym + "' at position " + i);
            }

            String next = null;
            Map<String, String> row = delta.get(current);
            if (row != null) next = row.get(sym);

            steps.add(current + " --" + sym + "--> " + (next == null ? "∅" : next));

            if (next == null) {
                return ValidationResult.reject(steps, current,
                        "No transition defined for (" + current + ", " + sym + ")");
            }
            current = next;
        }

        boolean ok = accept.contains(current);
        steps.add("End at " + current + (ok ? " (ACCEPT)" : " (REJECT)"));
        return ok
                ? ValidationResult.accept(steps, current)
                : ValidationResult.reject(steps, current, "Final state is not accepting");
    }

    private static void requireNonBlank(String s, String what) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException(what + " cannot be null/blank");
    }
}
