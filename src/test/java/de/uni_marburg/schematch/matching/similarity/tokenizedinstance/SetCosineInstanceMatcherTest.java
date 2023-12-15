package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.preprocessing.tokenization.CharBasedTokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.nGramTokenizer;
import de.uni_marburg.schematch.similarity.set.SetCosine;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class SetCosineInstanceMatcherTest {
    private static String NAME = "SetCosineInstanceMatcher";
    private static String PACKAGE_NAME = "similarity.tokenizedinstance";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        Tokenizer tokenizer = new CharBasedTokenizer(" ");
        Tokenizer tokenizerN2 = new nGramTokenizer(2);
        SetCosine<String> setCosine = new SetCosine<>();

        TokenizedInstanceSimilarityMatcherTest.testTokenizedInstanceSimilarityMatcher(matcherConfiguration, tokenizer, setCosine);
        TokenizedInstanceSimilarityMatcherTest.testTokenizedInstanceSimilarityMatcher(matcherConfiguration, tokenizerN2, setCosine);
    }
}