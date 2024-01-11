package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderA;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Similarity Flooding Matrix Boosting
 */
public class SimFloodingSimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(SimFloodingSimMatrixBoosting.class);

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix){
        // Create a DatabaseGraph
        DBGraph dbGraphSource = new FD2Graph(matchTask.getScenario().getSourceDatabase());
        DBGraph dbGraphTarget = new FD2Graph(matchTask.getScenario().getTargetDatabase());

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

        float[][] boostedMatrix = flooder.flood(1000, 0.0000001F);

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