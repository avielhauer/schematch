package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TokenizedMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Setter
public class CustomMatrixBoosting implements SimMatrixBoosting {
    private TablePair tablePair;
    // TODO: Improve output from single matcher with corresponding table pair over n-dependencies
    // beste werte für jeden match boosten, rest runter -> stable marriage
    // ucc, fd?
    // https://en.wikipedia.org/wiki/Stable_marriage_problem
    // eventuell über dependencies
    @Override
    public float[][] run(float[][] simMatrix) {

        if(this.tablePair != null) {
            float[][] uniqueColumnBoosting = bothUnique(this.tablePair.getSourceTable(), this.tablePair.getTargetTable());
            return boostSimMatrix(simMatrix, uniqueColumnBoosting);
        } else {
            return simMatrix;
        }
    }

    private ArrayList<Integer> getUniqueColumns(Table table, boolean ignoreEmpty){
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < table.getNumberOfColumns(); i++){
            ArrayList<String> values = (ArrayList<String>) table.getColumn(i).getValues();
            HashMap<String, Integer> map = new HashMap<>();
            boolean isUnique = true;
            for (String value : values) {
                if(value.isEmpty() && ignoreEmpty){
                    continue;
                }
                if (map.get(value) != null) {
                    isUnique = false;
                    break;
                } else {
                    map.put(value, map.size());
                }
            }
            if(isUnique){
                result.add(i);
            }
        }
        return result;
    }

    private float[][] bothUnique(Table source, Table target) {
        ArrayList<Integer> sourceUnique = getUniqueColumns(source, true);
        ArrayList<Integer> targetUnique = getUniqueColumns(target, true);

        float[][] result = new float[source.getNumberOfColumns()][target.getNumberOfColumns()];

        for(int i=0; i<sourceUnique.size(); i++) {
            for(int j=0; j<targetUnique.size(); j++) {
                result[sourceUnique.get(i)][targetUnique.get(j)] = 1.0f;
            }
        }
        return result;
    }

    private float[][] boostSimMatrix(float[][] simMatrix, float[][] boostMatrix) {
        for(int i=0; i<simMatrix.length; i++) {
            for(int j=0; j<simMatrix[0].length; j++) {
                if(boostMatrix[i][j] > 0f) {
                    simMatrix[i][j] = (simMatrix[i][j] + boostMatrix[i][j]) / 2f;
                }
            }
        }
        return simMatrix;
    }
}