package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

public class FeatureInstanceCryptical extends FeatureInstace {
    public FeatureInstanceCryptical(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        return 0;
    }
}
