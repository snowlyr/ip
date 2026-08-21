import java.util.Scanner;

public class Shan {
  private static final String DIVIDER = "____________________________________________________________";
  private static final String BANNER = " ____  _\n"
      + "/ ___|| |__   __ _ _ __\n"
      + "\\___ \\| '_ \\ / _` | '_ \\\n"
      + " ___) | | | | (_| | | | |\n"
      + "|____/|_| |_|\\__,_|_| |_| \n";

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
      sendMessage(reply(command));

      if (command.equals("bye")) {
        break;
      }
      System.out.print("> ");
    }

    sc.close();
  }

  /**
   * Determines Shan's response to a command
   *
   * @param command command entered by the user
   * @return farewell message for (@code bye), or the original command otherwise
   */
  private static String reply(String command) {
    if (command.equals("bye")) {
      return "Bye. Hope to see you again soon!!!";
    }

    return command;
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
}
