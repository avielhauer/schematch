package de.uni_marburg.schematch.matchtask.tablepair.generators;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroundTruthTablePairsGenerator implements TablePairsGenerator {
    private static final Logger log = LogManager.getLogger(GroundTruthTablePairsGenerator.class);

    @Override
    public List<TablePair> generateCandidates(Scenario scenario) {
        List<TablePair> tablePairs = new ArrayList<>();

        Database sourceDatabase = scenario.getSourceDatabase();
        Database targetDatabase = scenario.getTargetDatabase();

        String path = scenario.getPath() + File.separator + Configuration.getInstance().getDefaultGroundTruthDir();
        List<String> gtTablePairNames = InputReader.fetchGroundTruthTablePairNames(path);

        for (String gtTablePairName : gtTablePairNames) {
            String gtSourceTableName = gtTablePairName.split(Configuration.getInstance().getDefaultTablePairSeparator())[0];
            String gtTargetTableName = gtTablePairName.split(Configuration.getInstance().getDefaultTablePairSeparator())[1];
            Table sourceTable = sourceDatabase.getTableByName(gtSourceTableName);
            Table targetTable = targetDatabase.getTableByName(gtTargetTableName);
            tablePairs.add(new TablePair(sourceTable, targetTable, scenario));
        }

        return tablePairs;
    }
}
