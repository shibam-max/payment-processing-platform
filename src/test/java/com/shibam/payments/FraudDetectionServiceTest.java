package com.shibam.payments;

import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {
    
    @InjectMocks
    private FraudDetectionService fraudDetectionService;
    
    private PaymentRequest paymentRequest;
    
    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setMerchantId("MERCHANT_001");
        paymentRequest.setCardNumber("4111111111111111");
    }
    
    @Test
    void testIsPaymentSafe_ValidPayment() {
        boolean result = fraudDetectionService.isPaymentSafe(paymentRequest);
        assertTrue(result);
    }
    
    @Test
    void testIsPaymentSafe_AmountTooHigh() {
        paymentRequest.setAmount(new BigDecimal("15000.00"));
        
        boolean result = fraudDetectionService.isPaymentSafe(paymentRequest);
        assertFalse(result);
    }
    
    @Test
    void testIsPaymentSafe_InvalidCard() {
        paymentRequest.setCardNumber("1234567890123456"); // Invalid Luhn
        
        boolean result = fraudDetectionService.isPaymentSafe(paymentRequest);
        assertFalse(result);
    }
    
    @Test
    void testIsPaymentSafe_CryptoPayment() {
        paymentRequest.setPaymentMethod("CRYPTO");
        paymentRequest.setAmount(new BigDecimal("5000.00"));
        
        boolean result = fraudDetectionService.isPaymentSafe(paymentRequest);
        // Should pass but with higher risk score
        assertTrue(result);
    }
}