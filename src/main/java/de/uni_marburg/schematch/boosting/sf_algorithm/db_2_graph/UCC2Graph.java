package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.Collection;

public class UCC2Graph extends DBGraph{
    //TODO: Add Scoring for better edge weighting
    public UCC2Graph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source) {
        super(line, matchTask, tablePair, matcher, source);
    }

    @Override
    protected void generateGraph() {
        for(Column column : this.getColumns()){
            this.addVertex(column);
        }

        for(Column column : this.getColumns()){
            Collection<UniqueColumnCombination> uccs = this.getUccs().get(column);
            if(uccs == null) continue;
            for(UniqueColumnCombination ucc : uccs){
                for(Column dependant : ucc.getColumnCombination()){
                    if(dependant.equals(column)) continue; // Avoid loops
                    this.addEdge(column, dependant, new LabeledEdge("ucc"));
                }
            }
        }
    }
}
