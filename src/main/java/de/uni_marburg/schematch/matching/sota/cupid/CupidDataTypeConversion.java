package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.Datatype;

import java.math.BigInteger;
import java.util.List;

public class CupidDataTypeConversion {
    /**
     * Converts Schematch datatype to the closest matching cupid data type as a string
     * @param datatype Schematch Datatype
     * @return cupid data type (String)
     */
    public static String convertDatatype(Datatype datatype) {
        switch (datatype) {
            case DATE -> {
                return "date";
            }
            case INTEGER -> {
                return "int";
            }
            case FLOAT -> {
                return "float";
            }
            case BOOLEAN -> {
                return "bit";
            }
            case GEO_LOCATION -> {
                return "geolocation";
            }
            default -> {
                return "string";
            }
        }
    }

    /**
     * Expanded datatype conversion, which uses the content of the column to further specify the datatype
     * @param column column from which the datatype should be derived from
     * @return cupid data type (String)
     */
    public static String convertDatatype(Column column, boolean use_simple_data_types) {
        Datatype schematchType = column.getDatatype();

        if (use_simple_data_types)
            return convertDatatype(schematchType);

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
                    if (item.isEmpty() || item.isBlank()) continue;
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
                for (String s : values) {
                    if (s.isEmpty() || s.isBlank()) continue;
                    try {
                        float floatValue = Float.parseFloat(s);
                        double doubleValue = Double.parseDouble(s);

                        if (floatValue != doubleValue) {
                            return "double";
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                return "float";
            }
            case BOOLEAN -> {
                return "bit";
            }
            case GEO_LOCATION -> {
                return "geolocation";
            }
            case STRING,TEXT -> {
                int fixedLength = -1;
                boolean hasVariableLength = false;
                boolean containsUnicode = false;
                int maxLength = values.get(0).length();
                for (String value: values) {
                    if (value.isEmpty() || value.isBlank()) continue;
                    if (fixedLength == -1) {
                        fixedLength = value.length();
                    } else if (fixedLength != value.length()) {
                        hasVariableLength = true;
                    }
                    if (maxLength < value.length()) {
                        maxLength = value.length();
                    }
                    if (!containsUnicode && !isAscii(value)) {
                        containsUnicode = true;
                    }
                }
                if (containsUnicode) {
                    if (!hasVariableLength) {
                        return "nchar";
                    } else {
                        return "nvarchar";
                    }
                } else {
                    if (!hasVariableLength) {
                        if (fixedLength == 1) {
                            return "char";
                        } else {
                            return "text";
                        }
                    } else {
                        return "text";
                    }
                }
            }
            default -> {
                return "text";
            }
        }
    }

    /**
     * Checks if the string contains an ascii value
     * @param value String
     * @return true if string contains ascii and false if it contains no ascii values
     */
    private static boolean isAscii(String value) {
        return value.chars().allMatch(c -> c < 128);
    }
}
