package com.example.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTextContainingIgnoreCase(String term);
    List<Post> findByUsername(String username);
    Page<Post> findAllByOrderByPostTimeDesc(Pageable pageable);
}
