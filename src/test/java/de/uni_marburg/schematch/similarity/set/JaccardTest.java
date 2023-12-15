package de.uni_marburg.schematch.similarity.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JaccardTest {

    @Test
    void compare() {
        Jaccard<String> jaccard = new Jaccard<>();
        assertEquals((float) 2/6, jaccard.compare(TestSets.s1, TestSets.s2));
        assertEquals((float) 2/7, jaccard.compare(TestSets.s3, TestSets.s4));
        assertEquals(0.0, jaccard.compare(TestSets.s1, TestSets.s4));
    }
}