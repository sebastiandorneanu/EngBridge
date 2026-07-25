package com.paw.engbridge.services;

import com.paw.engbridge.model.SectionProgress;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisProgressService {

    private final RedisTemplate<String, SectionProgress> redisTemplate;

    public RedisProgressService(RedisTemplate<String, SectionProgress> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(Integer userId, Integer courseId, Integer sectionId) {
        return "user:" + userId + ":course:" + courseId + ":section:" + sectionId;
    }

    // Salvează progresul secțiunii (inclusiv exerciții)
    public void saveUserSectionProgress(SectionProgress progress) {
        redisTemplate.opsForValue().set(
                buildKey(progress.getUserId(), progress.getCourseId(), progress.getSectionId()),
                progress,
                Duration.ofDays(7) // expiră după 7 zile
        );
    }

    // Preia progresul secțiunii
    public SectionProgress getUserSectionProgress(Integer userId, Integer courseId, Integer sectionId) {
        return redisTemplate.opsForValue().get(buildKey(userId, courseId, sectionId));
    }

    // Șterge progresul secțiunii (opțional)
    public void deleteUserSectionProgress(Integer userId, Integer courseId, Integer sectionId) {
        redisTemplate.delete(buildKey(userId, courseId, sectionId));
    }

    public void deleteCourseProgress(Integer userId, Integer courseId) {
        String pattern = "user:" + userId + ":course:" + courseId + ":section:*";
        java.util.Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
