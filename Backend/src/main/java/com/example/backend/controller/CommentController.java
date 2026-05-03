package com.example.backend.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.JWTService;
import com.example.backend.model.Category;
import com.example.backend.model.Comment;
import com.example.backend.model.CommentRequest;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;

import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentController(CommentRepository commentRepository,
                             PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    // -----------------------------
    // GET ALL COMMENTS (SAFE COPY)
    // -----------------------------
    @GetMapping("/comments")
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
                copy.setCategories(comment.getCategories());

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

    // -----------------------------
    // GET COMMENTS BY POST ID
    // (NO AUTH REQUIRED — LEFT AS IS)
    // -----------------------------
    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable Long postId) {

        var comments = commentRepository.findByPostId(postId);

        var safe = comments.stream().map(comment -> {
            Comment copy = new Comment();
            copy.setId(comment.getId());
            copy.setContent(comment.getContent());
            copy.setPostTime(comment.getPostTime());
            copy.setCategories(comment.getCategories());
            copy.setUsername(null);
            copy.setPost(null);
            return copy;
        }).toList();

        return ResponseEntity.ok(safe);
    }

    // -----------------------------
    // CREATE COMMENT
    // -----------------------------
    @PostMapping("/comments")
    public ResponseEntity<?> createComment(
            @RequestBody CommentRequest commentRequest,
            @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);

            Comment comment = new Comment();
            comment.setContent(commentRequest.getText());
            comment.setUsername(username);

            if (commentRequest.getCategories() != null) {
                Set<Category> categorySet = commentRequest.getCategories().stream()
                        .map(name -> new Category(name, name))
                        .collect(Collectors.toSet());

                comment.setCategories(categorySet);
            }

            comment.setPost(
                    postRepository.findById(commentRequest.getPostId())
                            .orElseThrow(() -> new RuntimeException("Post not found"))
            );

            Comment saved = commentRepository.save(comment);

            Comment response = new Comment();
            response.setId(saved.getId());
            response.setContent(saved.getContent());
            response.setPostTime(saved.getPostTime());
            response.setCategories(saved.getCategories());
            response.setUsername(null);
            response.setPost(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }
}