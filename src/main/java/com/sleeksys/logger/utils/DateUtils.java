package com.sleeksys.logger.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateUtils {

    private static final String DATE_FORMAT_PATTERN = "YYYY-MM-DD HH:MM:SS";

    public static Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat(DATE_FORMAT_PATTERN)
                    .parse(dateString);
        } catch (ParseException e) {
            log.error("Invalid date format. Expected format: {}", DATE_FORMAT_PATTERN);
        }
        return null;
    }

    /*** Formats current date as YYYY-MM-DD HH:MM:SS */
    public static String formatDate() {
        return formatDate(Calendar.getInstance());
    }

    /*** Formats a date as YYYY-MM-DD HH:MM:SS */
    public static String formatDate(Calendar date) {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1, // Calendar.MONTH is zero-based
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));
    }

    public static int compareDates(String dateString1, String dateString2) {
        Date date1 = parseDate(dateString1);
        Date date2 = parseDate(dateString2);

        // compare data by strings if parsing fails
        if (date1 == null || date2 == null) {
            return dateString1.compareTo(dateString2);
        }
        return date1.compareTo(date2);
    }
}
