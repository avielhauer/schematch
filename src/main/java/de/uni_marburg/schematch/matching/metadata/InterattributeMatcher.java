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

        //System.out.println("GUT");

        double[][]dependencyMatrixSource=buildDependencyMatrix(sourceTable);
        double[][]dependencyMatrixTarget=buildDependencyMatrix(targetTable);

        //System.out.println("GUT");


//        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
//            Datatype sourceType = sourceTable.getColumn(i).getDatatype();
//            for (int j = 0; j < targetTable.getNumColumns(); j++) {
//                Datatype targetType = targetTable.getColumn(j).getDatatype();
//                if (sourceType != Datatype.INTEGER && sourceType != Datatype.FLOAT) {
//                    simMatrix[i][j] = 0.0f;
//                    continue;
//                }
//                if (targetType != Datatype.INTEGER && targetType != Datatype.FLOAT) {
//                    simMatrix[i][j] = 0.0f;
//                    continue;
//                }
//                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
//            }
//        }
//        return simMatrix;

//        Table sourceTable = tablePair.getSourceTable();
//        Table targetTable = tablePair.getTargetTable();

//        float[][] simMatrix = tablePair.getEmptySimMatrix();

//        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
//            for (int j = 0; j < targetTable.getNumColumns(); j++) {
//
//            }
//        }


        return simMatrix;
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
        for (int i = 0; i < dependencyMatrix.length; i++) {
            for (int j = 0; j < dependencyMatrix[i].length; j++) {
                System.out.print(dependencyMatrix[i][j] + " ");
            }
            System.out.println();
        }
        return dependencyMatrix;
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