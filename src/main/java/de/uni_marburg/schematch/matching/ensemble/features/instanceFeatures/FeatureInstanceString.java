package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureInstanceString extends FeatureInstace {

    public FeatureInstanceString(String name){
        super(name);
    }

    //The byte cap has been set to 4000 here, as Varchar2 in Oracle can be a maximum of 4000 bytes; beyond this limit, BLOB must be chosen.
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
        return calc(result.get(0),result.get(1));
    }

    private double calculateStringRatio(String string){
        return Double.valueOf(string.length())/Double.valueOf(STRING_BASELINE);
    }

}
