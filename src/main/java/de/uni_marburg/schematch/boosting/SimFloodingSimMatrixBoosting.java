package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.*;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Similarity Flooding Matrix Boosting
 */
public class SimFloodingSimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(SimFloodingSimMatrixBoosting.class);

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix){
        // Create a DatabaseGraph
        DBGraph dbGraphSource = new FDExtraNode2_2Graph(matchTask.getScenario().getSourceDatabase());
        DBGraph dbGraphTarget = new FDExtraNode2_2Graph(matchTask.getScenario().getTargetDatabase());

        // Create SimilarityCalculator
        SimilarityCalculator simCalculator = new SimilarityCalculator(matchTask, simMatrix) {
            @Override
            protected float calcStringSim(String stringA, String stringB){
                return 0f;
            }

            @Override
            protected float calcOtherSim(Object objectA, Object objectB){
                if(objectA.getClass() == FunctionalDependency.class){
                    FunctionalDependency fdA = (FunctionalDependency) objectA;
                    FunctionalDependency fdB = (FunctionalDependency) objectB;
                    float result = this.calcFloatSim((float) fdA.getPdepTuple().gpdep, (float) fdB.getPdepTuple().gpdep);
                    return result;
                }
                return 0f;
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = new ConstantWeightingGraph(dbGraphSource, dbGraphTarget, simCalculator);
        // Create Flooder
        Flooder flooder = new FlooderC(pGraph);

        float[][] boostedMatrix = flooder.flood(1, 0.0000001F);

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