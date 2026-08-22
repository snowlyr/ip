public class Task {
  private String taskName;
  private boolean markedDone = false;

  /**
   * Constrcuts task
   *
   * @param taskName name of task
   */
  public Task(String taskName) {
    this.taskName = taskName;
  }

  /**
   * Mark this task as done
   *
   * @return this task string
   */
  public String markDone() {
    this.markedDone = true;
    return this.toString();
  }

  /**
   * Unmark this task as done
   *
   * @return this task string
   */
  public String unmarkDone() {
    this.markedDone = false;
    return this.toString();
  }

  /**
   * Return string display of task
   *
   * @return string description of this task
   */
  @Override
  public String toString() {
    return String.format("[%s] %s", this.markedDone ? "X" : " ", this.taskName);
  }
}
