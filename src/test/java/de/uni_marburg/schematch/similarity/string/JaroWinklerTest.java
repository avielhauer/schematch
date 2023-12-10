package de.uni_marburg.schematch.similarity.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JaroWinklerTest {

    @Test
    void compare() {
        JaroWinkler jw = new JaroWinkler();
        assertEquals(0.466, jw.compare(TestStrings.HELLO, TestStrings.WORLD), TestStrings.TEST_DELTA);
        assertEquals(0.0, jw.compare(TestStrings.HELLO, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals((float) 2/3, jw.compare(TestStrings.TWELVE, TestStrings.FORTY_TWO), TestStrings.TEST_DELTA);
        assertEquals(0.611, jw.compare(TestStrings.aa, TestStrings.a_a), TestStrings.TEST_DELTA);
        assertEquals(0.393, jw.compare(TestStrings.HELLO_WORLD, TestStrings.hello_world), TestStrings.TEST_DELTA);
        assertEquals(0.583, jw.compare(TestStrings.John_Doe, TestStrings.Doe_John), TestStrings.TEST_DELTA);
    }
}