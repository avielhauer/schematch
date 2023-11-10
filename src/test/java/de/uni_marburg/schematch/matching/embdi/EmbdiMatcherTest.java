package de.uni_marburg.schematch.matching.embdi;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.MatcherTest;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class EmbdiMatcherTest {

    @Test
    void match() throws Exception {
        Map<String, Object> params = new HashMap<>();
        String NAME = "EmbdiMatcher";
        String PACKAGE_NAME = "embdi";
        Configuration.MatcherConfiguration matcherConfiguration = new Configuration.MatcherConfiguration(NAME, PACKAGE_NAME, params);

        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test1").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");
        Table targetTable = scenario.getTargetDatabase().getTableByName("authors");
        TablePair tp = new TablePair(sourceTable, targetTable);

        Matcher matcher = MatcherTest.getMatcherFactory().createMatcherInstance(matcherConfiguration);

        float[][] sim_matrix = matcher.match(tp);

        boolean any_non_null = false;
        for (float[] line : sim_matrix) {
            for (float sim : line) {
                if (sim != 0.0) {
                    any_non_null = true;
                    break;
                }
            }
        }
        assertTrue(any_non_null);
    }
}
