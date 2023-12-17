package de.uni_marburg.schematch.similarity.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HammingTest {

    @Test
    void compare() {
        Hamming h = new Hamming();
        assertEquals(0.2, h.compare(TestStrings.HELLO, TestStrings.WORLD), TestStrings.TEST_DELTA);
        assertEquals(0.0, h.compare(TestStrings.HELLO, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.5, h.compare(TestStrings.TWELVE, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.5, h.compare(TestStrings.aa, TestStrings.a_a), TestStrings.TEST_DELTA);
        assertEquals((float) 1/11, h.compare(TestStrings.HELLO_WORLD, TestStrings.hello_world), TestStrings.TEST_DELTA);
        assertEquals(0.125, h.compare(TestStrings.John_Doe, TestStrings.Doe_John), TestStrings.TEST_DELTA);
    }
}