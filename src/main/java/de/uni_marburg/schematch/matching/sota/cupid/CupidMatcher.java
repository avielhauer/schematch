package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Implement Cupid Matcher
 */
public class CupidMatcher extends TablePairMatcher {
    private HashSet<String> categories = new HashSet<>();

    //Standard Config:
    private final float leaf_w_struct = 0.2f;
    private final float w_struct = 0.2f;
    private final float th_accept = 0.7f;
    private final float th_high = 0.6f;
    private final float th_low = 0.35f;
    private final float c_inc = 1.2f;
    private final float c_dec = 0.9f;
    private final float th_ns = 0.7f;
    private final int parallelism = 1;


    @Override
    public float[][] match(TablePair tablePair) {
        Pair<SchemaTree, SchemaTree> treePair = buildTreesFromTables(tablePair);

        Map<String, Map<StringPair, Float>> sims = treeMatch(
                treePair.getFirst(),
                treePair.getSecond(),
                categories,
                leaf_w_struct,
                w_struct,
                th_accept,
                th_high,
                th_low,
                c_inc,
                c_dec,
                th_ns,
                parallelism
        );

        Map<String, Map<StringPair, Float>> newSims = recomputewsim(treePair.getFirst(), treePair.getSecond(), sims, w_struct, th_accept);

        return mapSimilarityMatrix(tablePair, newSims);
    }

    private float[][] mapSimilarityMatrix(TablePair tablePair, Map<String, Map<StringPair, Float>> sims) {
        float[][] symMatrix = tablePair.getEmptySimMatrix();

        List<Column> sourceColumns = tablePair.getSourceTable().getColumns();
        List<Column> targetColumns = tablePair.getTargetTable().getColumns();

        for (int i = 0; i < sourceColumns.size(); i++) {
            for (int j = 0; j < targetColumns.size(); j++) {
                StringPair p = new StringPair(sourceColumns.get(i).getLabel(), targetColumns.get(j).getLabel());
                symMatrix[i][j] = sims.get("wsim").get(p);
            }
        }

        return symMatrix;
    }

    private Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree sourceTree, SchemaTree targetTree, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept) {
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
                        lsim = (float) new LinguisticMatching().computeLsim(s.getCurrent(),t.getCurrent());
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
            HashSet<String> categories,
            float leafWStruct,
            float wStruct,
            float thAccept,
            float thHigh,
            float thLow,
            float cInc,
            float cDec,
            float thNs,
            int parallelism
    ) {
        Map<String, Map<String, Double>> compatibilityTable = new LinguisticMatching().computeCompatibility(categories);
        Map<StringPair, Double> lSims = new LinguisticMatching().comparison(sourceTree, targetTree, compatibilityTable, thNs, parallelism);
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
                    if (Double.isNaN(ssim)) {
                        continue;
                    }
                    if (!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair, Float.NaN);
                    }
                    float wsim = computeWeightedSimilarity(ssim, lSims.get(pair).floatValue(), wStruct);
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
            Map<StringPair, Double> lsimsDouble,
            float leafWStruct) {
        Map<String, Map<StringPair, Float>> sims = new HashMap<>();
        Map<StringPair, Float> lsim = doubleDictToFloat(lsimsDouble);
        Map<StringPair, Float> ssim = new HashMap<>();
        Map<StringPair, Float> wsim = new HashMap<>();
        for (SchemaElementNode s : sLeaves) {
            for (SchemaElementNode t : tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name, t.name);
                    float ssimVal = compatibilityTable.get(s.name).get(t.name).floatValue();
                    ssim.put(pair, ssimVal);
                    wsim.put(pair, computeWeightedSimilarity(ssimVal, lsim.get(pair), leafWStruct));
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

    public static float[][] mappingGenerationLeaves(
            TablePair tablePair,
            Map<String, Map<StringPair, Float>> sims,
            float thAccept
    ) {
        Table sTable = tablePair.getSourceTable();
        Table tTable = tablePair.getTargetTable();

        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int s = 0; s < simMatrix.length; s++) {
            for (int t = 0; t < simMatrix[0].length; t++) {
                StringPair pair = new StringPair(sTable.getLabels().get(s), tTable.getLabels().get(t));
                float val = sims.get("wsim").get(pair);
                if (val > thAccept) {
                    simMatrix[s][t] = val;
                }
            }
        }
        return simMatrix;
    }

    public Pair<SchemaTree, SchemaTree> buildTreesFromTables(TablePair tablePair) {
        SchemaTree sourceTree = new SchemaTree(new SchemaElement("DB__" + tablePair.getSourceTable().getName(), "DB"));
        SchemaTree targetTree = new SchemaTree(new SchemaElement("DB__" + tablePair.getSourceTable().getName(), "DB"));

        sourceTree.addNode(tablePair.getSourceTable().getName(), sourceTree.getRoot(), new ArrayList<>(), new SchemaElement(tablePair.getSourceTable().getName(), "tableRoot"));
        targetTree.addNode(tablePair.getTargetTable().getName(), targetTree.getRoot(), new ArrayList<>(), new SchemaElement(tablePair.getTargetTable().getName(), "tableRoot"));

        for (Column column : tablePair.getSourceTable().getColumns()) {
            AddColumn(sourceTree, column);
        }

        for (Column column : tablePair.getTargetTable().getColumns()) {
            AddColumn(targetTree, column);
        }

        return new Pair<>(sourceTree, targetTree);
    }

    private void AddColumn(SchemaTree targetTree, Column column) {
        String datatype = convertDatatype(column);
        categories.add(datatype);
        SchemaElement schemtmp = new SchemaElement(column.getLabel(), datatype);
        schemtmp.addCategory(datatype);
        targetTree.addNode(column.getLabel(), targetTree.getRoot().getChildren().get(0), new ArrayList<>(), schemtmp);
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

    private String convertDatatype(Column column) {
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
                for (String s: values) {
                    if (s.contains("e") || s.contains("E"))
                    {
                        return "float";
                    }

                    String[] parts = s.split("\\.");
                    int decimalLength = parts[1].length();

                    if (decimalLength <= 7)
                    {
                        return "float";
                    }
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
}