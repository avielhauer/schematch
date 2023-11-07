package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.JaroWinkler;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class JaroWinklerMatcherTest {
    private static String NAME = "JaroWinklerMatcher";
    private static String PACKAGE_NAME = "similarity.label";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        JaroWinkler jw = new JaroWinkler();

        LabelSimilarityMatcherTest.testLabelSimilarityMatcher(matcherConfiguration, jw);
    }
}