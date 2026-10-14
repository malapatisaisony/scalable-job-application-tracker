package com.saisony.jobtracker.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(
            String key,
            int limit,
            Duration window) {

        Long count = redisTemplate
                .opsForValue()
                .increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }

        return count != null && count <= limit;
    }
}