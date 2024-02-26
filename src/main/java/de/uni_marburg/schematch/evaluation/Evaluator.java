package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Data
public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    private final List<Metric> metrics;
    private final Scenario scenario;
    private int[][] groundTruthMatrix;
    private final int[] groundTruthVector;
    private final int numGroundTruth;
    private List<Integer> sourceGroundTruthIndices;
    private List<Integer> targetGroundTruthIndices;
    private int[][] transposedGroundTruthMatrix;
    private Pair<Integer, Integer>[][] groundTruthTransformationIndexLookup;

    public Evaluator(List<Metric> metrics, Scenario scenario, int[][] groundTruthMatrix) {
        this.metrics = metrics;
        this.scenario = scenario;
        this.groundTruthMatrix = groundTruthMatrix;
        if(scenario.getDataset().isDenormalized()){
            reconstructOriginalGroundTruth();
        }
        this.groundTruthVector = ArrayUtils.flattenMatrix(groundTruthMatrix);
        this.numGroundTruth = ArrayUtils.sumOfMatrix(groundTruthMatrix);

        if (Configuration.getInstance().isEvaluateAttributes()) {
            this.sourceGroundTruthIndices = new ArrayList<>();
            this.targetGroundTruthIndices = new ArrayList<>();
            this.transposedGroundTruthMatrix = ArrayUtils.transposeMatrix(groundTruthMatrix);
            for (int i = 0; i < groundTruthMatrix.length; i++) {
                for (int j = 0; j < groundTruthMatrix[0].length; j++) {
                    if (groundTruthMatrix[i][j] == 1) {
                        sourceGroundTruthIndices.add(i);
                        targetGroundTruthIndices.add(j);
                    }
                }
            }
        }
    }

    private Map<String, Map<String, Integer>> importMapping(Path filePath, Database database){
        Map<String, Map<String, Integer>> mapping = new HashMap<>();
        try {
            for(String line : Files.readAllLines(filePath)){
                String[] parts = line.split(" = ");
                String[] denormalizedParts = parts[0].split("\\.");
                String[] originalParts= parts[1].split("\\.");

                Table denormalizedTable = database.getTableByName(denormalizedParts[0]);
                Column denormalizedColumn = denormalizedTable.getColumnByName(denormalizedParts[1]);
                int gtOffset = denormalizedTable.getOffset() + denormalizedTable.getColumns().indexOf(denormalizedColumn);

                Map<String, Integer> innerMap = mapping.computeIfAbsent(originalParts[0], k -> new HashMap<>());

                innerMap.put(originalParts[1], gtOffset);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Could not parse " + filePath + " for mapping Import");
        }
        return mapping;
    }

    private int getNumberAttributes(Map<String, Map<String, Integer>> map) {
        int totalCount = 0;
        for (Map<String, Integer> innerMap : map.values()) {
            totalCount += innerMap.size();
        }
        return totalCount;
    }

    private Map<String, Integer> getNewPositionLookup(Map<String, Map<String, Integer>> mapping){
        Map<String, Integer> lookup = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, Map<String, Integer>> outerEntry : mapping.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, Integer> innerMap = outerEntry.getValue();

            for (Map.Entry<String, Integer> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                Integer value = innerEntry.getValue();
                lookup.put(outerKey + "." +  innerKey, i);
                i++;
            }
        }
        return lookup;
    }

    private Collection<Pair<String, String>> importOriginalMappings(Path pathToOriginalGroundTruth){
        List<Pair<String, String>> mappings = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(pathToOriginalGroundTruth)) {
                String[] parts = line.split(" = ");
                String key = parts[0];
                String value = parts[1];
                mappings.add(new ImmutablePair<>(key, value));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mappings;
    }

    private void reconstructOriginalGroundTruth(){
        Configuration config = Configuration.getInstance();
        Map<String, Map<String, Integer>> sourceMappings = importMapping(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "source_mapping.csv"), scenario.getSourceDatabase());
        Map<String, Map<String, Integer>> targetMappings = importMapping(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "target_mapping.csv"), scenario.getTargetDatabase());
        int sourceN = getNumberAttributes(sourceMappings);
        int targetN = getNumberAttributes(targetMappings);
        Map<String, Integer> sourcePositionLookup = getNewPositionLookup(sourceMappings);
        Map<String, Integer> targetPositionLookup = getNewPositionLookup(targetMappings);
        Collection<Pair<String, String>> originalMatchings = importOriginalMappings(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "actual_ground_truth.txt"));

        groundTruthMatrix = new int[sourceN][targetN];
        for(Pair<String, String> originalMatch : originalMatchings) {
            groundTruthMatrix[sourcePositionLookup.get(originalMatch.getLeft())][targetPositionLookup.get(originalMatch.getRight())] = 1;
        }

        groundTruthTransformationIndexLookup = new Pair[sourceN][targetN];

        generateTransformationIndexLookup(sourceMappings, targetMappings);

    }

    private void generateTransformationIndexLookup(Map<String, Map<String, Integer>> sourceMappings, Map<String, Map<String, Integer>> targetMappings) {
        int i = 0;
        for (Map.Entry<String, Map<String, Integer>> sourceTableEntry : sourceMappings.entrySet()) {
            Map<String, Integer> sourceColumnsEntry = sourceTableEntry.getValue();

            for (Map.Entry<String, Integer> sourceColumn : sourceColumnsEntry.entrySet()) {
                Integer sourceOriginalIndex = sourceColumn.getValue();
                int j = 0;
                for (Map.Entry<String, Map<String, Integer>> targetTableEntry : targetMappings.entrySet()) {
                    Map<String, Integer> targetColumnsEntry = targetTableEntry.getValue();

                    for (Map.Entry<String, Integer> targetColumn : targetColumnsEntry.entrySet()) {
                        Integer targetOriginalIndex = targetColumn.getValue();
                        groundTruthTransformationIndexLookup[i][j] = new ImmutablePair<>(sourceOriginalIndex, targetOriginalIndex);
                        j++;
                    }
                }
                i++;

            }
        }
    }

    private float[][] reconstructOriginalSimMatrix(float[][] simMatrix) {
        float[][] newSimMatrix = new float[groundTruthMatrix.length][groundTruthMatrix[0].length];
        for(int i = 0 ; i < newSimMatrix.length; i++){
            for(int j = 0; j < newSimMatrix[0].length; j++){
                Pair<Integer, Integer> pos = groundTruthTransformationIndexLookup[i][j];
                newSimMatrix[i][j] = simMatrix[pos.getLeft()][pos.getRight()];
            }
        }
        return newSimMatrix;
    }

    public Map<Metric, Performance> evaluate(float[][] simMatrix) {
        if(scenario.getDataset().isDenormalized()){
            simMatrix = reconstructOriginalSimMatrix(simMatrix);
        }
        float[] simVector = ArrayUtils.flattenMatrix(simMatrix);
        Map<Metric, Performance> performances = new HashMap<>();

        for (Metric metric : this.metrics) {
            Performance performance = new Performance(metric.run(this.groundTruthVector, simVector));
            if (Configuration.getInstance().isEvaluateAttributes()) {
                assert this.sourceGroundTruthIndices != null;
                assert this.targetGroundTruthIndices != null;
                assert this.transposedGroundTruthMatrix != null;
                for (Integer i : this.sourceGroundTruthIndices) {
                    performance.addSourceAttributeScore(i, metric.run(this.groundTruthMatrix[i], simMatrix[i]));
                }
                float[][] transposedSimMatrix = ArrayUtils.transposeMatrix(simMatrix);
                for (Integer j : this.targetGroundTruthIndices) {
                    performance.addTargetAttributeScore(j, metric.run(this.transposedGroundTruthMatrix[j], transposedSimMatrix[j]));
                }
            }
            performances.put(metric, performance);
        }

        return performances;
    }
}