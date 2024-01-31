package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum Datatype {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
    DATE,
    TEXT, // long string (e.g., comments or descriptions) not implemented yet
    GEO_LOCATION;

    // boolean values
    final static String[] t = {"1", "true", "t", "yes", "y", "ja", "j"};
    final static String[] f = {"0", "false", "f", "no", "n", "nein"};
    final static List<String> patternsT = Arrays.stream(t).toList();
    final static List<String> patternsF = Arrays.stream(f).toList();
    final static List<String> booleanPatterns = Stream.concat(patternsT.stream(), patternsF.stream()).toList();
    // geolocation pattern
    final static Pattern geoLocationPattern = Pattern.compile("-?[0-9]+.[0-9]+,-?[0-9]+.[0-9]+");
    // date formats
    final static SimpleDateFormat sdfDashes = new SimpleDateFormat("dd-MM");
    final static SimpleDateFormat sdfSlashes = new SimpleDateFormat("dd/MM");
    final static SimpleDateFormat sdfDots = new SimpleDateFormat("dd.MM");
    final static SimpleDateFormat[] sdfs = {sdfDashes, sdfSlashes, sdfDots};

    /**
     * Determines the definitive data type from a list of scores
     *
     * @param scores A vector of scores for a certain column
     * @return The determined datatype
     */
    public static Datatype determineDatatype(HashMap<Datatype, Double> scores) {

        ArrayList<Helper> percentages = new ArrayList<>();
        Helper integerHelper = new Helper(scores.get(INTEGER), INTEGER);
        Helper floatHelper = new Helper(scores.get(FLOAT), FLOAT);
        Helper booleanHelper = new Helper(scores.get(BOOLEAN), BOOLEAN);
        Helper dateHelper = new Helper(scores.get(DATE), DATE);
        Helper geoHelper = new Helper(scores.get(GEO_LOCATION), GEO_LOCATION);

        // Prefer boolean to int
        // columns containing only (0, 1) should rather be boolean than int
        if (booleanHelper.percentage >= integerHelper.percentage) {
            percentages.add(booleanHelper);
        } else {
            percentages.add(integerHelper);
        }
        // Prefer int to float
        // If column only contains integers then floats can be ignored
        if (integerHelper.percentage >= floatHelper.percentage) {
            percentages.add(integerHelper);
        } else {
            percentages.add(floatHelper);
        }
        // Prefer geolocation to date
        if (geoHelper.percentage >= dateHelper.percentage) {
            percentages.add(geoHelper);
        } else {
            percentages.add(dateHelper);
        }

        // leave out string as everything can be a string

        // return highest matching type detection
        // we chose 95% as a threshold because this value is often used in statistics
        percentages.sort(Helper::compareTo);
        if (percentages.get(0).percentage < 0.95) return STRING;
        else return percentages.get(0).type;
    }

    /**
     * Calculates the scores for all data types for the given column
     * Score of 0.0 means that the column is not of the given Datatype
     * Score of 1.0 means that the column can be interpreted as the given Datatype
     *
     * @param column the column to calculate the scores for
     * @return a HashMap mapping the datatype to its score
     */
    public static HashMap<Datatype, Double> calculateScores(Column column) {
        HashMap<Datatype, Double> scores = new HashMap<>();
        scores.put(INTEGER, isInteger(column));
        scores.put(FLOAT, isFloat(column));
        scores.put(BOOLEAN, isBoolean(column));
        scores.put(DATE, isDate(column));
        scores.put(GEO_LOCATION, isGeoLocation(column));
        scores.put(STRING, 1.0d);

        return scores;
    }

    /**
     * Prints the matching percentages for all data types and the final chosen type
     *
     * @param column The column to determine the data type of
     */
    public static void printScores(Column column, HashMap<Datatype, Double> scores) {
        StringBuilder sb = new StringBuilder();

        String label = column.getLabel();
        String isInteger = String.valueOf(scores.get(INTEGER));
        String isFloat = String.valueOf(scores.get(FLOAT));
        String isBoolean = String.valueOf(scores.get(BOOLEAN));
        String isDate = String.valueOf(scores.get(DATE));
        String isGeoLocation = String.valueOf(scores.get(GEO_LOCATION));

        isInteger = isInteger.substring(0, Math.min(5, isInteger.length()));
        isFloat = isFloat.substring(0, Math.min(5, isFloat.length()));
        isBoolean = isBoolean.substring(0, Math.min(5, isBoolean.length()));
        isDate = isDate.substring(0, Math.min(5, isDate.length()));
        isGeoLocation = isGeoLocation.substring(0, Math.min(5, isGeoLocation.length()));

        int headerLength = 24 - label.length();
        String padding = String.format("%1$" + ((headerLength / 2) - 1) + "s", "").replace(' ', '=');
        sb.append(padding).append(' ').append(label).append(' ').append(padding);
        if (headerLength % 2 == 1) {
            sb.append("=");
        }
        sb.append("\n");
        sb.append("Int:         ").append(isInteger).append("\n");
        sb.append("Float:       ").append(isFloat).append("\n");
        sb.append("Boolean:     ").append(isBoolean).append("\n");
        sb.append("Date:        ").append(isDate).append("\n");
        sb.append("GeoLocation: ").append(isGeoLocation).append("\n");
        sb.append("Final type:  ").append(determineDatatype(scores)).append("\n");
        sb.append(String.format("%1$" + 24 + "s", "").replace(' ', '=')).append("\n");

        System.out.println(sb);
    }

    public static List<Boolean> castToBoolean(Column column) {
        List<String> input = column.getValues();
        ArrayList<Boolean> list = new ArrayList<>();
        for (String s : input) {
            if (patternsT.contains(s)) list.add(true);
            else if (patternsF.contains(s)) list.add(false);
            else list.add(null);
        }
        return list;
    }

    public static List<Integer> castToInt(Column input) {
        List<Integer> list = new ArrayList<>();
        for (String s : input.getValues()) {
            try {
                list.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                list.add(null);
            }
        }
        return list;
    }

    public static List<Float> castToFloat(Column input) {
        List<Float> list = new ArrayList<>();
        for (String s : input.getValues()) {
            try {
                list.add(Float.parseFloat(s.replace(",", ".")));
            } catch (NumberFormatException ignored) {
                list.add(null);
            }
        }
        return list;
    }

    public static List<Date> castToDate(Column input) {
        List<Date> dates = new ArrayList<>();
        for (String value : input.getValues()) {
            if (value.equals("\"\"") || value.isEmpty()) {
                continue;
            }
            if (value.contains("+")) value = value.substring(0, value.indexOf("+"));
            boolean isDate = false;
            for (SimpleDateFormat sdf : sdfs) {
                try {
                    dates.add(sdf.parse(value));
                    isDate = true;
                    break;
                } catch (ParseException ignored) {
                }
            }
            if (!isDate) {
                dates.add(null);
            }
        }

        return dates;
    }


    private static class Helper implements Comparable<Helper> {
        double percentage;
        Datatype type;

        private Helper(double percentage, Datatype type) {
            this.percentage = percentage;
            this.type = type;
        }

        @Override
        public int compareTo(Helper h) {
            return Double.compare(h.percentage, this.percentage); //descending
        }
    }

    private static double isInteger(Column column) {
        List<String> values = column.getValues();

        int nullCounter = 0;
        int exceptionCounter = 0;

        for (String value : values) {
            if (isNull(value)) {
                nullCounter++;
                continue;
            }
            if (value.endsWith(".0")) value = value.replace(".0", "");
            //TODO check for hexadecimal
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                exceptionCounter++;
            }

        }

        int fullSize = values.size();
        int nonNullSize = fullSize - nullCounter;

        return 1 - ((double) exceptionCounter / nonNullSize);
    }

    private static double isFloat(Column column) {
        List<String> values = column.getValues();

        int nullCounter = 0;
        int exceptionCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else {
                value = value.replace(",", ".");
                try {
                    Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    exceptionCounter++;
                }
            }
        }

        int fullSize = values.size();
        int nonNullSize = fullSize - nullCounter;

        return 1 - ((double) exceptionCounter / nonNullSize);
    }

    private static double isDate(Column column) {
        List<String> values = column.getValues();

        int nullCounter = 0;
        int exceptionCounter = 0;

        for (String value : values) {
            if (isNull(value)) {
                nullCounter++;
                continue;
            }
            if (value.contains("+")) value = value.substring(0, value.indexOf("+"));
            boolean isDate = false;
            for (SimpleDateFormat sdf : sdfs) {
                try {
                    sdf.parse(value);
                    isDate = true;
                    break;
                } catch (ParseException ignored) {
                }
            }
            if (!isDate) {
                exceptionCounter++;
            }
        }

        final int fullSize = values.size();
        final int nonNullSize = fullSize - nullCounter;

        return 1 - (double) exceptionCounter / nonNullSize;
    }

    private static double isBoolean(Column column) {
        List<String> values = column.getValues();

        int nullCounter = 0;
        int parseCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else if (booleanPatterns.contains(value)) parseCounter++;
        }

        return (double) parseCounter / (values.size() - nullCounter);
    }

    private static double isGeoLocation(Column column) {
        List<String> values = column.getValues();

        int nullCounter = 0;
        int parseCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else if (geoLocationPattern.matcher(value).find()) parseCounter++;
        }

        return (double) parseCounter / (values.size() - nullCounter);
    }

    private static boolean isNull(String value) {
        return value.equals("\"\"") || value.isEmpty();
    }

}
