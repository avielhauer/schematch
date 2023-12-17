package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.Objects;

public class StringPair {
    private final String first;
    private final String second;

    public StringPair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringPair that = (StringPair) o;
        return first.equals(that.first) && second.equals(that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
}
