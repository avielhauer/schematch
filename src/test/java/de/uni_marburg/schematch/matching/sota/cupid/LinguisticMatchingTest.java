package de.uni_marburg.schematch.matching.sota.cupid;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LinguisticMatchingTest {

    LinguisticMatching l = new LinguisticMatching();

    @Test
    void normalization() {
        SchemaElement  ele = l.normalization("This is my sentence. I want to get all tokens out of it, even though this is just a test.", new SchemaElement("One", "String"));
        System.out.println(ele.getTokens());
        assertTrue(true);
    }

    @Test
    void computeCompatibility() {
        Set<String> s = new HashSet<>();
        s.add("int");
        s.add("char");
        s.add("double");
        s.add("Geld");
        s.add("Gelder");
        s.add("nchar");

        Map<String, Map<String, Double>> test = l.computeCompatibility(s);
        for (Map.Entry<String, Map<String, Double>> entry : test.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        assertTrue(true);
    }

    @Test
    void comparison() {
    }

    @Test
    void computeLsim() {
    }
}