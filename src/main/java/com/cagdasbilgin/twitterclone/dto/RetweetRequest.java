package com.cagdasbilgin.twitterclone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetweetRequest {

    @NotNull(message = "Tweet ID is required")
    private Long tweetId;

    private Long userId;
}
