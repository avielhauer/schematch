package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.HashMap;
import java.util.Map;

public class DataCompatibilityTable {
    public final Map<String, Map<String, Double>> table;
    public DataCompatibilityTable() {
        this.table = new HashMap<>();

        Map<String, Double> textMap = new HashMap<>();
        textMap.put("keyword", 1.0);
        textMap.put("varchar", 1.0);
        textMap.put("nvarchar", 0.9);
        textMap.put("nchar", 0.8);
        textMap.put("char", 0.6);

        Map<String, Double> keywordMap = new HashMap<>();
        keywordMap.put("text", 1.0);
        keywordMap.put("varchar", 1.0);
        keywordMap.put("nvarchar", 0.9);
        keywordMap.put("nchar", 0.8);
        keywordMap.put("char", 0.6);

        Map<String, Double> varcharMap = new HashMap<>();
        varcharMap.put("text", 1.0);
        varcharMap.put("keyword", 1.0);
        varcharMap.put("nvarchar", 0.9);
        varcharMap.put("nchar", 0.8);
        varcharMap.put("char", 0.6);
        varcharMap.put("int", 0.1);

        Map<String, Double> nvarcharMap = new HashMap<>();
        nvarcharMap.put("text", 0.9);
        nvarcharMap.put("keyword", 0.9);
        nvarcharMap.put("varchar", 0.9);
        nvarcharMap.put("nchar", 0.8);
        nvarcharMap.put("char", 0.6);

        Map<String, Double> ncharMap = new HashMap<>();
        ncharMap.put("text", 0.7);
        ncharMap.put("keyword", 0.7);
        ncharMap.put("varchar", 0.7);
        ncharMap.put("nvarchar", 1.0);
        ncharMap.put("char", 0.7);

        Map<String, Double> charMap = new HashMap<>();
        charMap.put("text", 0.7);
        charMap.put("keyword", 0.7);
        charMap.put("varchar", 0.7);
        charMap.put("nvarchar", 0.6);
        charMap.put("nchar", 0.8);

        Map<String, Double> dateMap = new HashMap<>();
        dateMap.put("double", 0.1);
        dateMap.put("int", 0.1);
        dateMap.put("decimal", 0.1);
        dateMap.put("bit", 0.1);

        Map<String, Double> doubleMap = new HashMap<>();
        doubleMap.put("date", 0.1);
        doubleMap.put("float", 1.0);
        doubleMap.put("decimal", 1.0);

        Map<String, Double> decimalMap = new HashMap<>();
        decimalMap.put("date", 0.1);
        decimalMap.put("float", 1.0);
        decimalMap.put("double", 1.0);

        Map<String, Double> intMap = new HashMap<>();
        intMap.put("date", 0.1);
        intMap.put("long", 0.8);
        intMap.put("short", 0.7);
        intMap.put("smallint", 0.7);
        intMap.put("integer", 1.0);
        intMap.put("varchar", 0.1);

        Map<String, Double> integerMap = new HashMap<>();
        integerMap.put("date", 0.1);
        integerMap.put("long", 0.8);
        integerMap.put("short", 0.7);
        integerMap.put("smallint", 0.7);
        integerMap.put("int", 1.0);

        Map<String, Double> bitMap = new HashMap<>();
        bitMap.put("time", 0.1);
        bitMap.put("date", 0.1);

        Map<String, Double> timeMap = new HashMap<>();
        timeMap.put("bit", 0.1);

        Map<String, Double> floatMap = new HashMap<>();
        floatMap.put("double", 0.9);


        Map<String, Double> longMap = new HashMap<>();
        longMap.put("short", 0.6);
        longMap.put("int", 0.8);
        longMap.put("bigint", 0.6);
        longMap.put("smallint", 1.0);
        longMap.put("integer", 0.8);

        Map<String, Double> bigintMap = new HashMap<>();
        bigintMap.put("short", 0.6);
        bigintMap.put("int", 0.8);
        bigintMap.put("long", 1.0);
        bigintMap.put("smallint", 0.6);
        bigintMap.put("integer", 0.8);

        Map<String, Double> shortMap = new HashMap<>();
        shortMap.put("long", 0.6);
        shortMap.put("int", 0.8);
        shortMap.put("bigint", 0.6);
        shortMap.put("smallint", 1.0);
        shortMap.put("integer", 0.8);

        Map<String, Double> smallintMap = new HashMap<>();
        smallintMap.put("long", 0.6);
        smallintMap.put("int", 0.8);
        smallintMap.put("bigint", 0.6);
        smallintMap.put("short", 1.0);
        smallintMap.put("integer", 0.6);


        table.put("text", textMap);
        table.put("keyword", keywordMap);
        table.put("varchar", varcharMap);
        table.put("nvarchar", nvarcharMap);
        table.put("nchar", ncharMap);
        table.put("char", charMap);
        table.put("date", dateMap);
        table.put("double", doubleMap);
        table.put("decimal", decimalMap);
        table.put("int", intMap);
        table.put("integer", integerMap);
        table.put("bit", bitMap);
        table.put("time", timeMap);
        table.put("float", floatMap);
        table.put("long", longMap);
        table.put("bigint", bigintMap);
        table.put("short", shortMap);
        table.put("smallint", smallintMap);
    }


}
