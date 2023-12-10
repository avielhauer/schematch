package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
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
    private List<MatchStep> matchSteps;
    private List<MatchTask> scenarioMatchTasks;

    public Dataset(Configuration.DatasetConfiguration datasetConfiguration, List<MatchStep> matchSteps) {
        this.name = datasetConfiguration.getName();
        this.path = datasetConfiguration.getPath();
        this.matchSteps = matchSteps;
        this.scenarioMatchTasks = new ArrayList<>();

        File dir = new File(this.path);

        for (File subdir : dir.listFiles()) {
            if (subdir.isDirectory()) {
                Scenario scenario = new Scenario(subdir.getPath());
                scenarioMatchTasks.add(new MatchTask(this, scenario, matchSteps));
            }
        }
    }
}