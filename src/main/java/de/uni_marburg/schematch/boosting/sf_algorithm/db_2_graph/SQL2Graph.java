package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.boosting.SimFloodingSimMatrixBoosting;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQL2Graph extends DBGraph{
    private final static Logger log = LogManager.getLogger(SQL2Graph.class);

    public SQL2Graph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source) {
        super(line, matchTask, tablePair, matcher, source);
    }

    @Override
    protected void generateGraph(){
        // Add Table
        this.addVertex(this.getTable());
        // Add Table type
        this.addVertex("Table");
        this.addEdge(this.getTable(), "Table", new LabeledEdge("type"));
        // Add columns, column type and name
        this.addVertex("ColumnType");
        for(Column.Datatype type : Column.Datatype.values()){
            this.addVertex(type);
            this.addVertex(type.name());
            this.addEdge(type, "ColumnType", new LabeledEdge("type"));
            this.addEdge(type, type.name(), new LabeledEdge("name"));
        }

        this.addVertex("Column");
        for(Column column : this.getColumns()){
            this.addVertex(column);
            this.addEdge(this.getTable(), column, new LabeledEdge("column"));
            this.addEdge(column, "Column", new LabeledEdge("type"));
            this.addVertex(column.getLabel());
            this.addEdge(column, column.getLabel(), new LabeledEdge("name"));
            this.addEdge(column, column.getDatatype(), new LabeledEdge("SQLtype"));
        }


    }
}
