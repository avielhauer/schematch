package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        ArrayList<Helper> percentages = new ArrayList<>();

        Helper integerHelper = new Helper(isInteger(column), INTEGER);
        Helper floatHelper = new Helper(isFloat(column), FLOAT);
        Helper booleanHelper = new Helper(isBoolean(column), BOOLEAN);
        Helper dateHelper = new Helper(isDate(column), DATE);
        Helper geoHelper = new Helper(isGeoLocation(column), GEO_LOCATION);

        // Prefer boolean over int
        // columns containing only (0, 1) should rather be boolean than int
        if (booleanHelper.percentage >= integerHelper.percentage) {
            percentages.add(booleanHelper);
        } else {
            percentages.add(integerHelper);
        }

        // all other detection function outputs can be directly compared to each other
        percentages.add(floatHelper);
        percentages.add(dateHelper);
        percentages.add(geoHelper);


        // return highest matching type detection
        // we chose 95% as a threshold because this value is often used in statistics
        percentages.sort(Helper::compareTo);
        if (percentages.get(0).percentage < 0.95) return STRING;
        else return percentages.get(0).type;
    }

    /**
     * Prints the matching percentages for all data types and the final chosen type
     *
     * @param column The column to determine the data type of
     */
    public static void printMatching(Column column) {
        StringBuilder sb = new StringBuilder();

        String label = column.getLabel();
        String isInteger = String.valueOf(isInteger(column));
        String isFloat = String.valueOf(isFloat(column));
        String isBoolean = String.valueOf(isBoolean(column));
        String isDate = String.valueOf(isDate(column));
        String isGeoLocation = String.valueOf(isGeoLocation(column));

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
        int kommaCounter = 0;
        int endsWithPointOCounter = 0;

        for (String value : values) {
            if (isNull(value)) nullCounter++;
            else {
                value = value.replace(",", ".");
                if (value.contains(".")) kommaCounter++;
                if (value.endsWith(".0")) endsWithPointOCounter++;

                try {
                    Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    exceptionCounter++;
                }
            }
        }

        // if no kommas are present within the column then it is just an int
        // if all parsed values end with .0 it is likely formatting and also an int
        if (kommaCounter == 0 || kommaCounter == endsWithPointOCounter) {
            return 0.0;
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
