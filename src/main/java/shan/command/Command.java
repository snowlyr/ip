package shan.command;

import shan.exception.ShanException;
import shan.storage.Storage;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Represents a user command that can act on Shan's collaborators.
 */
public abstract class Command {
    /**
     * Constructs a command.
     */
    protected Command() {
    }

    /**
     * Executes this command.
     *
     * @param tasks   Task list on which the command operates.
     * @param ui      User interface through which the command responds.
     * @param storage Storage used to persist task changes.
     * @throws ShanException If the command cannot be completed.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws ShanException;

    /**
     * Returns whether Shan should exit after executing this command.
     *
     * @return {@code true} when this command exits Shan.
     */
    public boolean isExit() {
        return false;
    }
}
