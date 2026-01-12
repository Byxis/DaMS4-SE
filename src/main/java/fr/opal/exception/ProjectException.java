package fr.opal.exception;

/**
 * Custom unchecked exception for project access errors.
 * Wraps lower-level exceptions.
 */
public class ProjectException extends RuntimeException {

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public ProjectException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message the error message
     * @param cause the cause
     */
    public ProjectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with cause
     *
     * @param cause the cause
     */
    public ProjectException(Throwable cause)
    {
        super(cause);
    }
}
