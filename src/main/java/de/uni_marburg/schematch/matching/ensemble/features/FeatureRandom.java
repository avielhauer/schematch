package de.uni_marburg.schematch.matching.ensemble.features;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.Random;

public class FeatureRandom extends Feature{
    public FeatureRandom(String name) {
        super(name);
    }

    public double calculateScore(ColumnPair columnPair)
    {
        Random random=new Random();
        return random.nextDouble();
    }

}
