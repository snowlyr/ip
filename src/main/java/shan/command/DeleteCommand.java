package shan.command;

import shan.ResponseSink;
import shan.exception.DataFileException;
import shan.exception.InvalidArgumentException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;

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

    /**
     * Deletes the selected task, saves the updated list, and reports the result.
     *
     * @param tasks   Task list to update.
     * @param responseSink Receives the message produced by the command.
     * @param storage Storage used to persist the updated task list.
     * @throws InvalidArgumentException If the task number does not exist.
     * @throws DataFileException        If the updated task list cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, ResponseSink responseSink, Storage storage)
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
        responseSink.showMessage(String.format(
                "Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
                removedTask, tasks.size()));
    }
}
