package com.paw.engbridge.controllers;

import com.engbridge.auth.grpc.*;
import com.paw.engbridge.grpc.UACClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {

    @PostConstruct
    public void init() {
        System.out.println(">>> GatewayController LOADED <<<");
    }

    @Value("${service.content.url}")
    private String contentServiceUrl;

    @Value("${service.quiz.url}")
    private String quizServiceUrl;

    private final UACClient uacClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GatewayController(UACClient uacClient) {
        this.uacClient = uacClient;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing token");
        }
        return authHeader.substring(7);
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("X-User-Role");
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Admin role required");
        }
    }
    // ========================= QUIZ INITIAL =========================
    @GetMapping("/api/quizzes/initial")
    public ResponseEntity<?> getInitialQuiz() {
        try {
            String url = quizServiceUrl + "/quizzes/initial";
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/quizzes/initial/submit")
    public ResponseEntity<?> submitInitialQuiz(@RequestBody Map<Integer, String> answers,
                                               @RequestParam Integer userId) {
        try {
            String url = quizServiceUrl + "/quizzes/initial/submit?userId=" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<Integer, String>> entity = new HttpEntity<>(answers, headers);

            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String getUserId(HttpServletRequest request) {
        return (String) request.getAttribute("X-User-Id");
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Gateway works!"));
    }

    @RequestMapping(
            value = "/api/content/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
    )
    public ResponseEntity<?> forwardToContentService(
            @RequestBody(required = false) String body,
            HttpServletRequest request
    ) {
        try {
            String method = request.getMethod();
            if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
                requireAdmin(request);
            }

            String token = extractToken(request);

            String path = request.getRequestURI().substring("/api/content".length());
            String url = contentServiceUrl + path;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            HttpMethod httpMethod = HttpMethod.valueOf(method);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, httpMethod, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ========================= AUTH =========================
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            LoginRequest req = LoginRequest.newBuilder()
                    .setUsername(body.get("username"))
                    .setPassword(body.get("password"))
                    .build();

            LoginResponse resp = uacClient.login(req);

            if (resp.hasError() && !resp.getError().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", resp.getError()));
            }

            return ResponseEntity.ok(Map.of("token", resp.getToken()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            RegisterRequest req = RegisterRequest.newBuilder()
                    .setUsername(body.get("username"))
                    .setPassword(body.get("password"))
                    .setEmail(body.get("email"))
                    .build();

            RegisterResponse resp = uacClient.register(req);

            if (!resp.getSuccess()) {
                return ResponseEntity.status(400).body(Map.of("error", resp.getMessage()));
            }
            return ResponseEntity.ok(Map.of("message", resp.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ========================= USERS =========================
    @GetMapping("/api/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            requireAdmin(request);
            String token = extractToken(request);

            UserList users = uacClient.getAllUsers(token);

            List<Map<String, Object>> result = new ArrayList<>();
            for (UserInfo u : users.getUsersList()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("uid", u.getUid());
                userMap.put("username", u.getUsername());
                userMap.put("email", u.getEmail());
                userMap.put("role", u.getRole());
                result.add(userMap);
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable int id, HttpServletRequest request) {
        try {
            String token = extractToken(request);
            String role = (String) request.getAttribute("X-User-Role");
            String userId = getUserId(request);

            if (!"ADMIN".equals(role) && !Integer.toString(id).equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            UserResponse grpcResp = uacClient.getUser(UserIdRequest.newBuilder().setUid(id).build(), token);

            Map<String, Object> response = new HashMap<>();
            response.put("uid", grpcResp.getUid());
            response.put("username", grpcResp.getUsername());
            response.put("role", grpcResp.getRole());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            requireAdmin(request);
            String token = extractToken(request);

            String username = body.get("username");
            String password = body.get("password");
            String email = body.get("email");
            String role = body.get("role");

            if (username == null || password == null || email == null || role == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Missing required field(s). Make sure you introduced username, password, email and role."));
            }

            CreateUserRequest grpcReq = CreateUserRequest.newBuilder()
                    .setUsername(body.get("username"))
                    .setPassword(body.get("password"))
                    .setEmail(body.get("email"))
                    .setRole(body.get("role"))
                    .build();

            CreateUserResponse grpcResp = uacClient.createUser(grpcReq, token);

            if (!grpcResp.getSuccess()) {
                return ResponseEntity.status(400).body(Map.of("error", grpcResp.getError()));
            }

            return ResponseEntity.ok(Map.of(
                    "uid", grpcResp.getUid(),
                    "message", grpcResp.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            requireAdmin(request);
            String token = extractToken(request);

            UpdateRequest grpcReq = UpdateRequest.newBuilder()
                    .setUid(id)
                    .setUsername(body.getOrDefault("username", ""))
                    .setPassword(body.getOrDefault("password", ""))
                    .setRole(body.getOrDefault("role", ""))
                    .build();

            UpdateResponse grpcResp = uacClient.updateUser(grpcReq, token);

            if (!grpcResp.getSuccess()) {
                return ResponseEntity.status(400).body(Map.of("error", grpcResp.getError()));
            }

            return ResponseEntity.ok(Map.of("message", grpcResp.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, HttpServletRequest request) {
        try {
            requireAdmin(request);
            String token = extractToken(request);

            DeleteResponse grpcResp = uacClient.deleteUser(DeleteRequest.newBuilder().setUid(id).build(), token);

            if (!grpcResp.getSuccess()) {
                return ResponseEntity.status(400).body(Map.of("error", grpcResp.getError()));
            }

            return ResponseEntity.ok(Map.of("message", grpcResp.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            String userIdStr = getUserId(request);

            if (userIdStr == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized"));
            }

            int userId = Integer.parseInt(userIdStr);

            UserResponse grpcResp = uacClient.getUser(
                    UserIdRequest.newBuilder().setUid(userId).build(),
                    token
            );

            Map<String, Object> response = new HashMap<>();
            response.put("uid", grpcResp.getUid());
            response.put("username", grpcResp.getUsername());
            response.put("email", grpcResp.getEmail());
            response.put("role", grpcResp.getRole());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
