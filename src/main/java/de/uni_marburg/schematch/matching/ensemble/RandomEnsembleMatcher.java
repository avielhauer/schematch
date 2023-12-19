package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

/**
 * Dummy implementation for an ensemble matcher that randomly picks each row for its output similarity matrix
 * from all first line matcher results. If similarity matrix boosting is enabled for first line matchers,
 * it uses the boosted similarity matrices instead of the vanilla first line matcher results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomEnsembleMatcher extends Matcher {
    private long seed;

    @Override
    public float[][] match(TablePair tablePair) {
        int numSourceColumns = tablePair.getSourceTable().getNumberOfColumns();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        Object[] firstLineMatchers;
        if (Configuration.getInstance().isRunSimMatrixBoostingOnFirstLineMatchers()) {
            firstLineMatchers = tablePair.getBoostedFirstLineMatcherResults().keySet().toArray();

        } else {
            firstLineMatchers = tablePair.getFirstLineMatcherResults().keySet().toArray();
        }
        Random random = new Random(this.seed);
        for (int i = 0; i < numSourceColumns; i++) {
            int randomIndex = random.nextInt(firstLineMatchers.length);
            if (Configuration.getInstance().isRunSimMatrixBoostingOnFirstLineMatchers()) {
                simMatrix[i] = tablePair.getBoostedResultsForFirstLineMatcher((Matcher) firstLineMatchers[randomIndex])[i];
            } else {
                simMatrix[i] = tablePair.getResultsForFirstLineMatcher((Matcher) firstLineMatchers[randomIndex])[i];
            }
        }
        return simMatrix;
    }
}
