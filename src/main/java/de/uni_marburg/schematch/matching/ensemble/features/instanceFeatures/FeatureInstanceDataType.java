package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

public class FeatureInstanceDataType extends FeatureInstace {
    public FeatureInstanceDataType(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        return columnPair.getSourceColumn().getDatatype().equals(columnPair.getTargetColumn().getDatatype())?1:0;
    }
}
