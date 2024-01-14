package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.Random;

public class Feature {
    public Feature(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    String Name;
    public double calculateScore(ColumnPair columnPair)
    {
        Random random=new Random();
        return random.nextDouble();
    }

}
