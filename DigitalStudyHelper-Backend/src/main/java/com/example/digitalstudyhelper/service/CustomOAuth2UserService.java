package com.example.digitalstudyhelper.service;

import com.example.digitalstudyhelper.dto.OAuth2UserRegistrationRequest;
import com.example.digitalstudyhelper.entity.User;
import com.example.digitalstudyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        // Check if user exists
        User existingUser = userRepository.findByEmail(email).orElse(null);
        
        if (existingUser == null) {
            // Store OAuth2 user info in session for registration
            OAuth2UserRegistrationRequest registrationRequest = new OAuth2UserRegistrationRequest();
            registrationRequest.setEmail(email);
            registrationRequest.setName(name);
            registrationRequest.setPicture(picture);
            
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();
            session.setAttribute("oauth2User", registrationRequest);
            
            // Redirect to registration page
            throw new OAuth2AuthenticationException("User not found. Please complete registration.");
        }

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "email"
        );
    }
} 