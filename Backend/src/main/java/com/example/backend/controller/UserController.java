package com.example.backend.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.JWTService;
import com.example.backend.model.Comment;
import com.example.backend.model.Post;
import com.example.backend.model.User;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;

import io.jsonwebtoken.JwtException;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public UserController(UserRepository userRepository,
                          PostRepository postRepository,
                          CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public static class AuthRequest {
        public String username;
        public String password;
    }

    public static class AuthResponse {
        public String token;

        public AuthResponse(String token) {
            this.token = token;
        }
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AuthRequest req) {
        if (req == null || req.username == null || req.username.isBlank() || req.password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and password required"));
        }

        if (userRepository.findByUsername(req.username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "username already exists"));
        }

        User u = new User();
        u.setUsername(req.username);
        u.setPassword(req.password);
        userRepository.save(u);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        if (req == null || req.username == null || req.password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and password required"));
        }

        return userRepository.findByUsername(req.username)
                .map(user -> {
                    if (user.verifyPassword(req.password)) {
                        String jwt = JWTService.createJWTFromUsername(user.getUsername());
                        return ResponseEntity.ok(new AuthResponse(jwt));
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "invalid credentials"));
                    }
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "invalid credentials"))
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authorization header required"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);
            String newJwt = JWTService.createJWTFromUsername(username);
            return ResponseEntity.ok(new AuthResponse(newJwt));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }

    @GetMapping("/all/posts")
    public ResponseEntity<?> getAllPosts(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            JWTService.getUsernameFromJWT(token);

            Page<Post> posts = postRepository.findAllByOrderByPostTimeDesc(PageRequest.of(0, 50));

            var safePosts = posts.map(post -> {
                Post copy = new Post();
                copy.setId(post.getId());
                copy.setText(post.getText());
                copy.setPostTime(post.getPostTime());
                copy.setUsername(null);
                return copy;
            });

            return ResponseEntity.ok(safePosts);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }

    @GetMapping("/all/comments")
    public ResponseEntity<?> getAllComments(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            JWTService.getUsernameFromJWT(token);

            var comments = commentRepository.findAll();
            comments.sort((a, b) -> b.getPostTime().compareTo(a.getPostTime()));

            var safeComments = comments.stream().map(comment -> {
                Comment copy = new Comment();
                copy.setId(comment.getId());
                copy.setContent(comment.getContent());
                copy.setPostTime(comment.getPostTime());
                copy.setUsername(null);
                copy.setPost(null);
                return copy;
            }).toList();

            return ResponseEntity.ok(safeComments);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }
}