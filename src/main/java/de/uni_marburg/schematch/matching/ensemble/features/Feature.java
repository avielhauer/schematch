package de.uni_marburg.schematch.matching.ensemble.features;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.Random;

public abstract class Feature {
    String name;
    double k;

    public String getName() {
        return name;
    }

    public void setName(){
        this.name = this.getClass().getName();
    }


    public double calculateScoreOfFeatrue(double column1, double column2,double sensitivityToDifferences){

//        return 1/1+sensitivityToDifferences*Math.pow((column1-column2),2);
        return  Math.exp(-sensitivityToDifferences * Math.abs(column1 - column2));
    }

    public double calculateScoreOfFeatrue(double column1, double column2){
        return calculateScoreOfFeatrue(column1,column2,k);
    }

    public void initiateK (double value1,double value2){
        this.k = 1.0/Math.max(value1,value2);
    }

    public abstract double calculateScore(ColumnPair columnPair);




}
