package shan.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import shan.exception.InvalidArgumentException;

class DateTimeParserTest {
    private static final String INVALID_DATE_TIME_MESSAGE =
            "Use a valid date and time in yyyy-MM-dd HH:mm or d/M/yyyy HHmm format.";

    @Test
    void parse_isoDateTime_returnsParsedDateTime() throws InvalidArgumentException {
        LocalDateTime result = DateTimeParser.parse("2019-12-02 18:00");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), result);
    }

    @Test
    void parse_dayFirstDateWithCompactTime_returnsParsedDateTime()
            throws InvalidArgumentException {
        LocalDateTime result = DateTimeParser.parse("2/12/2019 1800");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), result);
    }

    @Test
    void parse_validLeapDay_returnsParsedDateTime() throws InvalidArgumentException {
        LocalDateTime result = DateTimeParser.parse("2020-02-29 09:30");

        assertEquals(LocalDateTime.of(2020, 2, 29, 9, 30), result);
    }

    @Test
    void parse_invalidCalendarDate_exceptionThrown() {
        assertInvalidDateTime("2019-02-29 09:30");
    }

    @Test
    void parse_invalidTime_exceptionThrown() {
        assertInvalidDateTime("2019-12-02 24:00");
    }

    @Test
    void parse_unsupportedFormat_exceptionThrown() {
        assertInvalidDateTime("12-02-2019 18:00");
    }

    @Test
    void parse_blankInput_exceptionThrown() {
        assertInvalidDateTime("");
    }

    @Test
    void parse_inputWithSurroundingWhitespace_exceptionThrown() {
        assertInvalidDateTime(" 2019-12-02 18:00 ");
    }

    private void assertInvalidDateTime(String input) {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> DateTimeParser.parse(input));

        assertEquals(INVALID_DATE_TIME_MESSAGE, exception.getMessage());
    }
}
