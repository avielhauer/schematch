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

        // Perform hill climb approach to match two graphs
        for (int i = 0; i < simMatrix.length; i++) {
            for (int j = 0; j < simMatrix[i].length; j++) {
                Map<Integer, Integer> mapping = matchGraphs(dependencyMatrixSource, dependencyMatrixTarget);
                // Calculate similarity score based on the mapping
                // For simplicity, let's assume similarity score is the negative of Euclidean distance
                double similarityScore = -calculateEuclideanDistance(dependencyMatrixSource, dependencyMatrixTarget, mapping);
                // Normalize similarity score to range [0, 1]
                similarityScore = normalizeSimilarity(similarityScore);
                simMatrix[i][j] = (float) similarityScore;
            }
        }

        System.out.println("SIMILARITY MATRIX:");
        for (int i = 0; i < simMatrix.length; i++) {
            for (int j = 0; j < simMatrix[i].length; j++) {
                System.out.print(simMatrix[i][j] + " ");
            }
            System.out.println();
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
        double bestDistance = Double.MAX_VALUE;
        Map<Integer, Integer> bestMapping = null;

        long[] seeds = {12345L, 67890L, 13579L, 24680L, 98765L};

        for (long seed : seeds) {
            Random random = new Random(seed);

            Map<Integer, Integer> currentMapping = getRandomMapping(dependencyGraphA.length, dependencyGraphB.length, random);
            double currentDistance = calculateEuclideanDistance(dependencyGraphA, dependencyGraphB, currentMapping);

            // Hill climb iterations
            int maxIterations = 1000;
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                // Generate a random neighbor mapping
                Map<Integer, Integer> neighborMapping = getRandomNeighborMapping(currentMapping, dependencyGraphB.length, random);
                double neighborDistance = calculateEuclideanDistance(dependencyGraphA, dependencyGraphB, neighborMapping);

                // Accept the neighbor if it improves the distance
                if (neighborDistance < currentDistance) {
                    currentMapping = neighborMapping;
                    currentDistance = neighborDistance;
                }
            }

            // Update best mapping if the current one is better
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                bestMapping = currentMapping;
            }
        }

        return bestMapping;
    }


    // Generate a random initial mapping
    private Map<Integer, Integer> getRandomMapping(int sizeA, int sizeB, Random random) {
        Map<Integer, Integer> mapping = new HashMap<>();
        for (int i = 0; i < sizeA; i++) {
            int randomIndexB = random.nextInt(sizeB);
            //System.out.println(randomIndexB);
            mapping.put(i, randomIndexB);
        }
        return mapping;
    }

    // Generate a random neighbor mapping by perturbing the current mapping
    private Map<Integer, Integer> getRandomNeighborMapping(Map<Integer, Integer> currentMapping, int sizeB, Random random) {
        Map<Integer, Integer> neighborMapping = new HashMap<>(currentMapping);
        int randomIndexA = random.nextInt(currentMapping.size());
        int randomIndexB = random.nextInt(sizeB);
        //System.out.println(randomIndexA);
        //System.out.println(randomIndexB);
        neighborMapping.put(randomIndexA, randomIndexB);
        return neighborMapping;
    }

    public float[][] hillClimbApproach(double[][]dependencyMatrix1,double[][]dependencyMatrix2){
        int[] seeds = {1234, 5678, 9012, 3456, 7890};

        for (int seed : seeds) {
            Random random = new Random(seed);

            dependencyMatrix1=shuffleDependencyMatrix(dependencyMatrix1,random,seed);
            dependencyMatrix2=shuffleDependencyMatrix(dependencyMatrix2,random,seed);


        }

        return null;
    }

    public double[][] shuffleDependencyMatrix(double[][]dependencyMatrix, Random random, int seed){
        for (int j = dependencyMatrix.length - 1; j > 0; j--) {
            int index = random.nextInt(j + 1);
            double[] temp = dependencyMatrix[index];
            dependencyMatrix[index] = dependencyMatrix[j];
            dependencyMatrix[j] = temp;
        }

        return dependencyMatrix;
    }

    public static double sumOfColumn(double[][] matrix, int columnIndex) {
        double sum = 0.0;
        for (double[] row : matrix) {
            sum += row[columnIndex];
        }
        return sum;
    }

    public double calculateFitness(double[][]dependencyMatrix1,double[][]dependencyMatrix2){
        return 0;
    }

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