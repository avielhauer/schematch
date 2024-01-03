package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Dummy implementation for an ensemble matcher that randomly picks each row for its output similarity matrix
 * from all first line matcher results. If similarity matrix boosting is enabled for first line matchers,
 * it uses the boosted similarity matrices instead of the vanilla first line matcher results.
 */
@NoArgsConstructor
@AllArgsConstructor
public class RandomEnsembleMatcher extends Matcher {
    @Getter
    @Setter
    private long seed;

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        Random random = new Random(this.seed);
        List<Matcher> matchers = matchTask.getFirstLineMatchers();

        int numSourceColumns = matchTask.getScenario().getSourceDatabase().getNumColumns();
        int numTargetColumns = matchTask.getScenario().getTargetDatabase().getNumColumns();

        float[][] newSimMatrix = new float[numSourceColumns][numTargetColumns];

        for (int i = 0; i < numSourceColumns; i++) {
            for (int j = 0; j < numTargetColumns; j++) {
                Matcher matcher = matchers.get(random.nextInt(matchers.size()));
                newSimMatrix[i][j] = matchTask.getSimMatrixFromPreviousMatchStep(matchStep, matcher)[i][j];
            }
        }

        return newSimMatrix;
    }
}
