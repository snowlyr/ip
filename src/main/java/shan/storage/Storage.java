package shan.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import shan.datetime.DateTimeParser;
import shan.exception.DataFileException;
import shan.exception.InvalidArgumentException;
import shan.task.Deadline;
import shan.task.Event;
import shan.task.Task;
import shan.task.ToDo;

/**
 * Loads tasks from and saves tasks to a data file.
 */
public class Storage {
    private final Path dataFile;

    /**
     * Constructs storage that uses the specified data file.
     *
     * @param dataFile Relative path to the data file.
     */
    public Storage(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Loads valid tasks from the data file when it exists.
     *
     * @return Valid tasks and the number of invalid nonblank entries skipped.
     * @throws DataFileException If the data file cannot be read.
     */
    public LoadResult load() throws DataFileException {
        ArrayList<Task> loadedTasks = new ArrayList<>();
        try {
            if (Files.notExists(this.dataFile)) {
                return new LoadResult(loadedTasks, 0);
            }
            if (!Files.isRegularFile(this.dataFile)) {
                throw new DataFileException(String.format(
                        "I couldn't read %s. Starting with an empty task list.", getFilePath()));
            }

            int skippedTasks = 0;
            for (String line : Files.readAllLines(this.dataFile)) {
                if (line.isBlank()) {
                    continue;
                }

                Task task = parseSavedTask(line);
                if (task == null) {
                    skippedTasks++;
                    continue;
                }
                loadedTasks.add(task);
            }
            return new LoadResult(loadedTasks, skippedTasks);
        } catch (IOException | SecurityException exception) {
            throw new DataFileException(String.format(
                    "I couldn't read %s. Starting with an empty task list.", getFilePath()));
        }
    }

    /**
     * Saves the supplied tasks to the data file.
     *
     * @param tasks Tasks to save.
     * @throws DataFileException If the data directory or file cannot be written.
     */
    public void save(List<Task> tasks) throws DataFileException {
        try {
            Path parentDirectory = this.dataFile.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            ArrayList<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(task.toFileString());
            }
            Files.write(this.dataFile, lines);
        } catch (IOException | SecurityException exception) {
            throw new DataFileException(
                    String.format("I couldn't save your tasks to %s.", getFilePath()));
        }
    }

    /**
     * Returns the data-file path using separators suitable for user messages.
     *
     * @return Display representation of the data-file path.
     */
    public String getFilePath() {
        return this.dataFile.toString().replace('\\', '/');
    }

    /**
     * Converts one valid save-file entry into a task.
     *
     * @param line Save-file entry.
     * @return Parsed task, or {@code null} when the entry is invalid.
     */
    private Task parseSavedTask(String line) {
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
     * @throws InvalidArgumentException If a date-time is invalid or the end is not
     *                                  after the start.
     */
    private Event parseSavedEvent(String[] fields) throws InvalidArgumentException {
        LocalDateTime startDate = DateTimeParser.parse(fields[3]);
        LocalDateTime endDate = DateTimeParser.parse(fields[4]);
        if (!endDate.isAfter(startDate)) {
            throw new InvalidArgumentException("The event end must be after its start.");
        }
        return new Event(fields[2], startDate, endDate);
    }

    /**
     * Contains the valid tasks and corruption count produced by one load.
     *
     * @param tasks        Valid tasks loaded from the file.
     * @param skippedTasks Number of invalid nonblank entries skipped.
     */
    public record LoadResult(List<Task> tasks, int skippedTasks) {
        /**
         * Constructs an immutable load result.
         */
        public LoadResult {
            tasks = List.copyOf(tasks);
        }
    }
}
