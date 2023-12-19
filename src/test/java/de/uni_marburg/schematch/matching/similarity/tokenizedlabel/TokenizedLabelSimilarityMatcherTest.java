package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.TestUtils;
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

class TokenizedLabelSimilarityMatcherTest {
    public static void testTokenizedLabelSimilarityMatcher (
            Configuration.MatcherConfiguration matcherConfiguration,
            Tokenizer tokenizer,
            SimilarityMeasure<Set<String>> similarityMeasure
    ) throws Exception {
        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test1").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");
        Table targetTable = scenario.getTargetDatabase().getTableByName("authors");
        TablePair tp = new TablePair(sourceTable, targetTable);

        Matcher matcher = MatcherTest.getMatcherFactory().createTokenizedMatcherInstance(matcherConfiguration, tokenizer);
        float[][] simMatrix = matcher.match(tp);

        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                float simScore = similarityMeasure.compare(sourceTable.getColumn(i).getLabelTokens(tokenizer),
                        targetTable.getColumn(j).getLabelTokens(tokenizer));
                assertEquals(simScore, simMatrix[i][j]);
            }
        }
    }
}