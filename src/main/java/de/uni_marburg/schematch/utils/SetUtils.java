package de.uni_marburg.schematch.utils;

import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
        HashSet<T> n1 = new HashSet<>(s1);
        HashSet<T> n2 = new HashSet<>(s2);
        n1.retainAll(n2);
        return n1;
    }
}
