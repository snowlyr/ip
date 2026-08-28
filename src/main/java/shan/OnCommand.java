package shan;

import java.time.LocalDate;

/**
 * Displays deadlines and events occurring within an inclusive date range.
 */
public class OnCommand extends Command {
    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Constructs a command that searches the supplied inclusive date range.
     *
     * @param startDate First date in the range.
     * @param endDate   Last date in the range.
     */
    public OnCommand(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        boolean isRange = !this.startDate.equals(this.endDate);
        String displayStartDate = DateTimeParser.formatDateForDisplay(this.startDate);
        String displayEndDate = DateTimeParser.formatDateForDisplay(this.endDate);
        String resultHeading = isRange
                ? String.format("Here are the deadlines and events from %s to %s:",
                        displayStartDate, displayEndDate)
                : String.format("Got your deadlines and events on %s:", displayStartDate);
        StringBuilder result = new StringBuilder(resultHeading);
        int matchCount = 0;
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            Task task = tasks.get(taskNumber);
            if (task.occursBetween(this.startDate, this.endDate)) {
                result.append(String.format("\n%d.%s", taskNumber, task));
                matchCount++;
            }
        }

        if (matchCount == 0) {
            ui.showMessage(isRange
                    ? String.format("There are no deadlines or events from %s to %s.",
                            displayStartDate, displayEndDate)
                    : String.format("There are no deadlines or events on %s.", displayStartDate));
            return;
        }
        ui.showMessage(result.toString());
    }
}
