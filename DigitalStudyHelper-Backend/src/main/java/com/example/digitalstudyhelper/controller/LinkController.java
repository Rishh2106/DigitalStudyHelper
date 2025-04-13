package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.Link;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.entity.Group;
import com.example.digitalstudyhelper.repository.LinkRepository;
import com.example.digitalstudyhelper.repository.UserRepository;
import com.example.digitalstudyhelper.repository.GroupRepository;
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

    @Autowired
    private GroupRepository groupRepository;

    @PostMapping
    public ResponseEntity<?> createLink(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String hyperlink = request.get("hyperlink");
        String groupId = request.get("groupId");

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        if (hyperlink == null || hyperlink.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Hyperlink text is required"));
        }

        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Group ID is required"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Optional<Group> groupOptional = groupRepository.findById(Long.parseLong(groupId));

        if (groupOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Group not found"));
        }

        Group group = groupOptional.get();
        if (!group.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to add links to this group"));
        }

        Link link = new Link();
        link.setUrl(url);
        link.setHyperlink(hyperlink);
        link.setCreatedBy(user);
        link.setGroup(group);
        link.setCreatedAt(LocalDateTime.now());

        Link savedLink = linkRepository.save(link);
        return ResponseEntity.ok(Map.of(
            "id", savedLink.getId(),
            "url", savedLink.getUrl(),
            "hyperlink", savedLink.getHyperlink(),
            "createdAt", savedLink.getCreatedAt(),
            "groupId", savedLink.getGroup().getId()
        ));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getLinksByGroup(@PathVariable Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group group = groupOptional.get();
        if (!group.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to view this group's links"));
        }

        List<Link> links = linkRepository.findByGroupOrderByCreatedAtDesc(group);
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