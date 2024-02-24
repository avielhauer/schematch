package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Dataset {
    private String name;
    private String path;
    private boolean isDenormalized;
    private List<String> scenarioNames;

    public Dataset(Configuration.DatasetConfiguration datasetConfiguration) {
        this.name = datasetConfiguration.getName();
        this.path = datasetConfiguration.getPath();
        this.isDenormalized = datasetConfiguration.isDenormalized();
        this.scenarioNames = new ArrayList<>();

        File dir = new File(this.path);

        for (File subdir : dir.listFiles()) {
            if (subdir.isDirectory()) {
                this.scenarioNames.add(subdir.getName());
            }
        }
    }
}