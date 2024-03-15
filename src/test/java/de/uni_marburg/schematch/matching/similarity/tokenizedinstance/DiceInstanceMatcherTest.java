package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.preprocessing.tokenization.CharBasedTokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.nGramTokenizer;
import de.uni_marburg.schematch.similarity.set.Dice;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class DiceInstanceMatcherTest {
    private static String NAME = "DiceInstanceMatcher";
    private static String PACKAGE_NAME = "similarity.tokenizedinstance";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        Tokenizer tokenizer = new CharBasedTokenizer(" ");
        Tokenizer tokenizerN2 = new nGramTokenizer(2);
        Dice<String> dice = new Dice<>();

        TokenizedInstanceSimilarityMatcherTest.testTokenizedInstanceSimilarityMatcher(matcherConfiguration, tokenizer, dice);
        TokenizedInstanceSimilarityMatcherTest.testTokenizedInstanceSimilarityMatcher(matcherConfiguration, tokenizerN2, dice);
    }
}