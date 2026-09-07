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
        this.tasks.clear();
        this.tasks.addAll(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        this.tasks.add(task);
    }

    /**
     * Deletes the task with the supplied one-based task number.
     *
     * @param taskNumber One-based number of the task to delete.
     * @return Deleted task.
     */
    public Task delete(int taskNumber) {
        return this.tasks.remove(taskNumber - 1);
    }

    /**
     * Restores a task at its former one-based position.
     *
     * @param taskNumber One-based position at which to restore the task.
     * @param task       Task to restore.
     */
    public void restore(int taskNumber, Task task) {
        this.tasks.add(taskNumber - 1, task);
    }

    /**
     * Removes and returns the final task in the list.
     *
     * @return Removed task.
     */
    public Task removeLast() {
        return this.tasks.remove(this.tasks.size() - 1);
    }

    /**
     * Returns the task with the supplied one-based task number.
     *
     * @param taskNumber One-based number of the task to return.
     * @return Task with the supplied number.
     */
    public Task get(int taskNumber) {
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
        return this.tasks.stream()
                .filter(task -> task.containsKeyword(keyword))
                .toList();
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
