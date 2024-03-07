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
    private double[][] buildDependencyMatrix(Table table) {
        int numColumns = table.getNumColumns();
        double[][] dependencyMatrix = new double[numColumns][numColumns];

//        for (int i = 0; i <= numColumns; i++) {
//
//            int currentColumn = 0;
//
//            while (currentColumn <= numColumns) {
//                List<String> columnValues = table.getColumn(currentColumn).getValues();
//                if (i == currentColumn) {
//                    dependencyMatrix[i][currentColumn] = getEntropy(columnValues);
//                } else {
//                    dependencyMatrix[i][currentColumn] = calculateMutualInformation();
//                }
//                currentColumn++;
//            }
//            currentColumn = 0;
//        }

        fillDependencyMatrix(dependencyMatrix,table);

        for (int i = 0; i < dependencyMatrix.length; i++) {
            for (int j = 0; j < dependencyMatrix[i].length; j++) {
                System.out.print(dependencyMatrix[i][j] + " ");
            }
            System.out.println();
        }

        return dependencyMatrix;
    }

//    private void fillDependencyMatrix(double[][] dependencyMatrix, Table table) {
//        Set<List<Integer>> uniqueTuples = getColumnIndexCombinations(table);
//
//        System.out.println("uniqueTuples");
//        System.out.println(uniqueTuples);
//        System.out.println("uniqueTuples size");
//        System.out.println(uniqueTuples.size());
//
//        for (List<Integer> tuple : uniqueTuples) {
//            int index1 = tuple.get(0);
//            int index2 = tuple.get(1);
//
//            List<String> column1Values = table.getColumn(index1).getValues();
//            List<String> column2Values = table.getColumn(index2).getValues();
//
//            System.out.println("column1Values");
//            System.out.println(column1Values);
//
//            Map<String, Integer> column1ValueFrequencies = getValueFrequency(column1Values);
//            Map<String, Integer> column2ValueFrequencies = getValueFrequency(column2Values);
//
//            Map<Pair<String, String>, Integer> columnsValueTupleFrequencies = getTupleFrequency(column1Values, column2Values);
//
//            System.out.println("columnsValueTupleFrequencies");
//            System.out.println(columnsValueTupleFrequencies);
//            System.out.println("columnsValueTupleFrequencies size");
//            System.out.println(columnsValueTupleFrequencies.size());
//
//            int numColumns = table.getNumColumns();
//
//            for (int i = 0; i <= numColumns; i++) {
//
//                int currentColumn = 0;
//
//                while (currentColumn <= numColumns) {
//                    List<String> columnValues = table.getColumn(currentColumn).getValues();
//                    if (i != currentColumn) {
//                        int total = column1Values.size();
//                        double mutualInformation = 0.0;
//
//                        //dependencyMatrix[i][currentColumn] = calculateMutualInformation(column1Values, column2Values, column1ValueFrequencies, column2ValueFrequencies, columnsValueTupleFrequencies);
//                        for (Map.Entry<String, Integer> entry1 : column1ValueFrequencies.entrySet()) {
//                            for(Map.Entry<String, Integer> entry2 : column2ValueFrequencies.entrySet()) {
//
//                                Pair<Integer, Integer> valuePair = Pair.of(entry1.getValue(), entry2.getValue());
//
//
//                                //columnsValueTupleFrequencies.get(valuePair);
//
//                                double probabilityEntry1 = (double) entry1.getValue() / total;
//                                double probabilityEntry2 = (double) entry2.getValue() / total;
//                                double probabilityEntry1and2 = (double) columnsValueTupleFrequencies.get(valuePair) / total;
//                                mutualInformation += probabilityEntry1and2 * Math.log(probabilityEntry1and2/(probabilityEntry1*probabilityEntry2)) / Math.log(2);
//
//                                dependencyMatrix[i][currentColumn]=mutualInformation;
//                            }
//                        }
//                    }
//                    else {
//                        dependencyMatrix[i][currentColumn] = getEntropy(columnValues);
//                    }
//                    currentColumn++;
//                }
//                currentColumn = 0;
//            }
//
//        }
//    }

