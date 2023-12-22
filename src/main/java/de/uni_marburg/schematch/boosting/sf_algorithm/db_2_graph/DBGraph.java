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
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public abstract class DBGraph extends SimpleDirectedGraph<Object, LabeledEdge> {
    private final static Logger log = LogManager.getLogger(DBGraph.class);
    private final int line;
    private final MatchTask matchTask;
    private final TablePair tablePair;
    private final Matcher matcher;
    private final Table table;
    private final List<Column> columns;
    private final Scenario scenario;
    private final ScenarioMetadata scenarioMetadata;
    private final Database database;
    private final DatabaseMetadata dbMetadata;
    private final Map<Column, Collection<UniqueColumnCombination>> uccs;
    private final Map<Column, Collection<FunctionalDependency>> fds;
    private final Collection<InclusionDependency> inds;

    protected DBGraph(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher, boolean source){
        super(LabeledEdge.class);
        this.line = line;
        this.matchTask = matchTask;
        this.tablePair = tablePair;
        this.matcher = matcher;

        // Extract Tables
        this.table = (source)? tablePair.getSourceTable() : tablePair.getTargetTable();
        //Extract Columns
        this.columns = table.getColumns();
        // Extract and load scenario meta data
        this.scenario = matchTask.getScenario();
        this.scenarioMetadata = scenario.getMetadata();
        // Extract and load database meta data
        this.database = (source)? matchTask.getScenario().getSourceDatabase() : matchTask.getScenario().getTargetDatabase();
        this.dbMetadata = this.database.getMetadata();
        // Extract UCCs
        this.uccs = this.dbMetadata.getUccMap();
        // Extract FDs
        this.fds = this.dbMetadata.getFdMap();
        // Extract INDs
        this.inds = this.dbMetadata.getInds();

        this.generateGraph();
        log.debug("Generated new DB-Graph with {} vertices and {} edges.", this.vertexSet().size(), this.edgeSet().size());
    }

    protected abstract void generateGraph();
}
