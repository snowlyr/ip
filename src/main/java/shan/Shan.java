package shan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

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
        TODO,
        DEADLINE,
        EVENT
    }

    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = " ____  _\n"
            + "/ ___|| |__   __ _ _ __\n"
            + "\\___ \\| '_ \\ / _` | '_ \\\n"
            + " ___) | | | | (_| | | | |\n"
            + "|____/|_| |_|\\__,_|_| |_| \n";
    private static final Path DATA_FILE = Path.of("data", "shan.txt");
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
        try {
            int skippedTasks = loadTasks();
            if (skippedTasks > 0) {
                startupWarning = String.format(
                    "Warning: I skipped %d invalid task %s in data/shan.txt.",
                    skippedTasks, skippedTasks == 1 ? "entry" : "entries");
            }
        } catch (DataFileException exception) {
            startupWarning = exception.getMessage();
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println(DIVIDER);
        System.out.println(BANNER);
        String greeting = "Hey! I'm Shan.\nHow can I help?";
        System.out.println(greeting);
        System.out.println(DIVIDER);
        if (startupWarning != null) {
            sendMessage(startupWarning);
        }

        while (scanner.hasNextLine()) {
            String inputLine = scanner.nextLine().trim();
            try {
                sendMessage(reply(inputLine));
            } catch (ShanException exception) {
                sendMessage(exception.getMessage());
            }

            if (inputLine.equals("bye")) {
                break;
            }
        }

        scanner.close();
    }

    /**
     * Prints a message using Shan's standard response formatting.
     *
     * @param message Message to print.
     */
    private static void sendMessage(String message) {
        System.out.println(DIVIDER);
        System.out.println("Shan: " + message);
        System.out.println(DIVIDER);
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
     * @throws InvalidArgumentException If the task contains the save-file delimiter.
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
            throw new InvalidArgumentException("The event end must be after its start.");
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
            saveTasks();
        } catch (DataFileException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, tasks.size());
    }

    /**
     * Writes the current task list to the data file.
     *
     * @throws DataFileException If the data directory or file cannot be written.
     */
    private static void saveTasks() throws DataFileException {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            ArrayList<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(task.toFileString());
            }
            Files.write(DATA_FILE, lines);
        } catch (IOException | SecurityException exception) {
            throw new DataFileException("I couldn't save your tasks to data/shan.txt.");
        }
    }

    /**
     * Loads valid tasks from the data file when it exists.
     *
     * @return Number of invalid nonblank entries that were skipped.
     * @throws DataFileException If the data file cannot be read.
     */
    private static int loadTasks() throws DataFileException {
        tasks.clear();
        try {
            if (Files.notExists(DATA_FILE)) {
                return 0;
            }
            if (!Files.isRegularFile(DATA_FILE)) {
                throw new DataFileException(
                        "I couldn't read data/shan.txt. Starting with an empty task list.");
            }

            int skippedTasks = 0;
            for (String line : Files.readAllLines(DATA_FILE)) {
                if (line.isBlank()) {
                    continue;
                }

                Task task = parseSavedTask(line);
                if (task == null) {
                    skippedTasks++;
                    continue;
                }
                tasks.add(task);
            }
            return skippedTasks;
        } catch (IOException | SecurityException exception) {
            tasks.clear();
            throw new DataFileException(
                    "I couldn't read data/shan.txt. Starting with an empty task list.");
        }
    }

    /**
     * Converts one valid save-file entry into a task.
     *
     * @param line Save-file entry.
     * @return Parsed task, or {@code null} when the entry is invalid.
     */
    private static Task parseSavedTask(String line) {
        String[] fields = line.trim().split("\\s*\\|\\s*", -1);
        if (fields.length < 2 || (!fields[1].equals("0") && !fields[1].equals("1"))) {
            return null;
        }

        int expectedFieldCount = switch (fields[0]) {
            case "T" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> -1;
        };
        if (fields.length != expectedFieldCount) {
            return null;
        }
        for (int i = 2; i < fields.length; i++) {
            if (fields[i].isBlank()) {
                return null;
            }
        }

        Task task;
        try {
            task = switch (fields[0]) {
                case "T" -> new ToDo(fields[2]);
                case "D" -> new Deadline(fields[2], DateTimeParser.parse(fields[3]));
                case "E" -> parseSavedEvent(fields);
                default -> throw new AssertionError("Task type was already validated");
            };
        } catch (InvalidArgumentException exception) {
            return null;
        }
        if (fields[1].equals("1")) {
            task.markDone();
        }
        return task;
    }

    /**
     * Parses an Event from fields whose type, status, and field count are valid.
     *
     * @param fields Serialized Event fields.
     * @return Parsed Event.
     * @throws InvalidArgumentException If a date-time is invalid or the end is not after the start.
     */
    private static Event parseSavedEvent(String[] fields) throws InvalidArgumentException {
        LocalDateTime startDate = DateTimeParser.parse(fields[3]);
        LocalDateTime endDate = DateTimeParser.parse(fields[4]);
        if (!endDate.isAfter(startDate)) {
            throw new InvalidArgumentException("The event end must be after its start.");
        }
        return new Event(fields[2], startDate, endDate);
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
            saveTasks();
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
            saveTasks();
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
            saveTasks();
        } catch (DataFileException exception) {
            tasks.add(taskNumber - 1, removedTask);
            throw exception;
        }
        return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, tasks.size());
    }
}
