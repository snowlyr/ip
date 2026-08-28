/**
 * Represents a task that can be marked as completed.
 */
public abstract class Task {
    private String taskName;
    private boolean markedDone = false;

    /**
   * Constructs a task.
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
   * Returns whether this task is completed.
   *
   * @return {@code true} when completed
   */
    public boolean isDone() {
        return this.markedDone;
    }

    /**
   * Returns the fields shared by all task types in the save-file format.
   *
   * @return completion status and task name separated by {@code |}
   */
    protected String getFileFields() {
        return String.format("%s | %s", this.markedDone ? "1" : "0", this.taskName);
    }

    /**
   * Returns this task in the save-file format.
   *
   * @return serialized task
   */
    public abstract String toFileString();

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
