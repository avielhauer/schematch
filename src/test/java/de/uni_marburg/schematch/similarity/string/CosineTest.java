package de.uni_marburg.schematch.similarity.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosineTest {

    @Test
    void compare() {
        Cosine c = new Cosine();
        assertEquals(0.507, c.compare(TestStrings.HELLO, TestStrings.WORLD), TestStrings.TEST_DELTA);
        assertEquals(0.0, c.compare(TestStrings.HELLO, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.5, c.compare(TestStrings.TWELVE, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.894, c.compare(TestStrings.aa, TestStrings.a_a), TestStrings.TEST_DELTA);
        assertEquals(0.052, c.compare(TestStrings.HELLO_WORLD, TestStrings.hello_world), TestStrings.TEST_DELTA);
        assertEquals(0.699, c.compare("CABCADD", "BBBAADA"), TestStrings.TEST_DELTA);
        assertEquals(1.0, c.compare(TestStrings.John_Doe, TestStrings.Doe_John), TestStrings.TEST_DELTA);
    }
}