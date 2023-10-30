package de.uni_marburg.schematch.evaluation.performance;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MatcherPerformance extends Performance {
    private final Map<TablePair, TablePairPerformance> tablePairPerformances;

    public MatcherPerformance() {
        super();
        this.tablePairPerformances = new HashMap<>();
    }

    public TablePairPerformance getTablePairPerformance(TablePair tablePair) {
        return this.tablePairPerformances.get(tablePair);
    }

    public void addTablePairPerformance(TablePair tablePair, TablePairPerformance tablePairPerformance) {
        this.tablePairPerformances.put(tablePair, tablePairPerformance);
    }
}
