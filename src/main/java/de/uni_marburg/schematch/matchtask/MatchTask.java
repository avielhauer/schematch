package de.uni_marburg.schematch.matchtask;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
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
    private List<TablePair> tablePairs; // set by tablepair gen match step
    private Map<MatchStep, Map<Matcher, float[][]>> globalSimMatrices;

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
        for (TablePair tablePair : this.tablePairs) {
            int[][] gt = InputReader.readGroundTruthFile(basePath + File.separator + tablePair.toString() + ".csv");
            tablePair.setGroundTruth(gt);
        }
    }

    public void setGlobalSimMatrix(Matcher matcher, MatchStep matchStep, float[][] globalSimMatrix) {
        this.globalSimMatrices.get(matchStep).put(matcher, globalSimMatrix);
    }

    public float[][] getGlobalSimMatrix(Matcher matcher, MatchStep matchStep) {
        return this.globalSimMatrices.get(matchStep).get(matcher);
    }

    public float[][] assembleGlobalSimMatrix(TablePairMatcher tablePairMatcher, MatchStep matchStep) {
        int numSourceColumns = this.scenario.getSourceDatabase().getNumColumns();
        int numTargetColumns = this.scenario.getTargetDatabase().getNumColumns();
        float[][] globalSimMatrix = new float[numSourceColumns][numTargetColumns];

        // FIXME: come up with a solution for attribute pairs not covered in table pairs (i.e., table pairs are smaller than cross product of tables)

        setGlobalSimMatrix(tablePairMatcher, matchStep, globalSimMatrix);
        return globalSimMatrix;
    }
}
