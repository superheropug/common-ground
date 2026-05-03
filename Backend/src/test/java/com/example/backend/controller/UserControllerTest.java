package com.example.backend.controller;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.Services.JWTService;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        JWTService.setSecretKey(key);
    }

    @Test
    void createUser_succeeds() throws Exception {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"pw\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"));

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(cap.capture());
        User saved = cap.getValue();
        assert saved.getUsername().equals("alice");
        assert saved.getSalt() != null && saved.getHashedPassword() != null;
    }

    @Test
    void login_returns_jwt_on_valid_credentials() throws Exception {
        User u = new User();
        u.setUsername("bob");
        u.setPassword("secretpass");

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(u));

        mvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bob\",\"password\":\"secretpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
