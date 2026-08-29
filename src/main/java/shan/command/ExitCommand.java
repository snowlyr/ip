package shan.command;

import shan.storage.Storage;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Displays the farewell message and exits Shan.
 */
public class ExitCommand extends Command {
    /**
     * Constructs an exit command.
     */
    public ExitCommand() {
    }

    /**
     * Displays Shan's farewell message.
     *
     * @param tasks   Task list collaborator; unchanged by this command.
     * @param ui      User interface through which to display the farewell.
     * @param storage Storage collaborator; unused because exiting does not persist changes.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMessage("Bye! See you soon.");
    }

    /**
     * Returns whether this command ends the application.
     *
     * @return {@code true} because this is the exit command.
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
