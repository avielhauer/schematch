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
        Collection<FunctionalDependency> fds = getDatabase().getMetadata().getFds();

        List<FunctionalDependency> sortedFDs = fds.stream()
                .sorted(Comparator.comparing(FunctionalDependency::getSortingCriteria))
                .collect(Collectors.toList());

        for (Table table : getDatabase().getTables()) {
            if (table != null) {
                for (Column column : table.getColumns()) {
                    addVertex(column);
                }
            }
        }

        for (FunctionalDependency fd : sortedFDs) {
          //  System.out.println("Processing FD: " + fd);

            Collection<Column> dependentColumns = fd.getDependentColumns();
            for (Column dependentColumn : dependentColumns) {
                //System.out.println("Processing dependentColumn: " + dependentColumn);

                for (Column column : fd.getDeterminant()) {
                  //  System.out.println("Processing determinant column: " + column);

                    if (!column.equals(dependentColumn)) {
                        if (vertexSet().contains(column) && vertexSet().contains(dependentColumn)) {
                          //  System.out.println("Adding edge: " + column + " -> " + dependentColumn);
                            addEdge(column, dependentColumn, new LabeledEdge("FD"));
                        }
                    }
                }
            }
        }
    }

}


