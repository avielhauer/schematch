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
    private List<List<Integer>> sourceTransformationLookup;
    private List<List<Integer>> targetTransformationLookup;
    private Pair<Integer, Integer>[][] groundTruthTransformationIndexLookup;

    public Evaluator(List<Metric> metrics, Scenario scenario, int[][] groundTruthMatrix) {
        this.metrics = metrics;
        this.scenario = scenario;
        if (scenario.getDataset().isDenormalized()) {
            groundTruthMatrix =  reconstructOriginalGroundTruth();
        }
        this.groundTruthMatrix = groundTruthMatrix;

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

    private Map<String, Map<String, List<Integer>>> importMapping(Path filePath, Database database) {
        Map<String, Map<String, List<Integer>>> mapping = new HashMap<>();
        try {
            for (String line : Files.readAllLines(filePath)) {
                String[] parts = line.split(" = ");
                String[] denormalizedParts = parts[0].split("\\.");
                String[] originalParts = parts[1].split("\\.");

                Table denormalizedTable = database.getTableByName(denormalizedParts[0]);
                Column denormalizedColumn = denormalizedTable.getColumnByName(denormalizedParts[1]);
                int gtOffset = denormalizedTable.getOffset() + denormalizedTable.getColumns().indexOf(denormalizedColumn);

                mapping.computeIfAbsent(originalParts[0], k -> new HashMap<>())
                        .computeIfAbsent(originalParts[1], k -> new ArrayList<>())
                        .add(gtOffset);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Could not parse " + filePath + " for mapping Import");
        }
        return mapping;
    }

    private int getNumberAttributes(Map<String, Map<String, List<Integer>>> map) {
        int totalCount = 0;
        for (Map<String, List<Integer>> innerMap : map.values()) {
            totalCount += innerMap.size();
        }
        return totalCount;
    }

    private Map<String, Integer> getNewPositionLookup(Map<String, Map<String, List<Integer>>> mapping) {
        Map<String, Integer> lookup = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, Map<String, List<Integer>>> outerEntry : mapping.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, List<Integer>> innerMap = outerEntry.getValue();

            for (Map.Entry<String, List<Integer>> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                lookup.put(outerKey + "." + innerKey, i);
                i++;
            }
        }
        return lookup;
    }

    private Collection<Pair<String, String>> importOriginalMappings(Path pathToOriginalGroundTruth) {
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

    private int[][] reconstructOriginalGroundTruth() {
        Configuration config = Configuration.getInstance();
        Map<String, Map<String, List<Integer>>> sourceMappings = importMapping(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "source_mapping.csv"), scenario.getSourceDatabase());
        Map<String, Map<String, List<Integer>>> targetMappings = importMapping(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "target_mapping.csv"), scenario.getTargetDatabase());
        int sourceN = getNumberAttributes(sourceMappings);
        int targetN = getNumberAttributes(targetMappings);
        Map<String, Integer> sourcePositionLookup = getNewPositionLookup(sourceMappings);
        Map<String, Integer> targetPositionLookup = getNewPositionLookup(targetMappings);
        Collection<Pair<String, String>> originalMatchings = importOriginalMappings(Paths.get(scenario.getPath(), config.getDefaultGroundTruthDir(), "actual_ground_truth.txt"));

        int[][] reconstructedGroundTruthMatrix = new int[sourceN][targetN];
        for (Pair<String, String> originalMatch : originalMatchings) {
            Integer sourceIndex = sourcePositionLookup.get(originalMatch.getLeft());
            Integer targetIndex = targetPositionLookup.get(originalMatch.getRight());
            if(sourceIndex == null){
                log.warn("Could not find " + originalMatch.getLeft() + " in  sourcePositionLookup.");
                continue;
            }
            if(targetIndex == null){
                log.warn("Could not find "+ originalMatch.getRight()+" in targetPositionLookup.");
                continue;
            }
            reconstructedGroundTruthMatrix[sourcePositionLookup.get(originalMatch.getLeft())][targetPositionLookup.get(originalMatch.getRight())] = 1;
        }


        generateTransformationIndexLookup(sourceMappings, sourcePositionLookup, sourceN, targetMappings, targetPositionLookup, targetN);
        return reconstructedGroundTruthMatrix;
    }

    private void generateTransformationIndexLookup(Map<String, Map<String, List<Integer>>> sourceMappings, Map<String, Integer> sourcePositionLookup, Integer sourceN, Map<String, Map<String, List<Integer>>> targetMappings, Map<String, Integer> targetPositionLookup, Integer targetN) {
        sourceTransformationLookup = buildLookup(sourceMappings, sourcePositionLookup, sourceN);
        assert(sourcePositionLookup.size() == groundTruthMatrix.length);
        targetTransformationLookup = buildLookup(targetMappings, targetPositionLookup, targetN);
        assert(targetTransformationLookup.size() == groundTruthMatrix[0].length);
    }


    private List<List<Integer>> buildLookup(Map<String, Map<String, List<Integer>>> mappings, Map<String, Integer> positionLookup, Integer N) {
        List<List<Integer>> lookup = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            lookup.add(new ArrayList<>());
        }

        for (Map.Entry<String, Integer> sourcePosition : positionLookup.entrySet()) {
            String columnID = sourcePosition.getKey();
            String table = columnID.split("\\.")[0];
            String column = columnID.split("\\.")[1];
            Integer posInTransformedMatrix = sourcePosition.getValue();
            lookup.set(posInTransformedMatrix, mappings.get(table).get(column));
        }

        return lookup;
    }

    private float[][] reconstructOriginalSimMatrix(float[][] simMatrix) {
        float[][] newSimMatrix = new float[groundTruthMatrix.length][groundTruthMatrix[0].length];
        for (int i = 0; i < sourceTransformationLookup.size(); i++) {
            for (int j = 0; j < targetTransformationLookup.size(); j++) {
                List<Integer> sourceLookups = sourceTransformationLookup.get(i);
                List<Integer> targetLookups = targetTransformationLookup.get(j);
                float max = 0.0f;
                for(Integer sourceLookup : sourceLookups){
                    for(Integer targetLookup: targetLookups){
                        max = Math.max(max, simMatrix[sourceLookup][targetLookup]);
                    }
                }
                newSimMatrix[i][j] = max;
            }
        }
        return newSimMatrix;
    }

    public Map<Metric, Performance> evaluate(float[][] simMatrix) {
        if (scenario.getDataset().isDenormalized()) {
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