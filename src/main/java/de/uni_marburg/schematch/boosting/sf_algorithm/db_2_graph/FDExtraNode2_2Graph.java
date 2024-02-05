package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FDExtraNode2_2Graph extends DBGraph {
    //TODO: Add Scoring for better edge weighting
    public FDExtraNode2_2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph() {
        //Load FDs:
        Collection<FunctionalDependency> fds = this.getDatabase().getMetadata().getFds();
        List<FunctionalDependency> sortedFDs = fds.stream()
                .sorted(Comparator.comparingDouble(fd -> -fd.getPdepTuple().gpdep)).toList();

        List<FunctionalDependency> chosenFDs = sortedFDs.subList(0, Math.min(10, sortedFDs.size()));


        // Create graph:
        for(FunctionalDependency fd : chosenFDs){
           this.addVertex(fd);
           this.addVertex(fd.getDependant());
           this.addEdge(fd.getDependant(), fd, new LabeledEdge("dependant"));
           for(Column determinant : fd.getDeterminant()){
               this.addVertex(determinant);
               this.addEdge(determinant, fd, new LabeledEdge("determinant"));
           }
        }
    }
}
