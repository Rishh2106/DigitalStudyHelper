package com.example.digitalstudyhelper.repository;

import com.example.digitalstudyhelper.entity.Group;
import com.example.digitalstudyhelper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByNameAndUser(String name, User user);
} 