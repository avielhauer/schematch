package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;


import de.uni_marburg.schematch.boosting.StructuredBoostingTester;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class FD2Graph2 extends DBGraph {
    final int numberOfFDs;
    private final static Logger log = LogManager.getLogger(FD2Graph2.class);
    //TODO: Add Scoring for better edge weighting
    public FD2Graph2(Database database, int numberOfFDs)
    {
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
        log.error(numberOfFDs);

        // Create graph:
        for(FunctionalDependency fd : chosenFDs){
            this.addVertex(fd.getDependant());
            for(Column determinant : fd.getDeterminant()){
                this.addVertex(determinant);
                this.addEdge(fd.getDependant(), determinant, new LabeledEdge("determinant"));
            }
        }
    }
}
