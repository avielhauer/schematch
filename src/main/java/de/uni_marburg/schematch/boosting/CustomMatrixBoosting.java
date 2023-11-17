package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TokenizedMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

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
            ArrayList<Integer> unique = getUniqueColumns(this.tablePair.getTargetTable(), true);
            System.out.println(unique);

            /**
            System.out.println("SOURCE DATA");
            for (int i = 0; i < this.tablePair.getSourceTable().getNumberOfColumns(); i++){
                System.out.println("COLUMN: " + this.tablePair.getSourceTable().getColumn(i).getLabel());
                System.out.println("VALUES: " + this.tablePair.getSourceTable().getColumn(i).getValues());
            }

            System.out.println("\nTARGET DATA");
            for (int i = 0; i < this.tablePair.getTargetTable().getNumberOfColumns(); i++){
                System.out.println("COLUMN: " + this.tablePair.getTargetTable().getColumn(i).getLabel());
                System.out.println("VALUES: " + this.tablePair.getTargetTable().getColumn(i).getValues());
            }
             **/
        }
        c++;
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
}
