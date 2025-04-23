package com.example.digitalstudyhelper.controller;

import com.example.digitalstudyhelper.entity.*;
import com.example.digitalstudyhelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getNoteByGroup(@PathVariable Long groupId) {
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
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to view this group's note"));
        }

        Optional<Note> noteOptional = noteRepository.findByGroup(group);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.ok(Map.of("content", ""));
        }

        Note note = noteOptional.get();
        return ResponseEntity.ok(Map.of(
            "id", note.getId(),
            "content", note.getContent(),
            "createdAt", note.getCreatedAt(),
            "updatedAt", note.getUpdatedAt()
        ));
    }

    @PostMapping("/group/{groupId}")
    public ResponseEntity<?> saveNote(@PathVariable Long groupId, @RequestBody Map<String, String> request) {
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
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to edit this group's note"));
        }

        String content = request.get("content");
        if (content == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
        }

        Optional<Note> existingNoteOptional = noteRepository.findByGroup(group);
        Note note;

        if (existingNoteOptional.isPresent()) {
            note = existingNoteOptional.get();
            note.setContent(content);
        } else {
            note = new Note();
            note.setContent(content);
            note.setGroup(group);
        }

        Note savedNote = noteRepository.save(note);
        return ResponseEntity.ok(Map.of(
            "id", savedNote.getId(),
            "content", savedNote.getContent(),
            "createdAt", savedNote.getCreatedAt(),
            "updatedAt", savedNote.getUpdatedAt()
        ));
    }
} 