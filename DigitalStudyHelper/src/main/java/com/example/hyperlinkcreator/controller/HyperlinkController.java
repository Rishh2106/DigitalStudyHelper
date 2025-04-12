package com.example.digitalstudyhelper.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudyResourceController {

    @GetMapping("/")
    public String showForm() {
        return "index";
    }

    @PostMapping("/create-resource")
    public String createResource(@RequestParam String url, @RequestParam String name, Model model) {
        model.addAttribute("url", url);
        model.addAttribute("name", name);
        return "index";
    }
} 