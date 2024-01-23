package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class TablePairMatcher extends Matcher {
    public abstract float[][] match(TablePair tablePair) throws CrediblityPredictorModel.ModelTrainedException;

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        List<TablePair> tablePairs = matchTask.getTablePairs();

        float[][] simMatrix = matchTask.getEmptySimMatrix();

        for (TablePair tablePair : tablePairs) {
            float[][] tablePairSimMatrix = new float[0][];
            try {
                tablePairSimMatrix = this.match(tablePair);
            } catch (CrediblityPredictorModel.ModelTrainedException e) {
                e.printStackTrace();
            }
            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(tablePairSimMatrix, simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }
}
