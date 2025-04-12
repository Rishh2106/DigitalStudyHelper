package com.example.digitalstudyhelper.repository;

import com.example.digitalstudyhelper.entity.Link;
import com.example.digitalstudyhelper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByUser(User user);
    List<Link> findByUserOrderByCreatedAtDesc(User user);
} 