package shan;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Runs the Shan chatbot.
 */
public class Shan {
    /**
     * Commands understood by Shan.
     */
    private enum CommandType {
        BYE,
        LIST,
        MARK,
        UNMARK,
        DELETE,
        ON,
        TODO,
        DEADLINE,
        EVENT
    }

    private static final Storage STORAGE = new Storage(Path.of("data", "shan.txt"));
    private static final ArrayList<Task> tasks = new ArrayList<>();

    private Shan() {
    }

    /**
     * Starts Shan and processes commands from standard input.
     *
     * @param args Command-line arguments; unused.
     */
    public static void main(String[] args) {
        String startupWarning = null;
        tasks.clear();
        try {
            Storage.LoadResult loadResult = STORAGE.load();
            tasks.addAll(loadResult.tasks());
            int skippedTasks = loadResult.skippedTasks();
            if (skippedTasks > 0) {
                startupWarning = String.format(
                        "Warning: I skipped %d invalid task %s in %s.",
                        skippedTasks, skippedTasks == 1 ? "entry" : "entries",
                        STORAGE.getFilePath());
            }
        } catch (DataFileException exception) {
            startupWarning = exception.getMessage();
        }

        Ui ui = new Ui();
        ui.showWelcome();
        if (startupWarning != null) {
            ui.showMessage(startupWarning);
        }

        while (ui.hasNextCommand()) {
            String inputLine = ui.readCommand();
            try {
                ui.showMessage(reply(inputLine));
            } catch (ShanException exception) {
                ui.showMessage(exception.getMessage());
            }

            if (inputLine.equals("bye")) {
                break;
            }
        }

        ui.close();
    }

    /**
     * Returns Shan's reply to a user command.
     *
     * @param inputLine Command entered by the user.
     * @return Shan's reply.
     * @throws InvalidCommandException  If the command is blank or unknown.
     * @throws MissingArgumentException If a required argument is missing.
     * @throws InvalidArgumentException If an argument has an invalid value.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String reply(String inputLine)
            throws InvalidCommandException, MissingArgumentException,
            InvalidArgumentException, DataFileException {
        if (inputLine.isBlank()) {
            throw new InvalidCommandException("Enter a command dood.");
        }

        String[] tokens = inputLine.split("\\s+", 2);
        CommandType command = parseCommand(tokens[0]);
        String arguments = tokens.length == 2 ? tokens[1] : "";

        return switch (command) {
            case BYE -> commandExit();
            case LIST -> commandList();
            case MARK -> commandMark(parseTaskNumber(arguments));
            case UNMARK -> commandUnmark(parseTaskNumber(arguments));
            case DELETE -> commandDelete(parseTaskNumber(arguments));
            case ON -> commandShowOnDateOrRange(arguments);
            case TODO -> commandAddToDo(arguments);
            case DEADLINE -> commandAddDeadline(arguments);
            case EVENT -> commandAddEvent(arguments);
        };
    }

    /**
     * Converts user input into a supported command type.
     *
     * @param command Command word entered by the user.
     * @return Matching command type.
     * @throws InvalidCommandException If the command is unknown.
     */
    private static CommandType parseCommand(String command) throws InvalidCommandException {
        try {
            return CommandType.valueOf(command.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidCommandException("I don't understand bro.");
        }
    }

    /**
     * Parses a task number supplied to a mark or unmark command.
     *
     * @param argument Task number entered by the user.
     * @return Parsed task number.
     * @throws MissingArgumentException If the task number is missing.
     * @throws InvalidArgumentException If the task number is not an integer.
     */
    private static int parseTaskNumber(String argument)
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
     * Returns the farewell message.
     *
     * @return Farewell message for {@code bye}.
     */
    private static String commandExit() {
        return "Bye! See you soon.";
    }

    /**
     * Adds a ToDo task.
     *
     * @param taskName Name of the task to add.
     * @return Reply confirming the added task.
     * @throws MissingArgumentException If the task description is empty.
     * @throws InvalidArgumentException If the task contains the save-file
     *                                  delimiter.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandAddToDo(String taskName)
            throws MissingArgumentException, InvalidArgumentException, DataFileException {
        if (taskName.isBlank()) {
            throw new MissingArgumentException("The task description cannot be empty my guy.");
        }
        validateFileFields(taskName);
        return addTask(new ToDo(taskName));
    }

    /**
     * Adds a Deadline task.
     *
     * @param arguments Task description followed by {@code /by} and the deadline.
     * @return Reply confirming the added task.
     * @throws MissingArgumentException If the description, deadline, or delimiter
     *                                  is missing.
     * @throws InvalidArgumentException If a task field contains the save-file
     *                                  delimiter.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandAddDeadline(String arguments)
            throws MissingArgumentException, InvalidArgumentException, DataFileException {
        if (arguments.isBlank()) {
            throw new MissingArgumentException("The deadline description cannot be empty, else its not a deadline");
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
        return addTask(new Deadline(taskName, endDate));
    }

    /**
     * Adds an Event task.
     *
     * @param arguments Task description followed by {@code /from} and {@code /to}
     *                  values
     * @return Reply confirming the added task.
     * @throws MissingArgumentException If the description, times, or delimiters are
     *                                  missing.
     * @throws InvalidArgumentException If a task field contains the save-file
     *                                  delimiter.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandAddEvent(String arguments)
            throws MissingArgumentException, InvalidArgumentException, DataFileException {
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
            throw new MissingArgumentException("The event description, start, and end cannot be empty, lock in bro.");
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
        return addTask(new Event(taskName, startDate, endDate));
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
     * Adds a task to the task list.
     *
     * @param task Task to add.
     * @return Reply confirming that the task was added.
     * @throws DataFileException If the task list cannot be saved.
     */
    private static String addTask(Task task) throws DataFileException {
        tasks.add(task);
        try {
            STORAGE.save(tasks);
        } catch (DataFileException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, tasks.size());
    }

    /**
     * Returns the list of tasks.
     *
     * @return Enumerated list of tasks.
     */
    private static String commandList() {
        StringBuilder result = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            result.append(String.format("\n%d.%s", i + 1, tasks.get(i)));
        }
        return result.toString();
    }

