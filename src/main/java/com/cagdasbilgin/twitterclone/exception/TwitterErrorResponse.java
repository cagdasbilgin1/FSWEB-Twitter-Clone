package com.cagdasbilgin.twitterclone.exception;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwitterErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
