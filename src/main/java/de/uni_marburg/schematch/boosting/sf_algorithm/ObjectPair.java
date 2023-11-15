package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import java.util.Map;

public record ObjectPair<A,B>(A objectA, B objectB) {
    @Override
    public String toString(){
        return "("+objectA+","+objectB+")";
    }

    public float similarity(SimilarityMeasure<String> stringSimilarity, Map<ObjectPair<Column, Column>, Float> columnSimilarity){
        if(objectA == null || objectB == null) return 0;
        if(objectA.getClass() != objectB.getClass()) return 0;
        if(objectA.getClass() == Float.class){
            float a = (Float) objectA;
            float b = (Float) objectB;
            return Math.abs(a - b) / Math.max(a,b);
        }
        if(objectA.getClass() == String.class){
            String a = (String) objectA;
            String b = (String) objectB;
            return stringSimilarity.compare(a,b);
        }
        if(objectA.getClass() == Column.class){
            return columnSimilarity.get(this);
        }
        return 0;
    }
}
