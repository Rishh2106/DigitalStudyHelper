package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.Link;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.repository.LinkRepository;
import com.example.digitalstudyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-link")
    public ResponseEntity<?> createLink(@RequestBody Map<String, String> request) {
        try {
            // Log the incoming request
            System.out.println("Received create-link request: " + request);

            // Get authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("Authentication failed: Not authenticated");
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            System.out.println("Authenticated user: " + username);

            // Find user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Validate request
            String url = request.get("url");
            String name = request.get("name");
            
            if (url == null || url.trim().isEmpty()) {
                System.out.println("Invalid URL");
                return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
            }
            
            if (name == null || name.trim().isEmpty()) {
                System.out.println("Invalid name");
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }

            // Create and save link
            User user = userOpt.get();
            Link link = new Link();
            link.setUrl(url);
            link.setName(name);
            link.setUser(user);
            
            System.out.println("Saving link: " + link);
            Link savedLink = linkRepository.save(link);
            System.out.println("Link saved successfully: " + savedLink);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedLink.getId());
            response.put("url", savedLink.getUrl());
            response.put("name", savedLink.getName());
            response.put("createdAt", savedLink.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error creating link: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create link: " + e.getMessage()));
        }
    }

    @GetMapping("/links")
    public ResponseEntity<?> getUserLinks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            List<Link> links = linkRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
            return ResponseEntity.ok(links);
        } catch (Exception e) {
            System.out.println("Error fetching links: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch links: " + e.getMessage()));
        }
    }

    @DeleteMapping("/links/{id}")
    public ResponseEntity<?> deleteLink(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Optional<Link> linkOpt = linkRepository.findById(id);
            if (linkOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Link link = linkOpt.get();
            if (!link.getUser().getUsername().equals(username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to delete this link"));
            }

            linkRepository.delete(link);
            return ResponseEntity.ok(Map.of("message", "Link deleted successfully"));
        } catch (Exception e) {
            System.out.println("Error deleting link: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete link: " + e.getMessage()));
        }
    }
} 