package com.cagdasbilgin.twitterclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 280, message = "Comment cannot exceed 280 characters")
    private String content;

    @NotNull(message = "Tweet ID is required")
    private Long tweetId;

    private Long userId;
}
