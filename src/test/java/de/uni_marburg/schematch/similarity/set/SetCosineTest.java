package de.uni_marburg.schematch.similarity.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetCosineTest {

    @Test
    void compare() {
        SetCosine<String> setCosine = new SetCosine<>();
        assertEquals(0.516, setCosine.compare(TestSets.s1, TestSets.s2), 0.01);
        assertEquals(0.471, setCosine.compare(TestSets.s3, TestSets.s4), 0.01);
        assertEquals(0.0, setCosine.compare(TestSets.s1, TestSets.s4));
    }
}