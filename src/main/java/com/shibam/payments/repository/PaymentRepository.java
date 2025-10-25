package com.shibam.payments.repository;

import com.shibam.payments.model.Payment;
import com.shibam.payments.model.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByMerchantId(String merchantId);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :status")
    BigDecimal getTotalAmountByMerchantAndStatus(@Param("merchantId") String merchantId, 
                                               @Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId AND p.createdAt > :since")
    Long countRecentPaymentsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    boolean existsByTransactionId(String transactionId);
}