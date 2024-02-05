package de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator;

import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SimilarityCalculator{
    private final static Logger log = LogManager.getLogger(SimilarityCalculator.class);
    private final Map<PropagationNode, Float> columnSimilarity;

    protected SimilarityCalculator(MatchTask matchTask, float[][] simMatrix) {
        this.columnSimilarity = new HashMap<>();
        for(int i = 0; i < simMatrix.length; i++){
            Column sourceColumn = matchTask.getScenario().getSourceDatabase().getColumnByIndex(i);
            for(int j = 0; j < simMatrix[0].length; j++){
                Column targetColumn = matchTask.getScenario().getTargetDatabase().getColumnByIndex(j);
                PropagationNode pair = new PropagationNode(sourceColumn, targetColumn);
                this.columnSimilarity.put(pair, simMatrix[i][j]);
            }
        }
    }

    public final float calcSim (PropagationNode node){
        Object objectA = node.getObjectA();
        Object objectB = node.getObjectB();

        if(objectA == null || objectB == null) return 0F;
        if(!objectA.getClass().equals(objectB.getClass())) return 0F;
        if(objectA.getClass().equals(Float.class)) return calcFloatSim((float) objectA, (float) objectB);
        if(objectA.getClass().equals(String.class)) return calcStringSim((String) objectA, (String) objectB);
        if(objectA.getClass().equals(Column.class)) return calcColumnSim(node);
        return calcOtherSim(objectA, objectB);
    }

    protected abstract float calcStringSim(String stringA, String stringB);

    private float calcColumnSim(PropagationNode node){
        if(!this.columnSimilarity.containsKey(node)) return 0F;
        return this.columnSimilarity.get(node);
    }

    protected float calcFloatSim(float floatA, float floatB){
        return Math.abs(floatA-floatB) / Math.max(floatA, floatB);
    }

    protected abstract float calcOtherSim(Object objectA, Object objectB);
}
