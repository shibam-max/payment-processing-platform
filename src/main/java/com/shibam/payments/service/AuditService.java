package com.shibam.payments.service;

import com.shibam.payments.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void auditPaymentCreated(Payment payment) {
        Map<String, Object> auditEvent = createAuditEvent("PAYMENT_CREATED", payment);
        publishAuditEvent(auditEvent);
    }
    
    public void auditPaymentStatusChanged(Payment payment, String oldStatus, String newStatus) {
        Map<String, Object> auditEvent = createAuditEvent("PAYMENT_STATUS_CHANGED", payment);
        auditEvent.put("oldStatus", oldStatus);
        auditEvent.put("newStatus", newStatus);
        publishAuditEvent(auditEvent);
    }
    
    public void auditRefundProcessed(Payment payment) {
        Map<String, Object> auditEvent = createAuditEvent("REFUND_PROCESSED", payment);
        publishAuditEvent(auditEvent);
    }
    
    public void auditFraudDetected(String transactionId, String reason) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "FRAUD_DETECTED");
        auditEvent.put("transactionId", transactionId);
        auditEvent.put("reason", reason);
        auditEvent.put("timestamp", LocalDateTime.now());
        publishAuditEvent(auditEvent);
    }
    
    private Map<String, Object> createAuditEvent(String eventType, Payment payment) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", eventType);
        auditEvent.put("transactionId", payment.getTransactionId());
        auditEvent.put("userId", payment.getUserId());
        auditEvent.put("amount", payment.getAmount());
        auditEvent.put("currency", payment.getCurrency());
        auditEvent.put("status", payment.getStatus());
        auditEvent.put("paymentMethod", payment.getPaymentMethod());
        auditEvent.put("merchantId", payment.getMerchantId());
        auditEvent.put("timestamp", LocalDateTime.now());
        return auditEvent;
    }
    
    private void publishAuditEvent(Map<String, Object> auditEvent) {
        try {
            kafkaTemplate.send("audit-events", auditEvent.get("transactionId").toString(), auditEvent);
            log.info("Audit event published: {}", auditEvent.get("eventType"));
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", e.getMessage(), e);
        }
    }
}