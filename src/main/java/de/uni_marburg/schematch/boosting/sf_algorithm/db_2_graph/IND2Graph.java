package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

public class IND2Graph extends DBGraph {
    //TODO: Add Scoring for better edge weighting
    //TODO: CAVE: not implemented by now, since similarityMatrixBoosting only works on tables in this version of schematch

    public IND2Graph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source) {
        super(line, matchTask, tablePair, matcher, source);
    }

    @Override
    protected void generateGraph() {
        throw new RuntimeException("IND2Graph not implemented yet.");
    }
}
