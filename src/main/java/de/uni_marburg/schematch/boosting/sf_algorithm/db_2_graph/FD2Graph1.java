package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;


import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class FD2Graph1 extends DBGraph {
    final int numberOfFDs;

    //TODO: Add Scoring for better edge weighting
    public FD2Graph1(Database database, int numberOfFDs) {
        super(database);
        this.numberOfFDs = numberOfFDs;
        this.generateGraph();
        super.generateGraph();
    }

    @Override
    protected void generateGraph() {
        //Load FDs:
        Collection<FunctionalDependency> fds = this.getDatabase().getMetadata().getFds();
        List<FunctionalDependency> sortedFDs = fds.stream()
                .sorted(Comparator.comparingDouble(fd -> -fd.getPdepTuple().gpdep)).toList();

        List<FunctionalDependency> chosenFDs = sortedFDs.subList(0, Math.min(numberOfFDs, sortedFDs.size()));


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
