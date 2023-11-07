package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matching.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Example for a single column metadata matcher
 * TODO: determine datatypes first (at the moment everything is a String)
 * TODO: find heuristic to give a score 1>s>0 for unequal but similar datatypes
 */
@Data
@NoArgsConstructor
public class DatatypeMatcher extends Matcher {
    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            Column.Datatype datatype_i = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {
                Column.Datatype datatype_j = targetTable.getColumn(j).getDatatype();
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
