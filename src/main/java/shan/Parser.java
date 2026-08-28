package shan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Interprets user commands and their arguments.
 */
public final class Parser {
    /**
     * Commands understood by Shan.
     */
    public enum CommandType {
        /** Exits Shan. */
        BYE,
        /** Lists all tasks. */
        LIST,
        /** Marks a task as completed. */
        MARK,
        /** Marks a task as not completed. */
        UNMARK,
        /** Deletes a task. */
        DELETE,
        /** Shows dated tasks on a date or within a range. */
        ON,
        /** Adds a ToDo task. */
        TODO,
        /** Adds a Deadline task. */
        DEADLINE,
        /** Adds an Event task. */
        EVENT
    }

    private Parser() {
    }

    /**
     * Parses a complete input line into its command type and raw arguments.
     *
     * @param inputLine Input entered by the user.
     * @return Parsed command and arguments.
     * @throws InvalidCommandException If the command is blank or unknown.
     */
    public static ParsedCommand parse(String inputLine) throws InvalidCommandException {
        if (inputLine.isBlank()) {
            throw new InvalidCommandException("Enter a command dood.");
        }

        String[] tokens = inputLine.split("\\s+", 2);
        CommandType commandType;
        try {
            commandType = CommandType.valueOf(tokens[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidCommandException("I don't understand bro.");
        }
        String arguments = tokens.length == 2 ? tokens[1] : "";
        return new ParsedCommand(commandType, arguments);
    }

    /**
     * Parses a task number supplied to a mark, unmark, or delete command.
     *
     * @param argument Task number entered by the user.
     * @return Parsed task number.
     * @throws MissingArgumentException If the task number is missing.
     * @throws InvalidArgumentException If the task number is not an integer.
     */
    public static int parseTaskNumber(String argument)
            throws MissingArgumentException, InvalidArgumentException {
        if (argument.isBlank()) {
            throw new MissingArgumentException("Specify a task number.");
        }

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new InvalidArgumentException("The task number must be an int.");
        }
    }

    /**
     * Parses a ToDo task description.
     *
     * @param taskName Name of the task.
     * @return Parsed ToDo task.
     * @throws MissingArgumentException If the description is empty.
     * @throws InvalidArgumentException If the description contains the save-file delimiter.
     */
    public static ToDo parseToDo(String taskName)
            throws MissingArgumentException, InvalidArgumentException {
        if (taskName.isBlank()) {
            throw new MissingArgumentException("The task description cannot be empty my guy.");
        }
        validateFileFields(taskName);
        return new ToDo(taskName);
    }

    /**
     * Parses a Deadline description and date-time.
     *
     * @param arguments Task description followed by {@code /by} and the deadline.
     * @return Parsed Deadline task.
     * @throws MissingArgumentException If a required field or delimiter is missing.
     * @throws InvalidArgumentException If a field or date-time is invalid.
     */
    public static Deadline parseDeadline(String arguments)
            throws MissingArgumentException, InvalidArgumentException {
        if (arguments.isBlank()) {
            throw new MissingArgumentException(
                    "The deadline description cannot be empty, else its not a deadline");
        }

        String[] deadlineDetails = arguments.split("/by", 2);
        if (deadlineDetails.length < 2) {
            throw new MissingArgumentException("Please specify a deadline using /by.");
        }
        if (deadlineDetails[0].isBlank() || deadlineDetails[1].isBlank()) {
            throw new MissingArgumentException("The deadline description and date cannot be empty bruh.");
        }

        String taskName = deadlineDetails[0].trim();
        String endDateInput = deadlineDetails[1].trim();
        validateFileFields(taskName, endDateInput);
        LocalDateTime endDate = DateTimeParser.parse(endDateInput);
        return new Deadline(taskName, endDate);
    }

    /**
     * Parses an Event description and date-time range.
     *
     * @param arguments Task description followed by {@code /from} and {@code /to} values.
     * @return Parsed Event task.
     * @throws MissingArgumentException If a required field or delimiter is missing.
     * @throws InvalidArgumentException If a field, date-time, or range is invalid.
     */
    public static Event parseEvent(String arguments)
            throws MissingArgumentException, InvalidArgumentException {
        if (arguments.isBlank()) {
            throw new MissingArgumentException("The event description cannot be empty...");
        }

        String[] fromDetails = arguments.split("/from", 2);
        if (fromDetails.length < 2) {
            throw new MissingArgumentException("Specify the event start using /from.");
        }

        String[] toDetails = fromDetails[1].split("/to", 2);
        if (toDetails.length < 2) {
            throw new MissingArgumentException("Specify the event end using /to.");
        }
        if (fromDetails[0].isBlank() || toDetails[0].isBlank() || toDetails[1].isBlank()) {
            throw new MissingArgumentException(
                    "The event description, start, and end cannot be empty, lock in bro.");
        }

        String taskName = fromDetails[0].trim();
        String startDateInput = toDetails[0].trim();
        String endDateInput = toDetails[1].trim();
        validateFileFields(taskName, startDateInput, endDateInput);
        LocalDateTime startDate = DateTimeParser.parse(startDateInput);
        LocalDateTime endDate = DateTimeParser.parse(endDateInput);
        if (!endDate.isAfter(startDate)) {
            throw new InvalidArgumentException("The event end must be after its start bro.");
        }
        return new Event(taskName, startDate, endDate);
    }

    /**
     * Parses one date or an inclusive range of dates.
     *
     * @param argument One date, or two dates separated by {@code /to}.
     * @return Parsed inclusive date range.
     * @throws MissingArgumentException If a required date is missing.
     * @throws InvalidArgumentException If a date or range is invalid.
     */
    public static DateRange parseDateRange(String argument)
            throws MissingArgumentException, InvalidArgumentException {
        if (argument.isBlank()) {
            throw new MissingArgumentException("Specify a date using yyyy-MM-dd format pleaseee ><.");
        }

        String[] dateInputs = argument.split("/to", 2);
        if (dateInputs.length == 2 && (dateInputs[0].isBlank() || dateInputs[1].isBlank())) {
            throw new MissingArgumentException("Specify both range dates using /to.");
        }

        LocalDate startDate = DateTimeParser.parseDate(dateInputs[0].trim());
        LocalDate endDate = dateInputs.length == 1
                ? startDate
                : DateTimeParser.parseDate(dateInputs[1].trim());
        if (endDate.isBefore(startDate)) {
            throw new InvalidArgumentException(
                    "The range end date cannot be before its start date, bro?");
        }
        return new DateRange(startDate, endDate);
    }

    /**
     * Rejects values containing the delimiter reserved by the save-file format.
     *
     * @param fields Task fields to validate.
     * @throws InvalidArgumentException If a field contains {@code |}.
     */
    private static void validateFileFields(String... fields) throws InvalidArgumentException {
        for (String field : fields) {
            if (field.contains("|")) {
                throw new InvalidArgumentException("Task details cannot contain |.");
            }
        }
    }

    /**
     * Contains the type and raw arguments of a parsed command.
     *
     * @param commandType Parsed command type.
     * @param arguments   Raw command arguments.
     */
    public record ParsedCommand(CommandType commandType, String arguments) {
    }

    /**
     * Contains the inclusive dates of a parsed date query.
     *
     * @param startDate First date in the range.
     * @param endDate   Last date in the range.
     */
    public record DateRange(LocalDate startDate, LocalDate endDate) {
        /**
         * Returns whether this value represents more than one date.
         *
         * @return {@code true} when the start and end dates differ.
         */
        public boolean isRange() {
            return !this.startDate.equals(this.endDate);
        }
    }
}
