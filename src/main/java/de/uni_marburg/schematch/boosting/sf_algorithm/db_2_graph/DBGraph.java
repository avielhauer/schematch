package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.data.metadata.ScenarioMetadata;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public abstract class DBGraph extends DefaultDirectedGraph<Object, LabeledEdge> {
    private final static Logger log = LogManager.getLogger(DBGraph.class);
    private final Database database;

    public DBGraph(Database database){
        super(LabeledEdge.class);
        this.database = database;
    }



    protected void generateGraph(){
        log.debug("Generated new DB-Graph with {} vertices and {} edges.", this.vertexSet().size(), this.edgeSet().size());
    }

    public final void addDBGraph(DBGraph that){
        for(Object vertex : that.vertexSet()){
            this.addVertex(vertex);
        }
        for(LabeledEdge edge : that.edgeSet()){
            this.addEdge(that.getEdgeSource(edge), that.getEdgeTarget(edge), edge);
        }
    }

    @Override
    public boolean addEdge(Object source, Object target, LabeledEdge edge){
        if(source.equals(target)) throw new RuntimeException("DB graphs should not have loops.");
        return super.addEdge(source, target, edge);
    }
}
