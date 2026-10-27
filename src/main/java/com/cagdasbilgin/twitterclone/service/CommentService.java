package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.CommentRequest;
import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.entity.User;

import java.util.List;

public interface CommentService {
    CommentResponse create(CommentRequest request, User currentUser);
    CommentResponse update(Long id, CommentRequest request, User currentUser);
    void delete(Long id, User currentUser, Long userId);
    List<CommentResponse> findByTweetId(Long tweetId);
}
