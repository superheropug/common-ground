package com.example.backend.model;

import java.time.Instant;
import java.util.Set;

public class CommentResponse {

    public Long id;
    public String content;
    public Instant postTime;
    public Set<Category> categories;

    public int voteScore;
    public String userVote;

    public String username;
}