package shan;

import java.nio.file.Path;

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

        boolean isExit = false;
        while (!isExit && this.ui.hasNextCommand()) {
            String inputLine = this.ui.readCommand();
            try {
                Command command = Parser.parse(inputLine);
                command.execute(this.tasks, this.ui, this.storage);
                isExit = command.isExit();
            } catch (ShanException exception) {
                this.ui.showMessage(exception.getMessage());
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
}
