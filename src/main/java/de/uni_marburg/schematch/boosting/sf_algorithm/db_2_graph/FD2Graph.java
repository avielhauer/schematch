package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;


import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;

import java.util.*;
import java.util.stream.Collectors;


public class FD2Graph extends DBGraph {
    //TODO: Add Scoring for better edge weighting
    public FD2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph() {
        List<String> selectedTableNames = getSelectedTableNames();

        for (String tableName : selectedTableNames) {
            Table table = getDatabase().getTableByName(tableName);
            if (table != null) {
                for (Column column : table.getColumns()) {
                    addVertex(column);
                }


                List<FunctionalDependency> sortedFDs = table.getFunctionalDependencies().stream()
                        .sorted(Comparator.comparing(FunctionalDependency::getSortingCriteria))
                        .collect(Collectors.toList());


                for (FunctionalDependency fd : sortedFDs) {
                    Collection<Column> dependentColumns = fd.getDependentColumns();

                    for (Column dependentColumn : dependentColumns) {
                        if (vertexSet().contains(dependentColumn)) {
                            addEdge(fd.getDeterminantColumn(), dependentColumn, new LabeledEdge("FD"));
                        }
                    }
                }
            }
        }
    }


    private List<String> getSelectedTableNames() {
        return Arrays.asList("Table1", "Table2");
    }


}
