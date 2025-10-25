package com.shibam.payments;

import com.shibam.payments.model.Merchant;
import com.shibam.payments.repository.MerchantRepository;
import com.shibam.payments.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {
    
    @Mock
    private MerchantRepository merchantRepository;
    
    @InjectMocks
    private MerchantService merchantService;
    
    private Merchant merchant;
    
    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setMerchantId("MERCHANT_001");
        merchant.setName("Test Merchant");
        merchant.setEmail("test@merchant.com");
        merchant.setCountry("USA");
        merchant.setCurrency("USD");
        merchant.setIsActive(true);
    }
    
    @Test
    void testFindByMerchantId_Found() {
        when(merchantRepository.findByMerchantId(anyString())).thenReturn(Optional.of(merchant));
        
        Optional<Merchant> result = merchantService.findByMerchantId("MERCHANT_001");
        
        assertTrue(result.isPresent());
        assertEquals("MERCHANT_001", result.get().getMerchantId());
        assertEquals("Test Merchant", result.get().getName());
    }
    
    @Test
    void testFindByMerchantId_NotFound() {
        when(merchantRepository.findByMerchantId(anyString())).thenReturn(Optional.empty());
        
        Optional<Merchant> result = merchantService.findByMerchantId("INVALID_MERCHANT");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testIsValidMerchant_Active() {
        when(merchantRepository.findByMerchantId(anyString())).thenReturn(Optional.of(merchant));
        
        boolean result = merchantService.isValidMerchant("MERCHANT_001");
        
        assertTrue(result);
    }
    
    @Test
    void testIsValidMerchant_Inactive() {
        merchant.setIsActive(false);
        when(merchantRepository.findByMerchantId(anyString())).thenReturn(Optional.of(merchant));
        
        boolean result = merchantService.isValidMerchant("MERCHANT_001");
        
        assertFalse(result);
    }
}