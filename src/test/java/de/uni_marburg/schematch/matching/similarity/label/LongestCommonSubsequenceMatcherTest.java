package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.LongestCommonSubsequence;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class LongestCommonSubsequenceMatcherTest {
    private static String NAME = "LongestCommonSubsequenceMatcher";
    private static String PACKAGE_NAME = "similarity.label";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();

        LabelSimilarityMatcherTest.testLabelSimilarityMatcher(matcherConfiguration, lcs);
    }
}