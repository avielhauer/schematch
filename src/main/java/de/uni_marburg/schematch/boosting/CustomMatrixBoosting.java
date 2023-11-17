package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Setter;

@Setter
public class CustomMatrixBoosting implements SimMatrixBoosting {
    private Scenario scenario;
    // TODO: Improve output from single matcher with corresponding table pair over n-dependencies
    // beste werte für jeden match boosten, rest runter -> stable marriage
    // ucc, fd?
    // https://en.wikipedia.org/wiki/Stable_marriage_problem
    // eventuell über dependencies
    @Override
    public float[][] run(float[][] simMatrix) {
        return simMatrix;
    }

    public void test(){
        System.out.println(this.scenario.getSourceDatabase().getTables());
    }
}
