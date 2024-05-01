package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class KangEtNaughton extends TablePairMatcher {
    final private int numSeededRuns = 100;

    @Override
    public float[][] match(TablePair tablePair) {

        // Extract Tables
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();

        boolean swapped = false;
        if (targetTable.getColumns().size() > sourceTable.getColumns().size()) {
            Table temp = sourceTable;
            sourceTable = targetTable;
            targetTable = temp;
            swapped = true;
        }

        double[][] sourceEntropyMatrix = getEntropyMatrix(sourceTable);
        double[][] targetEntropyMatrix = getEntropyMatrix(targetTable);

        return HC(sourceEntropyMatrix, targetEntropyMatrix, swapped);
    }

    float[][] HC(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, boolean swapped){
        double maxError = Double.MAX_VALUE;
        List<Integer> minAlignment = new ArrayList<>();
        Random random = new Random(42);
        for(int i = 0; i < numSeededRuns; i++){
            Pair<List<Integer>, Double> runResult = runSeededHC(sourceEntropyMatrix, targetEntropyMatrix, random);
            if(runResult.getRight() < maxError){
                minAlignment = runResult.getLeft();
                maxError = runResult.getRight();
            }
        }
        return buildSimilarityMatrix(minAlignment, sourceEntropyMatrix, targetEntropyMatrix, swapped);
    }

    float[][] buildSimilarityMatrix(List<Integer> minAlignment, double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, boolean swapped){
        float[][] sm;
        if(swapped){
            sm = new float[targetEntropyMatrix.length][sourceEntropyMatrix.length];
            for(int i = 0; i < minAlignment.size(); i++){
                if(minAlignment.get(i) != -1){
                    sm[minAlignment.get(i)][i] = 1.0f;
                }
            }
        }else{
            sm = new float[sourceEntropyMatrix.length][targetEntropyMatrix.length];
            for(int i = 0; i < minAlignment.size(); i++){
                if(minAlignment.get(i) != -1){
                    sm[i][minAlignment.get(i)] = 1.0f;
                }
            }
        }
        return sm;
    }
    Pair<List<Integer>, Double> runSeededHC(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, Random random){
        List<Integer> alignments = getInitialAlignments(sourceEntropyMatrix.length, targetEntropyMatrix.length, random);
        double minError = calculateError(sourceEntropyMatrix, targetEntropyMatrix, alignments);
        boolean changed = true;
        while(changed){
            changed = false;
            List<Integer> newAlignments = null;
            for(int i = 0; i < alignments.size(); i ++){
                for(int j = i + 1; j < alignments.size(); j++){
                    List<Integer> permutedAlignment = new ArrayList<>(alignments);
                    Integer temp = permutedAlignment.get(i);
                    permutedAlignment.set(i, permutedAlignment.get(j));
                    permutedAlignment.set(j, temp);
                    double permutedError = calculateError(sourceEntropyMatrix, targetEntropyMatrix, alignments);
                    if(permutedError < minError){
                        minError = permutedError;
                        newAlignments = permutedAlignment;
                        changed = true;
                    }

                }
            }
            if(changed){
                alignments = newAlignments;
            }
        }
        return Pair.of(alignments, minError);
    }

    private double calculateError(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, List<Integer> alignments) {
        double euclideanDistance = 0;
        for(int i = 0; i < sourceEntropyMatrix.length; i++){
            for(int j = 0; j < sourceEntropyMatrix.length; j++){
                if(alignments.get(i) != -1 && alignments.get(j) != -1){ // TODO: verify this is fine.
                    euclideanDistance += Math.pow(sourceEntropyMatrix[i][j] - targetEntropyMatrix[alignments.get(i)][alignments.get(j)], 2);
                }
            }
        }
        return Math.sqrt(euclideanDistance);
    }

    List<Integer> getInitialAlignments(int sourceSize, int targetSize, Random random){
        List<Integer> alignments = IntStream.range(0, sourceSize).boxed().collect(Collectors.toList());;
        for(int i = targetSize; i < sourceSize; i++){
            alignments.set(i, -1); // no alignment when source and target are of unequal size (we assume bijective)
        }
        Collections.shuffle(alignments, random);
        return alignments;
    }
    double[][] getEntropyMatrix(Table table){
        double[][] entropyMatrix = new double[table.getNumColumns()][table.getNumColumns()];

        for(int i = 0; i < table.getNumColumns(); i++){
            for(int j = 0; j < table.getNumColumns() && j <= i; j++){
                if(i == j){
                    entropyMatrix[i][j] = getEntropy(combineColumns(table.getColumn(i).getValues(), table.getColumn(j).getValues()));
                }
                else{
                    entropyMatrix[j][i] = getMutualInformation(combineColumns(table.getColumn(i).getValues(), table.getColumn(j).getValues()));
                    entropyMatrix[i][j] = getMutualInformation(combineColumns(table.getColumn(i).getValues(), table.getColumn(j).getValues()));
                }
            }
        }
        return entropyMatrix;
    }

    List<List<String>> combineColumns(List<String> column1, List<String> column2){
        assert(column1.size() == column2.size());
        List<List<String>> combinedColumns = new ArrayList<>(column1.size());
        for(int i = 0; i < column1.size(); i++){
            combinedColumns.add(List.of(column1.get(i), column2.get(i)));
        }
        return combinedColumns;
    }

    private double getMutualInformation(final List<List<String>> values){
        int total = values.size();

        Map<String, Integer> frequencyCounterX = new HashMap<>();
        Map<String, Integer> frequencyCounterY = new HashMap<>();
        Map<List<String>, Integer> frequencyCounterXY = new HashMap<>();
        for (List<String> value : values) {
            frequencyCounterX.put(value.get(0), frequencyCounterX.getOrDefault(value.get(0), 0) + 1);
            frequencyCounterY.put(value.get(1), frequencyCounterY.getOrDefault(value.get(1), 0) + 1);
            frequencyCounterXY.put(value, frequencyCounterXY.getOrDefault(value, 0 ) + 1);
        }

        // Calculate entropy
        double mutualInformation = 0.0;
        for (Map.Entry<List<String>, Integer> entry : frequencyCounterXY.entrySet()) {
            double probabilityXY = (double) entry.getValue() / total;
            double probabilityX = (double) frequencyCounterX.get(entry.getKey().get(0)) / total;
            double probabilityY = (double) frequencyCounterY.get(entry.getKey().get(1)) / total;
            mutualInformation += probabilityXY * Math.log(probabilityXY / (probabilityX * probabilityY)) / Math.log(2);
        }

        return mutualInformation;
    }

    private double getEntropy(final List<List<String>> values){
        int total = values.size();

        Map<List<String>, Integer> frequencyCounter = new HashMap<>();
        for (List<String> value : values) {
            frequencyCounter.put(value, frequencyCounter.getOrDefault(value, 0) + 1);
        }

        // Calculate entropy
        double entropy = 0.0;
        for (Map.Entry<List<String>, Integer> entry : frequencyCounter.entrySet()) {
            double probability = (double) entry.getValue() / total;
            entropy -= probability * Math.log(probability) / Math.log(2);
        }

        return entropy;
    }

}

