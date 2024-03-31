package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class InterattributeMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();

        double[][] dependencyMatrixSource = buildDependencyMatrix(sourceTable);
        double[][] dependencyMatrixTarget = buildDependencyMatrix(targetTable);

        // Find the best mapping using hill climb approach
        Map<Integer, Integer> bestMapping = matchGraphs(dependencyMatrixSource, dependencyMatrixTarget);

        // Generate the similarity matrix based on the best mapping
        for (Map.Entry<Integer, Integer> entry : bestMapping.entrySet()) {
            int sourceIndex = entry.getKey();
            int targetIndex = entry.getValue();
            if (sourceIndex < simMatrix.length && targetIndex < simMatrix[sourceIndex].length) {
                simMatrix[sourceIndex][targetIndex] = 1.0f;
            }
        }

        return simMatrix;
    }

    private double normalizeSimilarity(double similarityScore) {
        // Normalize similarity score to range [0, 1]
        return 1 / (1 + Math.exp(-similarityScore));
    }

    public double calculateCosineSimilarity(double[][] dependencyGraphA, double[][] dependencyGraphB, Map<Integer, Integer> nodeMappings) {
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < dependencyGraphA.length; i++) {
            for (int j = 0; j < dependencyGraphA[i].length; j++) {
                int mappedI = nodeMappings.getOrDefault(i, -1);
                int mappedJ = nodeMappings.getOrDefault(j, -1);
                if (mappedI != -1 && mappedJ != -1) {
                    dotProduct += dependencyGraphA[i][j] * dependencyGraphB[mappedI][mappedJ];
                    magnitudeA += Math.pow(dependencyGraphA[i][j], 2);
                    magnitudeB += Math.pow(dependencyGraphB[mappedI][mappedJ], 2);
                }
            }
        }

        double magnitudeProduct = Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB);
        if (magnitudeProduct == 0) return 0; // Avoid division by zero

        return dotProduct / magnitudeProduct;
    }

    // Perform hill climb approach to match two graphs
    public Map<Integer, Integer> matchGraphs(double[][] dependencyGraphA, double[][] dependencyGraphB) {
        Map<Integer, Integer> currentMapping = getRandomMapping(dependencyGraphA.length, dependencyGraphB.length);
        double currentSimilarity = calculateCosineSimilarity(dependencyGraphA, dependencyGraphB, currentMapping);

        // Hill climb iterations
        int maxIterations = 1000;
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Generate a random neighbor mapping
            Map<Integer, Integer> neighborMapping = getRandomNeighborMapping(currentMapping, dependencyGraphB.length);
            double neighborSimilarity = calculateCosineSimilarity(dependencyGraphA, dependencyGraphB, neighborMapping);

            // Check if neighbor mapping is valid
            if (isValidMapping(neighborMapping)) {
                // Accept the neighbor if it improves the similarity and maintains the constraint
                if (neighborSimilarity > currentSimilarity) {
                    currentMapping = neighborMapping;
                    currentSimilarity = neighborSimilarity;
                }
            }
        }

        return currentMapping;
    }

    private boolean isValidMapping(Map<Integer, Integer> mapping) {
        Set<Integer> targets = new HashSet<>();
        for (Integer target : mapping.values()) {
            if (targets.contains(target)) {
                // If a target attribute is already mapped, the mapping is invalid
                return false;
            }
            targets.add(target);
        }
        return true;
    }


    // Generate a random initial mapping
    private Map<Integer, Integer> getRandomMapping(int sizeA, int sizeB) {
        Map<Integer, Integer> mapping = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < sizeA; i++) {
            int randomIndexB = random.nextInt(sizeB);
            mapping.put(i, randomIndexB);
        }
        return mapping;
    }

    // Generate a random neighbor mapping by perturbing the current mapping
    private Map<Integer, Integer> getRandomNeighborMapping(Map<Integer, Integer> currentMapping, int sizeB) {
        Map<Integer, Integer> neighborMapping = new HashMap<>(currentMapping);
        Random random = new Random();
        int randomIndexA = random.nextInt(currentMapping.size());
        int randomIndexB = random.nextInt(sizeB);
        neighborMapping.put(randomIndexA, randomIndexB);
        return neighborMapping;
    }

