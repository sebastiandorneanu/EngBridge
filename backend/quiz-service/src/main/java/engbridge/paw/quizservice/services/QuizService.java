package engbridge.paw.quizservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import engbridge.paw.quizservice.model.QuizQuestion;
import engbridge.paw.quizservice.model.QuizResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final ObjectMapper objectMapper;
    private List<QuizQuestion> cachedQuestions;

    @PostConstruct
    public void loadQuizData() {
        try {
            ClassPathResource resource = new ClassPathResource("data/initial_quiz.json");
            InputStream inputStream = resource.getInputStream();
            cachedQuestions = objectMapper.readValue(inputStream, new TypeReference<List<QuizQuestion>>() {});
            log.info("Testul initial a fost incarcat cu succes: {} intrebari.", cachedQuestions.size());
        } catch (IOException e) {
            log.error("Nu s-a putut incarca fisierul JSON pentru testul initial.", e);
            cachedQuestions = Collections.emptyList();
        }
    }

    public List<QuizQuestion> getInitialQuiz() {
        return cachedQuestions.stream()
                .map(q -> {
                    QuizQuestion dto = new QuizQuestion();
                    dto.setId(q.getId());
                    dto.setQuestion(q.getQuestion());
                    dto.setOptions(q.getOptions());
                    dto.setType(q.getType());
                    dto.setCorrectAnswer(null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public QuizResult submitQuiz(Integer userId, Map<Integer, String> userAnswers) {
        int correctCount = 0;

        for (QuizQuestion question : cachedQuestions) {
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                correctCount++;
            }
        }

        double score = (cachedQuestions.isEmpty()) ? 0 : ((double) correctCount / cachedQuestions.size()) * 100;
        String level = calculateLevel(score);

        return new QuizResult(userId, score, level, correctCount, cachedQuestions.size());
    }

    private String calculateLevel(double score) {
        if (score >= 90) return "C1";
        if (score >= 75) return "B2";
        if (score >= 50) return "B1";
        if (score >= 30) return "A2";
        return "A1";
    }

    public void addQuestion(QuizQuestion question) {
        this.cachedQuestions.add(question);
    }
}