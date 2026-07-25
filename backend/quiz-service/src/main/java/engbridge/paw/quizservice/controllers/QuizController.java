package engbridge.paw.quizservice.controllers;

import engbridge.paw.quizservice.model.QuizQuestion;
import engbridge.paw.quizservice.model.QuizResult;
import engbridge.paw.quizservice.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/initial")
    public ResponseEntity<List<QuizQuestion>> getInitialQuiz() {
        return ResponseEntity.ok(quizService.getInitialQuiz());
    }

    @PostMapping("/initial/submit")
    public ResponseEntity<QuizResult> submitQuiz(
            @RequestParam Integer userId,
            @RequestBody Map<Integer, String> answers) {

        QuizResult result = quizService.submitQuiz(userId, answers);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/initial/add")
    public ResponseEntity<String> addQuestion(@RequestBody QuizQuestion question) {
        quizService.addQuestion(question);
        return ResponseEntity.ok("Intrebarea a fost adaugata cu succes!");
    }
}