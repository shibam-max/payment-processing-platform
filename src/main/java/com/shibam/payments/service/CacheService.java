package com.shibam.payments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl.toSeconds(), TimeUnit.SECONDS);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {}", key, e);
        }
    }
    
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved cached value for key: {}", key);
            return value;
        } catch (Exception e) {
            log.error("Failed to retrieve cached value for key: {}", key, e);
            return null;
        }
    }
    
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete cached value for key: {}", key, e);
        }
    }
    
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check existence for key: {}", key, e);
            return false;
        }
    }
    
    public void incrementCounter(String key, long delta, Duration ttl) {
        try {
            Long count = redisTemplate.opsForValue().increment(key, delta);
            if (count != null && count == delta) {
                // First time setting, apply TTL
                redisTemplate.expire(key, ttl.toSeconds(), TimeUnit.SECONDS);
            }
            log.debug("Incremented counter for key: {} to {}", key, count);
        } catch (Exception e) {
            log.error("Failed to increment counter for key: {}", key, e);
        }
    }
    
    public Long getCounter(String key) {
        try {
            String value = (String) redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.error("Failed to get counter for key: {}", key, e);
            return 0L;
        }
    }
}