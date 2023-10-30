package de.uni_marburg.schematch.matchtask.tablepair.generators;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.List;

public interface TablePairsGenerator {
    public List<TablePair> generateCandidates(Scenario scenario);
}
