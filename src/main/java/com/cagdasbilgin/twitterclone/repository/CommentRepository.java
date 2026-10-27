package com.cagdasbilgin.twitterclone.repository;

import com.cagdasbilgin.twitterclone.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByTweetIdOrderByCreatedAtAsc(Long tweetId);
}
