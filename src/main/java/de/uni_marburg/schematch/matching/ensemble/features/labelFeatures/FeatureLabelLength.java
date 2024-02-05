package de.uni_marburg.schematch.matching.ensemble.features.labelFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

public class FeatureLabelLength extends Feature{
    int low ;
    int middle;
    int upper;
    public FeatureLabelLength(String name,int low ,int middle, int upper) {
        super(name);
        //TODO zaza fragen, wie diese definiert werden? wird im testrun jetzt erstmal außenvor gelassen
        this.low=low;
        this.middle=middle;
        this.upper=upper;
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        Column source=columnPair.getSourceColumn();
        Column target=columnPair.getTargetColumn();

        return calc(calcScore(source.getLabel()),calcScore(target.getLabel()));
    }

    private double calcScore(String label) {
        if(label.length()<low){

            return (label.length()/low)*0.3;
        }
        else if(label.length()<middle){

            return (label.length()/middle)*0.6;
        }
        else
        {
            return 1;

        }
    }
}
