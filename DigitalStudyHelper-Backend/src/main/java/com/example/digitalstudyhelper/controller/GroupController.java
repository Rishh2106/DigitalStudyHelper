package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.Group;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.repository.GroupRepository;
import com.example.digitalstudyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
} 