import java.util.Scanner;

public class Shan {
  private static final String DIVIDER = "____________________________________________________________";
  private static final String BANNER = " ____  _\n"
      + "/ ___|| |__   __ _ _ __\n"
      + "\\___ \\| '_ \\ / _` | '_ \\\n"
      + " ___) | | | | (_| | | | |\n"
      + "|____/|_| |_|\\__,_|_| |_| \n";
  private static String[] taskList = new String[100];
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
      String command = sc.nextLine();

      switch (command) {
        case "bye" -> sendMessage(commandExit());
        case "list" -> sendMessage(commandList());
        default -> sendMessage(commandAdd(command));
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
   * @param task task to add
   * @return reply when adding task
   */
  private static String commandAdd(String task) {
    taskList[taskIdx] = task;
    taskIdx++;
    return String.format("added: %s", task);
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
}
