package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.FloodingFunctions;
import de.uni_marburg.schematch.boosting.sf_algorithm.ObjectPair;
import de.uni_marburg.schematch.boosting.sf_algorithm.PropagationGraph;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.data.metadata.ScenarioMetadata;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Similarity Flooding Matrix Boosting
 */
public class SimFloodingSimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(SimFloodingSimMatrixBoosting.class);

    @Override
    public float[][] run(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher) {
        // Extract similarity matrix
        float[][] simMatrix = switch (line) {
            case 1 -> tablePair.getResultsForFirstLineMatcher(matcher);
            case 2 -> tablePair.getResultsForSecondLineMatcher(matcher);
            default -> throw new RuntimeException("Illegal matcher line set for similarity matrix boosting");
        };

        // Extract Tables
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        //Extract Columns
        List<Column> sourceColumns = sourceTable.getColumns();
        List<Column> targetColumns = targetTable.getColumns();
        // Extract and load scenario meta data
        Scenario scenario = matchTask.getScenario();
        ScenarioMetadata scenarioMetadata = scenario.getMetadata();
        // Extract and load database meta data
        Database source = matchTask.getScenario().getSourceDatabase();
        Database target = matchTask.getScenario().getTargetDatabase();
        DatabaseMetadata sourceMetadata = source.getMetadata();
        DatabaseMetadata targetMetadata = target.getMetadata();
        // Extract UCCs
        Map<Column, Collection<UniqueColumnCombination>> sourceUccs = sourceMetadata.getUccMap();
        Map<Column, Collection<UniqueColumnCombination>> targetUccs = targetMetadata.getUccMap();
        // Extract FDs
        Map<Column, Collection<FunctionalDependency>> sourceFds = sourceMetadata.getFdMap();
        Map<Column, Collection<FunctionalDependency>> targetFds = targetMetadata.getFdMap();
        // Extract INDs
        Collection<InclusionDependency> sourceToTargetInds = scenarioMetadata.getSourceToTargetMetadata();
        Collection<InclusionDependency> TargetToSourceInds = scenarioMetadata.getTargetToSourceMetadata();

        DBGraph sourceGraph = new DBGraph(sourceTable);
        sourceGraph.addNumericMetadata();
        sourceGraph.addDatatypes();
        DBGraph targetGraph = new DBGraph(targetTable);
        targetGraph.addNumericMetadata();
        targetGraph.addDatatypes();

        PropagationGraph pGraph = sourceGraph.generatePropagationGraph(targetGraph);

        Map<ObjectPair<Column, Column>, Float> columnDefaultSimilarity = new HashMap<>();
        for(int i = 0; i < simMatrix.length; i++){
            for(int j = 0; j < simMatrix[0].length; j++){
                ObjectPair<Column, Column> pair = new ObjectPair<>(sourceColumns.get(i), targetColumns.get(j));
                columnDefaultSimilarity.put(pair, simMatrix[i][j]);
            }
        }

        SimilarityMeasure<String> stringSimMeasure = new Levenshtein();

        pGraph.setFloodingFunction(FloodingFunctions::floodingFunctionA);
        pGraph.setDefaultSimilarityMap(stringSimMeasure, columnDefaultSimilarity);

        pGraph.flood(100, (float) 0.01); //TODO: configure


        float[][] newSimMatrix = new float[simMatrix.length][];

        for (int i = 0; i < simMatrix.length; i++) {
            newSimMatrix[i] = simMatrix[i].clone(); // Cloning each subarray
            for (int j = 0; j < simMatrix[0].length; j++) {
                ObjectPair pair = new ObjectPair<Column, Column>(sourceColumns.get(i), targetColumns.get(j));
                if(pGraph.getColumnSimilarity().containsKey(pair)){
                    newSimMatrix[i][j] = pGraph.getColumnSimilarity().get(pair);
                }
            }
        }
        return newSimMatrix;
    }

    private void printMatrix(float[][] sim){
        for(float[] row : sim){
            for(float a : row){
                System.out.print(a+" ");
            }
            System.out.print("\n");
        }
    }
}