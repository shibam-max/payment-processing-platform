package com.shibam.payments.service;

import com.shibam.payments.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public NotificationService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendPaymentNotification(Payment payment) {
        try {
            PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                    .transactionId(payment.getTransactionId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus().toString())
                    .paymentMethod(payment.getPaymentMethod())
                    .merchantId(payment.getMerchantId())
                    .timestamp(payment.getUpdatedAt())
                    .build();
            
            kafkaTemplate.send("payment-notifications", payment.getTransactionId(), event);
            log.info("Payment notification sent: {}", payment.getTransactionId());
            
        } catch (Exception e) {
            log.error("Failed to send payment notification: {}", e.getMessage(), e);
        }
    }
    
    public void sendRefundNotification(Payment payment) {
        try {
            RefundNotificationEvent event = RefundNotificationEvent.builder()
                    .transactionId(payment.getTransactionId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .merchantId(payment.getMerchantId())
                    .timestamp(payment.getUpdatedAt())
                    .build();
            
            kafkaTemplate.send("refund-notifications", payment.getTransactionId(), event);
            log.info("Refund notification sent: {}", payment.getTransactionId());
            
        } catch (Exception e) {
            log.error("Failed to send refund notification: {}", e.getMessage(), e);
        }
    }
    
    // Email notification (simplified)
    public void sendEmailNotification(Long userId, String subject, String message) {
        log.info("Sending email to user {}: {} - {}", userId, subject, message);
        // In real implementation, integrate with email service like SendGrid, SES, etc.
    }
    
    // SMS notification (simplified)
    public void sendSmsNotification(Long userId, String message) {
        log.info("Sending SMS to user {}: {}", userId, message);
        // In real implementation, integrate with SMS service like Twilio, SNS, etc.
    }
}