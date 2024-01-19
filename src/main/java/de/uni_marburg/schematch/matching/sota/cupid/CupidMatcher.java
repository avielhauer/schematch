package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.*;

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
        Pair<SchemaTree,SchemaTree> treePair= buildTreesFromTables(tablePair);

        Map<String,Map<StringPair,Float>> sims = treeMatch(
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

        Map<String,Map<StringPair,Float>> newSims = recomputewsim(treePair.getFirst(),treePair.getSecond(), sims, w_struct,th_accept);

        float[][] symMatrix = mapSimilarityMatrix(treePair.getFirst(),treePair.getSecond(), newSims, tablePair.getEmptySimMatrix());

        return symMatrix;
    }

    private float[][] mapSimilarityMatrix(SchemaTree first, SchemaTree second, Map<String, Map<StringPair, Float>> newSims, float[][] emptySimMatrix) {
        //Todo: implement mapSimilarityMatrix
        return emptySimMatrix;
    }

    private Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree first, SchemaTree second, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept) {
        //TODO: implement recomputewsim
        return sims;
    }

    // Gibt eine Map zurück mit 3 Schlüsseln "ssim","lsim" und "wsim"
    // mit denen auf 3 weitere Maps zugegriffen werden kann, welche
    // die Namenspaare der beiden bäume als schlüssel haben.
    private Map<String,Map<StringPair,Float>> treeMatch(
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
        Map<String, Map<String, Double>> compatibilityTable = new LinguisticMatching().computeCompatibility((List<String>) categories);
        Map<StringPair,Double> lSims = new LinguisticMatching().comparison(sourceTree,targetTree,compatibilityTable,thNs,parallelism);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String,Map<StringPair,Float>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            for (SchemaElementNode t : tPostOrder) {
                StringPair pair = new StringPair(s.name,t.name);
                if (s.height() == t.height()) {
                    float ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Double.isNaN(ssim)) {
                        continue;
                    }
                    if(!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair,Float.NaN);
                    }
                    float wsim = computeWeightedSimilarity(ssim,lSims.get(pair).floatValue(), wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("wsim").put(pair,wsim);
                }
                if(sims.get("wsim").containsKey(pair)) {
                    if (sims.get("wsim").get(pair) > thHigh) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(),t.leaves(),sims,cInc);
                    }
                    if (sims.get("wsim").get(pair) < thLow) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(),t.leaves(),sims,cDec);
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
        Map<String, Map<StringPair, Float>> sims = new HashMap<String, Map<StringPair, Float>>();
        Map<StringPair, Float> lsim = doubleDictToFloat(lsimsDouble);
        Map<StringPair, Float> ssim = new HashMap<StringPair, Float>();
        Map<StringPair, Float> wsim = new HashMap<StringPair, Float>();
        for (SchemaElementNode s: sLeaves) {
            for (SchemaElementNode t: tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name,t.name);
                    float ssimVal = compatibilityTable.get(s.name).get(t.name).floatValue();
                    ssim.put(pair,ssimVal);
                    wsim.put(pair, computeWeightedSimilarity(ssimVal,lsim.get(pair),leafWStruct));
                }
            }
        }
        sims.put("ssim",ssim);
        sims.put("lsim",lsim);
        sims.put("wsim",wsim);
        return sims;
    }

    private Map<StringPair, Float> doubleDictToFloat(Map<StringPair, Double> dict) {
        Map<StringPair, Float> newDict = new HashMap<>();
        for (StringPair pair: dict.keySet()) {
            newDict.put(pair, dict.get(pair).floatValue());
        }
        return newDict;
    }

    private float computeWeightedSimilarity(float ssim, float lsim, float wStruct) {
        return ssim * wStruct + (1-wStruct) * lsim;
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
                StringPair pair = new StringPair(sTable.getLabels().get(s),tTable.getLabels().get(t));
                float val = sims.get("wsim").get(pair);
                if (val > thAccept) {
                    simMatrix[s][t] = val;
                }
            }
        }
        return simMatrix;
    }

    public Pair<SchemaTree, SchemaTree> buildTreesFromTables(TablePair tablePair) {
        SchemaTree sourceTree = new SchemaTree(new SchemaElement("DB__"+tablePair.getSourceTable().getName(), "DB"));
        SchemaTree targetTree = new SchemaTree(new SchemaElement("DB__"+tablePair.getSourceTable().getName(), "DB"));

        sourceTree.addNode(tablePair.getSourceTable().getName(),sourceTree.getRoot(), new ArrayList<>(), new SchemaElement(tablePair.getSourceTable().getName(), "tableRoot"));
        targetTree.addNode(tablePair.getTargetTable().getName(),targetTree.getRoot(), new ArrayList<>(), new SchemaElement(tablePair.getTargetTable().getName(), "tableRoot"));

        for (Column column: tablePair.getSourceTable().getColumns()) {
            AddColumn(sourceTree, column);
        }

        for (Column column: tablePair.getTargetTable().getColumns()) {
            AddColumn(targetTree, column);
        }

        return new Pair<>(sourceTree, targetTree);
    }

    private void AddColumn(SchemaTree targetTree, Column column) {
        String datatype = convertDatatype(column.getDatatype());
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
}








