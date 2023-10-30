package de.uni_marburg.schematch.matchtask.matchstep;


import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.generators.TablePairsGenerator;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Data
@RequiredArgsConstructor
public class TablePairGenerationStep implements MatchStep {
    final static Logger log = LogManager.getLogger(TablePairGenerationStep.class);

    private final TablePairsGenerator tablePairsGenerator;

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running table pair generation on scenario: " + matchTask.getScenario().getPath());
        matchTask.setTablePairs(this.tablePairsGenerator.generateCandidates(matchTask.getScenario()));
        log.debug("Source tables: " + matchTask.getScenario().getSourceDatabase().getTables().size() + ", Target tables: " +
                matchTask.getScenario().getTargetDatabase().getTables().size() + ", table pairs: " +
                matchTask.getTablePairs().size());
        Configuration config = Configuration.getInstance();
        if (config.isEvaluateFirstLineMatchers() || config.isEvaluateSimMatrixBoostingOnFirstLineMatchers() ||
                config.isEvaluateSecondLineMatchers() || config.isEvaluateSimMatrixBoostingOnSecondLineMatchers()) {
            matchTask.readGroundTruth();
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if (!Configuration.getInstance().isSaveOutputTablePairGeneration()) {
            return;
        }
        throw new NotImplementedException("Method for saving table pair generation outputs not implemented yet.");
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if (!Configuration.getInstance().isEvaluateTablePairGeneration()) {
            return;
        }
        throw new NotImplementedException("Method for evaluating table pair generation outputs not implemented yet.");
    }
}
