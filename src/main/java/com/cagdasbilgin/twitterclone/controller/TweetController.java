package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.TweetRequest;
import com.cagdasbilgin.twitterclone.dto.TweetResponse;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.service.TweetService;
import com.cagdasbilgin.twitterclone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tweet")
@RequiredArgsConstructor
public class TweetController {

    private final TweetService tweetService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<TweetResponse> create(@Valid @RequestBody TweetRequest request) {
        User currentUser = getCurrentUserOrNull();
        TweetResponse response = tweetService.create(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Supports: GET /tweet/findByUserId?userId=1
    @GetMapping("/findByUserId")
    public ResponseEntity<List<TweetResponse>> findByUserIdParam(@RequestParam(name = "userId", required = false) Long userId) {
        Long targetUserId = userId;
        if (targetUserId == null) {
            User currentUser = getCurrentUserOrNull();
            if (currentUser != null) {
                targetUserId = currentUser.getId();
            }
        }
        if (targetUserId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tweetService.findByUserId(targetUserId));
    }

    // Supports: GET /tweet/findByUserId/1
    @GetMapping("/findByUserId/{userId}")
    public ResponseEntity<List<TweetResponse>> findByUserIdPath(@PathVariable Long userId) {
        return ResponseEntity.ok(tweetService.findByUserId(userId));
    }

    // Supports: GET /tweet/findById?id=1
    @GetMapping("/findById")
    public ResponseEntity<TweetResponse> findByIdParam(@RequestParam(name = "id") Long id) {
        return ResponseEntity.ok(tweetService.findById(id));
    }

    // Supports: GET /tweet/findById/1
    @GetMapping("/findById/{id}")
    public ResponseEntity<TweetResponse> findByIdPath(@PathVariable Long id) {
        return ResponseEntity.ok(tweetService.findById(id));
    }

    // Supports: GET /tweet/1
    @GetMapping("/{id}")
    public ResponseEntity<TweetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tweetService.findById(id));
    }

    // Supports: GET /tweet (list all tweets)
    @GetMapping
    public ResponseEntity<List<TweetResponse>> getAll() {
        return ResponseEntity.ok(tweetService.findAll());
    }

    // Supports: PUT /tweet/:id
    @PutMapping("/{id}")
    public ResponseEntity<TweetResponse> update(@PathVariable Long id, @Valid @RequestBody TweetRequest request) {
        User currentUser = getCurrentUserOrNull();
        return ResponseEntity.ok(tweetService.update(id, request, currentUser));
    }

    // Supports: DELETE /tweet/:id and DELETE /tweet/:id?userId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(name = "userId", required = false) Long userId) {
        User currentUser = getCurrentUserOrNull();
        tweetService.delete(id, currentUser, userId);
        return ResponseEntity.noContent().build();
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
