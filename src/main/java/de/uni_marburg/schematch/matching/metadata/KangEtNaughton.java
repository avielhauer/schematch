package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class KangEtNaughton extends TablePairMatcher {
    final private int numSeededRuns = 100;
    final private double normalizedDistanceAlpha = 2.0;

    @Override
    public float[][] match(TablePair tablePair) {

        // Extract Tables
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();

        // We swap tables, so that the sourceTable contains more attributes, which makes the latter alignment code
        // simpler. Note that the final sm need to be swapped back, so that the original order is respected.
        boolean swapped = false;
        if (targetTable.getColumns().size() > sourceTable.getColumns().size()) {
            Table temp = sourceTable;
            sourceTable = targetTable;
            targetTable = temp;
            swapped = true;
        }

        double[][] sourceMutualInformationMatrix = getMutualInformationMatrix(sourceTable);
        double[][] targetMutualInformationMatrix = getMutualInformationMatrix(targetTable);

        return HillClimbing(sourceMutualInformationMatrix, targetMutualInformationMatrix, swapped);
    }

    float[][] HillClimbing(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, boolean swapped) {
        double highestDistanceScore = Double.MIN_VALUE;
        List<Integer> minAlignment = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < numSeededRuns; i++) {
            Pair<List<Integer>, Double> runResult = runSeededHC(sourceEntropyMatrix, targetEntropyMatrix, random);
            if (runResult.getRight() > highestDistanceScore) {
                minAlignment = runResult.getLeft();
                highestDistanceScore = runResult.getRight();
            }
        }
        return buildSimilarityMatrix(minAlignment, sourceEntropyMatrix, targetEntropyMatrix, swapped);
    }

    float[][] buildSimilarityMatrix(List<Integer> minAlignment, double[][] sourceMutualInformationMatrix, double[][] targetMutualInformationMatrix, boolean swapped) {
        float[][] sm;
        if (swapped) {
            sm = new float[targetMutualInformationMatrix.length][sourceMutualInformationMatrix.length];
            for (int i = 0; i < minAlignment.size(); i++) {
                if (minAlignment.get(i) != -1) {
                    sm[minAlignment.get(i)][i] = 1.0f;
                }
            }
        } else {
            sm = new float[sourceMutualInformationMatrix.length][targetMutualInformationMatrix.length];
            for (int i = 0; i < minAlignment.size(); i++) {
                if (minAlignment.get(i) != -1) {
                    sm[i][minAlignment.get(i)] = 1.0f;
                }
            }
        }
        return sm;
    }

    Pair<List<Integer>, Double> runSeededHC(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, Random random) {
        List<Integer> alignments = getInitialAlignments(sourceEntropyMatrix.length, targetEntropyMatrix.length, random);
        double highestDistanceScore = calculateDistance(sourceEntropyMatrix, targetEntropyMatrix, alignments);
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Integer> newAlignments = null;
            for (int i = 0; i < alignments.size(); i++) {
                for (int j = i + 1; j < alignments.size(); j++) {
                    List<Integer> permutedAlignment = new ArrayList<>(alignments);
                    Integer temp = permutedAlignment.get(i);
                    permutedAlignment.set(i, permutedAlignment.get(j));
                    permutedAlignment.set(j, temp);
                    double permutedDistance = calculateDistance(sourceEntropyMatrix, targetEntropyMatrix, permutedAlignment);
                    if (permutedDistance > highestDistanceScore) { // Normal distance metric -> the higher, the "better"
                        highestDistanceScore = permutedDistance;
                        newAlignments = permutedAlignment;
                        changed = true;
                    }

                }
            }
            if (changed) {
                alignments = newAlignments;
            }
        }
        return Pair.of(alignments, highestDistanceScore);
    }

    private double calculateDistance(double[][] sourceEntropyMatrix, double[][] targetEntropyMatrix, List<Integer> alignments) {
        double normalDistance = 0;
        for (int i = 0; i < sourceEntropyMatrix.length; i++) {
            for (int j = 0; j < sourceEntropyMatrix.length; j++) {
                if (alignments.get(i) == -1 || alignments.get(j) == -1) { // TODO: verify this is fine.
                    continue;
                }
                double a_ij = sourceEntropyMatrix[i][j];
                double b_ij = targetEntropyMatrix[alignments.get(i)][alignments.get(j)];
                normalDistance += 1 - (normalizedDistanceAlpha * Math.abs(a_ij - b_ij) / (a_ij + b_ij));
            }
        }
        return normalDistance;
    }

    List<Integer> getInitialAlignments(int sourceSize, int targetSize, Random random) {
        List<Integer> alignments = IntStream.range(0, sourceSize).boxed().collect(Collectors.toList());
        for (int i = targetSize; i < sourceSize; i++) {
            alignments.set(i, -1); // no alignment when source and target are of unequal size (we assume bijective)
        }
        Collections.shuffle(alignments, random);
        return alignments;
    }

    double[][] getMutualInformationMatrix(Table table) {
        double[][] entropyMatrix = new double[table.getNumColumns()][table.getNumColumns()];

        for (int i = 0; i < table.getNumColumns(); i++) {
            for (int j = 0; j < table.getNumColumns() && j <= i; j++) {
                double mutualInformation = getMutualInformation(table.getColumn(i).getValues(), table.getColumn(j).getValues());
                entropyMatrix[j][i] = mutualInformation;
                entropyMatrix[i][j] = mutualInformation;
            }
        }
        return entropyMatrix;
    }

    private double getMutualInformation(final List<String> columnValuesX, final List<String> columnValuesY) {

        assert (columnValuesX.size() == columnValuesY.size());
        int total = columnValuesX.size();
        // Count occurrences of X, Y, and XY.
        Map<String, Integer> frequencyCounterX = new HashMap<>();
        Map<String, Integer> frequencyCounterY = new HashMap<>();
        Map<List<String>, Integer> frequencyCounterXY = new HashMap<>();
        for (int i = 0; i < total; i++) {
            frequencyCounterX.put(columnValuesX.get(i), frequencyCounterX.getOrDefault(columnValuesX.get(i), 0) + 1);
            frequencyCounterY.put(columnValuesY.get(i), frequencyCounterY.getOrDefault(columnValuesY.get(i), 0) + 1);
            List<String> combination = List.of(columnValuesX.get(i), columnValuesY.get(i));
            frequencyCounterXY.put(combination, frequencyCounterXY.getOrDefault(combination, 0) + 1);
        }

        // Calculate Mutual Information based on P_X, P_Y, and P_XY.
        double mutualInformation = 0.0;
        for (Map.Entry<List<String>, Integer> entry : frequencyCounterXY.entrySet()) {
            double probabilityXY = (double) entry.getValue() / total;
            double probabilityX = (double) frequencyCounterX.get(entry.getKey().get(0)) / total;
            double probabilityY = (double) frequencyCounterY.get(entry.getKey().get(1)) / total;
            mutualInformation += probabilityXY * Math.log(probabilityXY / (probabilityX * probabilityY)) / Math.log(2);
        }

        return mutualInformation;
    }

}

