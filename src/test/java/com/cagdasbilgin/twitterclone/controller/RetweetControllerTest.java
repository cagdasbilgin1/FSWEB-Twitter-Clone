package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.RetweetRequest;
import com.cagdasbilgin.twitterclone.dto.RetweetResponse;
import com.cagdasbilgin.twitterclone.service.RetweetService;
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
class RetweetControllerTest {

    @Mock
    private RetweetService retweetService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RetweetController retweetController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void retweet_ShouldReturnCreatedResponse() {
        RetweetRequest request = RetweetRequest.builder().tweetId(10L).userId(2L).build();
        RetweetResponse mockResponse = RetweetResponse.builder()
                .id(100L)
                .tweetId(10L)
                .retweeterId(2L)
                .build();

        when(retweetService.retweet(request, null)).thenReturn(mockResponse);

        ResponseEntity<RetweetResponse> response = retweetController.retweet(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());
        verify(retweetService, times(1)).retweet(request, null);
    }

    @Test
    void deleteRetweet_ShouldReturnNoContent() {
        ResponseEntity<Void> response = retweetController.deleteRetweet(100L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(retweetService, times(1)).deleteRetweet(100L, null, 2L);
    }

    @Test
    void deleteRetweetByTweetId_ShouldReturnNoContent() {
        ResponseEntity<Void> response = retweetController.deleteRetweetByTweetId(10L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(retweetService, times(1)).deleteByTweetId(10L, null, 2L);
    }

    @Test
    void findByUserId_ShouldReturnUserRetweets() {
        RetweetResponse item = RetweetResponse.builder().id(100L).tweetId(10L).retweeterId(2L).build();
        when(retweetService.findByUserId(2L)).thenReturn(List.of(item));

        ResponseEntity<List<RetweetResponse>> response = retweetController.findByUserId(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(retweetService, times(1)).findByUserId(2L);
    }
}

