package com.shibam.payments.controller;

import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.dto.PaymentResponse;
import com.shibam.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for user: {}", request.getUserId());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            if (response.getStatus().toString().contains("FAILED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Payment processing error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = PaymentResponse.error(null, "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable String transactionId) {
        log.info("Fetching payment details for transaction: {}", transactionId);
        
        return paymentService.getPaymentByTransactionId(transactionId)
                .map(payment -> ResponseEntity.ok(payment))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable Long userId) {
        log.info("Fetching payments for user: {}", userId);
        
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String transactionId) {
        log.info("Processing refund for transaction: {}", transactionId);
        
        try {
            PaymentResponse response = paymentService.refundPayment(transactionId);
            
            if (response.getStatus().toString().contains("FAILED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Refund processing error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = PaymentResponse.error(transactionId, "Refund processing failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment service is healthy");
    }
    
    @PostMapping("/{transactionId}/capture")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable String transactionId) {
        log.info("Capturing payment for transaction: {}", transactionId);
        
        try {
            PaymentResponse response = paymentService.capturePayment(transactionId);
            
            if (response.getStatus().toString().contains("FAILED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Payment capture error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = PaymentResponse.error(transactionId, "Payment capture failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable String transactionId) {
        log.info("Cancelling payment for transaction: {}", transactionId);
        
        try {
            PaymentResponse response = paymentService.cancelPayment(transactionId);
            
            if (response.getStatus().toString().contains("FAILED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Payment cancellation error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = PaymentResponse.error(transactionId, "Payment cancellation failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}