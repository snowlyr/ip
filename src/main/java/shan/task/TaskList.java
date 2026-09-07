package shan.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of tasks used by Shan.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /**
     * Constructs an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Removes every task from the list.
     */
    public void clear() {
        this.tasks.clear();
    }

    /**
     * Replaces the current tasks with the supplied tasks.
     *
     * @param tasks Tasks that should populate this list.
     */
    public void replaceAll(List<Task> tasks) {
        assert tasks != null : "Replacement task list must not be null";
        assert tasks.stream().allMatch(task -> task != null) : "Replacement task list must not contain null";
        this.tasks.clear();
        this.tasks.addAll(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        assert task != null : "Task to add must not be null";
        this.tasks.add(task);
    }

    /**
     * Deletes the task with the supplied one-based task number.
     *
     * @param taskNumber One-based number of the task to delete.
     * @return Deleted task.
     */
    public Task delete(int taskNumber) {
        assert containsTaskNumber(taskNumber) : "Task number to delete must exist";
        return this.tasks.remove(taskNumber - 1);
    }

    /**
     * Restores a task at its former one-based position.
     *
     * @param taskNumber One-based position at which to restore the task.
     * @param task       Task to restore.
     */
    public void restore(int taskNumber, Task task) {
        assert taskNumber >= 1 && taskNumber <= this.tasks.size() + 1
                : "Restore position must be within the task list";
        assert task != null : "Task to restore must not be null";
        this.tasks.add(taskNumber - 1, task);
    }

    /**
     * Removes and returns the final task in the list.
     *
     * @return Removed task.
     */
    public Task removeLast() {
        assert !this.tasks.isEmpty() : "Task list must not be empty when removing its final task";
        return this.tasks.remove(this.tasks.size() - 1);
    }

    /**
     * Returns the task with the supplied one-based task number.
     *
     * @param taskNumber One-based number of the task to return.
     * @return Task with the supplied number.
     */
    public Task get(int taskNumber) {
        assert containsTaskNumber(taskNumber) : "Task number to retrieve must exist";
        return this.tasks.get(taskNumber - 1);
    }

    /**
     * Returns whether the supplied one-based task number exists.
     *
     * @param taskNumber One-based task number to check.
     * @return {@code true} if the task number exists.
     */
    public boolean containsTaskNumber(int taskNumber) {
        return taskNumber >= 1 && taskNumber <= this.tasks.size();
    }

    /**
     * Returns tasks whose descriptions contain the supplied keyword.
     *
     * @param keyword Keyword to find in task descriptions.
     * @return Immutable list of matching tasks in their original order.
     */
    public List<Task> find(String keyword) {
        ArrayList<Task> matchingTasks = new ArrayList<>();
        for (Task task : this.tasks) {
            if (task.containsKeyword(keyword)) {
                matchingTasks.add(task);
            }
        }
        return List.copyOf(matchingTasks);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return Number of tasks.
     */
    public int size() {
        return this.tasks.size();
    }

    /**
     * Returns an immutable snapshot for persistence.
     *
     * @return Immutable copy of the current tasks.
     */
    public List<Task> snapshot() {
        return List.copyOf(this.tasks);
    }
}
