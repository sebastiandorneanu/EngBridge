package com.paw.engbridge.services;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.UserProgress;
import com.paw.engbridge.repositories.CourseRepository;
import com.paw.engbridge.repositories.SectionRepository;
import com.paw.engbridge.repositories.UserProgressRepository;
import com.paw.engbridge.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;

    @Autowired
    public ProgressService(UserProgressRepository progressRepository, CourseRepository courseRepository, UserRepository userRepository, SectionRepository sectionRepository) {
        this.progressRepository = progressRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
    }
    public UserProgress updateProgress(Integer userId, Integer courseId, Integer levelId, Integer sectionId, BigDecimal score, String status) {
        Optional<UserProgress> existingProgress = progressRepository.findByUserIdAndCourseIdAndSectionId(userId, courseId, sectionId);

        UserProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.setScore(score);
            if (status != null) {
                progress.setStatus(status);
            }
        } else {
            progress = new UserProgress();
            progress.setUserId(userId);
            progress.setCourseId(courseId);
            progress.setLevelId(levelId);
            progress.setSectionId(sectionId);
            progress.setScore(score != null ? score : BigDecimal.ZERO);
            progress.setStatus(status != null ? status : "IN_PROGRESS");
        }

        UserProgress saved = progressRepository.save(progress);

        if ("COMPLETED".equalsIgnoreCase(status)) {
            checkAndUpgradeLevel(userId, levelId);
        }

        return saved;
    }

    private void checkAndUpgradeLevel(Integer userId, Integer levelId) {
        long totalSectionsInLevel = sectionRepository.countByLevelId(levelId);
        List<UserProgress> userProgressInLevel = progressRepository.findByUserId(userId)
                .stream()
                .filter(p -> p.getLevelId().equals(levelId) && "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .toList();

        // Count unique sections completed (to be safe, though updateProgress should handle it)
        long completedSectionsCount = userProgressInLevel.stream()
                .map(UserProgress::getSectionId)
                .distinct()
                .count();

        if (totalSectionsInLevel > 0 && completedSectionsCount >= totalSectionsInLevel) {
            userRepository.findById(userId).ifPresent(user -> {
                int currentMaxLevel = user.getLevels_id_lvl() != null ? user.getLevels_id_lvl() : 1;
                if (currentMaxLevel <= levelId && currentMaxLevel < 3) {
                    user.setLevels_id_lvl(levelId + 1);
                    userRepository.save(user);
                }
            });
        }
    }

    public List<UserProgress> getProgressForUser(Integer userId) {
        return progressRepository.findByUserId(userId);
    }

    public Optional<UserProgress> getSpecificProgress(Integer userId, Integer courseId) {
        return progressRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public java.util.Map<String, Long> getCourseProgressStats(Integer userId) {
        List<Course> allCourses = courseRepository.findAll();
        long totalCourses = allCourses.size();
        long completedCourses = 0;

        List<UserProgress> userProgress = progressRepository.findByUserId(userId);

        for (Course course : allCourses) {
            long totalSections = sectionRepository.countByCourseId(course.getId());
            if (totalSections == 0) continue;

            long completedSections = userProgress.stream()
                    .filter(p -> p.getCourseId().equals(course.getId()) && "COMPLETED".equalsIgnoreCase(p.getStatus()))
                    .map(UserProgress::getSectionId)
                    .distinct()
                    .count();

            if (completedSections >= totalSections) {
                completedCourses++;
            }
        }

        return java.util.Map.of("completed", completedCourses, "total", totalCourses);
    }

    @Transactional
    public void resetProgress(Integer userId, Integer courseId) {
        progressRepository.deleteByUserIdAndCourseId(userId, courseId);
    }
}