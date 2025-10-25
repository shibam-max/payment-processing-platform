package com.shibam.payments.repository;

import com.shibam.payments.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    Optional<Merchant> findByMerchantId(String merchantId);
    
    List<Merchant> findByIsActive(Boolean isActive);
    
    List<Merchant> findByCountry(String country);
    
    boolean existsByMerchantId(String merchantId);
}