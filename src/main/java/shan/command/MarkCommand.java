package shan.command;

import shan.exception.DataFileException;
import shan.exception.InvalidArgumentException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Marks a task as done and persists the change.
 */
public class MarkCommand extends Command {
    private final int taskNumber;

    /**
     * Constructs a command that marks the specified task as done.
     *
     * @param taskNumber One-based number of the task to mark.
     */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws InvalidArgumentException, DataFileException {
        if (!tasks.containsTaskNumber(this.taskNumber)) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }

        Task task = tasks.get(this.taskNumber);
        boolean wasDone = task.isDone();
        String taskDisplay = task.markDone();
        try {
            storage.save(tasks.snapshot());
        } catch (DataFileException exception) {
            if (!wasDone) {
                task.unmarkDone();
            }
            throw exception;
        }
        ui.showMessage(String.format(
                "Well done! I have marked this task as done!\n  %s", taskDisplay));
    }
}
