package de.uni_marburg.schematch;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    private static TestUtils.TestData testData = null;

    @Data
    @AllArgsConstructor
    public static class TestData {
        private Dataset dataset;
        private Map<String, Scenario> scenarios;
    }

    public static TestData getTestData() {
        if (TestUtils.testData == null) {
            TestUtils.testData = TestUtils.loadData();
        }
        return TestUtils.testData;
    }

    private static TestData loadData() {
        Configuration config = Configuration.getInstance();
        Configuration.DatasetConfiguration dsConfig = new Configuration.DatasetConfiguration("Test",
                config.getDefaultDatasetBasePath() + File.separator + "Test");
        Dataset d = new Dataset(dsConfig);
        HashMap<String, Scenario> s = new HashMap<>();
        for (String sName : d.getScenarioNames()) {
            s.put(sName, new Scenario(d.getPath() + File.separator + sName));
        }
        return new TestData(d, s);
    }
}
