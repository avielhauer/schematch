package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmbdiMatcher extends Matcher {
    Logger log = LogManager.getLogger(EmbdiMatcher.class);

    @Override
    public float[][] match(TablePair tablePair) {
        log.info("Running Embdi matcher for tables '{}' as source and '{}' as target.",
                tablePair.getSourceTable().getName(), tablePair.getTargetTable().getName());

        return PythonUtils.runPythonMatcher(
                tablePair,
                false,
                "embdi",
                "embdi.py",
                "-i_1", tablePair.getSourceTable().getPath(),
                "-i_2", tablePair.getTargetTable().getPath());
    }
}
