/**
 * Represents a task that must be completed by a deadline.
 */
public class Deadline extends Task {
  private String endDate;

  /**
   * Constructs deadline
   *
   * @param taskName name of task
   * @param endDate  the date before its due
   */
  public Deadline(String taskName, String endDate) {
    super(taskName);
    this.endDate = endDate;
  }

  /**
   * Return string representation of deadline
   *
   * @return string representation of deadline
   */
  @Override
  public String toString() {
    return String.format("[D]%s (by: %s)", super.toString(), this.endDate);
  }
}