//    private void fillDependencyMatrix(double[][] dependencyMatrix, Table table) {
////        Set<List<Integer>> uniqueTuples = getColumnIndexCombinations(table);
////
////        System.out.println("uniqueTuples");
////        System.out.println(uniqueTuples);
////        System.out.println("uniqueTuples size");
////        System.out.println(uniqueTuples.size());
//
//        for (int k=0; k<table.getNumColumns();k++) {
//            for(int l=0; l<table.getNumColumns();l++) {
//
//                //int index1 = tuple.get(0);
//                //int index2 = tuple.get(1);
//
//                List<String> column1Values = table.getColumn(k).getValues();
//                List<String> column2Values = table.getColumn(l).getValues();
//
//                //System.out.println("column1Values");
//                //System.out.println(column1Values);
//
//                Map<String, Integer> column1ValueFrequencies = getValueFrequency(column1Values);
//                Map<String, Integer> column2ValueFrequencies = getValueFrequency(column2Values);
//
//                Map<Pair<String, String>, Integer> columnsValueTupleFrequencies = getTupleFrequency(column1Values, column2Values);
//
//                //System.out.println("columnsValueTupleFrequencies");
//                //System.out.println(columnsValueTupleFrequencies);
//                //System.out.println("columnsValueTupleFrequencies size");
//                //System.out.println(columnsValueTupleFrequencies.size());
//
//                int numColumns = table.getNumColumns();
//
//                for (int i = 0; i < numColumns; i++) {
//
//                    int currentColumn = 0;
//
//                    while (currentColumn < numColumns) {
//                        try {
//                            List<String> columnValues = table.getColumn(currentColumn).getValues();
//                            if (i != currentColumn) {
//                                int total = column1Values.size();
//                                double mutualInformation = 0.0;
//
//                                //dependencyMatrix[i][currentColumn] = calculateMutualInformation(column1Values, column2Values, column1ValueFrequencies, column2ValueFrequencies, columnsValueTupleFrequencies);
//                                for (Map.Entry<String, Integer> entry1 : column1ValueFrequencies.entrySet()) {
//                                    for (Map.Entry<String, Integer> entry2 : column2ValueFrequencies.entrySet()) {
//
//                                        Pair<Integer, Integer> valuePair = Pair.of(entry1.getValue(), entry2.getValue());
//
//
//                                        //columnsValueTupleFrequencies.get(valuePair);
//
//                                        //System.out.println(valuePair.getValue());
//
//                                        double probabilityEntry1 = (double) entry1.getValue() / total;
//                                        double probabilityEntry2 = (double) entry2.getValue() / total;
//                                        double probabilityEntry1and2 = (double) columnsValueTupleFrequencies.get(valuePair) / total;
//
//                                        System.out.println("yee");
//
//                                        mutualInformation += probabilityEntry1and2 * Math.log(probabilityEntry1and2 / (probabilityEntry1 * probabilityEntry2)) / Math.log(2);
//
//                                        dependencyMatrix[i][currentColumn] = mutualInformation;
//                                    }
//                                }
//                            } else {
//                                dependencyMatrix[i][currentColumn] = getEntropy(columnValues);
//                            }
//                            //currentColumn++;
//                        }catch (NullPointerException e){
//                            dependencyMatrix[i][currentColumn]=444;
//                            //e.printStackTrace();
//                        }
//                        currentColumn++;
//                    }
//                    currentColumn = 0;
//                }
//            }
//        }
//    }

