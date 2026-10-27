package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.TweetRequest;
import com.cagdasbilgin.twitterclone.dto.TweetResponse;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.LikeRepository;
import com.cagdasbilgin.twitterclone.repository.RetweetRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.impl.TweetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private RetweetRepository retweetRepository;

    @InjectMocks
    private TweetServiceImpl tweetService;

    private User user;
    private Tweet tweet;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_pass")
                .role("ROLE_USER")
                .build();

        tweet = Tweet.builder()
                .id(10L)
                .content("First tweet!")
                .user(user)
                .createdAt(LocalDateTime.now())
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .retweets(new ArrayList<>())
                .build();
    }

    @Test
    void create_WhenValidUser_ShouldSaveAndReturnResponse() {
        TweetRequest request = TweetRequest.builder()
                .content("Hello World")
                .userId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tweetRepository.save(any(Tweet.class))).thenAnswer(invocation -> {
            Tweet t = invocation.getArgument(0);
            t.setId(10L);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });
        when(likeRepository.countByTweetId(any())).thenReturn(0L);
        when(retweetRepository.countByTweetId(any())).thenReturn(0L);

        TweetResponse response = tweetService.create(request, null);

        assertNotNull(response);
        assertEquals("Hello World", response.getContent());
        assertEquals(1L, response.getUserId());
        assertEquals("john_doe", response.getUsername());
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    void create_WhenNoUserProvided_ShouldThrowException() {
        TweetRequest request = TweetRequest.builder()
                .content("Anonymous tweet")
                .build();

        TwitterException ex = assertThrows(TwitterException.class, () -> tweetService.create(request, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(tweetRepository, never()).save(any());
    }

    @Test
    void findById_WhenTweetExists_ShouldReturnResponse() {
        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(likeRepository.countByTweetId(10L)).thenReturn(5L);
        when(retweetRepository.countByTweetId(10L)).thenReturn(2L);

        TweetResponse response = tweetService.findById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("First tweet!", response.getContent());
        assertEquals(5, response.getLikeCount());
        assertEquals(2, response.getRetweetCount());
    }

    @Test
    void findById_WhenTweetNotFound_ShouldThrowNotFound() {
        when(tweetRepository.findById(99L)).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class, () -> tweetService.findById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void findByUserId_WhenUserExists_ShouldReturnTweets() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tweetRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tweet));

        List<TweetResponse> responses = tweetService.findByUserId(1L);

        assertEquals(1, responses.size());
        assertEquals("First tweet!", responses.get(0).getContent());
    }

    @Test
    void update_WhenOwner_ShouldUpdateContent() {
        TweetRequest updateReq = TweetRequest.builder()
                .content("Updated content")
                .userId(1L)
                .build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(tweetRepository.save(any(Tweet.class))).thenReturn(tweet);

        TweetResponse response = tweetService.update(10L, updateReq, user);

        assertNotNull(response);
        verify(tweetRepository, times(1)).save(tweet);
    }

    @Test
    void update_WhenNotOwner_ShouldThrowForbidden() {
        User otherUser = User.builder().id(2L).username("hacker").build();
        TweetRequest updateReq = TweetRequest.builder().content("Hacked").build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));

        TwitterException ex = assertThrows(TwitterException.class, () -> tweetService.update(10L, updateReq, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(tweetRepository, never()).save(tweet);
    }

    @Test
    void delete_WhenOwner_ShouldDeleteTweet() {
        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));

        assertDoesNotThrow(() -> tweetService.delete(10L, user, null));
        verify(tweetRepository, times(1)).delete(tweet);
    }

    @Test
    void delete_WhenNotOwner_ShouldThrowForbidden() {
        User otherUser = User.builder().id(2L).username("other").build();
        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));

        TwitterException ex = assertThrows(TwitterException.class, () -> tweetService.delete(10L, otherUser, null));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(tweetRepository, never()).delete(any());
    }
}
