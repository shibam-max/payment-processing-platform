package com.shibam.payments.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentNotificationEvent {
    private String transactionId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String merchantId;
    private LocalDateTime timestamp;
}

@Data
@Builder
class RefundNotificationEvent {
    private String transactionId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private LocalDateTime timestamp;
}