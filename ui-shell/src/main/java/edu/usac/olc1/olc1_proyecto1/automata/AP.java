package edu.usac.olc1.olc1_proyecto1.automata;

import java.util.*;

public class AP extends Automaton {

    public static final String EPS = "$";

    private final Set<String> states = new LinkedHashSet<>();
    private final Set<String> alphabet = new LinkedHashSet<>();
    private final Set<String> stackSymbols = new LinkedHashSet<>();
    private String initial;
    private final Set<String> accept = new LinkedHashSet<>();

    public static class TransitionAP {
        public final String from, input, pop, to, push;

        public TransitionAP(String from, String input, String pop, String to, String push) {
            this.from = Objects.requireNonNull(from, "from == null").trim();
            this.input = Objects.requireNonNull(input, "input == null").trim();
            this.pop = Objects.requireNonNull(pop, "pop == null").trim();
            this.to = Objects.requireNonNull(to, "to == null").trim();
            this.push = Objects.requireNonNull(push, "push == null").trim();
            if (this.from.isEmpty() || this.to.isEmpty()) {
                throw new IllegalArgumentException("from/to cannot be blank");
            }
            if (this.input.isEmpty() || this.pop.isEmpty() || this.push.isEmpty()) {
                throw new IllegalArgumentException("input/pop/push cannot be blank");
            }
        }

        @Override
        public String toString() {
            return from + " --(" + input + ", pop:" + pop + ", push:" + push + ")--> " + to;
        }
    }

    private final List<TransitionAP> transitions = new ArrayList<>();

    public AP(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "AP";
    }

    public void addState(String s) {
        states.add(requireNonBlank(s, "state"));
    }

    public void addSymbol(String a) {
        alphabet.add(requireNonBlank(a, "alphabet symbol"));
    }

    public void addStackSymbol(String p) {
        stackSymbols.add(requireNonBlank(p, "stack symbol"));
    }

    public void setInitial(String s) {
        this.initial = requireNonBlank(s, "initial");
        states.add(this.initial);
    }

    public void addAccept(String s) {
        String v = requireNonBlank(s, "accept");
        accept.add(v);
        states.add(v);
    }

    public void addTransition(TransitionAP t) {
        if (!states.contains(t.from)) throw new IllegalArgumentException("from state not declared: " + t.from);
        if (!states.contains(t.to)) throw new IllegalArgumentException("to state not declared: " + t.to);

        if (!EPS.equals(t.input) && !alphabet.contains(t.input))
            throw new IllegalArgumentException("input not in alphabet (or $): " + t.input);

        if (!EPS.equals(t.pop) && !stackSymbols.contains(t.pop))
            throw new IllegalArgumentException("pop not in stack symbols (or $): " + t.pop);

        if (!EPS.equals(t.push) && !stackSymbols.contains(t.push))
            throw new IllegalArgumentException("push not in stack symbols (or $): " + t.push);

        transitions.add(t);
    }

    public Set<String> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public Set<String> getAlphabet() {
        return Collections.unmodifiableSet(alphabet);
    }

    public Set<String> getStackSymbols() {
        return Collections.unmodifiableSet(stackSymbols);
    }

    public String getInitial() {
        return initial;
    }

    public Set<String> getAccept() {
        return Collections.unmodifiableSet(accept);
    }

