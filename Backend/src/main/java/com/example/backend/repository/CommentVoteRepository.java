package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.CommentVote;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

    Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId);

    @Query("""
        SELECT COALESCE(
            SUM(
                CASE v.voteType
                    WHEN com.example.backend.model.CommentVote.VoteType.POSITIVE THEN 1
                    WHEN com.example.backend.model.CommentVote.VoteType.NEGATIVE THEN -1
                    ELSE 0
                END
            ), 0
        )
        FROM CommentVote v
        WHERE v.comment.id = :commentId
    """)
    int getScoreByCommentId(@Param("commentId") Long commentId);
}