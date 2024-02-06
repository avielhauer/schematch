package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.FD2Graph1;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.FD2Graph2;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.ConstantWeightingGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Similarity Flooding Matrix Boosting
 */

public class Test2 implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(Test2.class);
    private final int max_iterations;
    private final int numberOfFDs;

    public Test2(int max_iterations, int numberOfFDs){
        this.numberOfFDs = numberOfFDs;
        this.max_iterations = max_iterations;
    }

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix){
        log.debug("Test2");
        // Create a DatabaseGraph
        DBGraph dbGraphSource = new FD2Graph2(matchTask.getScenario().getSourceDatabase(), this.numberOfFDs);
        DBGraph dbGraphTarget = new FD2Graph2(matchTask.getScenario().getTargetDatabase(), this.numberOfFDs);

        // Create SimilarityCalculator
        SimilarityCalculator simCalculator = new SimilarityCalculator(matchTask, simMatrix) {
            @Override
            protected float calcStringSim(String stringA, String stringB) {
                return 0;
            }

            @Override
            protected float calcOtherSim(Object objectA, Object objectB) {
                return 0;
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = new ConstantWeightingGraph(dbGraphSource, dbGraphTarget, simCalculator);
        // Create Flooder
        Flooder flooder = new FlooderC(pGraph);

        float[][] boostedMatrix = flooder.flood(this.max_iterations, 0.0000001F);

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