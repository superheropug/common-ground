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
import com.example.backend.model.CommentResponse;
import com.example.backend.model.CommentVote;
import com.example.backend.model.CommentVote.VoteType;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.CommentVoteRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;

import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository,
                             PostRepository postRepository,
                             CommentVoteRepository commentVoteRepository,
                             UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentVoteRepository = commentVoteRepository;
        this.userRepository = userRepository;
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    // -----------------------------
    // GET COMMENTS BY POST
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
                        .map(u -> u.getId())
                        .orElse(null);
            } catch (Exception ignored) {}
        }

        Long finalUserId = userId;

        var response = comments.stream().map(comment -> {

            CommentResponse dto = new CommentResponse();

            dto.id = comment.getId();
            dto.content = comment.getContent();
            dto.postTime = comment.getPostTime();
            dto.categories = comment.getCategories();
            dto.username = null;

            // -----------------------------
            // SCORE (CLEAN DB QUERY)
            // -----------------------------
            dto.voteScore =
                    commentVoteRepository.getScoreByCommentId(comment.getId());

            // -----------------------------
            // USER VOTE STATE
            // -----------------------------
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

        return ResponseEntity.ok(response);
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

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }

    // -----------------------------
    // VOTE COMMENT
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
        
            VoteType newVote = VoteType.valueOf(body.get("vote"));
        
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
        
            CommentVote vote = commentVoteRepository
                    .findByCommentIdAndUserId(commentId, userId)
                    .orElse(null);
        
            if (vote == null) {
                vote = new CommentVote();
                vote.setComment(comment);
                vote.setUserId(userId);
            }
        
            // toggle logic
            if (vote.getVoteType() == newVote) {
                vote.setVoteType(VoteType.NEUTRAL);
            } else {
                vote.setVoteType(newVote);
            }
        
            commentVoteRepository.save(vote);
        
            int score = commentVoteRepository.getScoreByCommentId(commentId);
        
            return ResponseEntity.ok(Map.of(
                    "voteType", vote.getVoteType().name(),
                    "score", score
            ));
        
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }
}