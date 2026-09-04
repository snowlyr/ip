package shan.command;

import shan.ResponseSink;
import shan.storage.Storage;
import shan.task.TaskList;

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
     * @param responseSink Receives the message produced by the command.
     * @param storage Storage collaborator; unused because exiting does not persist changes.
     */
    @Override
    public void execute(TaskList tasks, ResponseSink responseSink, Storage storage) {
        responseSink.showMessage("Bye! See you soon.");
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
