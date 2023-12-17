package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.Hamming;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class HammingMatcherTest {
    private static String NAME = "HammingMatcher";
    private static String PACKAGE_NAME = "similarity.label";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        Hamming h = new Hamming();

        LabelSimilarityMatcherTest.testLabelSimilarityMatcher(matcherConfiguration, h);
    }
}