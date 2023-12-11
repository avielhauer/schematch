package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.MatcherTest;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.utils.Configuration;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizedInstanceSimilarityMatcherTest {
    public static void testTokenizedInstanceSimilarityMatcher (
            Configuration.MatcherConfiguration matcherConfiguration,
            Tokenizer tokenizer,
            SimilarityMeasure<Set<String>> similarityMeasure
    ) throws Exception {
        Dataset testDataset = TestUtils.getTestDataset();
        Scenario scenario = testDataset.getScenarioMatchTasks().get(0).getScenario();

        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");
        Table targetTable = scenario.getTargetDatabase().getTableByName("authors");
        TablePair tp = new TablePair(sourceTable, targetTable);

        Matcher matcher = MatcherTest.getMatcherFactory().createTokenizedMatcherInstance(matcherConfiguration, tokenizer);
        float[][] simMatrix = matcher.match(tp);

        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {
                float simScore = similarityMeasure.compare(sourceTable.getColumn(i).getValuesTokens(tokenizer),
                        targetTable.getColumn(j).getValuesTokens(tokenizer));
                assertEquals(simScore, simMatrix[i][j]);
            }
        }
    }
}