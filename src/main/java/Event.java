/**
 * Represents a task that occurs over a period of time.
 */
public class Event extends Task {
    private String startDate;
    private String endDate;

    /**
     * Constructs event with start and end dates
     *
     * @param taskName  name of task
     * @param startDate start date of event
     * @param endDate   end date of event
     */
    public Event(String taskName, String startDate, String endDate) {
        super(taskName);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns this Event in the save-file format.
     *
     * @return serialized Event
     */
    @Override
    public String toFileString() {
        return String.format("E | %s | %s | %s", getFileFields(), this.startDate, this.endDate);
    }

    /**
     * Return string representation of event
     *
     * @return string representation of event
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(), this.startDate, this.endDate);
    }
}
