/*
 * This file is part of ToolStats.
 *
 * ToolStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ToolStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToolStats.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.toolstats.tools;

import lol.hyper.toolstats.ToolStats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NumberFormat {

    private DecimalFormat DECIMAL_FORMAT;
    private DecimalFormat COMMA_FORMAT;
    private SimpleDateFormat DATE_FORMAT;

    /**
     * Utility class to format different numbers
     *
     * @param toolStats Plugin instance.
     */
    public NumberFormat(ToolStats toolStats) {

        String dateFormat = toolStats.config.getString("date-format");
        String decimalSeparator = toolStats.config.getString("number-formats.decimal-separator");
        String commaSeparator = toolStats.config.getString("number-formats.comma-separator");
        String commaFormat = toolStats.config.getString("number-formats.comma-format");
        String decimalFormat = toolStats.config.getString("number-formats.decimal-format");

        // if these config values are missing, use the default ones
        if (dateFormat == null) {
            dateFormat = "M/dd/yyyy";
            toolStats.logger.warning("date-format is missing! Using default American English format.");
        }

        if (decimalSeparator == null) {
            decimalSeparator = ".";
            toolStats.logger.warning("number-formats.decimal-separator is missing! Using default \".\" instead.");
        }

        if (commaSeparator == null) {
            commaSeparator = ",";
            toolStats.logger.warning("number-formats.comma-separator is missing! Using default \",\" instead.");
        }

        if (commaFormat == null) {
            commaFormat = "#,###";
            toolStats.logger.warning("number-formats.comma-format is missing! Using default #,### instead.");
        }

        if (decimalFormat == null) {
            decimalFormat = "#,##0.00";
            toolStats.logger.warning("number-formats.comma-separator is missing! Using default #,###.00 instead.");
        }

        // test the date format
        try {
            DATE_FORMAT = new SimpleDateFormat(dateFormat, Locale.getDefault());
        } catch (NullPointerException | IllegalArgumentException exception) {
            toolStats.logger.warning("date-format is NOT a valid format! Using default American English format.");
            exception.printStackTrace();
            DATE_FORMAT = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);
        }

        // set the separators
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
        formatSymbols.setGroupingSeparator(commaSeparator.charAt(0));


        // test the comma format
        try {
            COMMA_FORMAT = new DecimalFormat(commaFormat, formatSymbols);
        } catch (NullPointerException | IllegalArgumentException exception) {
            toolStats.logger.warning("number-formats.comma-format is NOT a valid format! Using default #,### instead.");
            exception.printStackTrace();
            COMMA_FORMAT = new DecimalFormat("#,###", formatSymbols);
        }

        // test the decimal format
        try {
            DECIMAL_FORMAT = new DecimalFormat(decimalFormat, formatSymbols);
        } catch (NullPointerException | IllegalArgumentException exception) {
            toolStats.logger.warning("number-formats.decimal-format is NOT a valid format! Using default #,###.00 instead.");
            exception.printStackTrace();
            DECIMAL_FORMAT = new DecimalFormat("#,###.00", formatSymbols);
        }
    }

    /**
     * Formats a number to make it pretty. Example: 4322 to 4,322
     *
     * @param number The number to format.
     * @return The formatted number.
     */
    public String formatInt(int number) {
        String finalNumber = COMMA_FORMAT.format(number);
        finalNumber = finalNumber.replaceAll("[\\x{202f}\\x{00A0}]", " ");
        return finalNumber;
    }

    /**
     * Formats a number to make it pretty. Example: 4322.33 to 4,322.33
     *
     * @param number The number to format.
     * @return The formatted number.
     */
    public String formatDouble(double number) {
        String finalNumber = DECIMAL_FORMAT.format(number);
        finalNumber = finalNumber.replaceAll("[\\x{202f}\\x{00A0}]", " ");
        return finalNumber;
    }

    /**
     * Formats a date into the readable format.
     *
     * @param date The date to format.
     * @return The date into a readable format.
     */
    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /**
     * Returns a human-readable form of time in milliseconds.
     * E.g. given 3752348000L outputs 1 year, 5 months, 3 days, 14 hours, 12 minutes, 28 seconds.
     *
     * @param time The time in ms.
     * @return Map with units as keys and time value, e.g. "years" (key) -> 1 (value)
     */
    public Map<String, String> formatTime(Long time) {
        final int SECONDS_PER_MINUTE = 60;
        final int MINUTES_PER_HOUR = 60;
        final int HOURS_PER_DAY = 24;
        final int DAYS_PER_MONTH = 30;  // Approximation
        final int DAYS_PER_YEAR = 365;  // Approximation

        long totalSeconds = time / 1000;

        Map<String, String> timeUnits = new HashMap<>();

        long years = totalSeconds / (DAYS_PER_YEAR * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
        if (years > 0) {
            timeUnits.put("years", Long.toString(years));
        }
        totalSeconds %= (DAYS_PER_YEAR * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);

        long months = totalSeconds / (DAYS_PER_MONTH * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
        if (months > 0) {
            timeUnits.put("months", Long.toString(months));
        }
        totalSeconds %= (DAYS_PER_MONTH * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);

        long days = totalSeconds / (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
        if (days > 0) {
            timeUnits.put("days", Long.toString(days));
        }
        totalSeconds %= (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);

        long hours = totalSeconds / (MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
        if (hours > 0) {
            timeUnits.put("hours", Long.toString(hours));
        }
        totalSeconds %= (MINUTES_PER_HOUR * SECONDS_PER_MINUTE);

        long minutes = totalSeconds / SECONDS_PER_MINUTE;
        if (minutes > 0) {
            timeUnits.put("minutes", Long.toString(minutes));
        }
        totalSeconds %= SECONDS_PER_MINUTE;

        long seconds = totalSeconds;
        if (seconds > 0 || timeUnits.isEmpty()) {  // Always include seconds if everything else is zero
            timeUnits.put("seconds", Long.toString(seconds));
        }

        return timeUnits;
    }

    public Date normalizeTime(Long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZoneId zone = ZoneId.systemDefault();

        LocalDate localDate = instant.atZone(zone).toLocalDate();
        ZonedDateTime midnight = localDate.atStartOfDay(zone);
        return Date.from(midnight.toInstant());
    }
}
