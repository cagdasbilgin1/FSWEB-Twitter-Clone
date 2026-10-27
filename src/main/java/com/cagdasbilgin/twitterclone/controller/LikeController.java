package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.LikeRequest;
import com.cagdasbilgin.twitterclone.dto.LikeResponse;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.service.LikeService;
import com.cagdasbilgin.twitterclone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;

    // Supports: POST http://localhost:3000/like/ and /like
    @PostMapping({"/like", "/like/"})
    public ResponseEntity<LikeResponse> like(@Valid @RequestBody LikeRequest request) {
        User currentUser = getCurrentUserOrNull();
        LikeResponse response = likeService.like(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Supports: POST http://localhost:3000/dislike/ and /dislike
    @PostMapping({"/dislike", "/dislike/"})
    public ResponseEntity<LikeResponse> dislike(@Valid @RequestBody LikeRequest request) {
        User currentUser = getCurrentUserOrNull();
        LikeResponse response = likeService.dislike(request, currentUser);
        return ResponseEntity.ok(response);
    }

    private User getCurrentUserOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return userService.getUserByUsername(auth.getName());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
