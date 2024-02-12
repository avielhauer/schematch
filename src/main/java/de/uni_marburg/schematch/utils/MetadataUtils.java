package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.PdepTuple;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;

import java.util.*;
import java.util.Map.Entry;

public class MetadataUtils {

    public static PdepTuple getPdep(FunctionalDependency fd){
        Collection<Column> determinant = fd.getDeterminant();
        Column dependant = fd.getDependant();
        int N = dependant.getValues().size();
        Map<String, Integer> frequencyMapDep = createFrequencyMap(dependant);
        Map<String, Integer> frequencyMapDet = createFrequencyMap(determinant, N);
        Map<String, Map<String, Integer>> frequencyMapDepByDet = createFrequencyMapGroupedByDeterminant(determinant, dependant, N);

        double pdep = pdepAB(frequencyMapDet, frequencyMapDepByDet, N);
        double gpdep = gpdep(pdep, frequencyMapDet, frequencyMapDep,N);
        return new PdepTuple(pdep, gpdep);
    }


    public static double epdep(int dA, Map<String, Integer> valuesB, int N) {
        double pdepB = pdep(valuesB, N);

        return pdepB + (dA - 1.0) / (N - 1.0) * (1.0 - pdepB);
    }

    private static double pdep(Map<String, Integer> valuesB, int N) {
        double result = 0;
        for (Integer count : valuesB.values()) {
            result += (count*count);
        }
        return result / (N*N);
    }

    private static double pdepAB(Map<String, Integer> valuesA, Map<String, Map<String, Integer>> valuesBByA, int N) {
        double result = 0;
        for (Map.Entry<String, Integer> aValue : valuesA.entrySet()) {
            for (Integer count : valuesBByA.get(aValue.getKey()).values()) {
                result += (double) (count * count) / aValue.getValue();
            }
        }
        return result / N;
    }

    public static double gpdep(double pdepAB, Map<String, Integer> valuesA, Map<String, Integer> valuesB, int N) {
        double epdepAB = epdep(valuesA.size(), valuesB, N);

        return pdepAB - epdepAB;
    }

    public static Map<String, Integer> createFrequencyMap(Column column) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String value : column.getValues()) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }

        return frequencyMap;
    }

    public static Map<String, Integer> createFrequencyMap(Collection<Column> columns, int size) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            StringBuilder concatenatedValue = new StringBuilder();
            for (Column col : columns) {
                concatenatedValue.append(col.getValues().get(i));
            }
            String key = concatenatedValue.toString();
            frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
        }

        return frequencyMap;
    }

    public static Map<String, Map<String, Integer>> createFrequencyMapGroupedByDeterminant(Collection<Column> determinant, Column dependant, int size) {
        Map<String, Map<String, Integer>> frequencyMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            StringBuilder concatenatedValue = new StringBuilder();
            for (Column col : determinant) {
                concatenatedValue.append(col.getValues().get(i));
            }
            String key = concatenatedValue.toString();
            Map<String, Integer> frequencyMapByDet = frequencyMap.computeIfAbsent(key, k -> new HashMap<>());
            String dependantKey = dependant.getValues().get(i);
            frequencyMapByDet.put(dependantKey, frequencyMapByDet.getOrDefault(dependantKey, 0) + 1);
        }

        return frequencyMap;
    }
}
