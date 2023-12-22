package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Metadata2Graph extends DBGraph{
    private final static Logger log = LogManager.getLogger(Metadata2Graph.class);

    public Metadata2Graph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source) {
        super(line, matchTask, tablePair, matcher, source);
    }

    @Override
    protected void generateGraph() {
        for(Column column : this.getColumns()){
            this.addVertex(column);
        }

        this.addNumericMetadata();
        this.addStringMetadata();
        this.addDatatypes();
    }

    private void addNumericMetadata(){
        for(Column column: this.getColumns()){
            for (String key : column.getMetadata().getNumMetaMap().keySet()){
                super.addVertex(column.getMetadata().getNumericMetadata(key));
                super.addEdge(column, column.getMetadata().getNumericMetadata(key), new LabeledEdge(key));
            }
        }
    }

    public void addStringMetadata(){
        for(Column column: this.getColumns()){
            for (String key : column.getMetadata().getStringMetaMap().keySet()){
                super.addVertex(column.getMetadata().getStringMetadata(key));
                super.addEdge(column, column.getMetadata().getStringMetadata(key), new LabeledEdge(key));
            }
        }
    }

    public void addDatatypes(){
        for(Column.Datatype datatype : Column.Datatype.values()){
            super.addVertex(datatype);
        }
        for (Column column : this.getColumns()){
            super.addEdge(column, column.getDatatype(), new LabeledEdge("datatype"));
        }
    }



}
