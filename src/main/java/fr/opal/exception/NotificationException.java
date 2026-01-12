package fr.opal.exception;

/**
 * Custom unchecked exception for notification errors.
 * Wraps lower-level exceptions.
 */
public class NotificationException extends RuntimeException {

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public NotificationException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message the error message
     * @param cause the cause
     */
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause
     *
     * @param cause the cause
     */
    public NotificationException(Throwable cause) {
        super(cause);
    }
}
