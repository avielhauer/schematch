package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.preprocessing.tokenization.CharBasedTokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.nGramTokenizer;
import de.uni_marburg.schematch.similarity.set.Jaccard;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class JaccardLabelMatcherTest {
    private static String NAME = "JaccardLabelMatcher";
    private static String PACKAGE_NAME = "similarity.tokenizedlabel";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        Tokenizer tokenizer = new CharBasedTokenizer(" ");
        Tokenizer tokenizerN2 = new nGramTokenizer(2);
        Jaccard<String> jaccard = new Jaccard<>();

        TokenizedLabelSimilarityMatcherTest.testTokenizedLabelSimilarityMatcher(matcherConfiguration, tokenizer, jaccard);
        TokenizedLabelSimilarityMatcherTest.testTokenizedLabelSimilarityMatcher(matcherConfiguration, tokenizerN2, jaccard);
    }
}