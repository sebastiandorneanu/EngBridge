package com.paw.engbridge.controllers;

import com.paw.engbridge.model.SectionProgress;
import com.paw.engbridge.services.ProgressService;
import com.paw.engbridge.services.RedisProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/redis-progress")
public class RedisProgressController {

    private final RedisProgressService redisProgressService;
    private final ProgressService progressService;

    public RedisProgressController(RedisProgressService redisProgressService,
                                   ProgressService progressService) {
        this.redisProgressService = redisProgressService;
        this.progressService = progressService;
    }

    // GET: preia progresul unei secțiuni
    @GetMapping("/section")
    public ResponseEntity<SectionProgress> getSectionProgress(
            @RequestParam Integer userId,
            @RequestParam Integer courseId,
            @RequestParam Integer sectionId) {

        SectionProgress progress = redisProgressService.getUserSectionProgress(userId, courseId, sectionId);
        if (progress == null) {
            return ResponseEntity.ok(new SectionProgress());
        }        return ResponseEntity.ok(progress);
    }

    // POST: salvează progresul unei secțiuni
    @PostMapping("/section")
    public ResponseEntity<SectionProgress> saveSectionProgress(@RequestBody SectionProgress progress) {
        if (progress.getUserId() == null || progress.getCourseId() == null || progress.getSectionId() == null) {
            return ResponseEntity.badRequest().build();
        }

        // salvează tot progresul secțiunii în Redis
        redisProgressService.saveUserSectionProgress(progress);

        BigDecimal scoreToSave = (progress.getFinalScore() != null && !progress.getFinalScore().isNaN())
                ? BigDecimal.valueOf(progress.getFinalScore())
                : BigDecimal.ZERO;

        // dacă secțiunea e COMPLETED, actualizează și progresul permanent în baza de date
        if ("COMPLETED".equalsIgnoreCase(progress.getStatus())) {
            progressService.updateProgress(
                    progress.getUserId(),
                    progress.getCourseId(),
                    progress.getLevelId(),
                    progress.getSectionId(),
                    scoreToSave,
                    "COMPLETED"
            );
        }

        return ResponseEntity.ok(progress);
    }
}
