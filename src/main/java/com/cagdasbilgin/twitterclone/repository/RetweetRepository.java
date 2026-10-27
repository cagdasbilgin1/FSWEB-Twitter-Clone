package com.cagdasbilgin.twitterclone.repository;

import com.cagdasbilgin.twitterclone.entity.Retweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetweetRepository extends JpaRepository<Retweet, Long> {
    Optional<Retweet> findByUserIdAndTweetId(Long userId, Long tweetId);
    boolean existsByUserIdAndTweetId(Long userId, Long tweetId);
    void deleteByUserIdAndTweetId(Long userId, Long tweetId);
    long countByTweetId(Long tweetId);
    List<Retweet> findAllByUserId(Long userId);
}
