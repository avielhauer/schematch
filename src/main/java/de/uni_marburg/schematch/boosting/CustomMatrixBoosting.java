package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TokenizedMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Set;

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
        return simMatrix;
    }

    static int c = 0;
    public void test(){
        if(c == 0){
            for (int i = 0; i < this.tablePair.getSourceTable().getNumberOfColumns(); i++){
                ArrayList<String> sourceTokens_i = (ArrayList<String>) this.tablePair.getSourceTable().getColumn(i).getValues();
                System.out.println(sourceTokens_i);
            }
        }
        c++;
    }
}
