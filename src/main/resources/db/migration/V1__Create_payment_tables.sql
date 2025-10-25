-- Payment Processing Platform Database Schema

-- Create payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status ENUM('PENDING', 'AUTHORIZED', 'CAPTURED', 'SETTLED', 'FAILED', 'REFUNDED', 'CANCELLED') NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    merchant_id VARCHAR(50),
    description TEXT,
    gateway_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_status (user_id, status),
    INDEX idx_merchant_status (merchant_id, status)
);

-- Create payment_methods table
CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    method_type VARCHAR(20) NOT NULL,
    provider VARCHAR(50),
    token VARCHAR(255),
    last_four VARCHAR(4),
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_default (user_id, is_default),
    INDEX idx_user_active (user_id, is_active)
);

-- Create fraud_checks table
CREATE TABLE fraud_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    risk_score DECIMAL(5,4),
    rules_triggered TEXT,
    decision ENUM('APPROVE', 'DECLINE', 'REVIEW') NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_decision (decision),
    INDEX idx_created_at (created_at)
);

-- Create refunds table
CREATE TABLE refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_id VARCHAR(50) UNIQUE NOT NULL,
    original_transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status ENUM('PENDING', 'PROCESSED', 'FAILED') NOT NULL,
    reason TEXT,
    gateway_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_refund_id (refund_id),
    INDEX idx_original_transaction (original_transaction_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create merchants table
CREATE TABLE merchants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    country VARCHAR(3),
    currency VARCHAR(3),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_active (is_active),
    INDEX idx_country (country)
);

-- Insert sample merchants
INSERT INTO merchants (merchant_id, name, email, country, currency) VALUES
('MERCHANT_001', 'Sample Store', 'store@example.com', 'USA', 'USD'),
('MERCHANT_002', 'Tech Shop', 'tech@example.com', 'USA', 'USD'),
('MERCHANT_003', 'Global Mart', 'global@example.com', 'GBR', 'GBP');