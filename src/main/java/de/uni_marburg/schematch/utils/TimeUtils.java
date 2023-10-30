package de.uni_marburg.schematch.utils;

import java.time.Duration;

public class TimeUtils {
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        int minutesPart = duration.toMinutesPart();
        int secondsPart = duration.toSecondsPart();
        int millisecondsPart = duration.toMillisPart();

        return String.format("%02d:%02d:%02d.%03d", hours, minutesPart, secondsPart, millisecondsPart);
    }
}
