package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

public interface SimMatrixBoosting {
    /**
     * @param line line of similarity matrix boosting, i.e. 1 or 2
     * @param matchTask MatchTask with all needed information for similarity boosting
     * @return An updated (hopefully better) similarity matrix
     */
    public float[][] run(int line, MatchTask matchTask, TablePair tablePait, Matcher matcher);
}
