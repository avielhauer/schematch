package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Metadata2Graph extends DBGraph{
    private final static Logger log = LogManager.getLogger(Metadata2Graph.class);

    public Metadata2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph() {
        //TODO
    }
}
