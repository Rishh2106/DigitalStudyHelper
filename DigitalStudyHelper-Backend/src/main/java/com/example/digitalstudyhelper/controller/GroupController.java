package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.Group;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.entity.Link;
import com.example.digitalstudyhelper.entity.Note;
import com.example.digitalstudyhelper.dto.GroupExportDTO;
import com.example.digitalstudyhelper.dto.LinkExportDTO;
import com.example.digitalstudyhelper.repository.GroupRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, String> request) {
        String name = request.get("name");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Group name is required"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();

        if (groupRepository.existsByNameAndUser(name, user)) {
            return ResponseEntity.badRequest().body(Map.of("error", "A group with this name already exists"));
        }

        Group group = new Group();
        group.setName(name);
        group.setUser(user);
        group.setCreatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);
        return ResponseEntity.ok(Map.of(
            "id", savedGroup.getId(),
            "name", savedGroup.getName(),
            "createdAt", savedGroup.getCreatedAt()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getGroups() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        List<Group> groups = groupRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<Map<String, Object>> formattedGroups = groups.stream()
            .map(group -> {
                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("id", group.getId());
                groupMap.put("name", group.getName());
                groupMap.put("createdAt", group.getCreatedAt());
                groupMap.put("linkCount", group.getLinks().size());
                return groupMap;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(formattedGroups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group group = groupOptional.get();
        if (!group.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to access this group"));
        }

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("id", group.getId());
        groupData.put("name", group.getName());
        groupData.put("createdAt", group.getCreatedAt());
        groupData.put("links", group.getLinks().stream()
            .map(link -> Map.of(
                "id", link.getId(),
                "url", link.getUrl(),
                "hyperlink", link.getHyperlink(),
                "createdAt", link.getCreatedAt()
            ))
            .collect(Collectors.toList()));

        return ResponseEntity.ok(groupData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group group = groupOptional.get();
        if (!group.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to delete this group"));
        }

        groupRepository.delete(group);
        return ResponseEntity.ok(Map.of("message", "Group deleted successfully"));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportGroups() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();
            List<Group> groups = groupRepository.findByUserOrderByCreatedAtDesc(user);
            
            List<Map<String, Object>> exportData = groups.stream()
                .map(group -> {
                    Map<String, Object> groupMap = new HashMap<>();
                    groupMap.put("name", group.getName());
                    groupMap.put("createdAt", group.getCreatedAt().toString());
                    
                    List<Map<String, Object>> linkList = group.getLinks().stream()
                        .map(link -> {
                            Map<String, Object> linkMap = new HashMap<>();
                            linkMap.put("url", link.getUrl());
                            linkMap.put("hyperlink", link.getHyperlink());
                            linkMap.put("createdAt", link.getCreatedAt().toString());
                            return linkMap;
                        })
                        .collect(Collectors.toList());
                    groupMap.put("links", linkList);
                    
                    if (group.getNote() != null) {
                        groupMap.put("noteContent", group.getNote().getContent());
                    }
                    
                    return groupMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to export groups: " + e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importGroups(@RequestBody List<Map<String, Object>> importData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();
            List<Group> importedGroups = new ArrayList<>();

            for (Map<String, Object> groupData : importData) {
                String name = (String) groupData.get("name");
                if (name == null || name.trim().isEmpty()) {
                    continue;
                }

                // Check if group with same name already exists
                if (groupRepository.existsByNameAndUser(name, user)) {
                    continue; // Skip duplicate groups
                }

                Group group = new Group();
                group.setName(name);
                group.setCreatedAt(LocalDateTime.parse((String) groupData.get("createdAt")));
                group.setUser(user);

                // Create and add links
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> linksData = (List<Map<String, Object>>) groupData.get("links");
                if (linksData != null) {
                    for (Map<String, Object> linkData : linksData) {
                        Link link = new Link();
                        link.setUrl((String) linkData.get("url"));
                        link.setHyperlink((String) linkData.get("hyperlink"));
                        link.setCreatedAt(LocalDateTime.parse((String) linkData.get("createdAt")));
                        link.setCreatedBy(user);
                        group.addLink(link);
                    }
                }

                // Create note if exists
                String noteContent = (String) groupData.get("noteContent");
                if (noteContent != null && !noteContent.isEmpty()) {
                    Note note = new Note();
                    note.setContent(noteContent);
                    note.setGroup(group);
                    group.setNote(note);
                }

                groupRepository.save(group);
                importedGroups.add(group);
            }

            return ResponseEntity.ok(importedGroups);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to import groups: " + e.getMessage()));
        }
    }
} 