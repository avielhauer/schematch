package de.uni_marburg.schematch.similarity.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiceTest {

    @Test
    void compare() {
        Dice<String> dice = new Dice<>();
        assertEquals((float) 4/8, dice.compare(TestSets.s1, TestSets.s2));
        assertEquals((float) 4/9, dice.compare(TestSets.s3, TestSets.s4));
        assertEquals(0.0, dice.compare(TestSets.s1, TestSets.s4));
    }
}