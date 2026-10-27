package com.cagdasbilgin.twitterclone.service.impl;

import com.cagdasbilgin.twitterclone.dto.LikeRequest;
import com.cagdasbilgin.twitterclone.dto.LikeResponse;
import com.cagdasbilgin.twitterclone.entity.Like;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.LikeRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LikeResponse like(LikeRequest request, User currentUser) {
        User user = currentUser;
        if (user == null && request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TwitterException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        if (user == null) {
            throw new TwitterException("User must be authenticated or provided to like a tweet", HttpStatus.BAD_REQUEST);
        }

        Tweet tweet = tweetRepository.findById(request.getTweetId())
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + request.getTweetId(), HttpStatus.NOT_FOUND));

        if (likeRepository.existsByUserIdAndTweetId(user.getId(), tweet.getId())) {
            throw new TwitterException("You have already liked this tweet", HttpStatus.BAD_REQUEST);
        }

        Like like = Like.builder()
                .user(user)
                .tweet(tweet)
                .build();

        likeRepository.save(like);

        return LikeResponse.builder()
                .message("Tweet liked successfully")
                .tweetId(tweet.getId())
                .userId(user.getId())
                .build();
    }

    @Override
    @Transactional
    public LikeResponse dislike(LikeRequest request, User currentUser) {
        User user = currentUser;
        if (user == null && request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TwitterException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        if (user == null) {
            throw new TwitterException("User must be authenticated or provided to dislike a tweet", HttpStatus.BAD_REQUEST);
        }

        Tweet tweet = tweetRepository.findById(request.getTweetId())
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + request.getTweetId(), HttpStatus.NOT_FOUND));

        Like like = likeRepository.findByUserIdAndTweetId(user.getId(), tweet.getId())
                .orElseThrow(() -> new TwitterException("You have not liked this tweet", HttpStatus.BAD_REQUEST));

        likeRepository.delete(like);

        return LikeResponse.builder()
                .message("Tweet disliked (like removed) successfully")
                .tweetId(tweet.getId())
                .userId(user.getId())
                .build();
    }
}
