package de.uni_marburg.schematch.matching.ensemble.features.labelFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;

import java.util.ArrayList;
import java.util.Arrays;

public class FeatureLabelComponents extends Feature {
    final static ArrayList<String> SEMANTIC_COMPONENTS = new ArrayList<>(Arrays.asList(
            " ", "_", "-","xxx"
    ));

    public FeatureLabelComponents(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {
        initiateK(Double.valueOf(columnPair.getSourceColumn().getLabel().length()),Double.valueOf(columnPair.getTargetColumn().getLabel().length()));
        return calc(getSemanticFrequency(columnPair.getSourceColumn()),getSemanticFrequency(columnPair.getTargetColumn()));
    }

    private int getSemanticFrequency(Column column){
        int result = 0;
        for(char labelchar:column.getLabel().toCharArray()){
            if (SEMANTIC_COMPONENTS.contains(labelchar)){
                result++;
            }

        }
        if (result > 0) {
            System.out.println(result);
        }
        return result;
    }
}