//    private void fillDependencyMatrix(double[][] dependencyMatrix, Table table) {
////        Set<List<Integer>> uniqueTuples = getColumnIndexCombinations(table);
////
////        System.out.println("uniqueTuples");
////        System.out.println(uniqueTuples);
////        System.out.println("uniqueTuples size");
////        System.out.println(uniqueTuples.size());
//
//        for (int k=0; k<table.getNumColumns();k++) {
//            for(int l=0; l<table.getNumColumns();l++) {
//
//                //int index1 = tuple.get(0);
//                //int index2 = tuple.get(1);
//
//                List<String> column1Values = table.getColumn(k).getValues();
//                List<String> column2Values = table.getColumn(l).getValues();
//
//                //System.out.println("column1Values");
//                //System.out.println(column1Values);
//
//                Map<String, Integer> column1ValueFrequencies = getValueFrequency(column1Values);
//                Map<String, Integer> column2ValueFrequencies = getValueFrequency(column2Values);
//
//                Map<Pair<String, String>, Integer> columnsValueTupleFrequencies = getTupleFrequency(column1Values, column2Values);
//
//                //System.out.println("columnsValueTupleFrequencies");
//                //System.out.println(columnsValueTupleFrequencies);
//                //System.out.println("columnsValueTupleFrequencies size");
//                //System.out.println(columnsValueTupleFrequencies.size());
//
//                int numColumns = table.getNumColumns();
//
//                for (int i = 0; i < numColumns; i++) {
//
//                    int currentColumn = 0;
//
//                    while (currentColumn < numColumns) {
//                        try {
//                            List<String> columnValues = table.getColumn(currentColumn).getValues();
//                            if (i != currentColumn) {
//                                int total = column1Values.size();
//                                double mutualInformation = 0.0;
//
//                                //dependencyMatrix[i][currentColumn] = calculateMutualInformation(column1Values, column2Values, column1ValueFrequencies, column2ValueFrequencies, columnsValueTupleFrequencies);
//                                for (Map.Entry<String, Integer> entry1 : column1ValueFrequencies.entrySet()) {
//                                    for (Map.Entry<String, Integer> entry2 : column2ValueFrequencies.entrySet()) {
//
//                                        Pair<Integer, Integer> valuePair = Pair.of(entry1.getValue(), entry2.getValue());
//
//                                        //System.out.println(valuePair);
//
//
//                                        //columnsValueTupleFrequencies.get(valuePair);
//
//                                        //System.out.println(valuePair.getValue());
//
//                                        double probabilityEntry1 = (double) entry1.getValue() / total;
//                                        double probabilityEntry2 = (double) entry2.getValue() / total;
//                                        double probabilityEntry1and2 = (double) columnsValueTupleFrequencies.get(valuePair) / total;
//
//                                        System.out.println("yee");
//
//                                        mutualInformation += probabilityEntry1and2 * Math.log(probabilityEntry1and2 / (probabilityEntry1 * probabilityEntry2)) / Math.log(2);
//
//                                        dependencyMatrix[i][currentColumn] = mutualInformation;
//                                    }
//                                }
//                            } else {
//                                dependencyMatrix[i][currentColumn] = getEntropy(columnValues);
//                            }
//                            //currentColumn++;
//                        }catch (NullPointerException e){
//                            dependencyMatrix[i][currentColumn]=444;
//                            //e.printStackTrace();
//                        }
//                        currentColumn++;
//                    }
//                    currentColumn = 0;
//                }
//            }
//        }
//    }

    private void fillDependencyMatrix(double[][] dependencyMatrix, Table table) {
//        Set<List<Integer>> uniqueTuples = getColumnIndexCombinations(table);
//
//        System.out.println("uniqueTuples");
//        System.out.println(uniqueTuples);
//        System.out.println("uniqueTuples size");
//        System.out.println(uniqueTuples.size());

        for (int k=0; k<table.getNumColumns();k++) {
            for(int l=0; l<table.getNumColumns();l++) {

                //int index1 = tuple.get(0);
                //int index2 = tuple.get(1);

                List<String> column1Values = table.getColumn(k).getValues();
                List<String> column2Values = table.getColumn(l).getValues();

                //System.out.println("column1Values");
                //System.out.println(column1Values);

                Map<String, Integer> column1ValueFrequencies = getValueFrequency(column1Values);
                Map<String, Integer> column2ValueFrequencies = getValueFrequency(column2Values);

                Map<Pair<String, String>, Integer> columnsValueTupleFrequencies = getTupleFrequency(column1Values, column2Values);

                //System.out.println("columnsValueTupleFrequencies");
                //System.out.println(columnsValueTupleFrequencies);
                //System.out.println("columnsValueTupleFrequencies size");
                //System.out.println(columnsValueTupleFrequencies.size());

                int numColumns = table.getNumColumns();

                for (int i = 0; i < numColumns; i++) {

                    int currentColumn = 0;

                    while (currentColumn < numColumns) {
                        try {
                            List<String> columnValues = table.getColumn(currentColumn).getValues();
                            if (i != currentColumn) {
                                int total = column1Values.size();
                                double mutualInformation = 0.0;

                                getMutualInformation(column1ValueFrequencies,column2ValueFrequencies,total,columnsValueTupleFrequencies,dependencyMatrix,i,currentColumn);
                            } else {
                                dependencyMatrix[i][currentColumn] = getEntropy(columnValues);
                            }
                            //currentColumn++;
                        }catch (NullPointerException e){
                            dependencyMatrix[i][currentColumn]=0.0444;
                            //e.printStackTrace();
                        }
                        currentColumn++;
                    }
                    currentColumn = 0;
                }
            }
        }
    }

    public double getMutualInformation(Map<String, Integer> column1ValueFrequencies, Map<String, Integer> column2ValueFrequencies, int total, Map<Pair<String, String>, Integer> columnsValueTupleFrequencies, double[][] dependencyMatrix, int currentRow, int currentColumn){
        double mutualInformation = 0.0;

        //dependencyMatrix[i][currentColumn] = calculateMutualInformation(column1Values, column2Values, column1ValueFrequencies, column2ValueFrequencies, columnsValueTupleFrequencies);
        for (Map.Entry<String, Integer> entry1 : column1ValueFrequencies.entrySet()) {
            for (Map.Entry<String, Integer> entry2 : column2ValueFrequencies.entrySet()) {

                Pair<Integer, Integer> valuePair = Pair.of(entry1.getValue(), entry2.getValue());

                //System.out.println(valuePair);


                //columnsValueTupleFrequencies.get(valuePair);

                //System.out.println(valuePair.getValue());

                double probabilityEntry1 = (double) entry1.getValue() / total;
                double probabilityEntry2 = (double) entry2.getValue() / total;
                double probabilityEntry1and2 = (double) columnsValueTupleFrequencies.get(valuePair) / total;

                System.out.println("yee");

                mutualInformation += probabilityEntry1and2 * Math.log(probabilityEntry1and2 / (probabilityEntry1 * probabilityEntry2)) / Math.log(2);

                dependencyMatrix[currentRow][currentColumn] = mutualInformation;
            }
        }
        return mutualInformation;
    }


