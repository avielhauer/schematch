package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.matching.Matcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MatcherUtils {
    public static List<Matcher> sortMatchersByName(Set<Matcher> matchers) {
        List<Matcher> sortedMatchers = new ArrayList<>(matchers);
        sortedMatchers.sort(Comparator.comparing(Matcher::toString));
        return sortedMatchers;
    }
}
