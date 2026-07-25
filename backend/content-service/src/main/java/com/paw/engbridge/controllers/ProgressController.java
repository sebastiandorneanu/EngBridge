package com.paw.engbridge.controllers;

import com.paw.engbridge.model.UserProgress;
import com.paw.engbridge.services.ProgressService;
import com.paw.engbridge.services.RedisProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final RedisProgressService redisProgressService;

    public ProgressController(ProgressService progressService, RedisProgressService redisProgressService) {
        this.progressService = progressService;
        this.redisProgressService = redisProgressService;
    }

    @PostMapping
    public ResponseEntity<UserProgress> saveProgress(@RequestBody UserProgress progressData) {
        if (progressData.getUserId() == null || progressData.getCourseId() == null) {
            return ResponseEntity.badRequest().build();
        }

        UserProgress saved = progressService.updateProgress(
                progressData.getUserId(),
                progressData.getCourseId(),
                progressData.getLevelId(),
                progressData.getSectionId(),
                progressData.getScore(),
                progressData.getStatus()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserProgress>> getUserProgress(@PathVariable Integer userId) {
        return ResponseEntity.ok(progressService.getProgressForUser(userId));
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<UserProgress> getSpecificProgress(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {

        return progressService.getSpecificProgress(userId, courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<?> resetProgress(@RequestParam Integer userId, @RequestParam Integer courseId) {
        progressService.resetProgress(userId, courseId);
        redisProgressService.deleteCourseProgress(userId, courseId);
        return ResponseEntity.ok("Progress reset successfully");
    }
}