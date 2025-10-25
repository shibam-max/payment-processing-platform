package com.shibam.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shibam.payments.controller.PaymentController;
import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.dto.PaymentResponse;
import com.shibam.payments.model.Payment.PaymentStatus;
import com.shibam.payments.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;
    
    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setMerchantId("MERCHANT_001");
        paymentRequest.setDescription("Test payment");
        
        paymentResponse = PaymentResponse.builder()
                .transactionId("TXN_123456789")
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.AUTHORIZED)
                .paymentMethod("CARD")
                .merchantId("MERCHANT_001")
                .message("Payment authorized successfully")
                .build();
    }
    
    @Test
    void testProcessPayment_Success() throws Exception {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);
        
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN_123456789"))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }
    
    @Test
    void testProcessPayment_ValidationError() throws Exception {
        paymentRequest.setAmount(null); // Invalid amount
        
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetPaymentDetails_Found() throws Exception {
        when(paymentService.getPaymentByTransactionId(anyString())).thenReturn(Optional.of(paymentResponse));
        
        mockMvc.perform(get("/api/v1/payments/TXN_123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN_123456789"))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));
    }
    
    @Test
    void testGetPaymentDetails_NotFound() throws Exception {
        when(paymentService.getPaymentByTransactionId(anyString())).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/v1/payments/INVALID_TXN"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testRefundPayment_Success() throws Exception {
        PaymentResponse refundResponse = PaymentResponse.success("TXN_123456789", PaymentStatus.REFUNDED, "Refund processed");
        when(paymentService.refundPayment(anyString())).thenReturn(refundResponse);
        
        mockMvc.perform(post("/api/v1/payments/TXN_123456789/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
    
    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment service is healthy"));
    }
}