//    private double calculateMutualInformation(double[][] dependencyMatrix, Table table) {
//        return 0;
//    }


    public Map<String, Integer> getValueFrequency(final List<String> values) {
        int total = values.size();

        Map<String, Integer> frequencyCounter = new HashMap<>();
        for (String value : values) {
            frequencyCounter.put(value, frequencyCounter.getOrDefault(value, 0) + 1);
        }

        return frequencyCounter;
    }

    public Map<Pair<String, String>, Integer> getTupleFrequency(final List<String> values1,
                                                                final List<String> values2) {
        int total = values1.size();

        Map<Pair<String, String>, Integer> frequencyCounter = new HashMap<>();
        for (int i = 0; i < total; i++) {
                Pair<String, String> tuple = Pair.of(values1.get(i), values2.get(i));
                frequencyCounter.put(tuple, frequencyCounter.getOrDefault(tuple, 0) + 1);
        }

        return frequencyCounter;
    }

//    public Map<Pair<String, String>, Integer> getTupleFrequency(final List<String> values1,
//                                                                final List<String> values2) {
//        int total = values1.size();
//
//        Map<String, Map<String, Integer>> frequencyCounter = new HashMap<>();
//        for (String value1 : values1) {
//            for(String value2 : values2) {
//                //frequencyCounter.put(value1, frequencyCounter.getOrDefault(value, 0) + 1);
//                frequencyCounter.put(value1, frequencyCounter.put(value2, frequencyCounter.getOrDefault(value2, 0)+1);
//            }
//        }
//
//
//        //Map<Pair<String, String>, Integer> frequencyCounter = new HashMap<>();
//        for (int i = 0; i < total; i++) {
//            for(int j=0; j<total; j++){
//                Pair<String, String> tuple = Pair.of(values1.get(i), values2.get(j));
//                frequencyCounter.put(tuple, frequencyCounter.getOrDefault(tuple, 0) + 1);
//            }
//        }
//
//        return frequencyCounter;
//    }

//    public Map<String, Integer> getCombinedValueFrequency(final List<String> values1, final List<String> values2) {
//        int total = values1.size();
//
//
//        Map<String, Integer> frequencyCounter = new HashMap<>();
//        for (String value : values) {
//            frequencyCounter.put(value, frequencyCounter.getOrDefault(value, 0) + 1);
//        }
//
//        return frequencyCounter;
//    }

    private Set<List<Integer>> getColumnIndexCombinations(Table table) {
        int amountColumns = table.getColumns().size();
        Set<List<Integer>> uniqueTuples = new HashSet<>();

        List<Integer> columnIndexes = new ArrayList<>();
        for (int i = 0; i < amountColumns - 1; i++) {
            columnIndexes.add(i);
        }

        for (int i = 0; i < columnIndexes.size(); i++) {
            for (int j = i + 1; j < columnIndexes.size(); j++) {
                List<Integer> tuple = new ArrayList<>();
                tuple.add(columnIndexes.get(i));
                tuple.add(columnIndexes.get(j));
                uniqueTuples.add(tuple);
            }
        }

        return uniqueTuples;
    }


    private double getEntropy(final List<String> values) {
        int total = values.size();

        Map<String, Integer> frequencyCounter = new HashMap<>();
        for (String value : values) {
            frequencyCounter.put(value, frequencyCounter.getOrDefault(value, 0) + 1);
        }

        // Calculate entropy
        double entropy = 0.0;
        for (Map.Entry<String, Integer> entry : frequencyCounter.entrySet()) {
            double probability = (double) entry.getValue() / total;
            entropy -= probability * Math.log(probability) / Math.log(2);
        }

        return entropy;
    }

}
