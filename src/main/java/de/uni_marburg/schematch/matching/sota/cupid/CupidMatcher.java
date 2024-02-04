package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Implement Cupid Matcher
 */
public class CupidMatcher extends Matcher {


    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        float leaf_w_struct = 0.2f;
        float w_struct = 0.2f;
        float th_accept = 0.7f;
        float th_high = 0.6f;
        float th_low = 0.35f;
        float c_inc = 1.2f;
        float c_dec = 0.9f;
        float th_ns = 0.7f;
        int parallelism = 1;
        WordNetFunctionalities wnf = null;
        try {
            wnf = new WordNetFunctionalities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LinguisticMatching linguisticMatching = new LinguisticMatching(wnf);

        List<TablePair> tablePairs = matchTask.getTablePairs();

        float[][] simMatrix = matchTask.getEmptySimMatrix();
        List<Pair<Set<String>,SchemaTree>> trees = new ArrayList<>();

        for (TablePair tablePair : tablePairs) {
            Pair<Set<String>,SchemaTree> sourceTree = get(trees, tablePair.getSourceTable());
            Pair<Set<String>,SchemaTree> targetTree = get(trees, tablePair.getTargetTable());
            if (sourceTree == null) {
                sourceTree = buildTreeFromTable(tablePair.getSourceTable());
                trees.add(sourceTree);
            }
            if (targetTree == null) {
                targetTree = buildTreeFromTable(tablePair.getTargetTable());
                trees.add(targetTree);
            }
            Set<String> categories = new HashSet<String>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<StringPair, Float>> sims = treeMatch(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    categories,
                    leaf_w_struct,
                    w_struct,
                    th_accept,
                    th_high,
                    th_low,
                    c_inc,
                    c_dec,
                    th_ns,
                    parallelism,
                    linguisticMatching
            );

            Map<String, Map<StringPair, Float>> newSims = recomputewsim(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    sims,
                    w_struct,
                    th_accept,
                    linguisticMatching
            );


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(mapSimilarityMatrix(tablePair, newSims, th_accept), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }

    static Pair<Set<String>, SchemaTree> get(List<Pair<Set<String>, SchemaTree>> trees, Table table) {
        for (Pair<Set<String>, SchemaTree> pair: trees) {
            if (pair.getSecond().getHashCode() == table.hashCode()) {
                return pair;
            }
        }
        return null;
    }

    private float[][] mapSimilarityMatrix(TablePair tablePair, Map<String, Map<StringPair, Float>> sims, float th_accept) {
        float[][] symMatrix = tablePair.getEmptySimMatrix();

        List<Column> sourceColumns = tablePair.getSourceTable().getColumns();
        List<Column> targetColumns = tablePair.getTargetTable().getColumns();

        Map<StringPair, Float> wsims = sims.get("wsim");

        for (int i = 0; i < sourceColumns.size(); i++) {
            for (int j = 0; j < targetColumns.size(); j++) {
                StringPair p = new StringPair(sourceColumns.get(i).getLabel(), targetColumns.get(j).getLabel());
                float wsim = wsims.get(p);
                if (wsim >= th_accept) symMatrix[i][j] = wsim;
            }
        }

        return symMatrix;
    }

    private Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree sourceTree, SchemaTree targetTree, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept, LinguisticMatching linguisticMatching) {
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s: sPostOrder) {
            for (SchemaElementNode t: tPostOrder) {
                if (s.height() == t.height() && (s.height() > 0 && t.height() > 0)) {
                    float ssim = StructuralSimilarity.computeSSim(s,t,sims,thAccept);

                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    StringPair pair = new StringPair(s.name,t.name);
                    float lsim;
                    if (!sims.get("lsim").containsKey(pair)) {
                        lsim = (float) linguisticMatching.computeLsim(s.getCurrent(),t.getCurrent());
                    } else {
                        lsim = sims.get("lsim").get(pair);
                    }
                    float wsim = computeWeightedSimilarity(ssim,lsim,wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("lsim").put(pair,lsim);
                    sims.get("wsim").put(pair,wsim);
                }
            }
        }
        return sims;
    }

    // Gibt eine Map zurück mit 3 Schlüsseln "ssim","lsim" und "wsim"
    // mit denen auf 3 weitere Maps zugegriffen werden kann, welche
    // die Namenspaare der beiden bäume als schlüssel haben.
    private Map<String, Map<StringPair, Float>> treeMatch(
            SchemaTree sourceTree,
            SchemaTree targetTree,
            Set<String> categories,
            float leafWStruct,
            float wStruct,
            float thAccept,
            float thHigh,
            float thLow,
            float cInc,
            float cDec,
            float thNs,
            int parallelism,
            LinguisticMatching linguisticMatching
    ) {
        Map<String, Map<String, Double>> compatibilityTable = linguisticMatching.computeCompatibility(categories);
        Map<StringPair, Float> lSims = linguisticMatching.comparison(sourceTree, targetTree, compatibilityTable, thNs, parallelism);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String, Map<StringPair, Float>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            for (SchemaElementNode t : tPostOrder) {
                StringPair pair = new StringPair(s.name, t.name);
                if (s.height() == t.height()) {
                    float ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    if (!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair, Float.NaN);
                    }
                    float wsim = computeWeightedSimilarity(ssim, sims.get("lsim").get(pair), wStruct);
                    sims.get("ssim").put(pair, ssim);
                    sims.get("wsim").put(pair, wsim);
                }
                if (sims.get("wsim").containsKey(pair)) {
                    if (sims.get("wsim").get(pair) > thHigh) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(), t.leaves(), sims, cInc);
                    }
                    if (sims.get("wsim").get(pair) < thLow) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(), t.leaves(), sims, cDec);
                    }
                }
            }
        }
        return sims;
    }

    private Map<String, Map<StringPair, Float>> getSims(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<String, Double>> compatibilityTable,
            Map<StringPair, Float> lsims,
            float leafWStruct) {
        Map<String, Map<StringPair, Float>> sims = new HashMap<>();
        Map<StringPair, Float> lsim = lsims;
        Map<StringPair, Float> ssim = new HashMap<>();
        Map<StringPair, Float> wsim = new HashMap<>();
        for (SchemaElementNode s : sLeaves) {
            for (SchemaElementNode t : tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name, t.name);
                    float ssimVal = compatibilityTable.get(s.current.getDataType()).get(s.current.getDataType()).floatValue();
                    ssim.put(pair, ssimVal);
                    float lsimValue = 0f;
                    if (lsim.get(pair) != null) lsimValue = lsim.get(pair);
                    float wsimValue = computeWeightedSimilarity(ssimVal, lsimValue, leafWStruct);
                    wsim.put(pair, wsimValue);
                }
            }
        }
        sims.put("ssim", ssim);
        sims.put("lsim", lsim);
        sims.put("wsim", wsim);
        return sims;
    }

    private Map<StringPair, Float> doubleDictToFloat(Map<StringPair, Double> dict) {

        Map<StringPair, Float> newDict = new HashMap<>();
        for (StringPair pair : dict.keySet()) {
            newDict.put(pair, dict.get(pair).floatValue());
        }
        return newDict;
    }

    private float computeWeightedSimilarity(float ssim, float lsim, float wStruct) {
        return ssim * wStruct + (1 - wStruct) * lsim;
    }

    static Pair<Set<String>,SchemaTree> buildTreeFromTable(Table table) {
        HashSet<String> categories = new HashSet<>();

        SchemaTree tree = new SchemaTree(new SchemaElement("DB__" + table.getName(), "DB"), table.hashCode());

        tree.addNode(table.getName(), tree.getRoot(), new ArrayList<>(), new SchemaElement(table.getName(), "tableRoot"));

        for (Column column : table.getColumns()) {
            categories.add(addColumn(tree, column));
        }

        return new Pair<>(categories, tree);
    }

    private static String addColumn(SchemaTree targetTree, Column column) {
        String datatype = convertDatatype(column);
        SchemaElement schemtmp = new SchemaElement(column.getLabel(), datatype);
        schemtmp.addCategory(datatype);
        targetTree.addNode(column.getLabel(), targetTree.getRoot().getChildren().get(0), new ArrayList<>(), schemtmp);
        return datatype;
    }

    private static String convertDatatype(Datatype datatype) {
        switch (datatype) {
            case DATE -> {
                return "date";
            }
            case INTEGER -> {
                return "integer";
            }
            case FLOAT -> {
                return "float";
            }
            case BOOLEAN -> {
                return "bit";
            }
            case GEO_LOCATION -> {
                //Todo: Extend DataCompatibilityTable with Geo Location
                return "string";
            }
            default -> {
                return "string";
            }
        }
    }

    private static String convertDatatype(Column column) {
        Datatype schematchType = column.getDatatype();
        List<String> values = column.getValues();

        if (values.isEmpty())
            return convertDatatype(column.getDatatype());

        switch (schematchType) {
            case DATE -> {
                return "date";
            }
            case INTEGER -> {
                boolean isShort = true;
                boolean isInt = true;
                boolean isLong = true;

                for (String item : values) {
                    try {
                        BigInteger big = new BigInteger(item);
                        try {
                            short shortVal = Short.parseShort(item);
                        } catch (NumberFormatException e) {
                            isShort = false;
                            try {
                                int intVal = Integer.parseInt(item);
                            } catch (NumberFormatException e1) {
                                isInt = false;
                                try {
                                    long longVal = Long.parseLong(item);
                                } catch (NumberFormatException e2) {
                                    isLong = false;
                                    break;
                                }
                            }
                        }
                    } catch (NumberFormatException e4) {
                        continue;
                    }
                }

                if (isShort) {
                    return "short";
                } else if (isInt) {
                    return "int";
                } else if (isLong) {
                    return "long";
                } else {
                    return "bigint";
                }
            }
            case FLOAT -> {
                try {
                    for (String s : values) {
                        if (s.contains("e") || s.contains("E")) {
                            return "float";
                        }

                        String[] parts = s.split("\\.");
                        int decimalLength = parts[1].length();

                        if (decimalLength <= 7) {
                            return "float";
                        }
                    }
                } catch (Exception e) {
                    return "double";
                }
                return "double";
            }
            case BOOLEAN -> {
                return "bit";
            }
            case GEO_LOCATION -> {
                //Todo: Extend DataCompatibilityTable with Geo Location
                return "text";
            }
            case STRING,TEXT -> {
                AtomicInteger size = new AtomicInteger();
                AtomicBoolean isChar = new AtomicBoolean(true);
                AtomicInteger maxLength = new AtomicInteger();
                AtomicInteger maxCount = new AtomicInteger();
                values.forEach(s -> {
                    size.addAndGet(s.length());
                    if (s.length() > 1) {
                        isChar.set(false);
                    }
                    if (s.length() == maxLength.get()) {
                        maxCount.addAndGet(1);
                    }
                    else if (s.length() > maxLength.get()) {
                        maxLength.set(s.length());
                        maxCount.set(0);
                    }
                });
                if (isChar.get()) {
                    return "char";
                }
                if (size.get() % values.size() == 0) {
                    return "nchar";
                }
                if (maxCount.get() > values.size() / 2) {
                    return "nvarchar";
                }
                return "text";
            }
            default -> {
                return "text";
            }
        }
    }

    /*
    public static void main(String[] args) {
        CupidMatcher cm = new CupidMatcher();
        ArrayList<String> t1Labels = new ArrayList<>();
        t1Labels.add("Vorname");
        t1Labels.add("Name");
        ArrayList<Column> t1Columns = new ArrayList<>();
        ArrayList<String> t1Vornamen = new ArrayList<>();
        t1Vornamen.add("Peter");
        t1Vornamen.add("Klaus");
        t1Columns.add(0, new Column("Vorname",t1Vornamen));
        ArrayList<String> t1Namen = new ArrayList<>();
        t1Namen.add("Peters");
        t1Namen.add("Klausus");
        t1Columns.add(1,new Column("Name",t1Namen));
        Table t1 = new Table("Author", t1Labels, t1Columns,"");
        Table t2 = new Table("Author", (List<String>) t1Labels.clone(), (List<Column>) t1Columns.clone(),"");
        TablePair tp = new TablePair(t1,t2);
        float[][] res = cm.match(tp);
        System.out.print("test");
    }
     */
}