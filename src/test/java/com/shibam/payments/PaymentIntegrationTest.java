package com.shibam.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shibam.payments.dto.PaymentRequest;
import com.shibam.payments.model.Payment;
import com.shibam.payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PaymentRequest paymentRequest;
    
    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        
        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setMerchantId("MERCHANT_001");
        paymentRequest.setDescription("Integration test payment");
        paymentRequest.setCardNumber("4111111111111111");
        paymentRequest.setExpiryMonth("12");
        paymentRequest.setExpiryYear("2025");
        paymentRequest.setCvv("123");
        paymentRequest.setCardHolderName("Test User");
    }
    
    @Test
    void testCompletePaymentFlow() throws Exception {
        // Process payment
        String response = mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extract transaction ID
        String transactionId = objectMapper.readTree(response).get("transactionId").asText();
        
        // Verify payment is saved in database
        Payment savedPayment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        assertNotNull(savedPayment);
        assertEquals(new BigDecimal("100.00"), savedPayment.getAmount());
        assertEquals("USD", savedPayment.getCurrency());
        assertEquals("MERCHANT_001", savedPayment.getMerchantId());
        
        // Get payment details
        mockMvc.perform(get("/api/v1/payments/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.amount").value(100.00));
        
        // Get user payments
        mockMvc.perform(get("/api/v1/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value(transactionId));
    }
    
    @Test
    void testPaymentValidation() throws Exception {
        // Test with invalid amount
        paymentRequest.setAmount(new BigDecimal("-10.00"));
        
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
        
        // Test with missing currency
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency(null);
        
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment service is healthy"));
    }
}