package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matching.Matcher;
import lombok.*;

import java.util.Random;

/**
 * Dummy implementation for a state-of-the-art matcher which outputs a randomized similarity matrix.
 */
@NoArgsConstructor
@AllArgsConstructor
public class RandomMatcher extends Matcher {
    @Getter
    @Setter
    private long seed;

    @Override
    public float[][] match(MatchTask matchTask, MatchStep matchStep) {
        float[][] simMatrix = matchTask.getEmptySimMatrix();
        Random random = new Random(this.seed);
        for (int i = 0; i < matchTask.getNumSourceColumns(); i++) {
            for (int j = 0; j < matchTask.getNumTargetColumns(); j++) {
                simMatrix[i][j] = random.nextFloat();
            }
        }
        return simMatrix;
    }
}
