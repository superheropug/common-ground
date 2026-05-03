package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.Services.JWTService;
import com.example.backend.model.Vote;
import com.example.backend.model.Vote.VoteType;
import com.example.backend.repository.VoteRepository;

import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteRepository voteRepository;

    public VoteController(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public static class VoteRequest {
        public Long commentId;
        public String voteType; // POSITIVE / NEGATIVE / toggle behavior handled below
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    @PostMapping
    public ResponseEntity<?> vote(
            @RequestBody VoteRequest req,
            @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization required"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);

            VoteType requested = VoteType.valueOf(req.voteType);

            var existing = voteRepository.findByUsernameAndCommentId(username, req.commentId);

            Vote vote;

            if (existing.isPresent()) {
                vote = existing.get();

                // toggle logic:
                if (vote.getVoteType() == requested) {
                    vote.setVoteType(VoteType.NEUTRAL); // clicking same = reset
                } else {
                    vote.setVoteType(requested);
                }

            } else {
                vote = new Vote(username, req.commentId, requested);
            }

            voteRepository.save(vote);

            return ResponseEntity.ok(Map.of(
                    "commentId", req.commentId,
                    "voteType", vote.getVoteType()
            ));

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid token"));
        }
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<?> getVote(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);

        if (token == null) {
            return ResponseEntity.ok(Map.of("voteType", "NEUTRAL"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(token);

            var vote = voteRepository.findByUsernameAndCommentId(username, commentId);

            return ResponseEntity.ok(Map.of(
                    "voteType",
                    vote.map(Vote::getVoteType).orElse(VoteType.NEUTRAL)
            ));

        } catch (JwtException e) {
            return ResponseEntity.ok(Map.of("voteType", "NEUTRAL"));
        }
    }
}