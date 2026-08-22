import java.util.ArrayList;
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
  private static final ArrayList<Task> taskList = new ArrayList<>();

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    System.out.println(DIVIDER);
    System.out.println(BANNER);
    String greeting = "Hey! I'm Shan.\nHow can I help?";
    System.out.println(greeting);
    System.out.println(DIVIDER);

    while (sc.hasNextLine()) {
      String inputLine = sc.nextLine().trim();
      try {
        sendMessage(reply(inputLine));
      } catch (ShanException e) {
        sendMessage(e.getMessage());
      }

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
    System.out.println("Shan: " + message);
    System.out.println(DIVIDER);
  }

  /**
   * Returns Shan's reply to a user command.
   *
   * @param inputLine command entered by the user
   * @return Shan's reply
   * @throws InvalidCommandException  if the command is blank or unknown
   * @throws MissingArgumentException if a required argument is missing
   * @throws InvalidArgumentException if an argument has an invalid value
   */
  private static String reply(String inputLine)
      throws InvalidCommandException, MissingArgumentException, InvalidArgumentException {
    if (inputLine.isBlank()) {
      throw new InvalidCommandException("Enter a command dood.");
    }

    String[] tokens = inputLine.split("\\s+", 2);
    String command = tokens[0];
    String arguments = tokens.length == 2 ? tokens[1] : "";

    return switch (command) {
      case "bye" -> commandExit();
      case "list" -> commandList();
      case "mark" -> commandMark(parseTaskNumber(arguments));
      case "unmark" -> commandUnmark(parseTaskNumber(arguments));
      case "delete" -> commandDelete(parseTaskNumber(arguments));
      case "todo" -> commandAddToDo(arguments);
      case "deadline" -> commandAddDeadline(arguments);
      case "event" -> commandAddEvent(arguments);
      default -> throw new InvalidCommandException("I don't understand bro.");
    };
  }

  /**
   * Parses a task number supplied to a mark or unmark command.
   *
   * @param argument task number entered by the user
   * @return parsed task number
   * @throws MissingArgumentException if the task number is missing
   * @throws InvalidArgumentException if the task number is not an integer
   */
  private static int parseTaskNumber(String argument)
      throws MissingArgumentException, InvalidArgumentException {
    if (argument.isBlank()) {
      throw new MissingArgumentException("Specify a task number.");
    }

    try {
      return Integer.parseInt(argument);
    } catch (NumberFormatException e) {
      throw new InvalidArgumentException("The task number must be an int.");
    }
  }

  /**
   * Returns the farewell message.
   *
   * @return farewell message for {@code bye}
   */
  private static String commandExit() {
    return "Bye! See you soon.";
  }

  /**
   * Adds a ToDo task.
   *
   * @param taskName name of task to add
   * @return reply when adding task
   * @throws MissingArgumentException if the task description is empty
   */
  private static String commandAddToDo(String taskName) throws MissingArgumentException {
    if (taskName.isBlank()) {
      throw new MissingArgumentException("The task description cannot be empty my guy.");
    }
    return addTask(new ToDo(taskName));
  }

  /**
   * Adds a Deadline task.
   *
   * @param arguments task description followed by {@code /by} and the deadline
   * @return reply when adding task
   * @throws MissingArgumentException if the description, deadline, or delimiter
   *                                  is missing
   */
  private static String commandAddDeadline(String arguments) throws MissingArgumentException {
    if (arguments.isBlank()) {
      throw new MissingArgumentException("The deadline description cannot be empty, else its not a deadline");
    }

    String[] details = arguments.split("/by", 2);

    if (details.length < 2) {
      throw new MissingArgumentException("Please specify a deadline using /by.");
    }
    if (details[0].isBlank() || details[1].isBlank()) {
      throw new MissingArgumentException("The deadline description and date cannot be empty bruh.");
    }

    return addTask(new Deadline(details[0].trim(), details[1].trim()));
  }

  /**
   * Adds an Event task.
   *
   * @param arguments task description followed by {@code /from} and {@code /to}
   *                  values
   * @return reply when adding task
   * @throws MissingArgumentException if the description, times, or delimiters are
   *                                  missing
   */
  private static String commandAddEvent(String arguments) throws MissingArgumentException {
    if (arguments.isBlank()) {
      throw new MissingArgumentException("The event description cannot be empty...");
    }

    String[] fromDetails = arguments.split("/from", 2);
    if (fromDetails.length < 2) {
      throw new MissingArgumentException("Specify the event start using /from.");
    }

    String[] toDetails = fromDetails[1].split("/to", 2);
    if (toDetails.length < 2) {
      throw new MissingArgumentException("Specify the event end using /to.");
    }
    if (fromDetails[0].isBlank() || toDetails[0].isBlank() || toDetails[1].isBlank()) {
      throw new MissingArgumentException("The event description, start, and end cannot be empty, lock in bro.");
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
    taskList.add(task);
    return String.format("I Gotchu. I've added this:\n  %s\nNow you have %d tasks.", task, taskList.size());
  }

  /**
   * Returns the list of tasks
   *
   * @return enumerated list of tasks
   */
  private static String commandList() {
    StringBuilder res = new StringBuilder("Here are the tasks in your list:");
    for (int i = 0; i < taskList.size(); i++) {
      res.append(String.format("\n%d.%s", i + 1, taskList.get(i)));
    }
    return res.toString();
  }

  /**
   * Mark the specified task index as done
   *
   * @param idx task index to mark as done
   * @return reply when marked as done
   * @throws InvalidArgumentException if the task index does not exist
   */
  private static String commandMark(int idx) throws InvalidArgumentException {
    if (idx > taskList.size() || idx < 1) {
      throw new InvalidArgumentException("Woopsies, this task does not exist!!");
    }
    String res = taskList.get(idx - 1).markDone();
    return String.format("Well done! I have marked this task as done!\n  %s", res);
  }

  /**
   * Unmark the specified task index as done
   *
   * @param idx task index to unmark
   * @return reply when unmarked
   * @throws InvalidArgumentException if the task index does not exist
   */
  private static String commandUnmark(int idx) throws InvalidArgumentException {
    if (idx > taskList.size() || idx < 1) {
      throw new InvalidArgumentException("oops, this task does not exist!!");
    }
    String res = taskList.get(idx - 1).unmarkDone();
    return String.format("What happened? I have unmarked this task as completed...\n  %s", res);
  }

  /**
   * Deletes the task at the specified index.
   *
   * @param idx task index to delete
   * @return reply confirming which task was deleted
   * @throws InvalidArgumentException if the task index does not exist
   */
  private static String commandDelete(int idx) throws InvalidArgumentException {
    if (idx > taskList.size() || idx < 1) {
      throw new InvalidArgumentException("Woopsies, this task does not exist!!");
    }
    Task removedTask = taskList.remove(idx - 1);
    return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks.",
        removedTask, taskList.size());
  }
}
