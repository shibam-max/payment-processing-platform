package com.shibam.payments.service;

import com.shibam.payments.dto.PaymentResponse;
import com.shibam.payments.model.Payment;
import com.shibam.payments.model.Payment.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class PaymentGatewayService {
    
    private final Random random = new Random();
    
    public PaymentResponse processPayment(Payment payment) {
        log.info("Processing payment with gateway: {}", payment.getTransactionId());
        
        try {
            // Simulate gateway processing time
            Thread.sleep(100 + random.nextInt(200));
            
            // Simulate gateway response (90% success rate)
            boolean isSuccessful = random.nextDouble() > 0.1;
            
            if (isSuccessful) {
                PaymentStatus status = determinePaymentStatus(payment);
                String message = getSuccessMessage(status);
                
                log.info("Payment gateway success: {} - {}", payment.getTransactionId(), status);
                return PaymentResponse.success(payment.getTransactionId(), status, message);
            } else {
                String errorMessage = getRandomErrorMessage();
                log.warn("Payment gateway failed: {} - {}", payment.getTransactionId(), errorMessage);
                return PaymentResponse.error(payment.getTransactionId(), errorMessage);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted: {}", payment.getTransactionId());
            return PaymentResponse.error(payment.getTransactionId(), "Payment processing interrupted");
        } catch (Exception e) {
            log.error("Payment gateway error: {}", e.getMessage(), e);
            return PaymentResponse.error(payment.getTransactionId(), "Gateway processing error");
        }
    }
    
    public PaymentResponse refundPayment(Payment payment) {
        log.info("Processing refund with gateway: {}", payment.getTransactionId());
        
        try {
            // Simulate refund processing
            Thread.sleep(150 + random.nextInt(100));
            
            // Simulate refund response (95% success rate for refunds)
            boolean isSuccessful = random.nextDouble() > 0.05;
            
            if (isSuccessful) {
                log.info("Refund gateway success: {}", payment.getTransactionId());
                return PaymentResponse.success(payment.getTransactionId(), PaymentStatus.REFUNDED, "Refund processed successfully");
            } else {
                log.warn("Refund gateway failed: {}", payment.getTransactionId());
                return PaymentResponse.error(payment.getTransactionId(), "Refund processing failed at gateway");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResponse.error(payment.getTransactionId(), "Refund processing interrupted");
        }
    }
    
    private PaymentStatus determinePaymentStatus(Payment payment) {
        // For card payments, go through authorization -> capture -> settlement
        if ("CARD".equals(payment.getPaymentMethod())) {
            return PaymentStatus.AUTHORIZED; // Would be captured and settled later
        }
        
        // For digital wallets, direct settlement
        if ("WALLET".equals(payment.getPaymentMethod())) {
            return PaymentStatus.SETTLED;
        }
        
        // For bank transfers, captured status
        if ("BANK_TRANSFER".equals(payment.getPaymentMethod())) {
            return PaymentStatus.CAPTURED;
        }
        
        return PaymentStatus.AUTHORIZED;
    }
    
    private String getSuccessMessage(PaymentStatus status) {
        return switch (status) {
            case AUTHORIZED -> "Payment authorized successfully";
            case CAPTURED -> "Payment captured successfully";
            case SETTLED -> "Payment settled successfully";
            default -> "Payment processed successfully";
        };
    }
    
    public PaymentResponse capturePayment(Payment payment) {
        log.info("Processing capture with gateway: {}", payment.getTransactionId());
        
        try {
            Thread.sleep(50 + random.nextInt(100));
            
            boolean isSuccessful = random.nextDouble() > 0.02;
            
            if (isSuccessful) {
                log.info("Capture gateway success: {}", payment.getTransactionId());
                return PaymentResponse.success(payment.getTransactionId(), PaymentStatus.CAPTURED, "Payment captured successfully");
            } else {
                log.warn("Capture gateway failed: {}", payment.getTransactionId());
                return PaymentResponse.error(payment.getTransactionId(), "Capture processing failed at gateway");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResponse.error(payment.getTransactionId(), "Capture processing interrupted");
        }
    }
    
    public PaymentResponse cancelPayment(Payment payment) {
        log.info("Processing cancellation with gateway: {}", payment.getTransactionId());
        
        try {
            Thread.sleep(30 + random.nextInt(70));
            
            boolean isSuccessful = random.nextDouble() > 0.01;
            
            if (isSuccessful) {
                log.info("Cancellation gateway success: {}", payment.getTransactionId());
                return PaymentResponse.success(payment.getTransactionId(), PaymentStatus.CANCELLED, "Payment cancelled successfully");
            } else {
                log.warn("Cancellation gateway failed: {}", payment.getTransactionId());
                return PaymentResponse.error(payment.getTransactionId(), "Cancellation processing failed at gateway");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResponse.error(payment.getTransactionId(), "Cancellation processing interrupted");
        }
    }
    
    private String getRandomErrorMessage() {
        String[] errorMessages = {
            "Insufficient funds",
            "Card declined by issuer",
            "Invalid card details",
            "Transaction limit exceeded",
            "Gateway timeout",
            "Network error",
            "Invalid merchant configuration"
        };
        
        return errorMessages[random.nextInt(errorMessages.length)];
    }
}