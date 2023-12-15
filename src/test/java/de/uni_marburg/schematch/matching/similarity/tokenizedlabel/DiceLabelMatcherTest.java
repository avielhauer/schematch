package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.preprocessing.tokenization.CharBasedTokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.nGramTokenizer;
import de.uni_marburg.schematch.similarity.set.Dice;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class DiceLabelMatcherTest {
    private static String NAME = "DiceLabelMatcher";
    private static String PACKAGE_NAME = "similarity.tokenizedlabel";

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);
        Tokenizer tokenizer = new CharBasedTokenizer(" ");
        Tokenizer tokenizerN2 = new nGramTokenizer(2);
        Dice<String> dice = new Dice<>();

        TokenizedLabelSimilarityMatcherTest.testTokenizedLabelSimilarityMatcher(matcherConfiguration, tokenizer, dice);
        TokenizedLabelSimilarityMatcherTest.testTokenizedLabelSimilarityMatcher(matcherConfiguration, tokenizerN2, dice);
    }
}