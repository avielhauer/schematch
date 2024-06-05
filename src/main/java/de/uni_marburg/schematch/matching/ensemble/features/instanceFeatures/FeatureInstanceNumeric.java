package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;


public class FeatureInstanceNumeric extends FeatureInstace {
    public FeatureInstanceNumeric(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        Column[] columns = new Column[]{columnPair.getSourceColumn(),columnPair.getTargetColumn()};
        List<Boolean> isColumnNumeric = new ArrayList<>();

        for (Column c : columns) {
            if(c.getDatatype().equals(Datatype.INTEGER) || c.getDatatype().equals(Datatype.FLOAT)){
                isColumnNumeric.add(true);
            }else {
                isColumnNumeric.add(!c.getValues().stream().map(this::isNumeric).collect(Collectors.toList()).contains(false));
            }
        }

        return isColumnNumeric.contains(false)?0:1;
    }

    private boolean isNumeric(String s) {
        return StringUtils.isNumeric(s);
    }
}
