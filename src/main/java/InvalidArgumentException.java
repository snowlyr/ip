/**
 * Represents a command argument that has an invalid value.
 */
public class InvalidArgumentException extends ShanException {
    /**
   * Constructs an invalid-argument exception.
   *
   * @param message explanation of the invalid argument
   */
    public InvalidArgumentException(String message) {
        super(message);
    }
}
