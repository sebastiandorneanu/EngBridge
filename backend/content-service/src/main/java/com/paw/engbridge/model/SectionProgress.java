package com.paw.engbridge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class SectionProgress {
    private Integer userId;
    private Integer courseId;
    private Integer sectionId;
    private Double finalScore;
    private String status; // IN_PROGRESS, COMPLETED
    private List<ExerciseAnswer> exercises;
    private Integer levelId;


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("courseId", courseId);
        map.put("sectionId", sectionId);
        map.put("levelId", levelId);
        map.put("finalScore", finalScore);
        map.put("status", status);

        Map<Integer, Object> answers = new HashMap<>();
        if (exercises != null) {
            for (ExerciseAnswer ex : exercises) {
                if (ex.getUserAnswers() != null) {
                    answers.put(ex.getExerciseId(), ex.getUserAnswers());
                } else if (ex.getUserAnswer() != null) {
                    answers.put(ex.getExerciseId(), ex.getUserAnswer());
                }
            }
        }
        map.put("userAnswers", answers);

        return map;
    }

    @Data
    @NoArgsConstructor
    public static class ExerciseAnswer {
        private Integer exerciseId;
        private Map<String, String> userAnswers;
        private String userAnswer;
        private Boolean submitted;
    }
}
