package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.RetweetRequest;
import com.cagdasbilgin.twitterclone.dto.RetweetResponse;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.service.RetweetService;
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
@RequestMapping("/retweet")
@RequiredArgsConstructor
public class RetweetController {

    private final RetweetService retweetService;
    private final UserService userService;

    // Supports: POST http://localhost:3000/retweet/ and /retweet
    @PostMapping({"", "/"})
    public ResponseEntity<RetweetResponse> retweet(@Valid @RequestBody RetweetRequest request) {
        User currentUser = getCurrentUserOrNull();
        RetweetResponse response = retweetService.retweet(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Supports: DELETE http://localhost:3000/retweet/:id and ?userId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetweet(@PathVariable Long id, @RequestParam(name = "userId", required = false) Long userId) {
        User currentUser = getCurrentUserOrNull();
        retweetService.deleteRetweet(id, currentUser, userId);
        return ResponseEntity.noContent().build();
    }

    // Supports: GET http://localhost:3000/retweet/user/:userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RetweetResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(retweetService.findByUserId(userId));
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
