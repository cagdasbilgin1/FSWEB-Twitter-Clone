package com.cagdasbilgin.twitterclone.service.impl;

import com.cagdasbilgin.twitterclone.dto.CommentRequest;
import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.entity.Comment;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.CommentRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse create(CommentRequest request, User currentUser) {
        User author = currentUser;
        if (author == null && request.getUserId() != null) {
            author = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TwitterException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        if (author == null) {
            throw new TwitterException("User must be authenticated or provided to comment", HttpStatus.BAD_REQUEST);
        }

        Tweet tweet = tweetRepository.findById(request.getTweetId())
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + request.getTweetId(), HttpStatus.NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(author)
                .tweet(tweet)
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CommentResponse update(Long id, CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Comment not found with id: " + id, HttpStatus.NOT_FOUND));

        Long callerId = currentUser != null ? currentUser.getId() : request.getUserId();
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to update a comment", HttpStatus.UNAUTHORIZED);
        }

        if (!comment.getUser().getId().equals(callerId)) {
            throw new TwitterException("Only the author can update this comment", HttpStatus.FORBIDDEN);
        }

        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, User currentUser, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Comment not found with id: " + id, HttpStatus.NOT_FOUND));

        Long callerId = currentUser != null ? currentUser.getId() : userId;
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to delete a comment", HttpStatus.UNAUTHORIZED);
        }

        boolean isCommentOwner = comment.getUser().getId().equals(callerId);
        boolean isTweetOwner = comment.getTweet().getUser().getId().equals(callerId);

        if (!isCommentOwner && !isTweetOwner) {
            throw new TwitterException("Only the comment owner or tweet owner can delete this comment", HttpStatus.FORBIDDEN);
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findByTweetId(Long tweetId) {
        if (!tweetRepository.existsById(tweetId)) {
            throw new TwitterException("Tweet not found with id: " + tweetId, HttpStatus.NOT_FOUND);
        }
        return commentRepository.findAllByTweetIdOrderByCreatedAtAsc(tweetId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
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
