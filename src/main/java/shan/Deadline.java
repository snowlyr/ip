package shan;

import java.time.LocalDateTime;

/**
 * Represents a task that must be completed by a deadline.
 */
public class Deadline extends Task {
    private final LocalDateTime endDate;

    /**
     * Constructs a deadline task.
     *
     * @param taskName Name of the task.
     * @param endDate  Date and time by which the task is due.
     */
    public Deadline(String taskName, LocalDateTime endDate) {
        super(taskName);
        this.endDate = endDate;
    }

    /**
     * Returns this Deadline in the save-file format.
     *
     * @return Serialized deadline.
     */
    @Override
    public String toFileString() {
        return String.format("D | %s | %s", getFileFields(),
                DateTimeParser.formatForStorage(this.endDate));
    }

    /**
     * Returns the display representation of this deadline.
     *
     * @return Display representation of this deadline.
     */
    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)", super.toString(),
                DateTimeParser.formatForDisplay(this.endDate));
    }
}
