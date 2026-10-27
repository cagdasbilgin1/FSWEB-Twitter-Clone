package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.LikeRequest;
import com.cagdasbilgin.twitterclone.dto.LikeResponse;
import com.cagdasbilgin.twitterclone.entity.User;

public interface LikeService {
    LikeResponse like(LikeRequest request, User currentUser);
    LikeResponse dislike(LikeRequest request, User currentUser);
}
