import java.util.Scanner;

/**
 * Runs the Shan chatbot.
 */
public class Shan {
  private static final String DIVIDER = "____________________________________________________________";
  private static final String BANNER = " ____  _\n"
      + "/ ___|| |__   __ _ _ __\n"
      + "\\___ \\| '_ \\ / _` | '_ \\\n"
      + " ___) | | | | (_| | | | |\n"
      + "|____/|_| |_|\\__,_|_| |_| \n";
  private static Task[] taskList = new Task[100];
  private static int taskIdx = 0;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    System.out.println(DIVIDER);
    System.out.println(BANNER);
    String greeting = "Hey! I'm Shan.\nHow can I help?";
    System.out.println(greeting);
    System.out.println(DIVIDER);

    while (sc.hasNextLine()) {
      String inputLine = sc.nextLine().trim();
      sendMessage(reply(inputLine));

      if (inputLine.equals("bye")) {
        break;
      }
    }

    sc.close();
  }

  /**
   * Prints a message using Shan's standard response formatting.
   *
   * @param message message to print
   */
  private static void sendMessage(String message) {
    System.out.println(DIVIDER);
    for (String line : message.split("\n")) {
      System.out.println(" " + line);
    }
    System.out.println(DIVIDER);
  }

  /**
   * Returns Shan's reply to a user command.
   *
   * @param inputLine command entered by the user
   * @return Shan's reply
   */
  private static String reply(String inputLine) {
    String[] tokens = inputLine.split("\\s+", 2);
    String command = tokens[0];
    String arguments = tokens.length == 2 ? tokens[1] : "";

    return switch (command) {
      case "bye" -> commandExit();
      case "list" -> commandList();
      case "mark" -> commandMark(Integer.parseInt(arguments));
      case "unmark" -> commandUnmark(Integer.parseInt(arguments));
      case "todo" -> commandAddToDo(arguments);
      case "deadline" -> commandAddDeadline(arguments);
      case "event" -> commandAddEvent(arguments);
      default -> "I don't understand that command.";
    };
  }

  /**
   * Returns the farewell message.
   *
   * @return farewell message for {@code bye}
   */
  private static String commandExit() {
    return "Bye. Hope to see you again soon!";
  }

  /**
   * Adds a ToDo task.
   *
   * @param taskName name of task to add
   * @return reply when adding task
   */
  private static String commandAddToDo(String taskName) {
    if (taskName.isBlank()) {
      return "The task description cannot be empty.";
    }
    return addTask(new ToDo(taskName));
  }

  /**
   * Adds a Deadline task.
   *
   * @param arguments task description followed by {@code /by} and the deadline
   * @return reply when adding task
   */
  private static String commandAddDeadline(String arguments) {
    String[] details = arguments.split("/by", 2);

    if (details.length < 2) {
      return "Please specify a deadline using /by.";
    }
    if (details[0].isBlank() || details[1].isBlank()) {
      return "The deadline description and date cannot be empty.";
    }

    return addTask(new Deadline(details[0].trim(), details[1].trim()));
  }

  /**
   * Adds an Event task.
   *
   * @param arguments task description followed by {@code /from} and {@code /to}
   *                  values
   * @return reply when adding task
   */
  private static String commandAddEvent(String arguments) {
    String[] fromDetails = arguments.split("/from", 2);

    if (fromDetails.length < 2) {
      return "Please specify the event start using /from.";
    }

    String[] toDetails = fromDetails[1].split("/to", 2);

    if (toDetails.length < 2) {
      return "Please specify the event end using /to.";
    }
    if (fromDetails[0].isBlank() || toDetails[0].isBlank() || toDetails[1].isBlank()) {
      return "The event description, start, and end cannot be empty.";
    }

    return addTask(new Event(fromDetails[0].trim(), toDetails[0].trim(), toDetails[1].trim()));
  }

  /**
   * Adds a task to the task list.
   *
   * @param task task to add
   * @return reply confirming that the task was added
   */
  private static String addTask(Task task) {
    taskList[taskIdx] = task;
    taskIdx++;
    return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, taskIdx);
  }

  /**
   * Returns the list of tasks
   *
   * @return enumerated list of tasks
   */
  private static String commandList() {
    StringBuilder res = new StringBuilder("Here are the tasks in your list:");
    for (int i = 0; i < taskIdx; i++) {
      res.append(String.format("\n%d.%s", i + 1, taskList[i]));
    }
    return res.toString();
  }

  /**
   * Mark the specified task index as done
   *
   * @param idx task index to mark as done
   * @return reply when marked as done
   */
  private static String commandMark(int idx) {
    if (idx > taskIdx || idx < 1) {
      return "Woopsies, this task does not exist!!";
    }
    String res = taskList[idx - 1].markDone();
    return String.format("Well done! I have marked this task as done!\n  %s", res);
  }

  /**
   * Unmark the specified task index as done
   *
   * @param idx task index to unmark
   * @return reply when unmarked
   */
  private static String commandUnmark(int idx) {
    if (idx > taskIdx || idx < 1) {
      return "oops, this task does not exist!!";
    }
    String res = taskList[idx - 1].unmarkDone();
    return String.format("What happened? I have unmarked this task as completed...\n  %s", res);
  }
}
