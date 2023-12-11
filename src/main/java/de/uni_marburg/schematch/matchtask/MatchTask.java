package de.uni_marburg.schematch.matchtask;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central class for the schema matching process. For each scenario, there is a match task which
 * sequentially runs all configured matching steps.
 */
@Data
public class MatchTask {
    final static Logger log = LogManager.getLogger(MatchTask.class);

    private final Dataset dataset;
    private final Scenario scenario;
    private final List<MatchStep> matchSteps;
    private List<TablePair> tablePairs; // is set by tablepair gen match step
    private Map<MatchStep, Map<Matcher, float[][]>> globalSimMatrices;
    private int[][] globalGroundTruthMatrix;

    public MatchTask(Dataset dataset, Scenario scenario, List<MatchStep> matchSteps) {
        this.dataset = dataset;
        this.scenario = scenario;
        this.matchSteps = matchSteps;
        this.globalSimMatrices = new HashMap<>();
    }

    /**
     * Sequentially runs all {@link #matchSteps}. For each step, it first calls {@link MatchStep#run},
     * then {@link MatchStep#save}, and finally {@link MatchStep#evaluate}. Each method call checks with
     * their respective config parameter whether it should be executed or not.
     */
    public void runSteps() {
        for (MatchStep matchStep : matchSteps) {
            this.globalSimMatrices.put(matchStep, new HashMap<>());
            matchStep.run(this);
            matchStep.save(this);
            matchStep.evaluate(this);
        }
    }

    /**
     * Reads ground truth matrices for all table pairs with at least one ground truth correspondence and updates
     * the table pair objects accordingly. Table pairs with no ground truth correspondence will have {@code null}
     * as their ground truth matrix
     */
    public void readGroundTruth() {
        log.debug("Reading ground truth for scenario: " + this.scenario.getPath());
        String basePath = scenario.getPath() + File.separator + Configuration.getInstance().getDefaultGroundTruthDir();

        Database sourceDatabase = this.scenario.getSourceDatabase();
        Database targetDatabase = this.scenario.getTargetDatabase();

        this.globalGroundTruthMatrix = new int[sourceDatabase.getNumColumns()][targetDatabase.getNumColumns()];

        for (TablePair tablePair : this.tablePairs) {
            int[][] gtMatrix = InputReader.readGroundTruthFile(basePath + File.separator + tablePair.toString() + ".csv");
            int sourceTableOffset = tablePair.getSourceTable().getGlobalMatrixOffset();
            int targetTableOffset = tablePair.getTargetTable().getGlobalMatrixOffset();
            ArrayUtils.insertSubmatrixInMatrix(gtMatrix, this.globalGroundTruthMatrix, sourceTableOffset, targetTableOffset);
        }
    }

    public void setGlobalSimMatrix(MatchStep matchStep, Matcher matcher, float[][] globalSimMatrix) {
        this.globalSimMatrices.get(matchStep).put(matcher, globalSimMatrix);
    }

    public float[][] getGlobalSimMatrix(Matcher matcher, MatchStep matchStep) {
        return this.globalSimMatrices.get(matchStep).get(matcher);
    }
}
