package com.canteen.exception;

/**
 * Exception for invalid order operations
 */
public class InvalidOrderException extends CanteenException {
    public InvalidOrderException(String message) {
        super(message, "INVALID_ORDER");
    }
}
