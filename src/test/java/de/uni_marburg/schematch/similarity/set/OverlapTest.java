package de.uni_marburg.schematch.similarity.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverlapTest {

    @Test
    void compare() {
        Overlap<String> overlap = new Overlap<>();
        assertEquals((float) 2/3, overlap.compare(TestSets.s1, TestSets.s2));
        assertEquals((float) 2/3, overlap.compare(TestSets.s3, TestSets.s4));
        assertEquals(0.0, overlap.compare(TestSets.s1, TestSets.s4));
    }
}