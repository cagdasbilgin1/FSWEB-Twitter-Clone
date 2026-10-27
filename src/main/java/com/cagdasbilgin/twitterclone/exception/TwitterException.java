package com.cagdasbilgin.twitterclone.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TwitterException extends RuntimeException {
    private final HttpStatus status;

    public TwitterException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
