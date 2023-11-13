package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.DBGraph;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static de.uni_marburg.schematch.utils.AdditionalInformationReader.readNUMFile;
import static de.uni_marburg.schematch.utils.AdditionalInformationReader.readTYPEFile;

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
        sourceGraph.addDatatypes();
        DBGraph targetGraph = new DBGraph(targetTable);
        targetGraph.addDatatypes();

        PropagationGraph pGraph = sourceGraph.generatePropagationGraph(targetGraph);
        log.debug(pGraph);
        System.exit(2);


        // Dummy return
        return simMatrix;
    }
}