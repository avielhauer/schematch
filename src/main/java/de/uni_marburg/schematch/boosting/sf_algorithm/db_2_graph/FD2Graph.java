package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.Collection;

public class FD2Graph extends DBGraph {
    //TODO: Add Scoring for better edge weighting
    public FD2Graph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source) {
        super(line, matchTask, tablePair, matcher, source);
    }

    @Override
    protected void generateGraph() {
        for(Column column : this.getColumns()){
            this.addVertex(column);
        }
        for(Column column : this.getColumns()){
            Collection<FunctionalDependency> fds = this.getFds().get(column);
            if(fds == null) continue;
            for(FunctionalDependency fd : fds){
                for(Column determinant : fd.getDeterminant()){
                    if(determinant.equals(column)) continue; // Avoid loops
                    this.addEdge(column, determinant, new LabeledEdge("fd"));
                }
            }
        }
    }
}
