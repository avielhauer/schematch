package de.uni_marburg.schematch.similarity.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongestCommonSubsequenceTest {

    @Test
    void compare() {
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        assertEquals(0.2, lcs.compare(TestStrings.HELLO, TestStrings.WORLD), TestStrings.TEST_DELTA);
        assertEquals(0.0, lcs.compare(TestStrings.HELLO, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.5, lcs.compare(TestStrings.TWELVE, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals((float) 2/3, lcs.compare(TestStrings.aa, TestStrings.a_a), TestStrings.TEST_DELTA);
        assertEquals((float) 1/11, lcs.compare(TestStrings.HELLO_WORLD, TestStrings.hello_world), TestStrings.TEST_DELTA);
        assertEquals((float) 3/7, lcs.compare("CABCADD", "BBBAADA"), TestStrings.TEST_DELTA);
        assertEquals(0.5, lcs.compare(TestStrings.John_Doe, TestStrings.Doe_John), TestStrings.TEST_DELTA);
    }
}