package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

@Getter
public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringPair that = (StringPair) o;
        return first.equals(that.getFirst()) && second.equals(that.getSecond());
    }

    //public static void main(String[] args) {
    //    Pair<String, String> test1 = new Pair<>("test1", "test2");
    //    Pair<String, String> test2 = new Pair<>("test1", "test2");
    //    System.out.println(test1.equals(test2));
//
    //}


}
