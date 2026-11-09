package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.LikeRequest;
import com.cagdasbilgin.twitterclone.dto.LikeResponse;
import com.cagdasbilgin.twitterclone.service.LikeService;
import com.cagdasbilgin.twitterclone.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    @Mock
    private LikeService likeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LikeController likeController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void like_ShouldReturnCreatedResponse() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).userId(1L).build();
        LikeResponse mockResponse = LikeResponse.builder()
                .message("Tweet liked successfully")
                .tweetId(10L)
                .userId(1L)
                .build();

        when(likeService.like(request, null)).thenReturn(mockResponse);

        ResponseEntity<LikeResponse> response = likeController.like(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tweet liked successfully", response.getBody().getMessage());
        verify(likeService, times(1)).like(request, null);
    }

    @Test
    void dislike_ShouldReturnOkResponse() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).userId(1L).build();
        LikeResponse mockResponse = LikeResponse.builder()
                .message("Tweet disliked (like removed) successfully")
                .tweetId(10L)
                .userId(1L)
                .build();

        when(likeService.dislike(request, null)).thenReturn(mockResponse);

        ResponseEntity<LikeResponse> response = likeController.dislike(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getTweetId());
        verify(likeService, times(1)).dislike(request, null);
    }

    @Test
    void findByUserId_ShouldReturnUserLikes() {
        LikeResponse item = LikeResponse.builder().message("Liked").tweetId(10L).userId(1L).build();
        when(likeService.findByUserId(1L)).thenReturn(List.of(item));

        ResponseEntity<List<LikeResponse>> response = likeController.findByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(likeService, times(1)).findByUserId(1L);
    }

    @Test
    void findByTweetId_ShouldReturnTweetLikes() {
        LikeResponse item = LikeResponse.builder().message("Liked").tweetId(10L).userId(1L).build();
        when(likeService.findByTweetId(10L)).thenReturn(List.of(item));

        ResponseEntity<List<LikeResponse>> response = likeController.findByTweetId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(likeService, times(1)).findByTweetId(10L);
    }
}

