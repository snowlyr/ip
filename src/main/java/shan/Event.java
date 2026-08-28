package shan;

import java.time.LocalDateTime;

/**
 * Represents a task that occurs over a period of time.
 */
public class Event extends Task {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    /**
     * Constructs an event with start and end date-times.
     *
     * @param taskName  Name of the task.
     * @param startDate Start date and time of the event.
     * @param endDate   End date and time of the event.
     */
    public Event(String taskName, LocalDateTime startDate, LocalDateTime endDate) {
        super(taskName);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns this Event in the save-file format.
     *
     * @return Serialized event.
     */
    @Override
    public String toFileString() {
        return String.format("E | %s | %s | %s", getFileFields(),
                DateTimeParser.formatForStorage(this.startDate),
                DateTimeParser.formatForStorage(this.endDate));
    }

    /**
     * Returns the display representation of this event.
     *
     * @return Display representation of this event.
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(),
                DateTimeParser.formatForDisplay(this.startDate),
                DateTimeParser.formatForDisplay(this.endDate));
    }
}
