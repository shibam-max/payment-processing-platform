package com.shibam.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Application Class for Global Payment Processing Platform
 * 
 * Enterprise-grade payment processing system built with:
 * - Java/J2EE, Spring Boot, Hibernate
 * - MySQL/Oracle database integration
 * - REST APIs and Web Services
 * - Complete transaction lifecycle management
 * 
 * @author Shibam Samaddar
 * @version 1.0.0
 */
@SpringBootApplication
@EnableTransactionManagement
public class PaymentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessingApplication.class, args);
    }
}