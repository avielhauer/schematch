package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.Objects;

@Getter
public class StringPair {
    /**
     * -- GETTER --
     *
     * @return returns first string
     */
    private final String first;
    /**
     * -- GETTER --
     *
     * @return returns second string
     */
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

}
