package de.uni_marburg.schematch;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.utils.Configuration;

import java.io.File;
import java.util.ArrayList;

public class TestUtils {
    private static Dataset testDataset = null;

    public static Dataset getTestDataset() {
        if (TestUtils.testDataset == null) {
            TestUtils.testDataset = TestUtils.loadData();
        }
        return TestUtils.testDataset;
    }

    private static Dataset loadData() {
        Configuration config = Configuration.getInstance();
        Configuration.DatasetConfiguration dsConfig = new Configuration.DatasetConfiguration("Test",
                config.getDefaultDatasetBasePath() + File.separator + "Test");
        return new Dataset(dsConfig, new ArrayList<>());
    }
}
