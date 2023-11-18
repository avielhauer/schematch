package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract class HelperFunctions {
    static ArrayList<Integer> getUniqueColumns(Table table, boolean ignoreEmpty){
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

    static ArrayList<String> getUniqueValuesFromList(List<String> values){
        ArrayList<String> uniqueValues = new ArrayList<>();
        for (String s: values){
            if(!uniqueValues.contains(s) && !s.isEmpty()){
                uniqueValues.add(s);
            }
        }
        return uniqueValues;
    }

}

