package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;
import java.util.Set;

public class DBGraph extends SimpleDirectedGraph<Object, LabeledEdge> {
    private final Table table;

    public DBGraph(Table table) {
        super(LabeledEdge.class);
        this.table = table;
        for (Column column : table.getColumns()){
            super.addVertex(column);
        }

    }

    public void addNumericMetadata(){
        for(Column column: this.table.getColumns()){
            for (String key : column.getMetadata().getNumMetaMap().keySet()){
                super.addVertex(column.getMetadata().getNumericMetadata(key));
                super.addEdge(column, column.getMetadata().getNumericMetadata(key), new LabeledEdge(key));
            }
        }
    }

    public void addNumericMetadata(Set<String> filter){
        for(String key : filter){
            for(Column column: this.table.getColumns()){
                if(column.getMetadata().getNumMetaMap().containsKey(key)){
                    super.addVertex(column.getMetadata().getNumericMetadata(key));
                    super.addEdge(column, column.getMetadata().getNumericMetadata(key), new LabeledEdge(key));
                }
            }
        }
    }

    public void addStringMetadata(){
        for(Column column: this.table.getColumns()){
            for (String key : column.getMetadata().getStringMetaMap().keySet()){
                super.addVertex(column.getMetadata().getStringMetadata(key));
                super.addEdge(column, column.getMetadata().getStringMetadata(key), new LabeledEdge(key));
            }
        }
    }

    public void addStringMetadata(Set<String> filter){
        for(String key : filter){
            for(Column column: this.table.getColumns()){
                if(column.getMetadata().getStringMetaMap().containsKey(key)){
                    super.addVertex(column.getMetadata().getStringMetadata(key));
                    super.addEdge(column, column.getMetadata().getStringMetadata(key), new LabeledEdge(key));
                }
            }
        }
    }
    public void addDatatypes(){
        for(Column.Datatype datatype : Column.Datatype.values()){
            super.addVertex(datatype);
        }
        for (Column column : table.getColumns()){
            super.addEdge(column, column.getDatatype(), new LabeledEdge("datatype"));
        }
    }

    public PropagationGraph generatePropagationGraph(DBGraph that, WeightDistributer distributor){
        Set<LabeledEdge> thisEdges = this.edgeSet();
        Set<LabeledEdge> thatEdges = that.edgeSet();

        PropagationGraph pGraph = new PropagationGraph();

        for(LabeledEdge thisEdge : thisEdges){
            for(LabeledEdge thatEdge : thatEdges){
                if(Objects.equals(thisEdge.getLabel(), thatEdge.getLabel())){
                    ObjectPair pair1 = new ObjectPair(this.getEdgeSource(thisEdge), that.getEdgeSource(thatEdge));
                    ObjectPair pair2 = new ObjectPair(this.getEdgeTarget(thisEdge), that.getEdgeTarget(thatEdge));
                    pGraph.addVertex(pair1);
                    pGraph.addVertex(pair2);
                    pGraph.addEdge(pair1, pair2, new WeightedEdge(0));
                    pGraph.addEdge(pair2, pair1, new WeightedEdge(0));
                }
            }
        }

        for(WeightedEdge edge : pGraph.edgeSet()){
            edge.setWeight(distributor.apply(edge, pGraph));
        }

        return pGraph;
    }

}
