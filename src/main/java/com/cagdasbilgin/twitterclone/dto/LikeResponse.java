package com.cagdasbilgin.twitterclone.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponse {
    private String message;
    private Long tweetId;
    private Long userId;
}
