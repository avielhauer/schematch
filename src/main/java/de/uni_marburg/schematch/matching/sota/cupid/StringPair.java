package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.Objects;

public class StringPair {
    private final String first;
    private final String second;

    /**
     * Creates string pair
     * @param first first string
     * @param second second string
     */
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

    /**
     * @return returns first string
     */
    public String getFirst() {
        return first;
    }

    /**
     * @return returns second string
     */
    public String getSecond() {
        return second;
    }
}
