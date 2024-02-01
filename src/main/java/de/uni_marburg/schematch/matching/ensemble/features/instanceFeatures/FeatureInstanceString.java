package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureInstanceString extends FeatureInstace {
    //Hier wurde 4000 als byte cap gesetzt, da Varchar2 in Oracle max 4000 bytes groß sein kann, hiernach muss BLOB gewählt werden.
    public final static int STRING_BASELINE = 4000;
    @Override
    public double calculateScore(ColumnPair columnPair) {
        Column[] columns = new Column[]{columnPair.getSourceColumn(),columnPair.getTargetColumn()};
        List<List<Double>> doubleScoreToStringBaseline = new ArrayList<>();
        for (Column c : columns) {
            doubleScoreToStringBaseline.add(c.getValues().stream().map(this::calculateStringRatio).collect(Collectors.toList()));
        }
        List<Double> result =  calculateAverage(doubleScoreToStringBaseline);
        initiateK(result);
        return calculateScoreOfFeatrue(result.get(0),result.get(1));
    }

    private double calculateStringRatio(String stringLength){
        return 0;
    }

}