    /**
     * Returns deadlines and events on one date or within an inclusive date range.
     *
     * @param argument One date, or two dates separated by {@code /to}.
     * @return Enumerated matching tasks, or a message when there are no matches.
     * @throws MissingArgumentException If a required date is missing.
     * @throws InvalidArgumentException If a date or range is invalid.
     */
    private static String commandShowOnDateOrRange(String argument)
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
            throw new InvalidArgumentException("The range end date cannot be before its start date, bro?");
        }

        boolean isRange = !startDate.equals(endDate);
        String displayStartDate = DateTimeParser.formatDateForDisplay(startDate);
        String displayEndDate = DateTimeParser.formatDateForDisplay(endDate);
        String resultHeading = isRange
                ? String.format("Here are the deadlines and events from %s to %s:",
                        displayStartDate, displayEndDate)
                : String.format("Got your deadlines and events on %s:", displayStartDate);
        StringBuilder result = new StringBuilder(resultHeading);
        int matchCount = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.occursBetween(startDate, endDate)) {
                result.append(String.format("\n%d.%s", i + 1, task));
                matchCount++;
            }
        }
        if (matchCount == 0) {
            return isRange
                    ? String.format("There are no deadlines or events from %s to %s.",
                            displayStartDate, displayEndDate)
                    : String.format("There are no deadlines or events on %s.", displayStartDate);
        }
        return result.toString();
    }

    /**
     * Marks the specified task as done.
     *
     * @param taskNumber Number of the task to mark as done.
     * @return Reply confirming the marked task.
     * @throws InvalidArgumentException If the task number does not exist.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandMark(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (taskNumber > tasks.size() || taskNumber < 1) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task task = tasks.get(taskNumber - 1);
        boolean wasDone = task.isDone();
        String taskDisplay = task.markDone();
        try {
            STORAGE.save(tasks);
        } catch (DataFileException exception) {
            if (!wasDone) {
                task.unmarkDone();
            }
            throw exception;
        }
        return String.format("Well done! I have marked this task as done!\n  %s", taskDisplay);
    }

    /**
     * Marks the specified task as not done.
     *
     * @param taskNumber Number of the task to mark as not done.
     * @return Reply confirming the unmarked task.
     * @throws InvalidArgumentException If the task number does not exist.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandUnmark(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (taskNumber > tasks.size() || taskNumber < 1) {
            throw new InvalidArgumentException("oops, this task does not exist!!");
        }
        Task task = tasks.get(taskNumber - 1);
        boolean wasDone = task.isDone();
        String taskDisplay = task.unmarkDone();
        try {
            STORAGE.save(tasks);
        } catch (DataFileException exception) {
            if (wasDone) {
                task.markDone();
            }
            throw exception;
        }
        return String.format("What happened? I have unmarked this task as completed...\n  %s", taskDisplay);
    }

    /**
     * Deletes the task at the specified index.
     *
     * @param taskNumber Number of the task to delete.
     * @return Reply confirming which task was deleted.
     * @throws InvalidArgumentException If the task number does not exist.
     * @throws DataFileException        If the task list cannot be saved.
     */
    private static String commandDelete(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (taskNumber > tasks.size() || taskNumber < 1) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task removedTask = tasks.remove(taskNumber - 1);
        try {
            STORAGE.save(tasks);
        } catch (DataFileException exception) {
            tasks.add(taskNumber - 1, removedTask);
            throw exception;
        }
        return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, tasks.size());
    }
}
