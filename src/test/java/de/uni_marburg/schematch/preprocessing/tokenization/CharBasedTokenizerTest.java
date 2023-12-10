package de.uni_marburg.schematch.preprocessing.tokenization;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharBasedTokenizerTest {

    @Test
    void tokenizeSpace() {
        CharBasedTokenizer cbt = new CharBasedTokenizer(" ");
        Set<String> hw = Set.of("HELLO","WORLD");
        Set<String> s1 = Set.of("The","quick","brown","fox","jumps","over","the","lazy","dog");
        Set<String> s2 = Set.of("Lorem","ipsum","dolor","sit","amet,","consectetur","adipisici","elit,","...");
        assertEquals(hw, cbt.tokenize(TestStrings.HELLO_WORLD));
        assertEquals(s1, cbt.tokenize(TestStrings.SENTENCE1));
        assertEquals(s2, cbt.tokenize(TestStrings.SENTENCE2));
    }

    @Test
    void tokenizeComma() {
        CharBasedTokenizer cbt = new CharBasedTokenizer(",");
        Set<String> s2 = Set.of("Lorem ipsum dolor sit amet"," consectetur adipisici elit"," ...");
        assertEquals(Set.of(TestStrings.HELLO_WORLD), cbt.tokenize(TestStrings.HELLO_WORLD));
        assertEquals(Set.of(TestStrings.SENTENCE1), cbt.tokenize(TestStrings.SENTENCE1));
        assertEquals(s2, cbt.tokenize(TestStrings.SENTENCE2));
    }
}