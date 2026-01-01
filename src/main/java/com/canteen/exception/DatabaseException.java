package com.canteen.exception;

/**
 * Exception for database-related errors
 */
public class DatabaseException extends CanteenException {
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
