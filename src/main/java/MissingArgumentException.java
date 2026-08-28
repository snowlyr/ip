/**
 * Represents a command that is missing a required argument.
 */
public class MissingArgumentException extends ShanException {
    /**
   * Constructs a missing-argument exception.
   *
   * @param message explanation of the missing argument
   */
    public MissingArgumentException(String message) {
        super(message);
    }
}
