package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.RetweetRequest;
import com.cagdasbilgin.twitterclone.dto.RetweetResponse;
import com.cagdasbilgin.twitterclone.entity.User;

import java.util.List;

public interface RetweetService {
    RetweetResponse retweet(RetweetRequest request, User currentUser);
    void deleteRetweet(Long id, User currentUser, Long userId);
    List<RetweetResponse> findByUserId(Long userId);
}
