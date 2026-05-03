package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.JWTService;
import com.example.backend.model.Category;
import com.example.backend.model.Post;
import com.example.backend.model.PostRequest;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.PostRepository;

import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostController(PostRepository postRepository,
                          CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    private String extractToken(String authorization) {
        return (authorization == null || authorization.isBlank()) ? null : authorization;
    }

    // -----------------------------
    // GET ALL POSTS
    // -----------------------------
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findAllByOrderByPostTimeDesc(pageable);

        var safePosts = posts.map(post -> {
            Post copy = new Post();
            copy.setId(post.getId());
            copy.setText(post.getText());
            copy.setPostTime(post.getPostTime());
            copy.setCategories(post.getCategories());
            copy.setUsername(null);
            return copy;
        });

        return ResponseEntity.ok(safePosts);
    }

    // -----------------------------
    // GET POST BY ID
    // -----------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        Post copy = new Post();
        copy.setId(post.getId());
        copy.setText(post.getText());
        copy.setPostTime(post.getPostTime());
        copy.setCategories(post.getCategories());
        copy.setUsername(null);

        return ResponseEntity.ok(copy);
    }

    // -----------------------------
    // CREATE POST (AUTH + VALIDATED CATEGORIES)
    // -----------------------------
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody PostRequest postRequest,
            @RequestHeader("Authorization") String token) {

        String cleanToken = extractToken(token);

        if (cleanToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing authorization token"));
        }

        try {
            String username = JWTService.getUsernameFromJWT(cleanToken);

            Post post = new Post();
            post.setText(postRequest.getText());
            post.setUsername(username);

            // -----------------------------
            // CATEGORY VALIDATION
            // -----------------------------
            if (postRequest.getCategories() != null) {

                List<String> requested = postRequest.getCategories();

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

                post.setCategories(categorySet);
            }

            Post saved = postRepository.save(post);

            Post response = new Post();
            response.setId(saved.getId());
            response.setText(saved.getText());
            response.setPostTime(saved.getPostTime());
            response.setCategories(saved.getCategories());
            response.setUsername(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }
}