package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matching.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

/**
 * Dummy implementation for a state-of-the-art matcher which outputs a randomized similarity matrix.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomMatcher extends Matcher {
    private long seed;

    @Override
    public float[][] match(TablePair tablePair) {
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        Random random = new Random(this.seed);
        for (int i = 0; i < tablePair.getSourceTable().getNumberOfColumns(); i++) {
            for (int j = 0; j < tablePair.getTargetTable().getNumberOfColumns(); j++) {
                simMatrix[i][j] = random.nextFloat();
            }
        }
        return simMatrix;
    }
}
