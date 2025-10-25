package com.shibam.payments.service;

import com.shibam.payments.model.Merchant;
import com.shibam.payments.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    
    @Transactional(readOnly = true)
    public Optional<Merchant> findByMerchantId(String merchantId) {
        return merchantRepository.findByMerchantId(merchantId);
    }
    
    @Transactional(readOnly = true)
    public List<Merchant> findActiveMerchants() {
        return merchantRepository.findByIsActive(true);
    }
    
    @Transactional(readOnly = true)
    public boolean isValidMerchant(String merchantId) {
        return merchantRepository.findByMerchantId(merchantId)
                .map(Merchant::getIsActive)
                .orElse(false);
    }
    
    public Merchant createMerchant(Merchant merchant) {
        log.info("Creating new merchant: {}", merchant.getMerchantId());
        return merchantRepository.save(merchant);
    }
    
    public Merchant updateMerchant(Merchant merchant) {
        log.info("Updating merchant: {}", merchant.getMerchantId());
        return merchantRepository.save(merchant);
    }
}