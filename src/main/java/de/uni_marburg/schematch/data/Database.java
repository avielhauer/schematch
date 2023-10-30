package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import lombok.Data;

import java.io.File;
import java.util.Map;

@Data
public class Database {
    private final String name;
    private final String path;
    private Map<String, Table> tables;
    private DatabaseMetadata metadata;

    public Database(String path) {
        this.name = new File(path).getName();
        this.path = path;
        this.tables = InputReader.readDataDir(this.path);
        // TODO: read dependencies on demand
        if (Configuration.getInstance().isReadDependencies()) {
            this.metadata = InputReader.readDatabaseMetadata(this.path, this.tables);
        }
    }

    public Table getTableByName(String tableName) {
        return this.tables.get(tableName);
    }
}