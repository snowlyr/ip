package shan;

import java.nio.file.Path;

import shan.command.Command;
import shan.exception.DataFileException;
import shan.exception.ShanException;
import shan.parser.Parser;
import shan.storage.Storage;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Runs the Shan chatbot.
 */
public class Shan {
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;
    private boolean isInitialized;

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
     * Loads saved tasks once and returns a startup warning, if any.
     *
     * @return Startup warning, or {@code null} when loading succeeds completely.
     */
    public String initialize() {
        if (this.isInitialized) {
            return null;
        }

        this.isInitialized = true;
        return loadTasks();
    }

    /**
     * Parses and executes one user command.
     *
     * @param input User command to execute.
     * @return Message to show the user and whether the application should exit.
     */
    public CommandResult executeCommand(String input) {
        StringBuilder response = new StringBuilder();
        try {
            Command command = Parser.parse(input);
            command.execute(this.tasks, response::append, this.storage);
            return new CommandResult(response.toString(), command.isExit());
        } catch (ShanException exception) {
            return new CommandResult(exception.getMessage(), false);
        }
    }

    /**
     * Loads saved tasks and processes commands from standard input.
     */
    public void run() {
        String startupWarning = initialize();
        this.ui.showWelcome();
        if (startupWarning != null) {
            this.ui.showMessage(startupWarning);
        }

        boolean isExit = false;
        while (!isExit && this.ui.hasNextCommand()) {
            String inputLine = this.ui.readCommand();
            CommandResult result = executeCommand(inputLine);
            this.ui.showMessage(result.message());
            isExit = result.shouldExit();
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
}
