package com.shibam.payments.service;

import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.dto.PaymentResponse;
import com.shibam.payments.model.Payment;
import com.shibam.payments.model.Payment.PaymentStatus;
import com.shibam.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final MerchantService merchantService;
    private final AuditService auditService;
    private final CacheService cacheService;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {}, amount: {}", request.getUserId(), request.getAmount());
        
        try {
            // 1. Generate unique transaction ID
            String transactionId = generateTransactionId();
            
            // 2. Validate payment request
            validatePaymentRequest(request);
            
            // 2.1. Validate merchant
            if (!merchantService.isValidMerchant(request.getMerchantId())) {
                log.warn("Invalid merchant: {}", request.getMerchantId());
                return PaymentResponse.error(transactionId, "Invalid merchant ID");
            }
            
            // 3. Fraud detection check
            if (!fraudDetectionService.isPaymentSafe(request)) {
                log.warn("Payment blocked by fraud detection: {}", transactionId);
                return PaymentResponse.error(transactionId, "Payment blocked by security checks");
            }
            
            // 4. Create payment record
            Payment payment = createPayment(request, transactionId);
            payment = paymentRepository.save(payment);
            
            // 4.1. Audit payment creation
            auditService.auditPaymentCreated(payment);
            
            // 4.2. Cache payment for quick lookup
            cacheService.set("payment:" + transactionId, payment, java.time.Duration.ofMinutes(15));
            
            // 5. Process with payment gateway
            PaymentResponse gatewayResponse = paymentGatewayService.processPayment(payment);
            
            // 6. Update payment status
            PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(gatewayResponse.getStatus());
            payment.setGatewayResponse(gatewayResponse.getMessage());
            payment = paymentRepository.save(payment);
            
            // 6.1. Audit status change
            auditService.auditPaymentStatusChanged(payment, oldStatus.toString(), payment.getStatus().toString());
            
            // 6.2. Update cache
            cacheService.set("payment:" + transactionId, payment, java.time.Duration.ofMinutes(15));
            
            // 7. Send notification
            notificationService.sendPaymentNotification(payment);
            
            log.info("Payment processed successfully: {}", transactionId);
            return mapToResponse(payment);
            
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);
            return PaymentResponse.error(null, "Payment processing failed: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByTransactionId(String transactionId) {
        // Try cache first
        Payment cachedPayment = (Payment) cacheService.get("payment:" + transactionId);
        if (cachedPayment != null) {
            log.debug("Payment found in cache: {}", transactionId);
            return Optional.of(mapToResponse(cachedPayment));
        }
        
        // Fallback to database
        Optional<Payment> payment = paymentRepository.findByTransactionId(transactionId);
        if (payment.isPresent()) {
            // Cache for future requests
            cacheService.set("payment:" + transactionId, payment.get(), java.time.Duration.ofMinutes(15));
        }
        
        return payment.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public PaymentResponse refundPayment(String transactionId) {
        log.info("Processing refund for transaction: {}", transactionId);
        
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            return PaymentResponse.error(transactionId, "Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatus.SETTLED) {
            return PaymentResponse.error(transactionId, "Payment cannot be refunded");
        }
        
        try {
            // Process refund with gateway
            PaymentResponse refundResponse = paymentGatewayService.refundPayment(payment);
            
            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            // Send notification
            notificationService.sendRefundNotification(payment);
            
            log.info("Refund processed successfully: {}", transactionId);
            return refundResponse;
            
        } catch (Exception e) {
            log.error("Refund processing failed: {}", e.getMessage(), e);
            return PaymentResponse.error(transactionId, "Refund processing failed");
        }
    }
    
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        if (!isValidCurrency(request.getCurrency())) {
            throw new IllegalArgumentException("Invalid currency code");
        }
        
        // Check for duplicate transaction (velocity check)
        Long recentPayments = paymentRepository.countRecentPaymentsByUser(
            request.getUserId(), LocalDateTime.now().minusMinutes(1));
        
        if (recentPayments > 5) {
            throw new IllegalArgumentException("Too many payment attempts. Please try again later.");
        }
    }
    
    private boolean isValidCurrency(String currency) {
        return List.of("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD").contains(currency);
    }
    
    private Payment createPayment(PaymentRequest request, String transactionId) {
        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setMerchantId(request.getMerchantId());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }
    
    public PaymentResponse capturePayment(String transactionId) {
        log.info("Capturing payment for transaction: {}", transactionId);
        
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            return PaymentResponse.error(transactionId, "Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            return PaymentResponse.error(transactionId, "Payment cannot be captured. Current status: " + payment.getStatus());
        }
        
        try {
            PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            auditService.auditPaymentStatusChanged(payment, oldStatus.toString(), payment.getStatus().toString());
            cacheService.set("payment:" + transactionId, payment, java.time.Duration.ofMinutes(15));
            notificationService.sendPaymentNotification(payment);
            
            log.info("Payment captured successfully: {}", transactionId);
            return mapToResponse(payment);
            
        } catch (Exception e) {
            log.error("Payment capture failed: {}", e.getMessage(), e);
            return PaymentResponse.error(transactionId, "Payment capture failed");
        }
    }
    
    public PaymentResponse cancelPayment(String transactionId) {
        log.info("Cancelling payment for transaction: {}", transactionId);
        
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            return PaymentResponse.error(transactionId, "Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.AUTHORIZED) {
            return PaymentResponse.error(transactionId, "Payment cannot be cancelled. Current status: " + payment.getStatus());
        }
        
        try {
            PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            auditService.auditPaymentStatusChanged(payment, oldStatus.toString(), payment.getStatus().toString());
            cacheService.set("payment:" + transactionId, payment, java.time.Duration.ofMinutes(15));
            notificationService.sendPaymentNotification(payment);
            
            log.info("Payment cancelled successfully: {}", transactionId);
            return mapToResponse(payment);
            
        } catch (Exception e) {
            log.error("Payment cancellation failed: {}", e.getMessage(), e);
            return PaymentResponse.error(transactionId, "Payment cancellation failed");
        }
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .merchantId(payment.getMerchantId())
                .description(payment.getDescription())
                .message("Payment " + payment.getStatus().toString().toLowerCase())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}