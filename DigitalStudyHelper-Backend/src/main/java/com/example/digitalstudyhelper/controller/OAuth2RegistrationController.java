package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.dto.OAuth2UserRegistrationRequest;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OAuth2RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registration-data")
    public ResponseEntity<?> getRegistrationData(HttpSession session) {
        OAuth2UserRegistrationRequest registrationRequest = 
            (OAuth2UserRegistrationRequest) session.getAttribute("oauth2User");
        
        if (registrationRequest == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No registration data found"));
        }

        return ResponseEntity.ok(registrationRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOAuth2User(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        OAuth2UserRegistrationRequest registrationRequest = 
            (OAuth2UserRegistrationRequest) session.getAttribute("oauth2User");
        
        if (registrationRequest == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No registration data found"));
        }

        String username = request.get("username");
        
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("OAUTH2_USER")); // Set a default password for OAuth2 users

        userRepository.save(user);
        session.removeAttribute("oauth2User");

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
} 