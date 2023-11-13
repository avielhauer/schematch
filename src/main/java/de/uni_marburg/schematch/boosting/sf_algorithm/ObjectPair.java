package de.uni_marburg.schematch.boosting.sf_algorithm;

public record ObjectPair(Object objectA, Object objectB) {
    @Override
    public String toString(){
        return "("+objectA+","+objectB+")";
    }
}
