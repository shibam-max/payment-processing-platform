package com.shibam.payments.dto;

import com.shibam.payments.model.Payment.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    private String transactionId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String merchantId;
    private String description;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Success response
    public static PaymentResponse success(String transactionId, PaymentStatus status, String message) {
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(status)
                .message(message)
                .build();
    }
    
    // Error response
    public static PaymentResponse error(String transactionId, String message) {
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(PaymentStatus.FAILED)
                .message(message)
                .build();
    }
}