package com.example.university.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CacheInvalidationListener {

    private static final String STUDENT_CACHE_PREFIX = "student:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Listen to cache invalidation topic
    @KafkaListener(topics = "student-cache-invalidation", groupId = "cache-invalidation-group")
    public void invalidateCache(String studentId) {
        String key = STUDENT_CACHE_PREFIX + studentId;
        redisTemplate.delete(key);
        System.out.println("Invalidated cache for studentId: " + studentId);
    }
}
