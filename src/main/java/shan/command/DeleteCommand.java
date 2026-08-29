package shan.command;

import shan.exception.DataFileException;
import shan.exception.InvalidArgumentException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Deletes a task and persists the updated task list.
 */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /**
     * Constructs a command that deletes the specified task.
     *
     * @param taskNumber One-based number of the task to delete.
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws InvalidArgumentException, DataFileException {
        if (!tasks.containsTaskNumber(this.taskNumber)) {
            throw new InvalidArgumentException("Woopsies, this task does not exist!!");
        }

        Task removedTask = tasks.delete(this.taskNumber);
        try {
            storage.save(tasks.snapshot());
        } catch (DataFileException exception) {
            tasks.restore(this.taskNumber, removedTask);
            throw exception;
        }
        ui.showMessage(String.format(
                "Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, tasks.size()));
    }
}
