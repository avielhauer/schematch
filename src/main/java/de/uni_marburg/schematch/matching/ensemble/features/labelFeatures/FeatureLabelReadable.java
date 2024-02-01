package de.uni_marburg.schematch.matching.ensemble.features.labelFeatures;

import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

public class FeatureLabelReadable extends Feature {
    public FeatureLabelReadable(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        return 0;
    }
}
