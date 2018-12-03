package com.eitraz.dashboard.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateUtils {
    public static final Locale SWEDEN = new Locale.Builder().setLanguage("sv").setRegion("se").build();
    public static final ZoneId EUROPE_STOCKHOLM = ZoneId.of("Europe/Stockholm");

    private DateUtils() {
    }

    public static String getDay(LocalDate date) {
        return getDay(date, false);
    }

    public static String getDay(LocalDate date, boolean useNameExcepToday) {
        return getDay(date, useNameExcepToday, "EEEE d/LLL");
    }

    public static String getDay(LocalDate date, boolean useDayNameExceptToday, String fallbackFormat) {
        LocalDate now = LocalDate.now();

        // Today
        if (now.isEqual(date)) {
            return "Idag";
        }
        // Tomorrow
        else if (!useDayNameExceptToday && now.isEqual(date.minusDays(1))) {
            return "Imorgon";
        }
        // Day after tomorrow
        else if (!useDayNameExceptToday && now.isEqual(date.minusDays(2))) {
            return "I övermorgon";
        }
        // Date
        else {
            return StringUtils.capitalize(date.format(DateTimeFormatter.ofPattern(fallbackFormat, SWEDEN)));
        }
    }

    public static String getTime(LocalDateTime start, LocalDateTime end) {
        String startTime = start.format(DateTimeFormatter.ofPattern("HH:mm", SWEDEN));
        String endTime = end != null ? end.format(DateTimeFormatter.ofPattern("HH:mm", SWEDEN)) : null;

        return startTime + ((endTime != null) ? (" - " + endTime) : "");
    }
}
