/**
 * Represents a command that Shan does not recognize or cannot process.
 */
public class InvalidCommandException extends ShanException {
  /**
   * Constructs an invalid-command exception.
   *
   * @param message explanation of the invalid command
   */
  public InvalidCommandException(String message) {
    super(message);
  }
}
