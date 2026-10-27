package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.LikeRequest;
import com.cagdasbilgin.twitterclone.dto.LikeResponse;
import com.cagdasbilgin.twitterclone.entity.Like;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.LikeRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.impl.LikeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User user;
    private Tweet tweet;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test_user").build();
        tweet = Tweet.builder().id(10L).content("A tweet to like").user(user).build();
    }

    @Test
    void like_WhenNotAlreadyLiked_ShouldSaveLike() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(likeRepository.existsByUserIdAndTweetId(1L, 10L)).thenReturn(false);

        LikeResponse response = likeService.like(request, user);

        assertNotNull(response);
        assertEquals("Tweet liked successfully", response.getMessage());
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    void like_WhenAlreadyLiked_ShouldThrowBadRequest() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(likeRepository.existsByUserIdAndTweetId(1L, 10L)).thenReturn(true);

        TwitterException ex = assertThrows(TwitterException.class, () -> likeService.like(request, user));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(likeRepository, never()).save(any());
    }

    @Test
    void dislike_WhenLiked_ShouldDeleteLike() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).build();
        Like like = Like.builder().id(5L).user(user).tweet(tweet).build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserIdAndTweetId(1L, 10L)).thenReturn(Optional.of(like));

        LikeResponse response = likeService.dislike(request, user);

        assertNotNull(response);
        assertEquals("Tweet disliked (like removed) successfully", response.getMessage());
        verify(likeRepository, times(1)).delete(like);
    }

    @Test
    void dislike_WhenNotLiked_ShouldThrowBadRequest() {
        LikeRequest request = LikeRequest.builder().tweetId(10L).build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserIdAndTweetId(1L, 10L)).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class, () -> likeService.dislike(request, user));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(likeRepository, never()).delete(any());
    }
}
