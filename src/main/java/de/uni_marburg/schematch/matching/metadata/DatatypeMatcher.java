package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatatypeMatcher extends TablePairMatcher {
    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            HashMap<Datatype, Double> datatypeScores_i = sourceTable.getColumn(i).getDataTypeScores();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                HashMap<Datatype, Double> datatypeScores_j = targetTable.getColumn(j).getDataTypeScores();
                simMatrix[i][j] = calculateSimilarity(datatypeScores_i, datatypeScores_j);
            }
        }
        return simMatrix;
    }

    private float calculateSimilarity(HashMap<Datatype, Double> scores_a, HashMap<Datatype, Double> scores_b) {
        Datatype a = Datatype.determineDatatype(scores_a);
        Datatype b = Datatype.determineDatatype(scores_b);
        if (a == b) {
            return  1;
        } else if ((a == Datatype.INTEGER && b == Datatype.FLOAT) || (b == Datatype.INTEGER && a == Datatype.FLOAT)) {
            // every int can be a float so both types are similar though not equal
            return  0.8f; // some arbitrary value
        } else if ((a == Datatype.STRING && b == Datatype.TEXT) || (b == Datatype.STRING && a == Datatype.TEXT)) {
            // everything can be a string however a text is just a long string
            return 0.7f; // some arbitrary value
        } else if (a == Datatype.STRING || b == Datatype.STRING) {
            // the closer to 1.0 the non-string score is the less similar the columns are
            return (float) (1f - Math.min(scores_a.get(a), scores_b.get(b)));
        } else {
            return  0;
        }
    }
}
