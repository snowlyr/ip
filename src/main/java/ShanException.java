/**
 * Base type for errors caused by user input.
 */
public class ShanException extends Exception {
  /**
   * Constructs an exception with a message suitable for displaying to the user.
   *
   * @param message explanation of the invalid command
   */
  public ShanException(String message) {
    super(message);
  }
}
