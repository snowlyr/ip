package shan;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Runs the Shan chatbot.
 */
public class Shan {
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
        Parser.ParsedCommand parsedCommand = Parser.parse(inputLine);
        String arguments = parsedCommand.arguments();

        return switch (parsedCommand.commandType()) {
            case BYE -> commandExit();
            case LIST -> commandList();
            case MARK -> commandMark(Parser.parseTaskNumber(arguments));
            case UNMARK -> commandUnmark(Parser.parseTaskNumber(arguments));
            case DELETE -> commandDelete(Parser.parseTaskNumber(arguments));
            case ON -> commandShowOnDateOrRange(arguments);
            case TODO -> addTask(Parser.parseToDo(arguments));
            case DEADLINE -> addTask(Parser.parseDeadline(arguments));
            case EVENT -> addTask(Parser.parseEvent(arguments));
        };
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
        Parser.DateRange dateRange = Parser.parseDateRange(argument);
        LocalDate startDate = dateRange.startDate();
        LocalDate endDate = dateRange.endDate();

        boolean isRange = dateRange.isRange();
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
