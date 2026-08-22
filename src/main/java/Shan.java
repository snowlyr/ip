import java.util.Scanner;

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

    System.out.print("> ");
    while (sc.hasNextLine()) {
      String inputLine = sc.nextLine();
      String[] tokens = inputLine.split("\\s+");
      String command = tokens[0];

      switch (command) {
        case "bye" -> sendMessage(commandExit());
        case "list" -> sendMessage(commandList());
        case "mark" -> sendMessage(commandMark(Integer.parseInt(tokens[1])));
        case "unmark" -> sendMessage(commandUnmark(Integer.parseInt(tokens[1])));
        default -> sendMessage(commandAdd(inputLine));
      }
      if (command.equals("bye")) {
        break;
      }
      System.out.print("> ");
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
   * Return farewell message
   *
   * @return farewell message for (@code bye)
   */
  private static String commandExit() {
    return "Bye. Hope to see you again soon!!!";
  }

  /**
   * Add a task to taskList
   *
   * @param taskName name of task to add
   * @return reply when adding task
   */
  private static String commandAdd(String taskName) {
    taskList[taskIdx] = new Task(taskName);
    taskIdx++;
    return String.format("added: %s", taskName);
  }

  /**
   * Returns the list of tasks
   *
   * @return enumerated list of tasks
   */
  private static String commandList() {
    String res = "\n";
    for (int i = 0; i < taskIdx; i++) {
      res = res + String.format("%d. %s\n", i + 1, taskList[i].toString());
    }
    return res.substring(0, res.length() - 1);
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
