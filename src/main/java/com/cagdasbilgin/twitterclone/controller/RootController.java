package com.cagdasbilgin.twitterclone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "application", "Twitter Clone API",
                "status", "UP",
                "version", "1.0.0",
                "port", 3000,
                "endpoints", Map.of(
                        "auth", List.of("POST /register", "POST /login"),
                        "tweet", List.of("POST /tweet", "GET /tweet", "GET /tweet/:id", "GET /tweet/findByUserId?userId={id}", "GET /tweet/findById?id={id}", "PUT /tweet/:id", "DELETE /tweet/:id"),
                        "comment", List.of("POST /comment", "GET /comment/tweet/:tweetId", "PUT /comment/:id", "DELETE /comment/:id"),
                        "like", List.of("POST /like", "POST /dislike"),
                        "retweet", List.of("POST /retweet", "DELETE /retweet/:id", "GET /retweet/user/:userId")
                )
        ));
    }
}
