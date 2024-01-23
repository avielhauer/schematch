package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderA;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
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
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
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
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix){
        // Create a DatabaseGraph
        DBGraph dbGraphSource = new SQL2Graph(matchTask.getScenario().getSourceDatabase());
        DBGraph dbGraphTarget = new SQL2Graph(matchTask.getScenario().getTargetDatabase());

        // Create SimilarityCalculator
        SimilarityCalculator levenshteinCalculator = new SimilarityCalculator(matchTask, simMatrix) {
            final SimilarityMeasure<String> stringMeasure = new Levenshtein();
            @Override
            public float calcStringSim(String stringA, String stringB){
                return stringMeasure.compare(stringA, stringB);
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = new InversedWaterWeightingGraph(dbGraphSource, dbGraphTarget, levenshteinCalculator);
        // Create Flooder
        Flooder flooder = new FlooderC(pGraph);

        float[][] boostedMatrix = flooder.flood(500, 0.0000001F);
       // this.exportToCsv(matchTask.getMatcher);
        return boostedMatrix;
    }

    private void printMatrix(float[][] sim){
        for(float[] row : sim){
            for(float a : row){
                System.out.print(a+" ");
            }
            System.out.print("\n");
        }
    }

    private void exportToCsv(String matcher, float[][] oldMatrix, float[][] newMatrix) {
        String fileName = matcher + "BoostingExport.csv";
        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {

            // Write oldMatrix to CSV
            for (float[] row : oldMatrix) {
                for (float number : row) {
                    csvPrinter.print(number);
                }
                csvPrinter.println();
            }

            // Add an empty line between the two matrices
            csvPrinter.println();

            // Write newMatrix to CSV
            for (float[] row1 : newMatrix) {
                for (float number1 : row1) {
                    csvPrinter.print(number1);
                }
                csvPrinter.println();
            }


            System.out.println("CSV Exported successfully to " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}