//    public double[][] shuffleDependencyMatrix(double[][]dependencyMatrix, Random random, int seed){
//        for (int j = dependencyMatrix.length - 1; j > 0; j--) {
//            int index = random.nextInt(j + 1);
//            double[] temp = dependencyMatrix[index];
//            dependencyMatrix[index] = dependencyMatrix[j];
//            dependencyMatrix[j] = temp;
//        }
//
//        return dependencyMatrix;
//    }

    //called it dependencyMatrix, is essentially the dependencyGraph from the paper
    public double[][] buildDependencyMatrix(Table table) {
        int numColumns = table.getNumColumns();
        double[][] dependencyMatrix = new double[numColumns][numColumns];

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (i == j) {
                    List<String> columnValues = table.getColumn(i).getValues();
                    dependencyMatrix[i][j] = getEntropy(columnValues);
                } else {
                    List<String> column1Values = table.getColumn(i).getValues();
                    List<String> column2Values = table.getColumn(j).getValues();
                    dependencyMatrix[i][j] = calculateMutualInformation(column1Values, column2Values);
                }
            }
        }
//        System.out.println("DEPENDENCY MATRIX:");
//        for (int i = 0; i < dependencyMatrix.length; i++) {
//            for (int j = 0; j < dependencyMatrix[i].length; j++) {
//                System.out.print(dependencyMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
        return dependencyMatrix;
    }

    public double calculateEuclideanDistance(double[][] dependencyGraphA, double[][] dependencyGraphB, Map<Integer, Integer> nodeMappings) {
//        double sum = 0.0;
//        for (int i = 0; i < dependencyGraphA.length; i++) {
//            for (int j = 0; j < dependencyGraphA[i].length; j++) {
//                int mappedI = nodeMappings.getOrDefault(i, -1); // Get the mapped node index in graph B
//                int mappedJ = nodeMappings.getOrDefault(j, -1); // Get the mapped node index in graph B
//                if (mappedI != -1 && mappedJ != -1) { // If both nodes have matching counterparts in graph B
//                    double mutualInformationA = dependencyGraphA[i][j];
//                    double mutualInformationB = dependencyGraphB[mappedI][mappedJ];
//                    double difference = mutualInformationA - mutualInformationB;
//                    sum += difference * difference; // Add squared difference to the sum
//                }
//            }
//        }
//        return Math.sqrt(sum); // Return the square root of the sum


        return 1-calculateCosineSimilarity(dependencyGraphA, dependencyGraphB, nodeMappings);

    }

    private double calculateMutualInformation(List<String> values1, List<String> values2) {
        int total = values1.size();
        Map<Pair<String, String>, Integer> columnsValueTupleFrequencies = getTupleFrequency(values1, values2);
        Map<String, Integer> column1ValueFrequencies = getValueFrequency(values1);
        Map<String, Integer> column2ValueFrequencies = getValueFrequency(values2);

        double mutualInformation = 0.0;
        for (Map.Entry<String, Integer> entry1 : column1ValueFrequencies.entrySet()) {
            for (Map.Entry<String, Integer> entry2 : column2ValueFrequencies.entrySet()) {
                Pair<String, String> valuePair = Pair.of(entry1.getKey(), entry2.getKey());
                int frequency = columnsValueTupleFrequencies.getOrDefault(valuePair, 0);
                double probabilityEntry1 = (double) entry1.getValue() / total;
                double probabilityEntry2 = (double) entry2.getValue() / total;
                double probabilityEntry1and2 = (double) frequency / total;
                if (probabilityEntry1and2 > 0) {  // Avoid log(0)
                    mutualInformation += probabilityEntry1and2 * Math.log(probabilityEntry1and2 / (probabilityEntry1 * probabilityEntry2)) / Math.log(2);
                }
            }
        }
        return mutualInformation;
    }

    private Map<String, Integer> getValueFrequency(final List<String> values) {
        Map<String, Integer> frequencyCounter = new HashMap<>();
        for (String value : values) {
            frequencyCounter.put(value, frequencyCounter.getOrDefault(value, 0) + 1);
        }
        return frequencyCounter;
    }

    private Map<Pair<String, String>, Integer> getTupleFrequency(final List<String> values1, final List<String> values2) {
        Map<Pair<String, String>, Integer> frequencyCounter = new HashMap<>();
        int total = values1.size();
        for (int i = 0; i < total; i++) {
            Pair<String, String> tuple = Pair.of(values1.get(i), values2.get(i));
            frequencyCounter.put(tuple, frequencyCounter.getOrDefault(tuple, 0) + 1);
        }
        return frequencyCounter;
    }

    private double getEntropy(final List<String> values) {
        int total = values.size();
        Map<String, Integer> frequencyCounter = getValueFrequency(values);

        double entropy = 0.0;
        for (Map.Entry<String, Integer> entry : frequencyCounter.entrySet()) {
            double probability = (double) entry.getValue() / total;
            entropy -= probability * Math.log(probability) / Math.log(2);
        }
        return entropy;
    }
}