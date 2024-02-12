package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class Database {
    private final Scenario scenario;
    private final String name;
    private final String path;
    private List<Table> tables;
    private DatabaseMetadata metadata;
    private int numColumns;
    private DatabaseGraph graph;
    private DatabaseFeatures databaseFeatures;

    public Database(Scenario scenario, String path) {
        this.scenario = scenario;
        this.name = new File(path).getName();
        this.path = path;
        this.tables = InputReader.readDataDir(this.path);
        // TODO: read dependencies on demand
        if (Configuration.getInstance().isReadDependencies()) {
            this.metadata = InputReader.readDatabaseMetadata(this);
        }

        // set matrix offsets for tables
        int currentOffset = 0;
        for (Table table : this.tables) {
            table.setOffset(currentOffset);
            currentOffset += table.getNumColumns();
        }
        // set numColumns
        numColumns = currentOffset;

        this.graph = new MetaNodesDatabaseGraph(this);
        this.databaseFeatures = new DatabaseFeatures(this);
        this.databaseFeatures.exportFeatures("target/features/" + scenario.getDataset().getName() +  "/" + scenario.getName());
    }

    public String getFullColumnNameByIndex(int idx) {
        for (Table table : this.tables) {
            if (table.getOffset() + table.getNumColumns() > idx) {
                return table.getName() + "." + table.getColumn(idx - table.getOffset()).getLabel();
            }
        }
        throw new IllegalStateException("Could not find full column name for index: " + idx);
    }

    public Column getColumnByIndex(int idx) {
        for (Table table : this.tables) {
            if (table.getOffset() + table.getNumColumns() > idx) {
                return table.getColumn(idx - table.getOffset());
            }
        }
        throw new IllegalStateException("Could not find column for index: " + idx);
    }

    public Table getTableByName(String tableName) {
        for (Table table : this.tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }
}