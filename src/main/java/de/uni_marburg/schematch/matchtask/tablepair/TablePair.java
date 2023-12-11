package de.uni_marburg.schematch.matchtask.tablepair;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.evaluation.performance.TablePairPerformance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.matchstep.FirstLineMatchingStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.SecondLineMatchingStep;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
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
    private int[][] candidates; // not used atm, probably should be List<ColumnPair>
    private int[][] groundTruth;
    // TODO: maybe matchsteps should hold their results/performances
    // results (similarity matrices)
    private Map<MatchStep, Map<Matcher, float[][]>> matcherResultsForMatchSteps;
    // results (performances)
    private Map<Matcher, TablePairPerformance> firstLineMatcherPerformances;
    private Map<Matcher, TablePairPerformance> boostedFirstLineMatcherPerformances;
    private Map<Matcher, TablePairPerformance> secondLineMatcherPerformances;
    private Map<Matcher, TablePairPerformance> boostedSecondLineMatcherPerformances;

    public TablePair(Table sourceTable, Table targetTable) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.matcherResultsForMatchSteps = new HashMap<>();
        this.firstLineMatcherPerformances = new HashMap<>();
        this.boostedFirstLineMatcherPerformances = new HashMap<>();
        this.secondLineMatcherPerformances = new HashMap<>();
        this.boostedSecondLineMatcherPerformances = new HashMap<>();
    }

    /**
     * @return An empty similarity matrix of size (m,n) where m is the number of source columns and
     * n is the number of target columns. The position (i,j) is supposed to hold the similarity score for
     * the column pair (i-th source column, j-th target column).
     */
    public float[][] getEmptySimMatrix() {
        return new float[this.sourceTable.getNumberOfColumns()][this.targetTable.getNumberOfColumns()];
    }

    public void addResults(TablePairMatcher tablePairMatcher, MatchStep matchStep, float[][] simMatrix) {
        if (!this.matcherResultsForMatchSteps.containsKey(matchStep)) {
            this.matcherResultsForMatchSteps.put(matchStep, new HashMap<>());
        }
        this.matcherResultsForMatchSteps.get(matchStep).put(tablePairMatcher, simMatrix);
    }

    public void addPerformanceForFirstLineMatcher(Matcher matcher, TablePairPerformance tablePerformance) {
        this.firstLineMatcherPerformances.put(matcher, tablePerformance);
    }

    public TablePairPerformance getPerformanceForFirstLineMatcher(Matcher matcher) {
        return this.firstLineMatcherPerformances.get(matcher);
    }

    public void addBoostedPerformanceForFirstLineMatcher(Matcher matcher, TablePairPerformance tablePerformance) {
        this.boostedFirstLineMatcherPerformances.put(matcher, tablePerformance);
    }

    public TablePairPerformance getBoostedPerformanceForFirstLineMatcher(Matcher matcher) {
        return this.boostedFirstLineMatcherPerformances.get(matcher);
    }

    public void addPerformanceForSecondLineMatcher(Matcher matcher, TablePairPerformance tablePerformance) {
        this.secondLineMatcherPerformances.put(matcher, tablePerformance);
    }

    public TablePairPerformance getPerformanceForSecondLineMatcher(Matcher matcher) {
        return this.secondLineMatcherPerformances.get(matcher);
    }

    public void addBoostedPerformanceForSecondLineMatcher(Matcher matcher, TablePairPerformance tablePerformance) {
        this.boostedSecondLineMatcherPerformances.put(matcher, tablePerformance);
    }

    public TablePairPerformance getBoostedPerformanceForSecondLineMatcher(Matcher matcher) {
        return this.boostedSecondLineMatcherPerformances.get(matcher);
    }

    public TablePairPerformance getPerformance(MatchStep matchStep, Matcher matcher) {
        TablePairPerformance tablePairPerformance = null;
        if (matchStep instanceof FirstLineMatchingStep) {
            tablePairPerformance = this.firstLineMatcherPerformances.get(matcher);
        } else if (matchStep instanceof SecondLineMatchingStep) {
            tablePairPerformance = this.secondLineMatcherPerformances.get(matcher);
        } else if (matchStep instanceof SimMatrixBoostingStep) {
            if (((SimMatrixBoostingStep) matchStep).getLine() == 1) {
                tablePairPerformance = this.boostedFirstLineMatcherPerformances.get(matcher);
            } else {
                tablePairPerformance = this.boostedSecondLineMatcherPerformances.get(matcher);
            }
        }
        return tablePairPerformance;
    }

    public int getNumGroundTruthMatches() {
        return ArrayUtils.sumOfMatrix(this.groundTruth);
    }

    public String toString() {
        return sourceTable.getName() + Configuration.getInstance().getDefaultTablePairSeparator() +
                targetTable.getName();
    }


}
