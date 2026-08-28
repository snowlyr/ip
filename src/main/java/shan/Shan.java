package shan;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Runs the Shan chatbot.
 */
public class Shan {
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;

    /**
     * Constructs Shan with collaborators that use the specified data file.
     *
     * @param dataFile Relative path to the task data file.
     */
    public Shan(Path dataFile) {
        this.storage = new Storage(dataFile);
        this.tasks = new TaskList();
        this.ui = new Ui();
    }

    /**
     * Starts Shan using the default task data file.
     *
     * @param args Command-line arguments; unused.
     */
    public static void main(String[] args) {
        new Shan(Path.of("data", "shan.txt")).run();
    }

    /**
     * Loads saved tasks and processes commands from standard input.
     */
    public void run() {
        String startupWarning = loadTasks();
        this.ui.showWelcome();
        if (startupWarning != null) {
            this.ui.showMessage(startupWarning);
        }

        while (this.ui.hasNextCommand()) {
            String inputLine = this.ui.readCommand();
            try {
                this.ui.showMessage(reply(inputLine));
            } catch (ShanException exception) {
                this.ui.showMessage(exception.getMessage());
            }

            if (inputLine.equals("bye")) {
                break;
            }
        }

        this.ui.close();
    }

    /**
     * Loads tasks and returns a warning when any saved data cannot be used.
     *
     * @return Startup warning, or {@code null} when loading succeeds completely.
     */
    private String loadTasks() {
        this.tasks.clear();
        try {
            Storage.LoadResult loadResult = this.storage.load();
            this.tasks.replaceAll(loadResult.tasks());
            int skippedTasks = loadResult.skippedTasks();
            if (skippedTasks > 0) {
                return String.format(
                        "Warning: I skipped %d invalid task %s in %s.",
                        skippedTasks, skippedTasks == 1 ? "entry" : "entries",
                        this.storage.getFilePath());
            }
            return null;
        } catch (DataFileException exception) {
            return exception.getMessage();
        }
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
    private String reply(String inputLine)
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
    private String commandExit() {
        return "Bye! See you soon.";
    }

    /**
     * Adds a task to the task list.
     *
     * @param task Task to add.
     * @return Reply confirming that the task was added.
     * @throws DataFileException If the task list cannot be saved.
     */
    private String addTask(Task task) throws DataFileException {
        this.tasks.add(task);
        try {
            this.storage.save(this.tasks.snapshot());
        } catch (DataFileException exception) {
            this.tasks.removeLast();
            throw exception;
        }
        return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, this.tasks.size());
    }

    /**
     * Returns the list of tasks.
     *
     * @return Enumerated list of tasks.
     */
    private String commandList() {
        StringBuilder result = new StringBuilder("Here are the tasks in your list:");
        for (int taskNumber = 1; taskNumber <= this.tasks.size(); taskNumber++) {
            result.append(String.format("\n%d.%s", taskNumber, this.tasks.get(taskNumber)));
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
    private String commandShowOnDateOrRange(String argument)
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
        for (int taskNumber = 1; taskNumber <= this.tasks.size(); taskNumber++) {
            Task task = this.tasks.get(taskNumber);
            if (task.occursBetween(startDate, endDate)) {
                result.append(String.format("\n%d.%s", taskNumber, task));
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
    private String commandMark(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (!this.tasks.containsTaskNumber(taskNumber)) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task task = this.tasks.get(taskNumber);
        boolean wasDone = task.isDone();
        String taskDisplay = task.markDone();
        try {
            this.storage.save(this.tasks.snapshot());
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
    private String commandUnmark(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (!this.tasks.containsTaskNumber(taskNumber)) {
            throw new InvalidArgumentException("oops, this task does not exist!!");
        }
        Task task = this.tasks.get(taskNumber);
        boolean wasDone = task.isDone();
        String taskDisplay = task.unmarkDone();
        try {
            this.storage.save(this.tasks.snapshot());
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
    private String commandDelete(int taskNumber) throws InvalidArgumentException, DataFileException {
        if (!this.tasks.containsTaskNumber(taskNumber)) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }
        Task removedTask = this.tasks.delete(taskNumber);
        try {
            this.storage.save(this.tasks.snapshot());
        } catch (DataFileException exception) {
            this.tasks.restore(taskNumber, removedTask);
            throw exception;
        }
        return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, this.tasks.size());
    }
}
