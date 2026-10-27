package com.cagdasbilgin.twitterclone.service.impl;

import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.dto.TweetRequest;
import com.cagdasbilgin.twitterclone.dto.TweetResponse;
import com.cagdasbilgin.twitterclone.entity.Comment;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.*;
import com.cagdasbilgin.twitterclone.service.TweetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final RetweetRepository retweetRepository;

    @Override
    @Transactional
    public TweetResponse create(TweetRequest request, User currentUser) {
        User author = currentUser;
        if (author == null && request.getUserId() != null) {
            author = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TwitterException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        if (author == null) {
            throw new TwitterException("Tweet must belong to a valid user. Anonymous tweets are not allowed.", HttpStatus.BAD_REQUEST);
        }

        Tweet tweet = Tweet.builder()
                .content(request.getContent())
                .user(author)
                .build();

        Tweet saved = tweetRepository.save(tweet);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TweetResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new TwitterException("User not found with id: " + userId, HttpStatus.NOT_FOUND);
        }
        return tweetRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TweetResponse findById(Long id) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + id, HttpStatus.NOT_FOUND));
        return mapToResponse(tweet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TweetResponse> findAll() {
        return tweetRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TweetResponse update(Long id, TweetRequest request, User currentUser) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + id, HttpStatus.NOT_FOUND));

        Long callerId = currentUser != null ? currentUser.getId() : request.getUserId();
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to update a tweet", HttpStatus.UNAUTHORIZED);
        }

        if (!tweet.getUser().getId().equals(callerId)) {
            throw new TwitterException("Only the author can update this tweet", HttpStatus.FORBIDDEN);
        }

        tweet.setContent(request.getContent());
        Tweet updated = tweetRepository.save(tweet);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, User currentUser, Long userId) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + id, HttpStatus.NOT_FOUND));

        Long callerId = currentUser != null ? currentUser.getId() : userId;
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to delete a tweet", HttpStatus.UNAUTHORIZED);
        }

        if (!tweet.getUser().getId().equals(callerId)) {
            throw new TwitterException("Only the tweet owner can delete this tweet", HttpStatus.FORBIDDEN);
        }

        tweetRepository.delete(tweet);
    }

    private TweetResponse mapToResponse(Tweet tweet) {
        List<CommentResponse> commentResponses = tweet.getComments() != null
                ? tweet.getComments().stream().map(this::mapCommentToResponse).collect(Collectors.toList())
                : List.of();

        return TweetResponse.builder()
                .id(tweet.getId())
                .content(tweet.getContent())
                .userId(tweet.getUser().getId())
                .username(tweet.getUser().getUsername())
                .createdAt(tweet.getCreatedAt())
                .updatedAt(tweet.getUpdatedAt())
                .likeCount((int) likeRepository.countByTweetId(tweet.getId()))
                .commentCount(commentResponses.size())
                .retweetCount((int) retweetRepository.countByTweetId(tweet.getId()))
                .comments(commentResponses)
                .build();
    }

    private CommentResponse mapCommentToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .tweetId(comment.getTweet().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
