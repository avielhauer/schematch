package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
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

        for (TablePair tablePair : tablePairs) {
            tablePair.addResults(this, matchStep, this.match(tablePair));
        }

        return matchTask.getGlobalSimMatrix(this, matchStep);
    }
}
