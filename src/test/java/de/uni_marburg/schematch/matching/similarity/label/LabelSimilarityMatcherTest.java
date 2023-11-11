package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.MatcherTest;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.utils.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelSimilarityMatcherTest {
    public static void testLabelSimilarityMatcher (
            Configuration.MatcherConfiguration matcherConfiguration,
            SimilarityMeasure<String> similarityMeasure
    ) throws Exception {
        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test1").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");
        Table targetTable = scenario.getTargetDatabase().getTableByName("authors");
        TablePair tp = new TablePair(sourceTable, targetTable);

        Matcher matcher = MatcherTest.getMatcherFactory().createMatcherInstance(matcherConfiguration);
        float[][] simMatrix = matcher.match(tp);

        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {
                float simScore = similarityMeasure.compare(sourceTable.getColumn(i).getLabel(),
                        targetTable.getColumn(j).getLabel());
                assertEquals(simScore, simMatrix[i][j]);
            }
        }
    }
}