package de.uni_marburg.schematch.matchtask.tablepair;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;

/**
 * Central class for the schema matching process. It consists of a table from a source database and another
 * table from a target database. It stores ground truth correspondences and all matching outputs for these two tables.
 */
@Data
public class TablePair {
    private final Table sourceTable;
    private final Table targetTable;
    private Scenario scenario;
    private int[][] candidates; // not used atm, probably should be List<ColumnPair>
    private int[][] groundTruth;

    public TablePair(Table sourceTable, Table targetTable, Scenario scenario) {
        this.scenario = scenario;
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        initializeMaps();
    }

    public TablePair(Table sourceTable, Table targetTable) {
        this.scenario = null;
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        initializeMaps();
    }

    private void initializeMaps() {
        this.firstLineMatcherResults = new HashMap<>();
        this.boostedFirstLineMatcherResults = new HashMap<>();
        this.secondLineMatcherResults = new HashMap<>();
        this.boostedSecondLineMatcherResults = new HashMap<>();
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
        return new float[this.sourceTable.getNumColumns()][this.targetTable.getNumColumns()];
    }

    public int[][] getEmptyGTMatrix() {
        return new int[this.sourceTable.getNumColumns()][this.targetTable.getNumColumns()];
    }

    public int getNumGroundTruthMatches() {
        return ArrayUtils.sumOfMatrix(this.groundTruth);
    }

    public String toString() {
        return sourceTable.getName() + Configuration.getInstance().getDefaultTablePairSeparator() +
                targetTable.getName();
    }
}
