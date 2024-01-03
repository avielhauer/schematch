package de.uni_marburg.schematch.matchtask.matchstep;


import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.generators.NaiveTablePairsGenerator;
import de.uni_marburg.schematch.matchtask.tablepair.generators.TablePairsGenerator;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TablePairGenerationStep extends MatchStep {
    private final static Logger log = LogManager.getLogger(TablePairGenerationStep.class);

    private final TablePairsGenerator tablePairsGenerator;

    public TablePairGenerationStep(boolean doSave, boolean doEvaluate, TablePairsGenerator tablePairsGenerator) {
        super(doSave, doEvaluate);
        this.tablePairsGenerator = tablePairsGenerator;
        if (!(tablePairsGenerator instanceof NaiveTablePairsGenerator)) {
            throw new IllegalArgumentException("Only naive table pairs generator allowed at the moment.");
        }
    }

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running table pair generation on scenario: " + matchTask.getScenario().getPath());
        matchTask.setTablePairs(this.tablePairsGenerator.generateCandidates(matchTask.getScenario()));
        log.debug("Source tables: " + matchTask.getScenario().getSourceDatabase().getTables().size() + ", Target tables: " +
                matchTask.getScenario().getTargetDatabase().getTables().size() + ", table pairs: " +
                matchTask.getTablePairs().size());
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
