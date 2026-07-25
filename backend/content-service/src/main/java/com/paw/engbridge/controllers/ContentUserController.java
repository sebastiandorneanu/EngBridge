package com.paw.engbridge.controllers;

import com.paw.engbridge.model.User;
import com.paw.engbridge.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class ContentUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.paw.engbridge.services.ProgressService progressService;

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestParam String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (userOpt.isEmpty()) {
            response.put("levelId", 1);
            response.put("placementTestScore", null);
            response.put("completedLessons", 0);
            response.put("totalLessons", 0);
            return ResponseEntity.ok(response);
        }
        User user = userOpt.get();
        response.put("levelId", user.getLevels_id_lvl() != null ? user.getLevels_id_lvl() : 1);
        response.put("placementTestScore", user.getPlacementTestScore());
        
        java.util.Map<String, Long> stats = progressService.getCourseProgressStats(user.getId_user());
        response.put("completedLessons", stats.get("completed"));
        response.put("totalLessons", stats.get("total"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/placement")
    public ResponseEntity<?> submitPlacementTest(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Integer score = (Integer) payload.get("score");

        if (username == null || score == null) {
            return ResponseEntity.badRequest().body("Username and score are required");
        }

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setLevels_id_lvl(1);
                    return newUser;
                });

        user.setPlacementTestScore(score);

        int currentLevel = user.getLevels_id_lvl() != null ? user.getLevels_id_lvl() : 1;
        int newLevel = currentLevel;

        if (score > 25) {
            newLevel = 3; // C1
        } else if (score > 15) {
            newLevel = 2; // B2
        }

        if (newLevel > currentLevel) {
            user.setLevels_id_lvl(newLevel);
        }

        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("levelId", user.getLevels_id_lvl());
        result.put("placementTestScore", user.getPlacementTestScore());
        return ResponseEntity.ok(result);
    }
}
