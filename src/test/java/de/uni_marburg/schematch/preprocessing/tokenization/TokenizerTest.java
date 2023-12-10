package de.uni_marburg.schematch.preprocessing.tokenization;

import de.uni_marburg.schematch.data.Column;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizerTest {

    @Test
    void tokenizeColumnWithNGrams4() {
        nGramTokenizer n4 = new nGramTokenizer(4);
        List<String> values = new ArrayList<>();
        values.add("HELLO");
        values.add("WORLD");
        Column c = new Column("test", values);
        Set<String> expected = Set.of("HELL","ELLO","WORL","ORLD");
        n4.tokenize(c);
        assertEquals(expected, c.getValuesTokens(n4));
    }
}