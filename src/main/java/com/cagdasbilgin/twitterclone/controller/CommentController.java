package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.CommentRequest;
import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.service.CommentService;
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
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    // Supports: POST /comment/ and POST /comment
    @PostMapping({"", "/"})
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
        User currentUser = getCurrentUserOrNull();
        CommentResponse response = commentService.create(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Supports: PUT /comment/:id
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        User currentUser = getCurrentUserOrNull();
        return ResponseEntity.ok(commentService.update(id, request, currentUser));
    }

    // Supports: DELETE /comment/:id and DELETE /comment/:id?userId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(name = "userId", required = false) Long userId) {
        User currentUser = getCurrentUserOrNull();
        commentService.delete(id, currentUser, userId);
        return ResponseEntity.noContent().build();
    }

    // Supports: GET /comment/tweet/:tweetId
    @GetMapping("/tweet/{tweetId}")
    public ResponseEntity<List<CommentResponse>> getByTweetId(@PathVariable Long tweetId) {
        return ResponseEntity.ok(commentService.findByTweetId(tweetId));
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
