package com.shibam.payments;

import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.dto.PaymentResponse;
import com.shibam.payments.model.Payment;
import com.shibam.payments.model.Payment.PaymentStatus;
import com.shibam.payments.repository.PaymentRepository;
import com.shibam.payments.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private FraudDetectionService fraudDetectionService;
    
    @Mock
    private PaymentGatewayService paymentGatewayService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private PaymentRequest paymentRequest;
    private Payment payment;
    
    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setMerchantId("MERCHANT_001");
        paymentRequest.setDescription("Test payment");
        
        payment = new Payment();
        payment.setId(1L);
        payment.setTransactionId("TXN_123456789");
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("CARD");
        payment.setMerchantId("MERCHANT_001");
    }
    
    @Test
    void testProcessPayment_Success() {
        // Given
        when(fraudDetectionService.isPaymentSafe(any(PaymentRequest.class))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentRepository.countRecentPaymentsByUser(anyLong(), any())).thenReturn(0L);
        when(paymentGatewayService.processPayment(any(Payment.class)))
                .thenReturn(PaymentResponse.success("TXN_123456789", PaymentStatus.AUTHORIZED, "Success"));
        
        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
        assertNotNull(response.getTransactionId());
        
        verify(fraudDetectionService).isPaymentSafe(paymentRequest);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentGatewayService).processPayment(any(Payment.class));
        verify(notificationService).sendPaymentNotification(any(Payment.class));
    }
    
    @Test
    void testProcessPayment_FraudDetected() {
        // Given
        when(fraudDetectionService.isPaymentSafe(any(PaymentRequest.class))).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentRepository.countRecentPaymentsByUser(anyLong(), any())).thenReturn(0L);
        
        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("Payment blocked by security checks", response.getMessage());
        
        verify(fraudDetectionService).isPaymentSafe(paymentRequest);
        verify(paymentGatewayService, never()).processPayment(any(Payment.class));
    }
    
    @Test
    void testGetPaymentByTransactionId_Found() {
        // Given
        when(paymentRepository.findByTransactionId("TXN_123456789")).thenReturn(Optional.of(payment));
        
        // When
        Optional<PaymentResponse> response = paymentService.getPaymentByTransactionId("TXN_123456789");
        
        // Then
        assertTrue(response.isPresent());
        assertEquals("TXN_123456789", response.get().getTransactionId());
        assertEquals(new BigDecimal("100.00"), response.get().getAmount());
    }
    
    @Test
    void testGetPaymentByTransactionId_NotFound() {
        // Given
        when(paymentRepository.findByTransactionId("INVALID_TXN")).thenReturn(Optional.empty());
        
        // When
        Optional<PaymentResponse> response = paymentService.getPaymentByTransactionId("INVALID_TXN");
        
        // Then
        assertFalse(response.isPresent());
    }
    
    @Test
    void testRefundPayment_Success() {
        // Given
        payment.setStatus(PaymentStatus.SETTLED);
        when(paymentRepository.findByTransactionId("TXN_123456789")).thenReturn(Optional.of(payment));
        when(paymentGatewayService.refundPayment(any(Payment.class)))
                .thenReturn(PaymentResponse.success("TXN_123456789", PaymentStatus.REFUNDED, "Refund successful"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        
        // When
        PaymentResponse response = paymentService.refundPayment("TXN_123456789");
        
        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        
        verify(paymentGatewayService).refundPayment(payment);
        verify(notificationService).sendRefundNotification(any(Payment.class));
    }
    
    @Test
    void testRefundPayment_PaymentNotFound() {
        // Given
        when(paymentRepository.findByTransactionId("INVALID_TXN")).thenReturn(Optional.empty());
        
        // When
        PaymentResponse response = paymentService.refundPayment("INVALID_TXN");
        
        // Then
        assertNotNull(response);
        assertEquals("Payment not found", response.getMessage());
        
        verify(paymentGatewayService, never()).refundPayment(any(Payment.class));
    }
}