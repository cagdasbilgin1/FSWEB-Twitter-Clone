package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.TweetRequest;
import com.cagdasbilgin.twitterclone.dto.TweetResponse;
import com.cagdasbilgin.twitterclone.entity.User;

import java.util.List;

public interface TweetService {
    TweetResponse create(TweetRequest request, User currentUser);
    List<TweetResponse> findByUserId(Long userId);
    TweetResponse findById(Long id);
    List<TweetResponse> findAll();
    TweetResponse update(Long id, TweetRequest request, User currentUser);
    void delete(Long id, User currentUser, Long userId);
}
