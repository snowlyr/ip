package shan.command;

import shan.storage.Storage;
import shan.task.TaskList;
import shan.ui.Ui;

/**
 * Displays every task in the task list.
 */
public class ListCommand extends Command {
    /**
     * Constructs a list command.
     */
    public ListCommand() {
    }

    /**
     * Displays every task in its current numbered order.
     *
     * @param tasks   Task list to display.
     * @param ui      User interface through which to display the tasks.
     * @param storage Storage collaborator; unused because listing does not persist changes.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        StringBuilder result = new StringBuilder("Here are the tasks in your list:");
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            result.append(String.format("\n%d.%s", taskNumber, tasks.get(taskNumber)));
        }
        ui.showMessage(result.toString());
    }
}
