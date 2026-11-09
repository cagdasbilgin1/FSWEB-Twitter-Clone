package com.cagdasbilgin.twitterclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 280, message = "Comment cannot exceed 280 characters")
    private String content;

    private Long tweetId;

    private Long userId;
}
