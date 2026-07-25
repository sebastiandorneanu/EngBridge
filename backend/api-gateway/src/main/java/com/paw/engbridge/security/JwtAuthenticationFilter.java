package com.paw.engbridge.security;

import com.engbridge.auth.grpc.ValidateResponse;
import com.paw.engbridge.grpc.UACClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UACClient uacClient;

    public JwtAuthenticationFilter(UACClient uacClient) {
        this.uacClient = uacClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for login/register endpoints
        if (path.startsWith("/api/auth/") || path.equals("/test") || path.startsWith("/api/quizzes/initial")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing authorization token\"}");
            return;
        }

        try {
            ValidateResponse validationResponse = uacClient.validateToken(token);

            if (!validationResponse.getValid()) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            // Add user info to request attributes for downstream services
            request.setAttribute("X-User-Id", validationResponse.getUserId());
            request.setAttribute("X-User-Role", validationResponse.getRole());

            System.out.println("Authenticated user ID: " + validationResponse.getUserId() +
                    " (Role: " + validationResponse.getRole() + ")");

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }
    // Helper method to send JSON error responses
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}