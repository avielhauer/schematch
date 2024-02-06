package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.FD2Graph2;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.SQL2Graph;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderA;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderB;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Similarity Flooding Matrix Boosting
 */

public class Test3 implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(Test3.class);
    private final int max_iterations;
    private final boolean flooderC;
    private final boolean invWeighting;

    public Test3(int max_iterations, boolean flooderC, boolean invWeighting){
        this.max_iterations = max_iterations;
        this.flooderC = flooderC;
        this.invWeighting = invWeighting;
    }

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix){
        log.debug("Test3");
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

            @Override
            protected float calcOtherSim(Object objectA, Object objectB) {
                return 0;
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = this.invWeighting ? new WaterWeightingGraph(dbGraphSource, dbGraphTarget, levenshteinCalculator) :
        new InversedWaterWeightingGraph(dbGraphSource, dbGraphTarget, levenshteinCalculator);
        // Create Flooder
        Flooder flooder = this.flooderC ? new FlooderC(pGraph) : new FlooderA(pGraph);

        float[][] boostedMatrix = flooder.flood(max_iterations, 0.0000001F);
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
}