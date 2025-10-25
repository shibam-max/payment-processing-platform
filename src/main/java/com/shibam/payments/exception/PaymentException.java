package com.shibam.payments.exception;

public class PaymentException extends RuntimeException {
    
    private final String errorCode;
    
    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
    }
    
    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException(String transactionId) {
        super("Payment not found: " + transactionId, "PAYMENT_NOT_FOUND");
    }
}

class InsufficientFundsException extends PaymentException {
    public InsufficientFundsException() {
        super("Insufficient funds for transaction", "INSUFFICIENT_FUNDS");
    }
}

class FraudDetectedException extends PaymentException {
    public FraudDetectedException(String reason) {
        super("Transaction blocked by fraud detection: " + reason, "FRAUD_DETECTED");
    }
}

class PaymentGatewayException extends PaymentException {
    public PaymentGatewayException(String message) {
        super("Payment gateway error: " + message, "GATEWAY_ERROR");
    }
}