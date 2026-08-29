package shan.command;

import shan.exception.DataFileException;
import shan.exception.InvalidArgumentException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Marks a task as not done and persists the change.
 */
public class UnmarkCommand extends Command {
    private final int taskNumber;

    /**
     * Constructs a command that marks the specified task as not done.
     *
     * @param taskNumber One-based number of the task to unmark.
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Marks the selected task as incomplete, saves the change, and reports the result.
     *
     * @param tasks   Task list containing the task to unmark.
     * @param ui      User interface through which to report the result.
     * @param storage Storage used to persist the updated task list.
     * @throws InvalidArgumentException If the task number does not exist.
     * @throws DataFileException        If the updated task list cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws InvalidArgumentException, DataFileException {
        if (!tasks.containsTaskNumber(this.taskNumber)) {
            throw new InvalidArgumentException("oops, this task does not exist!!");
        }

        Task task = tasks.get(this.taskNumber);
        boolean wasDone = task.isDone();
        String taskDisplay = task.unmarkDone();
        try {
            storage.save(tasks.snapshot());
        } catch (DataFileException exception) {
            if (wasDone) {
                task.markDone();
            }
            throw exception;
        }
        ui.showMessage(String.format(
                "What happened? I have unmarked this task as completed...\n  %s",
                taskDisplay));
    }
}
