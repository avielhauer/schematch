package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.HashMap;
import java.util.Map;

public class CompatibilityTable {
    Map<String, Map<String, Double>> table;
    public CompatibilityTable() {
        this.table = new HashMap<>();
    }
}
