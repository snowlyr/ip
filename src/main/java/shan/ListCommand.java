package shan;

/**
 * Displays every task in the task list.
 */
public class ListCommand extends Command {
    /**
     * Constructs a list command.
     */
    public ListCommand() {
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        StringBuilder result = new StringBuilder("Here are the tasks in your list:");
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            result.append(String.format("\n%d.%s", taskNumber, tasks.get(taskNumber)));
        }
        ui.showMessage(result.toString());
    }
}
