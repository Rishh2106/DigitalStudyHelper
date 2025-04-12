package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            response.put("error", "Username already exists");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            response.put("error", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        // Encode password and save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        Map<String, String> response = new HashMap<>();
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent() && passwordEncoder.matches(password, userOptional.get().getPassword())) {
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }

        response.put("error", "Invalid username or password");
        return ResponseEntity.badRequest().body(response);
    }
} 