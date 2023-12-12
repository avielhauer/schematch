package de.uni_marburg.schematch.matchtask.tablepair.generators;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class NaiveTablePairsGenerator implements TablePairsGenerator {
    private static final Logger log = LogManager.getLogger(NaiveTablePairsGenerator.class);

    @Override
    public List<TablePair> generateCandidates(Scenario scenario) {
        Database sourceDatabase = scenario.getSourceDatabase();
        Database targetDatabase = scenario.getTargetDatabase();

        List<TablePair> tablePairs = new ArrayList<>();
        for (Table sourceTable : sourceDatabase.getTables().values()) {
            for (Table targetTable : targetDatabase.getTables().values()) {
                tablePairs.add(new TablePair(sourceTable, targetTable, scenario));
            }
        }
        return tablePairs;
    }
}
