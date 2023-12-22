package de.uni_marburg.schematch.matchtask;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.matchstep.TablePairGenerationStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.ConfigUtils;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private Map<MatchStep, Map<Matcher, float[][]>> simMatrices;
    private int[][] groundTruthMatrix;
    private int numSourceColumns, numTargetColumns;
    private Evaluator evaluator;

    public MatchTask(Dataset dataset, Scenario scenario, List<MatchStep> matchSteps) {
        this.dataset = dataset;
        this.scenario = scenario;
        this.matchSteps = matchSteps;
        this.numSourceColumns = scenario.getSourceDatabase().getNumColumns();
        this.numTargetColumns = scenario.getTargetDatabase().getNumColumns();
        this.simMatrices = new HashMap<>();
    }

    /**
     * Sequentially runs all {@link #matchSteps}. For each step, it first calls {@link MatchStep#run},
     * then {@link MatchStep#save}, and finally {@link MatchStep#evaluate}. Each method call checks with
     * their respective config parameter whether it should be executed or not.
     */
    public void runSteps() {
        for (MatchStep matchStep : matchSteps) {
            this.simMatrices.put(matchStep, new HashMap<>());
            matchStep.run(this);
            if (matchStep instanceof TablePairGenerationStep && ConfigUtils.anyEvaluate()) {
                this.readGroundTruth();
                this.evaluator = new Evaluator(this.scenario, this.groundTruthMatrix);
            }
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

        this.groundTruthMatrix = new int[this.numSourceColumns][this.numTargetColumns];

        for (TablePair tablePair : this.tablePairs) {
            int[][] gtMatrix = InputReader.readGroundTruthFile(basePath + File.separator + tablePair.toString() + ".csv");
            if (gtMatrix == null) {
                gtMatrix = tablePair.getEmptyGTMatrix();
            }
            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(gtMatrix, this.groundTruthMatrix, sourceTableOffset, targetTableOffset);
        }
    }

    public float[][] getEmptySimMatrix() {
        return new float[this.numSourceColumns][this.numTargetColumns];
    }

    public void setSimMatrix(MatchStep matchStep, Matcher matcher, float[][] simMatrix) {
        this.simMatrices.get(matchStep).put(matcher, simMatrix);
    }

    public float[][] getSimMatrix(MatchStep matchStep, Matcher matcher) {
        return this.simMatrices.get(matchStep).get(matcher);
    }

    public float[][] getSimMatrixFromPreviousMatchStep(MatchStep matchStep, Matcher matcher) {
        MatchStep previousMatchStep = null;
        for (MatchStep currMatchStep : this.matchSteps) {
            if (currMatchStep == matchStep) {
                break;
            } else {
                previousMatchStep = currMatchStep;
            }
        }
        return this.getSimMatrix(previousMatchStep, matcher);
    }

    public Map<String, List<Matcher>> getFirstLineMatchers() {
        for (MatchStep matchStep : this.matchSteps) {
            if (matchStep instanceof MatchingStep && ((MatchingStep) matchStep).getLine() == 1) {
                return ((MatchingStep) matchStep).getMatchers();
            }
        }
        return null;
    }
    public Map<String, List<Matcher>> getSecondLineMatchers() {
        for (MatchStep matchStep : this.matchSteps) {
            if (matchStep instanceof MatchingStep && ((MatchingStep) matchStep).getLine() == 2) {
                return ((MatchingStep) matchStep).getMatchers();
            }
        }
        return null;
    }

    public Map<String, List<Matcher>> getMatchersForLine(int line) {
        return switch (line) {
            case 1 -> getFirstLineMatchers();
            case 2 -> getSecondLineMatchers();
            default -> throw new IllegalStateException("Unexpected value: " + line);
        };
    }
}
