package de.uni_marburg.schematch.preprocessing.tokenization;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class nGramTokenizerTest {

    @Test
    void tokenizeN2() {
        nGramTokenizer n2 = new nGramTokenizer(2);
        Set<String> hw = Set.of("HE","EL","LL","LO","O "," W","WO","OR","RL","LD");
        assertEquals(hw, n2.tokenize(TestStrings.HELLO_WORLD));
    }

    @Test
    void tokenizeN4() {
        nGramTokenizer n4 = new nGramTokenizer(4);
        Set<String> hw = Set.of("HELL","ELLO","LLO ","LO W","O WO"," WOR","WORL","ORLD");
        assertEquals(hw, n4.tokenize(TestStrings.HELLO_WORLD));
    }
}