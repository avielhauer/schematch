package de.uni_marburg.schematch.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;

public class StringUtils {
    private final static Logger log = LogManager.getLogger(StringUtils.class);

    public static String truncateExact(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(s);
            sb.append(" ".repeat(length - s.length()));
            return sb.toString();
        }
    }

    public static String truncateExact(float f, int length) {
        return truncateExact(Float.toString(f), length);
    }

    public static String getFileName(File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    public static String getFolderName(String path) {
        return path.substring(1 + path.lastIndexOf(File.separator));
    }

    public static String dateToString(Date date) {
        return DateFormatUtils.format(date, Configuration.TIMESTAMP_PATTERN);
    }
}
