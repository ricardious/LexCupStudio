package edu.usac.olc1.olc1_proyecto1.automata;

public record TransitionAFD(String from, String symbol, String to) {
    public TransitionAFD(String from, String symbol, String to) {
        this.from = requireNonBlank(from, "from");
        this.symbol = requireNonBlank(symbol, "symbol");
        this.to = requireNonBlank(to, "to");
    }

    @Override
    public String toString() {
        return from + " --" + symbol + "--> " + to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitionAFD(String from1, String symbol1, String v))) return false;
        return from.equals(from1) && symbol.equals(symbol1) && to.equals(v);
    }

    private static String requireNonBlank(String s, String what) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException(what + " cannot be null/blank");
        return s.trim();
    }
}