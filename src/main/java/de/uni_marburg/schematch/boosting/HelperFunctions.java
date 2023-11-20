package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.data.Table;

import java.lang.reflect.Array;
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

    static boolean functionalDependencyExists(ArrayList<String> source, ArrayList<String> target, boolean ignoreEmpty){
        HashMap<String, String> map = new HashMap<>();
        for (String v1: source){
            if(v1.isEmpty() && ignoreEmpty){
                continue;
            }
            for (String v2: target){
                if (map.get(v1) == null){
                    map.put(v1, v2);
                } else {
                    String objValue = map.get(v1);
                    if(!objValue.equals(v2)){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

