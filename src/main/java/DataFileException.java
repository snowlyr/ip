/**
 * Represents a failure to read or write Shan's task data file.
 */
public class DataFileException extends ShanException {
    /**
     * Constructs a data-file exception.
     *
     * @param message explanation suitable for displaying to the user
     */
    public DataFileException(String message) {
        super(message);
    }
}
