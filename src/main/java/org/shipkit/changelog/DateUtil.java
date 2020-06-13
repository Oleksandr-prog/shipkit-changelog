package org.shipkit.changelog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date and Time utilities
 */
class DateUtil {

    /**
     * Yesterday date from now
     */
    public static Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    /**
     * Formats the date to the format required by GitHub API,
     * ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ
     */
    public static String forGitHub(Date date) {
        return forGitHub(date, TimeZone.getDefault());
    }

    /**
     * See {@link #forGitHub(Date)}
     */
    public static String forGitHub(Date date, TimeZone tz) {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ");
        format.setTimeZone(tz);
        return format.format(date);
    }

    /**
     * Parses date in iso format, e.g. "yyyy-MM-dd HH:mm:ss Z"
     */
    public static Date parseDate(String date) {
        String pattern = "yyyy-MM-dd HH:mm:ss Z";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return parse(date, pattern, format);
    }

    private static Date parse(String date, String pattern, SimpleDateFormat format) {
        try {
            return format.parse(date.trim());
        } catch (ParseException e) {
            throw new RuntimeException("Problems parsing date: [" + date + "]. Required format is: [" + pattern + "].", e);
        }
    }

    /**
     * Parses date in simple format, UTC, e.g. "yyyy-MM-dd"
     */
    public static Date parseUTCDate(String date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return parse(date, pattern, f);
    }

    /**
     * Formats date to most reasonable format to show on the release notes
     */
    public static String formatDate(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(date);
    }

    /**
     * Parse Date in epoch seconds (Unix time).
     */
    public static Date parseDateInEpochSeconds(String date) {
        return new Date(Long.parseLong(date) * 1000);
    }

    /**
     * Formats date to local timezone to shows in debug logs
     */
    public static String formatDateToLocalTime(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm a z");
        f.setTimeZone(TimeZone.getDefault());
        return f.format(date);
    }
}
