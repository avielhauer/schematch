package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Database;
public class IND2Graph extends DBGraph {
    //TODO: Add Scoring for better edge weighting
    //TODO: CAVE: not implemented by now

    public IND2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph() {
        throw new RuntimeException("IND2Graph not implemented yet.");
    }
}