    public List<TransitionAP> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    public List<String> checkWellFormed() {
        List<String> issues = new ArrayList<>();
        if (initial == null || initial.isBlank()) {
            issues.add("Initial state is not set.");
        } else if (!states.contains(initial)) {
            issues.add("Initial state '" + initial + "' is not in the states set.");
        }
        for (String a : accept) {
            if (!states.contains(a)) issues.add("Accept state not in states: " + a);
        }
        for (TransitionAP t : transitions) {
            if (!states.contains(t.from)) issues.add("Transition 'from' not declared: " + t.from);
            if (!states.contains(t.to)) issues.add("Transition 'to' not declared: " + t.to);
            if (!EPS.equals(t.input) && !alphabet.contains(t.input))
                issues.add("Transition input not in alphabet: " + t.input);
            if (!EPS.equals(t.pop) && !stackSymbols.contains(t.pop))
                issues.add("Transition pop not in stack symbols: " + t.pop);
            if (!EPS.equals(t.push) && !stackSymbols.contains(t.push))
                issues.add("Transition push not in stack symbols: " + t.push);
        }
        return issues;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName())
                .append("\nType: AP")
                .append("\nStates: ").append(states)
                .append("\nAlphabet: ").append(alphabet)
                .append("\nStackSymbols: ").append(stackSymbols)
                .append("\nInitial: ").append(initial)
                .append("\nAccept: ").append(accept)
                .append("\nTransitions:");
        for (TransitionAP t : transitions) {
            sb.append("\n  ").append(t);
        }
        return sb.toString();
    }

    @Override
    public ValidationResult validate(String input) {
        List<String> trace = new ArrayList<>();

        List<String> wf = checkWellFormed();
        if (!wf.isEmpty()) {
            trace.addAll(wf);
            return ValidationResult.reject(trace, null, "AP is not well-formed");
        }

        record Config(String state, int idx, List<String> stack, List<String> steps) {
        }

        Queue<Config> q = new ArrayDeque<>();
        Set<String> visited = new HashSet<>(); // (state|idx|stackString)

        List<String> startSteps = new ArrayList<>();
        startSteps.add("Start at state " + initial + ", stack []");
        q.add(new Config(initial, 0, List.of(), startSteps));
        visited.add(key(initial, 0, List.of()));

        final int n = input.length();
        final int MAX_EXPANSIONS = 100000;
        int expansions = 0;

        while (!q.isEmpty()) {
            if (++expansions > MAX_EXPANSIONS) {
                trace.add("Exploration limit reached; possible ε-cycle.");
                return ValidationResult.reject(trace, null, "Exploration limit reached");
            }

            Config c = q.poll();
            String state = c.state;
            int idx = c.idx;
            List<String> stack = c.stack;
            List<String> steps = c.steps;

            if (idx == n && accept.contains(state)) {
                List<String> done = new ArrayList<>(steps);
                done.add("Accept at state " + state + " with stack " + stack);
                return ValidationResult.accept(done, state);
            }

            for (TransitionAP t : transitions) {
                if (!t.from.equals(state)) continue;

                boolean consumes = !EPS.equals(t.input);
                if (consumes) {
                    if (idx >= n) continue;
                    String sym = String.valueOf(input.charAt(idx));
                    if (!sym.equals(t.input)) continue;
                }

                List<String> newStack = new ArrayList<>(stack);
                if (!EPS.equals(t.pop)) {
                    if (newStack.isEmpty() || !newStack.get(newStack.size() - 1).equals(t.pop)) {
                        continue;
                    }
                    newStack.remove(newStack.size() - 1);
                }

                if (!EPS.equals(t.push)) {
                    newStack.add(t.push);
                }

                int newIdx = consumes ? idx + 1 : idx;
                String newState = t.to;

                List<String> newSteps = new ArrayList<>(steps);
                String consumed = consumes ? t.input : EPS;
                String popped = EPS.equals(t.pop) ? "ε" : t.pop;
                String pushed = EPS.equals(t.push) ? "ε" : t.push;
                newSteps.add(stepLine(state, consumed, popped, newState, pushed, newStack, newIdx, n));

                String k = key(newState, newIdx, newStack);
                if (visited.add(k)) {
                    q.add(new Config(newState, newIdx, List.copyOf(newStack), newSteps));
                }
            }
        }

        trace.add("No accepting path found.");
        return ValidationResult.reject(trace, null, "No accepting run");
    }

    private static String requireNonBlank(String s, String what) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException(what + " cannot be null/blank");
        return s.trim();
    }

    private static String key(String st, int idx, List<String> stack) {
        return st + "|" + idx + "|" + stack;
    }

    private static String stepLine(String from, String in, String pop,
                                   String to, String push, List<String> stack,
                                   int idx, int n) {
        return String.format(
                "(i=%d/%d) %s --(in:%s, pop:%s, push:%s)--> %s  stack=%s",
                idx, n, from, in, pop, push, to, stack.toString()
        );
    }
}
