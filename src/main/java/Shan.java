import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final ArrayList<Task> taskList = new ArrayList<>();

    public static void main(String[] args) {
        String startupWarning = null;
        try {
            int skippedTasks = loadTasks();
            if (skippedTasks > 0) {
                startupWarning = String.format(
                    "Warning: I skipped %d invalid task %s in data/shan.txt.",
                    skippedTasks, skippedTasks == 1 ? "entry" : "entries");
            }
        } catch (DataFileException e) {
            startupWarning = e.getMessage();
        }
        Scanner sc = new Scanner(System.in);

        System.out.println(DIVIDER);
        System.out.println(BANNER);
        String greeting = "Hey! I'm Shan.\nHow can I help?";
        System.out.println(greeting);
        System.out.println(DIVIDER);
        if (startupWarning != null) {
            sendMessage(startupWarning);
        }

        while (sc.hasNextLine()) {
            String inputLine = sc.nextLine().trim();
            try {
                sendMessage(reply(inputLine));
            } catch (ShanException e) {
                sendMessage(e.getMessage());
            }

            if (inputLine.equals("bye")) {
                break;
            }
        }

        sc.close();
    }

    /**
     * Prints a message using Shan's standard response formatting.
     *
     * @param message message to print
     */
    private static void sendMessage(String message) {
        System.out.println(DIVIDER);
        System.out.println("Shan: " + message);
        System.out.println(DIVIDER);
    }

    /**
     * Returns Shan's reply to a user command.
     *
     * @param inputLine command entered by the user
     * @return Shan's reply
     * @throws InvalidCommandException  if the command is blank or unknown
     * @throws MissingArgumentException if a required argument is missing
     * @throws InvalidArgumentException if an argument has an invalid value
     * @throws DataFileException        if the task list cannot be saved
     */
    private static String reply(String inputLine)
    throws InvalidCommandException, MissingArgumentException, InvalidArgumentException, DataFileException {
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
     * @param command command word entered by the user
     * @return matching command type
     * @throws InvalidCommandException if the command is unknown
     */
    private static CommandType parseCommand(String command) throws InvalidCommandException {
        try {
            return CommandType.valueOf(command.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("I don't understand bro.");
        }
    }

    /**
     * Parses a task number supplied to a mark or unmark command.
     *
     * @param argument task number entered by the user
     * @return parsed task number
     * @throws MissingArgumentException if the task number is missing
     * @throws InvalidArgumentException if the task number is not an integer
     */
    private static int parseTaskNumber(String argument)
    throws MissingArgumentException, InvalidArgumentException {
        if (argument.isBlank()) {
            throw new MissingArgumentException("Specify a task number.");
        }

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("The task number must be an int.");
        }
    }

    /**
     * Returns the farewell message.
     *
     * @return farewell message for {@code bye}
     */
    private static String commandExit() {
        return "Bye! See you soon.";
    }

    /**
     * Adds a ToDo task.
     *
     * @param taskName name of task to add
     * @return reply when adding task
     * @throws MissingArgumentException if the task description is empty
     * @throws InvalidArgumentException if the task contains the save-file delimiter
     * @throws DataFileException        if the task list cannot be saved
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
     * @param arguments task description followed by {@code /by} and the deadline
     * @return reply when adding task
     * @throws MissingArgumentException if the description, deadline, or delimiter
     *                                  is missing
     * @throws InvalidArgumentException if a task field contains the save-file
     *                                  delimiter
     * @throws DataFileException        if the task list cannot be saved
     */
    private static String commandAddDeadline(String arguments)
    throws MissingArgumentException, InvalidArgumentException, DataFileException {
        if (arguments.isBlank()) {
            throw new MissingArgumentException("The deadline description cannot be empty, else its not a deadline");
        }

        String[] details = arguments.split("/by", 2);

        if (details.length < 2) {
            throw new MissingArgumentException("Please specify a deadline using /by.");
        }
        if (details[0].isBlank() || details[1].isBlank()) {
            throw new MissingArgumentException("The deadline description and date cannot be empty bruh.");
        }

        String taskName = details[0].trim();
        String endDate = details[1].trim();
        validateFileFields(taskName, endDate);
        return addTask(new Deadline(taskName, endDate));
    }

    /**
     * Adds an Event task.
     *
     * @param arguments task description followed by {@code /from} and {@code /to}
     *                  values
     * @return reply when adding task
     * @throws MissingArgumentException if the description, times, or delimiters are
     *                                  missing
     * @throws InvalidArgumentException if a task field contains the save-file
     *                                  delimiter
     * @throws DataFileException        if the task list cannot be saved
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
        String startDate = toDetails[0].trim();
        String endDate = toDetails[1].trim();
        validateFileFields(taskName, startDate, endDate);
        return addTask(new Event(taskName, startDate, endDate));
    }

    /**
     * Rejects values containing the delimiter reserved by the save-file format.
     *
     * @param fields task fields to validate
     * @throws InvalidArgumentException if a field contains {@code |}
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
     * @param task task to add
     * @return reply confirming that the task was added
     * @throws DataFileException if the task list cannot be saved
     */
    private static String addTask(Task task) throws DataFileException {
        taskList.add(task);
        try {
            saveTasks();
        } catch (DataFileException e) {
            taskList.remove(taskList.size() - 1);
            throw e;
        }
        return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, taskList.size());
    }

    /**
     * Writes the current task list to the data file.
     *
     * @throws DataFileException if the data directory or file cannot be written
     */
    private static void saveTasks() throws DataFileException {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            ArrayList<String> lines = new ArrayList<>();
            for (Task task : taskList) {
                lines.add(task.toFileString());
            }
            Files.write(DATA_FILE, lines);
        } catch (IOException | SecurityException e) {
            throw new DataFileException("I couldn't save your tasks to data/shan.txt.");
        }
    }

    /**
     * Loads valid tasks from the data file when it exists.
     *
     * @return number of invalid nonblank entries that were skipped
     * @throws DataFileException if the data file cannot be read
     */
    private static int loadTasks() throws DataFileException {
        taskList.clear();
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
                taskList.add(task);
            }
            return skippedTasks;
        } catch (IOException | SecurityException e) {
            taskList.clear();
            throw new DataFileException(
                    "I couldn't read data/shan.txt. Starting with an empty task list.");
        }
    }

    /**
     * Converts one valid save-file entry into a task.
     *
     * @param line save-file entry
     * @return parsed task, or {@code null} when the entry is invalid
     */
    private static Task parseSavedTask(String line) {
        String[] fields = line.trim().split("\\s*\\|\\s*", -1);
        if (fields.length < 2 || (!fields[1].equals("0") && !fields[1].equals("1"))) {
            return null;
        }

        int expectedFields = switch (fields[0]) {
            case "T" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> -1;
        };
        if (fields.length != expectedFields) {
            return null;
        }
        for (int i = 2; i < fields.length; i++) {
            if (fields[i].isBlank()) {
                return null;
            }
        }

        Task task = switch (fields[0]) {
            case "T" -> new ToDo(fields[2]);
            case "D" -> new Deadline(fields[2], fields[3]);
            case "E" -> new Event(fields[2], fields[3], fields[4]);
            default -> throw new AssertionError("Task type was already validated");
        };
        if (fields[1].equals("1")) {
            task.markDone();
        }
        return task;
    }

    /**
     * Returns the list of tasks
     *
     * @return enumerated list of tasks
     */
    private static String commandList() {
        StringBuilder res = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < taskList.size(); i++) {
            res.append(String.format("\n%d.%s", i + 1, taskList.get(i)));
        }
        return res.toString();
    }

    /**
     * Mark the specified task index as done
     *
     * @param idx task index to mark as done
     * @return reply when marked as done
     * @throws InvalidArgumentException if the task index does not exist
     * @throws DataFileException        if the task list cannot be saved
     */
    private static String commandMark(int idx) throws InvalidArgumentException, DataFileException {
        if (idx > taskList.size() || idx < 1) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task task = taskList.get(idx - 1);
        boolean wasDone = task.isDone();
        String res = task.markDone();
        try {
            saveTasks();
        } catch (DataFileException e) {
            if (!wasDone) {
                task.unmarkDone();
            }
            throw e;
        }
        return String.format("Well done! I have marked this task as done!\n  %s", res);
    }

    /**
     * Unmark the specified task index as done
     *
     * @param idx task index to unmark
     * @return reply when unmarked
     * @throws InvalidArgumentException if the task index does not exist
     * @throws DataFileException        if the task list cannot be saved
     */
    private static String commandUnmark(int idx) throws InvalidArgumentException, DataFileException {
        if (idx > taskList.size() || idx < 1) {
            throw new InvalidArgumentException("oops, this task does not exist!!");
        }
        Task task = taskList.get(idx - 1);
        boolean wasDone = task.isDone();
        String res = task.unmarkDone();
        try {
            saveTasks();
        } catch (DataFileException e) {
            if (wasDone) {
                task.markDone();
            }
            throw e;
        }
        return String.format("What happened? I have unmarked this task as completed...\n  %s", res);
    }

    /**
     * Deletes the task at the specified index.
     *
     * @param idx task index to delete
     * @return reply confirming which task was deleted
     * @throws InvalidArgumentException if the task index does not exist
     * @throws DataFileException        if the task list cannot be saved
     */
    private static String commandDelete(int idx) throws InvalidArgumentException, DataFileException {
        if (idx > taskList.size() || idx < 1) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task removedTask = taskList.remove(idx - 1);
        try {
            saveTasks();
        } catch (DataFileException e) {
            taskList.add(idx - 1, removedTask);
            throw e;
        }
        return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, taskList.size());
    }
}
