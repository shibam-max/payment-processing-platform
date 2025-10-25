package com.shibam.payments.controller;

import com.shibam.payments.model.Merchant;
import com.shibam.payments.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MerchantController {
    
    private final MerchantRepository merchantRepository;
    
    @GetMapping("/{merchantId}")
    public ResponseEntity<Merchant> getMerchant(@PathVariable String merchantId) {
        return merchantRepository.findByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        List<Merchant> merchants = merchantRepository.findByIsActive(true);
        return ResponseEntity.ok(merchants);
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Merchant>> getMerchantsByCountry(@PathVariable String country) {
        List<Merchant> merchants = merchantRepository.findByCountry(country);
        return ResponseEntity.ok(merchants);
    }
}