package fr.opal.exception;

/**
 * Custom unchecked exception for database access errors.
 * Wraps lower-level exceptions like SQLException.
 */
public class DataAccessException extends RuntimeException {
    
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
