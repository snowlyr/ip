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

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMessage("Bye! See you soon.");
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
