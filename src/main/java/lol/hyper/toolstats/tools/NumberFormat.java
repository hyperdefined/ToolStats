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
import java.util.Date;
import java.util.Locale;

public class NumberFormat {

    private DecimalFormat DECIMAL_FORMAT;
    private DecimalFormat COMMA_FORMAT;
    private SimpleDateFormat DATE_FORMAT;

    /**
     * Utility class to format different numbers
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
            decimalFormat = "#,###.00";
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
}
