package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TokenizedMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Setter;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.util.ArrayList;

@Setter
public class CustomMatrixBoosting implements SimMatrixBoosting {
    private TablePair tablePair;
    @Override
    public float[][] run(float[][] simMatrix) {
        if(this.tablePair != null) {
            float[][] uniqueColumnBoosting = unaryUniqueColumnCombination(this.tablePair.getSourceTable(), this.tablePair.getTargetTable());
            float[][] functionalDependency = unaryFunctionalDependency(this.tablePair.getSourceTable(), this.tablePair.getTargetTable());
            float[][] inclusionDependency = unaryInclusionDependency(this.tablePair.getSourceTable(), this.tablePair.getTargetTable());
            simMatrix = boostSimMatrix(simMatrix, uniqueColumnBoosting);
            simMatrix = boostSimMatrix(simMatrix, functionalDependency);
            simMatrix = boostSimMatrix(simMatrix, inclusionDependency);
            return simMatrix;
        } else {
            return simMatrix;
        }
    }



    private float[][] unaryUniqueColumnCombination(Table source, Table target) {
        ArrayList<Integer> sourceUnique = HelperFunctions.getUniqueColumns(source, true);
        ArrayList<Integer> targetUnique = HelperFunctions.getUniqueColumns(target, true);

        float[][] result = new float[source.getNumberOfColumns()][target.getNumberOfColumns()];

        for (Integer v1 : sourceUnique) {
            for (Integer v2 : targetUnique) {
                result[v1][v2] = 1.0f;
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

    private float[][] unaryInclusionDependency(Table source, Table target){
        float[][] result = new float[source.getNumberOfColumns()][target.getNumberOfColumns()];
        for (int i = 0; i < source.getNumberOfColumns(); i++){
            ArrayList<String> sourceValues = HelperFunctions.getUniqueValuesFromList(source.getColumn(i).getValues());
            for (int j = 0; j < target.getNumberOfColumns(); j++){
                ArrayList<String> targetValues = HelperFunctions.getUniqueValuesFromList(target.getColumn(j).getValues());
                if(targetValues.containsAll(sourceValues)){
                    result[i][j] = 1.0f;
                    System.out.println("INCLUSION FOUND!");
                } else {
                    result[i][j] = 0.0f;
                }
            }
            System.out.println("TESTING: " + source.getName() + " AND " + target.getName());
        }
        return result;
    }

    private float[][] unaryFunctionalDependency(Table source, Table target){
        float[][] result = new float[source.getNumberOfColumns()][target.getNumberOfColumns()];
        for (int i = 0; i < source.getNumberOfColumns(); i++){
            ArrayList<String> sourceValues = (ArrayList<String>) source.getColumn(i).getValues();
            for (int j = 0; j < target.getNumberOfColumns(); j++){
                ArrayList<String> targetValues = (ArrayList<String>) target.getColumn(j).getValues();
                if(HelperFunctions.functionalDependencyExists(sourceValues, targetValues, true)){
                    result[i][j] = 1.0f;
                    System.out.println("FUNCTIONAL DEPENDENCY FOUND!");
                } else {
                    result[i][j] = 0.0f;
                }
            }
            System.out.println("TESTING: " + source.getName() + " AND " + target.getName());
        }
        return result;
    }
}