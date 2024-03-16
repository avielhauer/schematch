package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Pair<T, U> {
    private final T first;
    private final U second;

    /**
     * Initiates a Pair of the given object
     * @param first first object
     * @param second second object
     */
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<String, String> that = (Pair<String, String>) o;
        return first.equals(that.getFirst()) && second.equals(that.getSecond());
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    //public static void main(String[] args) {
    //    Pair<String, String> test1 = new Pair<>("test1", "test2");
    //    Pair<String, String> test2 = new Pair<>("test1", "test2");
    //    System.out.println(test1.equals(test2));
    //    System.out.println(test2.hashCode());
//
    //}


}
