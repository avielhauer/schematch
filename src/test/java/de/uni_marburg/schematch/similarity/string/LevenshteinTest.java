package de.uni_marburg.schematch.similarity.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinTest {

    @Test
    void compare() {
        Levenshtein ls = new Levenshtein();
        assertEquals(0.2, ls.compare(TestStrings.HELLO, TestStrings.WORLD), TestStrings.TEST_DELTA);
        assertEquals(0.0, ls.compare(TestStrings.HELLO, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.5, ls.compare(TestStrings.TWELVE, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals((float) 2/3, ls.compare(TestStrings.aa, TestStrings.a_a), TestStrings.TEST_DELTA);
        assertEquals((float) 1/11, ls.compare(TestStrings.HELLO_WORLD, TestStrings.hello_world), TestStrings.TEST_DELTA);
        assertEquals(0.25, ls.compare(TestStrings.John_Doe, TestStrings.Doe_John), TestStrings.TEST_DELTA);
    }
}