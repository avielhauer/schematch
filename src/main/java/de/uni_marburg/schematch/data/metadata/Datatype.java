package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public enum Datatype {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
    DATE,
    TEXT, // long string (e.g., comments or descriptions) not implemented yet
    GEO_LOCATION;

    /**
     * Determines the data type of a given column
     *
     * @param column The column to determine the datatype of
     * @return The determined datatype
     */
    public static Datatype determineDatatype(Column column) {
        HashMap<Datatype, Double> scores = calculateScores(column);

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

        // all other detection function outputs can be directly compared to each other
        percentages.add(dateHelper);
        percentages.add(geoHelper);


        // return highest matching type detection
        // we chose 95% as a threshold because this value is often used in statistics
        percentages.sort(Helper::compareTo);
        if (percentages.get(0).percentage < 0.95) return STRING;
        else return percentages.get(0).type;
    }

    public static HashMap<Datatype, Double> calculateScores(Column column) {
        HashMap<Datatype, Double> scores = new HashMap<>();
        scores.put(INTEGER, isInteger(column));
        scores.put(FLOAT, isFloat(column));
        scores.put(BOOLEAN, isBoolean(column));
        scores.put(DATE, isDate(column));
        scores.put(GEO_LOCATION, isGeoLocation(column));

        return scores;
    }

    /**
     * Prints the matching percentages for all data types and the final chosen type
     *
     * @param column The column to determine the data type of
     */
    public static void printScores(Column column) {
        StringBuilder sb = new StringBuilder();
        HashMap<Datatype, Double> scores = calculateScores(column);

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
        sb.append("Final type:  ").append(determineDatatype(column)).append("\n");
        sb.append(String.format("%1$" + 24 + "s", "").replace(' ', '=')).append("\n");

        System.out.println(sb);
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

        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM");
        for (String value : values) {
            if (isNull(value)) {
                nullCounter++;
                continue;
            }
            if (value.contains("+")) value = value.substring(0, value.indexOf("+"));
            try {
                sdf.parse(value);
            } catch (ParseException e) {
                exceptionCounter++;
            }
        }

        final int fullSize = values.size();
        final int nonNullSize = fullSize - nullCounter;

        return 1 - (double) exceptionCounter / nonNullSize;
    }

    private static double isBoolean(Column column) {
        String[] p = {"1", "0", "true", "false", "t", "f", "yes", "no", "y", "n", "ja", "nein", "j"};
        List<String> patterns = new ArrayList<>(Arrays.stream(p).toList());
        List<String> values = column.getValues();

        int nullCounter = 0;
        int parseCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else if (patterns.contains(value)) parseCounter++;
        }

        return (double) parseCounter / (values.size() - nullCounter);
    }

    private static double isGeoLocation(Column column) {
        Pattern pattern = Pattern.compile("[0-9]+.[0-9]+,[0-9]+.[0-9]+");
        List<String> values = column.getValues();

        int nullCounter = 0;
        int parseCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else if (pattern.matcher(value).find()) parseCounter++;
        }

        return (double) parseCounter / (values.size() - nullCounter);
    }


    private static boolean isNull(String value) {
        return value.equals("\"\"") || value.isEmpty();
    }

}
