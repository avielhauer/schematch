package de.uni_marburg.schematch.matching.ensemble.features;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.List;

public abstract class Feature {
    String name;
    double k;

    public Feature(String name){
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String suffix){
        this.name = this.getClass().getName();
        if (!suffix.isEmpty()){
            this.name = this.name + " description: " + suffix;
        }
    }

    @Deprecated
    public double calculateScoreOfFeatrue(double column1, double column2,double sensitivityToDifferences){

//        return 1/1+sensitivityToDifferences*Math.pow((column1-column2),2);
        return  Math.exp(-sensitivityToDifferences * Math.abs(column1 - column2));
    }
    @Deprecated
    public double calculateScoreOfFeatrue(double column1, double column2){
        return calculateScoreOfFeatrue(column1,column2,k);
    }

    public void initiateK (double columnValue1,double columnValue2){
        this.k = 1.0/Math.max(columnValue1,columnValue2);
    }

    public void initiateK (List<Double> twoColumnDoubleVal){
        initiateK(twoColumnDoubleVal.get(0),twoColumnDoubleVal.get(1));
    }

    public abstract double calculateScore(ColumnPair columnPair);


    public double calc(double x,double y)
    {
        double diff=1/(Math.pow(Math.abs(x-y),4)+1);
        double avg=(x+y)/2;
        return diff*avg;
    }

}
