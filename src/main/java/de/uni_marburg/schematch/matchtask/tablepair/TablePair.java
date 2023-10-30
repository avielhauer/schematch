package de.uni_marburg.schematch.matchtask.tablepair;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Central class for the schema matching process. It consists of a table from a source database and another
 * table from a target database. It stores ground truth correspondences and all matching outputs for these two tables.
 */
@Data
public class TablePair {
    private final Table sourceTable;
    private final Table targetTable;
    private int[][] candidates;
    private int[][] groundTruth;
    private Map<Matcher, float[][]> firstLineMatcherResults;
    private Map<Matcher, float[][]> boostedFirstLineMatcherResults;
    private Map<Matcher, float[][]> secondLineMatcherResults;
    private Map<Matcher, float[][]> boostedSecondLineMatcherResults;

    public TablePair(Table sourceTable, Table targetTable) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.firstLineMatcherResults = new HashMap<>();
        this.boostedFirstLineMatcherResults = new HashMap<>();
        this.secondLineMatcherResults = new HashMap<>();
        this.boostedSecondLineMatcherResults = new HashMap<>();
    }

    /**
     * @return An empty similarity matrix of size (m,n) where m is the number of source columns and
     * n is the number of target columns. The position (i,j) is supposed to hold the similarity score for
     * the column pair (i-th source column, j-th target column).
     */
    public float[][] getEmptySimMatrix() {
        return new float[this.sourceTable.getNumberOfColumns()][this.targetTable.getNumberOfColumns()];
    }

    public void addResultsForFirstLineMatcher(Matcher matcher, float[][] simMatrix) {
        this.firstLineMatcherResults.put(matcher, simMatrix);
    }

    public float[][] getResultsForFirstLineMatcher(Matcher matcher) {
        return this.firstLineMatcherResults.get(matcher);
    }

    public void addBoostedResultsForFirstLineMatcher(Matcher matcher, float[][] simMatrix) {
        this.boostedFirstLineMatcherResults.put(matcher, simMatrix);
    }

    public float[][] getBoostedResultsForFirstLineMatcher(Matcher matcher) {
        return this.boostedFirstLineMatcherResults.get(matcher);
    }

    public void addResultsForSecondLineMatcher(Matcher matcher, float[][] simMatrix) {
        this.secondLineMatcherResults.put(matcher, simMatrix);
    }

    public float[][] getResultsForSecondLineMatcher(Matcher matcher) {
        return this.secondLineMatcherResults.get(matcher);
    }

    public void addBoostedResultsForSecondLineMatcher(Matcher matcher, float[][] simMatrix) {
        this.boostedSecondLineMatcherResults.put(matcher, simMatrix);
    }

    public float[][] getBoostedResultsForSecondLineMatcher(Matcher matcher) {
        return this.boostedSecondLineMatcherResults.get(matcher);
    }

    public int getNumGroundTruthMatches() {
        return ArrayUtils.sumOfMatrix(this.groundTruth);
    }

    public String toString() {
        return sourceTable.getName() + Configuration.getInstance().getDefaultTablePairSeparator() +
                targetTable.getName();
    }
}
