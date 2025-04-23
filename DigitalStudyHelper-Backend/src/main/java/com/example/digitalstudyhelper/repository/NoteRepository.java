package com.example.digitalstudyhelper.repository;

import com.example.digitalstudyhelper.entity.Note;
import com.example.digitalstudyhelper.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByGroup(Group group);
} 