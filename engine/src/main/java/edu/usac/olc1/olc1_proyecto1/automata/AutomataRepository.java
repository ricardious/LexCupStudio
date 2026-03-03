package edu.usac.olc1.olc1_proyecto1.automata;

import java.util.*;

public class AutomataRepository {
    private final Map<String, Automaton> map = new LinkedHashMap<>();

    public void put(Automaton a) {
        Objects.requireNonNull(a, "automaton == null");
        String name = Objects.requireNonNull(a.getName(), "name == null").trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Automaton name cannot be empty");
        map.put(name, a);
    }

    public boolean putIfAbsent(Automaton a) {
        Objects.requireNonNull(a, "automaton == null");
        String name = Objects.requireNonNull(a.getName(), "name == null").trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Automaton name cannot be empty");
        return map.putIfAbsent(name, a) == null;
    }

    public Automaton get(String name) {
        return map.get(name);
    }

    public Optional<Automaton> find(String name) {
        return Optional.ofNullable(map.get(name));
    }

    public Automaton getOrThrow(String name) {
        Automaton a = map.get(name);
        if (a == null) throw new NoSuchElementException("Automaton not found: " + name);
        return a;
    }

    public boolean exists(String name) {
        return map.containsKey(name);
    }

    public Collection<Automaton> all() {
        return Collections.unmodifiableCollection(map.values());
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(map.keySet()));
    }

    public boolean remove(String name) {
        return map.remove(name) != null;
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }
}
