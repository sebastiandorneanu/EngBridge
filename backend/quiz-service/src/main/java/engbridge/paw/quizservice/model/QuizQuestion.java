package engbridge.paw.quizservice.model;

import lombok.Data;
import java.util.List;

@Data
public class QuizQuestion {
    private Integer id;
    private String question;
    private List<String> options;
    private String type;
    private String correctAnswer;
}