package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class FeatrueInstanceUniqueness extends FeatureInstace {
    public FeatrueInstanceUniqueness(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        try {

            double x= calcUniqueness(columnPair.getSourceColumn());
            double y= calcUniqueness(columnPair.getTargetColumn());

            return calc(x,y);
        }
        catch (IllegalArgumentException e){
            return 0;
        }
    }

    private double calcUniqueness(Column targetColumn) {
        if(targetColumn.getValues().size()==0)
            throw new IllegalArgumentException("Colomns are Empty");
        Set<String> set=new HashSet<>();
        for(String s:targetColumn.getValues())
            set.add(s);
        return set.size()/targetColumn.getValues().size();
    }
}
