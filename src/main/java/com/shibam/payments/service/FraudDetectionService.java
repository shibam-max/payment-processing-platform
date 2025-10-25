package com.shibam.payments.service;

import com.shibam.payments.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class FraudDetectionService {
    
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000.00");
    private static final List<String> BLOCKED_COUNTRIES = List.of("XX", "YY"); // Example blocked countries
    
    public boolean isPaymentSafe(PaymentRequest request) {
        log.info("Running fraud detection for user: {}, amount: {}", request.getUserId(), request.getAmount());
        
        // Amount-based check
        if (request.getAmount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            log.warn("Transaction amount exceeds limit: {}", request.getAmount());
            return false;
        }
        
        // Velocity check (simplified)
        if (isHighVelocityTransaction(request)) {
            log.warn("High velocity transaction detected for user: {}", request.getUserId());
            return false;
        }
        
        // Card validation (if card payment)
        if ("CARD".equals(request.getPaymentMethod()) && !isValidCard(request)) {
            log.warn("Invalid card details detected");
            return false;
        }
        
        // Risk scoring (simplified ML-like logic)
        double riskScore = calculateRiskScore(request);
        if (riskScore > 0.8) {
            log.warn("High risk score detected: {}", riskScore);
            return false;
        }
        
        log.info("Payment passed fraud detection checks");
        return true;
    }
    
    private boolean isHighVelocityTransaction(PaymentRequest request) {
        // Simplified velocity check - in real implementation, this would check database
        // for recent transactions by the same user
        return false;
    }
    
    private boolean isValidCard(PaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().length() < 13) {
            return false;
        }
        
        // Luhn algorithm check (simplified)
        return isValidLuhn(request.getCardNumber().replaceAll("\\s+", ""));
    }
    
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    private double calculateRiskScore(PaymentRequest request) {
        double score = 0.0;
        
        // Amount-based risk
        if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            score += 0.2;
        }
        
        // Payment method risk
        if ("CRYPTO".equals(request.getPaymentMethod())) {
            score += 0.3;
        }
        
        // Time-based risk (night transactions are riskier)
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 6 || hour > 22) {
            score += 0.1;
        }
        
        return Math.min(score, 1.0);
    }
}