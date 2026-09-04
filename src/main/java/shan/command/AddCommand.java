package shan.command;

import shan.ResponseSink;
import shan.exception.DataFileException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;

/**
 * Adds a task and persists the updated task list.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Constructs a command that adds the supplied task.
     *
     * @param task Task to add.
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Adds the task, saves the updated list, and reports the result.
     *
     * @param tasks   Task list to update.
     * @param responseSink Receives the message produced by the command.
     * @param storage Storage used to persist the updated task list.
     * @throws DataFileException If the updated task list cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, ResponseSink responseSink, Storage storage) throws DataFileException {
        tasks.add(this.task);
        try {
            storage.save(tasks.snapshot());
        } catch (DataFileException exception) {
            tasks.removeLast();
            throw exception;
        }
        responseSink.showMessage(String.format(
                "I Gotchu. I've added this:\n  %s\nNow you have %d tasks.",
                this.task, tasks.size()));
    }
}
