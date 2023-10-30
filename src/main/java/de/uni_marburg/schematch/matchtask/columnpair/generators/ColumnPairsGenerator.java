package de.uni_marburg.schematch.matchtask.columnpair.generators;

import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.List;

public interface ColumnPairsGenerator {
    public List<ColumnPair> generateCandidates(TablePair tablePair);
}
