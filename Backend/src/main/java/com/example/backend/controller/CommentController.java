package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.Services.JWTService;
import com.example.backend.model.*;
import com.example.backend.repository.*;

@RestController
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CommentController(CommentRepository commentRepository,
                             PostRepository postRepository,
                             CommentVoteRepository commentVoteRepository,
                             UserRepository userRepository,
                             CategoryRepository categoryRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentVoteRepository = commentVoteRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    // -----------------------------
    // GET COMMENTS
    // -----------------------------
    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<?> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        var comments = commentRepository.findByPostId(postId);

        Long userId = null;

        if (authorization != null) {
            try {
                String username = JWTService.getUsernameFromJWT(authorization);
                userId = userRepository.findByUsername(username)
                        .map(User::getId)
                        .orElse(null);
            } catch (Exception ignored) {}
        }

        Long finalUserId = userId;

        var safe = comments.stream().map(comment -> {
            CommentResponse dto = new CommentResponse();

            dto.id = comment.getId();
            dto.content = comment.getContent();
            dto.postTime = comment.getPostTime();
            dto.categories = comment.getCategories();

            dto.voteScore = commentVoteRepository.getScoreByCommentId(comment.getId());

            if (finalUserId != null) {
                dto.userVote = commentVoteRepository
                        .findByCommentIdAndUserId(comment.getId(), finalUserId)
                        .map(v -> v.getVoteType().name())
                        .orElse("NEUTRAL");
            } else {
                dto.userVote = "NEUTRAL";
            }

            return dto;
        }).toList();

        return ResponseEntity.ok(safe);
    }

    // -----------------------------
    // CREATE COMMENT (FIXED)
    // -----------------------------
    @PostMapping("/comments")
    public ResponseEntity<?> createComment(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow();

            Long postId = Long.valueOf(body.get("postId").toString());
            String text = body.get("text").toString();

            Comment comment = new Comment();
            comment.setContent(text);
            comment.setUsername(user.getUsername());
            comment.setPost(postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found")));

            // -----------------------------
            // CATEGORY HANDLING (NEW)
            // -----------------------------
            if (body.get("categories") != null) {

                @SuppressWarnings("unchecked")
                List<String> requested = (List<String>) body.get("categories");

                List<String> missing = requested.stream()
                        .filter(name -> !categoryRepository.existsById(name))
                        .toList();

                if (!missing.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "error", "Invalid categories",
                                    "missingCategories", missing
                            ));
                }

                Set<Category> categorySet = requested.stream()
                        .map(name -> categoryRepository.findById(name).get())
                        .collect(Collectors.toSet());

                comment.setCategories(categorySet);
            }

            commentRepository.save(comment);

            return ResponseEntity.ok(Map.of("status", "created"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "invalid request"));
        }
    }

    // -----------------------------
    // VOTE COMMENT (UNCHANGED)
    // -----------------------------
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<?> voteComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);

            Long userId = userRepository.findByUsername(username)
                    .orElseThrow()
                    .getId();

            CommentVote.VoteType newVote =
                    CommentVote.VoteType.valueOf(body.get("vote"));

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            var existing = commentVoteRepository.findByCommentIdAndUserId(commentId, userId);

            if (existing.isPresent()) {
                CommentVote vote = existing.get();

                vote.setVoteType(
                        vote.getVoteType() == newVote
                                ? CommentVote.VoteType.NEUTRAL
                                : newVote
                );

                commentVoteRepository.save(vote);

            } else {
                CommentVote vote = new CommentVote();
                vote.setComment(comment);
                vote.setUserId(userId);
                vote.setVoteType(newVote);

                commentVoteRepository.save(vote);
            }

            return ResponseEntity.ok(Map.of("status", "ok"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }
}