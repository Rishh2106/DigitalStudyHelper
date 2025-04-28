package com.example.digitalstudyhelper.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GroupExportDTO {
    private String name;
    private LocalDateTime createdAt;
    private List<LinkExportDTO> links;
    private String noteContent;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LinkExportDTO> getLinks() {
        return links;
    }

    public void setLinks(List<LinkExportDTO> links) {
        this.links = links;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
} 