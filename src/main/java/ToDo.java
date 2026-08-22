/**
 * Represents a task without a date or time.
 */
public class ToDo extends Task {
  /**
   * Constructs a ToDo task.
   *
   * @param taskName name of task
   */
  public ToDo(String taskName) {
    super(taskName);
  }

  /**
   * Return string representation of ToDo task
   *
   * @return string representation of ToDo task
   */
  @Override
  public String toString() {
    return String.format("[T]%s", super.toString());
  }
}
