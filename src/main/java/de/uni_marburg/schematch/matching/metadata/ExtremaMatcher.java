package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.GeoLocation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtremaMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {

        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();

        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Datatype datatype_i = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Datatype datatype_j = targetTable.getColumn(j).getDatatype();

                if (datatype_i != datatype_j) {
                    simMatrix[i][j] = 0;
                    continue;
                }

                List<String> sourceColumn = sourceTable.getColumn(i).getValues();
                List<String> targetColumn = targetTable.getColumn(j).getValues();
                switch (datatype_i) {
                    case BOOLEAN -> simMatrix[i][j] = booleanExtrema(sourceColumn, targetColumn);
                    case INTEGER -> simMatrix[i][j] = integerExtrema(sourceColumn, targetColumn);
                    case FLOAT -> simMatrix[i][j] = floatExtrema(sourceColumn, targetColumn);
                    case DATE -> simMatrix[i][j] = dateExtrema(sourceColumn, targetColumn);
                    case GEO_LOCATION -> simMatrix[i][j] = geoExtrema(sourceColumn, targetColumn);
                    case STRING, TEXT -> simMatrix[i][j] = stringExtrema(sourceColumn, targetColumn);
                    default -> simMatrix[i][j] = 0;
                }
            }
        }

        return simMatrix;
    }

    private float booleanExtrema(List<String> sourceRaw, List<String> targetRaw) {
        List<Boolean> source = castToBoolean(sourceRaw);
        List<Boolean> target = castToBoolean(targetRaw);

        boolean sourceMax = false;
        boolean sourceMin = true;
        boolean targetMax = false;
        boolean targetMin = true;

        for (Boolean b : source) {
            if (b) sourceMax = true;
            if (!b) sourceMin = false;

            if (!sourceMin && sourceMax) {
                break;
            }
        }

        for (Boolean b : target) {
            if (b) targetMax = true;
            if (!b) targetMin = false;

            if (!targetMin && targetMax) {
                break;
            }
        }

        float result = 0;
        if (sourceMax == targetMax) result += 0.5f;
        if (sourceMin == targetMin) result += 0.5f;
        return result;
    }

    private ArrayList<Boolean> castToBoolean(List<String> input) {
        ArrayList<Boolean> list = new ArrayList<>();
        String[] t = {"1", "true", "t", "yes", "y", "ja", "j"};
        String[] f = {"0", "false", "f", "no", "n", "nein",};
        List<String> patternsT = new ArrayList<>(Arrays.stream(t).toList());
        List<String> patternsF = new ArrayList<>(Arrays.stream(f).toList());
        for (String s : input) {
            if (patternsT.contains(s)) list.add(true);
            else if (patternsF.contains(s)) list.add(false);
        }
        return list;
    }

    private float integerExtrema(List<String> sourceRaw, List<String> targetRaw) {
        int sourceMin = Integer.MAX_VALUE;
        int sourceMax = Integer.MIN_VALUE;
        int targetMin = Integer.MAX_VALUE;
        int targetMax = Integer.MIN_VALUE;

        List<Integer> source = castToInt(sourceRaw);
        List<Integer> target = castToInt(targetRaw);

        for (Integer s : source) {
            if (s > sourceMax) sourceMax = s;
            if (s < sourceMin) sourceMin = s;
        }

        for (Integer t : target) {
            if (t > targetMax) targetMax = t;
            if (t < targetMin) targetMin = t;
        }

        return calculateMatchPercentage(sourceMax, targetMax, sourceMin, targetMin);
    }

    private List<Integer> castToInt(List<String> input) {
        List<Integer> list = new ArrayList<>();
        for (String s : input) {
            try {
                list.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    private float floatExtrema(List<String> sourceRaw, List<String> targetRaw) {
        float sourceMin = Float.MAX_VALUE;
        float sourceMax = Float.MIN_VALUE;
        float targetMin = Float.MAX_VALUE;
        float targetMax = Float.MIN_VALUE;

        List<Float> source = castToFloat(sourceRaw);
        List<Float> target = castToFloat(targetRaw);

        for (Float s : source) {
            if (s > sourceMax) sourceMax = s;
            if (s < sourceMin) sourceMin = s;
        }

        for (Float t : target) {
            if (t > targetMax) targetMax = t;
            if (t < targetMin) targetMin = t;
        }

        return calculateMatchPercentage(sourceMax, targetMax, sourceMin, targetMin);
    }

    private List<Float> castToFloat(List<String> input) {
        List<Float> list = new ArrayList<>();
        for (String s : input) {
            try {
                list.add(Float.parseFloat(s.replace(",", ".")));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    private float dateExtrema(List<String> sourceRaw, List<String> targetRaw) {
        Date sourceMin = new Date(Long.MAX_VALUE);
        Date sourceMax = new Date(Long.MIN_VALUE);
        Date targetMin = new Date(Long.MAX_VALUE);
        Date targetMax = new Date(Long.MIN_VALUE);

        List<Date> source = castToDate(sourceRaw);
        List<Date> target = castToDate(targetRaw);

        for (Date s : source) {
            if (s.after(sourceMax)) sourceMax = s;
            if (s.before(sourceMin)) sourceMin = s;
        }

        for (Date t : target) {
            if (t.after(targetMax)) sourceMax = t;
            if (t.before(targetMin)) sourceMin = t;
        }

        return calculateMatchPercentage(sourceMax, targetMax, sourceMin, targetMin);
    }

    private List<Date> castToDate(List<String> input) {
        List<Date> dates = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM");
        for (String value : input) {
            if (value.equals("\"\"") || value.isEmpty()) {
                continue;
            }
            if (value.contains("+")) value = value.substring(0, value.indexOf("+"));
            try {
                dates.add(sdf.parse(value));
            } catch (ParseException ignored) {
            }
        }

        return dates;
    }

    private float geoExtrema(List<String> sourceRaw, List<String> targetRaw) {
        float sourceMin = Float.MAX_VALUE;
        float sourceMax = Float.MIN_VALUE;
        float targetMin = Float.MAX_VALUE;
        float targetMax = Float.MIN_VALUE;

        GeoLocation reference = new GeoLocation(0.0d, 0.0d);

        for (String coords : sourceRaw) {
            GeoLocation target = new GeoLocation(coords);
            float distance = (float) reference.calculateDistance(target);
            if (distance > sourceMax) sourceMax = distance;
            if (distance < sourceMin) sourceMin = distance;
        }
        for (String coords : targetRaw) {
            GeoLocation target = new GeoLocation(coords);
            float distance = (float) reference.calculateDistance(target);
            if (distance > targetMax) targetMax = distance;
            if (distance < targetMin) targetMin = distance;
        }

        return calculateMatchPercentage(sourceMax, targetMax, sourceMin, targetMin);
    }

    private float stringExtrema(List<String> sourceRaw, List<String> targetRaw) {
        int sourceMin = Integer.MAX_VALUE;
        int sourceMax = Integer.MIN_VALUE;
        int targetMin = Integer.MAX_VALUE;
        int targetMax = Integer.MIN_VALUE;

        for (String s : sourceRaw) {
            int length = s.length();
            if (length > sourceMax) sourceMax = length;
            if (length < sourceMin) sourceMin = length;
        }

        for (String t : targetRaw) {
            int length = t.length();
            if (length > targetMax) targetMax = length;
            if (length < targetMin) targetMin = length;
        }

        return calculateMatchPercentage(sourceMax, targetMax, sourceMin, targetMin);
    }


    /*
    Helper functions to determine the final similarity between two columns.
    We calculate the similarity by determining the maximum and minimum values of both columns combined (union)
    Afterward we divide the intersecting part of both columns by the integral of the result of union of the columns.
     */
    private float calculateMatchPercentage(int sourceMax, int targetMax, int sourceMin, int targetMin) {
        int max = Math.max(sourceMax, targetMax);
        int min = Math.min(sourceMin, targetMin);
        int maxDif = Math.abs(sourceMax - targetMax);
        int minDif = Math.abs(sourceMin - targetMin);
        int dif = maxDif + minDif;
        int full = max - min + 1;
        int common = full - dif;
        return (float) common / full;
    }

    private float calculateMatchPercentage(float sourceMax, float targetMax, float sourceMin, float targetMin) {
        float max = Math.max(sourceMax, targetMax);
        float min = Math.min(sourceMin, targetMin);
        float maxDif = Math.abs(sourceMax - targetMax);
        float minDif = Math.abs(sourceMin - targetMin);
        float dif = maxDif + minDif;
        float full = max - min + 1;
        float common = full - dif;
        return common / full;
    }

    private float calculateMatchPercentage(Date sourceMax, Date targetMax, Date sourceMin, Date targetMin) {
        long max = sourceMax.after(targetMax) ? sourceMax.getTime() : targetMax.getTime();
        long min = sourceMin.before(targetMin) ? sourceMin.getTime() : targetMin.getTime();
        long maxDif = sourceMax.getTime() - targetMax.getTime();
        long minDif = sourceMin.getTime() - targetMin.getTime();
        long dif = maxDif + minDif;
        long full = max - min + 1;
        long common = full - dif;
        return (float) common / full;
    }
}
