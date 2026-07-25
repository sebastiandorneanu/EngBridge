package engbridge.paw.quizservice.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class QuizResult {
    private Integer userId;
    private Double score;
    private String recommendedLevel;
    private Integer correctAnswersCount;
    private Integer totalQuestions;
}