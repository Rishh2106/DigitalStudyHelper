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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createLink(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String hyperlink = request.get("hyperlink");

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        if (hyperlink == null || hyperlink.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Hyperlink text is required"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Link link = new Link();
        link.setUrl(url);
        link.setHyperlink(hyperlink);
        link.setCreatedBy(user);
        link.setCreatedAt(LocalDateTime.now());

        Link savedLink = linkRepository.save(link);
        return ResponseEntity.ok(Map.of(
            "id", savedLink.getId(),
            "url", savedLink.getUrl(),
            "hyperlink", savedLink.getHyperlink(),
            "createdAt", savedLink.getCreatedAt()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getLinks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        List<Link> links = linkRepository.findByCreatedByOrderByCreatedAtDesc(user);
        List<Map<String, Object>> formattedLinks = new ArrayList<>();

        for (Link link : links) {
            Map<String, Object> linkMap = new HashMap<>();
            linkMap.put("id", link.getId());
            linkMap.put("url", link.getUrl());
            linkMap.put("hyperlink", link.getHyperlink());
            linkMap.put("createdAt", link.getCreatedAt());
            formattedLinks.add(linkMap);
        }

        return ResponseEntity.ok(formattedLinks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLink(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Optional<Link> linkOptional = linkRepository.findById(id);

        if (linkOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Link link = linkOptional.get();
        if (!link.getCreatedBy().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to delete this link"));
        }

        linkRepository.delete(link);
        return ResponseEntity.ok(Map.of("message", "Link deleted successfully"));
    }
} 