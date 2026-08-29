package shan.command;

import shan.exception.DataFileException;
import shan.storage.Storage;
import shan.task.Task;
import shan.task.TaskList;
import shan.ui.Ui;

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

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DataFileException {
        tasks.add(this.task);
        try {
            storage.save(tasks.snapshot());
        } catch (DataFileException exception) {
            tasks.removeLast();
            throw exception;
        }
        ui.showMessage(String.format(
                "I Gotchu. I've added this:\n  %s\nNow you have %d tasks.",
                this.task, tasks.size()));
    }
}
