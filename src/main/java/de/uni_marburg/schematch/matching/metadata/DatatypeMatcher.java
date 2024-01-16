package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Example for a single column metadata matcher
 * TODO: determine datatypes first (at the moment everything is a String)
 * TODO: find heuristic to give a score 1>s>0 for unequal but similar datatypes
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class DatatypeMatcher extends TablePairMatcher {
    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Datatype datatype_i = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Datatype datatype_j = targetTable.getColumn(j).getDatatype();
                if (datatype_i == datatype_j) {
                    simMatrix[i][j] = 1;
                } else {
                    simMatrix[i][j] = 0;
                }
            }
        }
        return simMatrix;
    }
}
