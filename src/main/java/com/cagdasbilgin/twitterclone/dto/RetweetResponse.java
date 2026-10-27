package com.cagdasbilgin.twitterclone.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetweetResponse {
    private Long id;
    private Long tweetId;
    private String tweetContent;
    private Long tweetAuthorId;
    private String tweetAuthorUsername;
    private Long retweeterId;
    private String retweeterUsername;
    private LocalDateTime createdAt;
}
