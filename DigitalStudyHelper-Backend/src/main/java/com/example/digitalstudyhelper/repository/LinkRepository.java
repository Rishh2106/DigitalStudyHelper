package com.example.digitalstudyhelper.repository;

import com.example.digitalstudyhelper.entity.Link;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByCreatedBy(User user);
    List<Link> findByCreatedByOrderByCreatedAtDesc(User user);
    List<Link> findByGroupOrderByCreatedAtDesc(Group group);
} 