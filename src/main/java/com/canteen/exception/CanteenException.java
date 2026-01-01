package com.canteen.exception;

/**
 * Custom exception class for Canteen Billing System
 * Demonstrates Exception Handling requirement
 */
public class CanteenException extends Exception {
    private String errorCode;
    
    public CanteenException(String message) {
        super(message);
    }
    
    public CanteenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CanteenException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
