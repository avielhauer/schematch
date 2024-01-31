package de.uni_marburg.schematch.matching.ensemble.features;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.Random;

public abstract class Feature {



    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    String name;
    public abstract double calculateScore(ColumnPair columnPair);




}
