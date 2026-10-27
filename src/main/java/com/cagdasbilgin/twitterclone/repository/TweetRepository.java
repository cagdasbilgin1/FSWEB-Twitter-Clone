package com.cagdasbilgin.twitterclone.repository;

import com.cagdasbilgin.twitterclone.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {
    List<Tweet> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Tweet> findAllByOrderByCreatedAtDesc();
}
