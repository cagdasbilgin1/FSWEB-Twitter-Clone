package com.cagdasbilgin.twitterclone.controller;

import com.cagdasbilgin.twitterclone.dto.CommentRequest;
import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.service.CommentService;
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
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_ShouldReturnCreatedResponse() {
        CommentRequest request = CommentRequest.builder()
                .content("Nice post!")
                .tweetId(10L)
                .userId(2L)
                .build();
        CommentResponse mockResponse = CommentResponse.builder()
                .id(50L)
                .content("Nice post!")
                .tweetId(10L)
                .userId(2L)
                .username("comment_user")
                .build();

        when(commentService.create(request, null)).thenReturn(mockResponse);

        ResponseEntity<CommentResponse> response = commentController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getId());
        assertEquals("Nice post!", response.getBody().getContent());
        verify(commentService, times(1)).create(request, null);
    }

    @Test
    void update_ShouldReturnUpdatedComment() {
        CommentRequest request = CommentRequest.builder()
                .content("Edited comment")
                .userId(2L)
                .build();
        CommentResponse mockResponse = CommentResponse.builder()
                .id(50L)
                .content("Edited comment")
                .tweetId(10L)
                .userId(2L)
                .build();

        when(commentService.update(50L, request, null)).thenReturn(mockResponse);

        ResponseEntity<CommentResponse> response = commentController.update(50L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Edited comment", response.getBody().getContent());
        verify(commentService, times(1)).update(50L, request, null);
    }

    @Test
    void delete_ShouldReturnNoContent() {
        ResponseEntity<Void> response = commentController.delete(50L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commentService, times(1)).delete(50L, null, 2L);
    }

    @Test
    void getByTweetId_ShouldReturnComments() {
        CommentResponse item = CommentResponse.builder().id(50L).content("Hello").tweetId(10L).build();
        when(commentService.findByTweetId(10L)).thenReturn(List.of(item));

        ResponseEntity<List<CommentResponse>> response = commentController.getByTweetId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(commentService, times(1)).findByTweetId(10L);
    }
}

