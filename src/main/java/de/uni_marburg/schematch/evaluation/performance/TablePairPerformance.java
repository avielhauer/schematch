package de.uni_marburg.schematch.evaluation.performance;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TablePairPerformance extends Performance {
    Map<ColumnPair, ColumnPairPerformance> columnPairPerformances;

    public TablePairPerformance() {
        super();
        this.columnPairPerformances = new HashMap<>();
    }

    public TablePairPerformance(int TP, int FP, float simScoreTP, float simScoreFP, Map<ColumnPair, ColumnPairPerformance> columnPairPerformances) {
        super(TP, FP, simScoreTP, simScoreFP);
        this.columnPairPerformances = columnPairPerformances;
    }

    public ColumnPairPerformance getColumnPairPerformance(ColumnPair columnPair) {
        return this.columnPairPerformances.get(columnPair);
    }

    public void addColumnPairPerformance(ColumnPair columnPair, ColumnPairPerformance columnPairPerformance) {
        this.columnPairPerformances.put(columnPair, columnPairPerformance);
    }
}
