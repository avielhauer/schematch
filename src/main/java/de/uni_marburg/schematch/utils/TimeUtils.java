package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class TimeUtils {
    private final static Logger log = LogManager.getLogger(TimeUtils.class);

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        int minutesPart = duration.toMinutesPart();
        int secondsPart = duration.toSecondsPart();
        int millisecondsPart = duration.toMillisPart();

        return String.format("%02d:%02d:%02d.%03d", hours, minutesPart, secondsPart, millisecondsPart);
    }
}
