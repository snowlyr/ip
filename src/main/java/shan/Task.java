package shan;

import java.time.LocalDate;

/**
 * Represents a task that can be marked as completed.
 */
public abstract class Task {
    private final String taskName;
    private boolean isDone = false;

    /**
     * Constructs a task.
     *
     * @param taskName Name of the task.
     */
    public Task(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Marks this task as done.
     *
     * @return Display representation of this task.
     */
    public String markDone() {
        this.isDone = true;
        return this.toString();
    }

    /**
     * Marks this task as not done.
     *
     * @return Display representation of this task.
     */
    public String unmarkDone() {
        this.isDone = false;
        return this.toString();
    }

    /**
     * Returns whether this task is completed.
     *
     * @return {@code true} when completed.
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * Returns whether this task occurs on the specified date.
     *
     * @param date Date to check.
     * @return {@code true} when this task occurs on the date.
     */
    public boolean occursOn(LocalDate date) {
        return occursBetween(date, date);
    }

    /**
     * Returns whether this task occurs within an inclusive date range.
     *
     * @param startDate First date in the range.
     * @param endDate   Last date in the range.
     * @return {@code true} when this task occurs within the range.
     */
    public boolean occursBetween(LocalDate startDate, LocalDate endDate) {
        return false;
    }

    /**
     * Returns the fields shared by all task types in the save-file format.
     *
     * @return Completion status and task name separated by {@code |}.
     */
    protected String getFileFields() {
        return String.format("%s | %s", this.isDone ? "1" : "0", this.taskName);
    }

    /**
     * Returns this task in the save-file format.
     *
     * @return Serialized task.
     */
    public abstract String toFileString();

    /**
     * Returns the display representation of this task.
     *
     * @return Display representation of this task.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.isDone ? "X" : " ", this.taskName);
    }
}
