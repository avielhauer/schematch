package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.Metadata2Graph;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderA;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.WaterWeightingGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Similarity Flooding Matrix Boosting
 */
public class SimFloodingSimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(SimFloodingSimMatrixBoosting.class);

    @Override
    public float[][] run(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher) {

        // Create a DatabaseGraph
        DBGraph dbGraphSource = new Metadata2Graph(line, matchTask, tablePair, matcher, true);
        DBGraph dbGraphTarget = new Metadata2Graph(line, matchTask, tablePair, matcher, false);

        // Create Similaritycalculator
        SimilarityCalculator levenshteinCalculator = new SimilarityCalculator(line, matchTask, tablePair, matcher) {
            final SimilarityMeasure<String> stringMeasure = new Levenshtein();
            @Override
            public float calcStringSim(String stringA, String stringB){
                return stringMeasure.compare(stringA, stringB);
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = new WaterWeightingGraph(dbGraphSource, dbGraphTarget, levenshteinCalculator);

        // Create Flooder
        Flooder flooder = new FlooderA(pGraph);

        float[][] boostedMatrix = flooder.flood(100, 0.01F);

        float[][] simMatrix = switch (line) {
            case 1 -> tablePair.getResultsForFirstLineMatcher(matcher);
            case 2 -> tablePair.getResultsForSecondLineMatcher(matcher);
            default -> throw new RuntimeException("Illegal matcher line set for similarity matrix boosting");
        };
        printMatrix(simMatrix);
        System.out.println("---");
        printMatrix(boostedMatrix);
        System.out.println("__________");
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
}