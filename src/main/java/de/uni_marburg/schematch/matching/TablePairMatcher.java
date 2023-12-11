package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class TablePairMatcher extends Matcher {
    public abstract float[][] match(TablePair tablePair);

    @Override
    public float[][] match(MatchTask matchTask, MatchStep matchStep) {
        List<TablePair> tablePairs = matchTask.getTablePairs();
        Database sourceDatabase = matchTask.getScenario().getSourceDatabase();
        Database targetDatabase = matchTask.getScenario().getTargetDatabase();

        float[][] globalSimMatrix = new float[sourceDatabase.getNumColumns()][targetDatabase.getNumColumns()];

        for (TablePair tablePair : tablePairs) {
            float[][] tablePairSimMatrix = this.match(tablePair);
            int sourceTableOffset = tablePair.getSourceTable().getGlobalMatrixOffset();
            int targetTableOffset = tablePair.getTargetTable().getGlobalMatrixOffset();
            ArrayUtils.insertSubmatrixInMatrix(tablePairSimMatrix, globalSimMatrix, sourceTableOffset, targetTableOffset);
        }

        return globalSimMatrix;
    }
}
