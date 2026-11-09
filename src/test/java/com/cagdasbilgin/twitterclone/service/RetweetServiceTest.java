package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.RetweetRequest;
import com.cagdasbilgin.twitterclone.dto.RetweetResponse;
import com.cagdasbilgin.twitterclone.entity.Retweet;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.RetweetRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.impl.RetweetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetweetServiceTest {

    @Mock
    private RetweetRepository retweetRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RetweetServiceImpl retweetService;

    private User tweetAuthor;
    private User retweeter;
    private Tweet tweet;
    private Retweet retweet;

    @BeforeEach
    void setUp() {
        tweetAuthor = User.builder()
                .id(1L)
                .username("tweet_owner")
                .build();

        retweeter = User.builder()
                .id(2L)
                .username("retweet_user")
                .build();

        tweet = Tweet.builder()
                .id(10L)
                .content("Original tweet content")
                .user(tweetAuthor)
                .createdAt(LocalDateTime.now())
                .build();

        retweet = Retweet.builder()
                .id(100L)
                .user(retweeter)
                .tweet(tweet)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void retweet_WhenValidWithCurrentUser_ShouldSaveAndReturnResponse() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(10L)
                .build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(retweetRepository.existsByUserIdAndTweetId(2L, 10L)).thenReturn(false);
        when(retweetRepository.save(any(Retweet.class))).thenReturn(retweet);

        RetweetResponse response = retweetService.retweet(request, retweeter);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getTweetId());
        assertEquals("Original tweet content", response.getTweetContent());
        assertEquals(2L, response.getRetweeterId());
        assertEquals("retweet_user", response.getRetweeterUsername());
        verify(retweetRepository, times(1)).save(any(Retweet.class));
    }

    @Test
    void retweet_WhenRequestHasUserId_ShouldSaveAndReturnResponse() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(10L)
                .userId(2L)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(retweeter));
        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(retweetRepository.existsByUserIdAndTweetId(2L, 10L)).thenReturn(false);
        when(retweetRepository.save(any(Retweet.class))).thenReturn(retweet);

        RetweetResponse response = retweetService.retweet(request, null);

        assertNotNull(response);
        assertEquals(2L, response.getRetweeterId());
        verify(retweetRepository, times(1)).save(any(Retweet.class));
    }

    @Test
    void retweet_WhenNoUserProvided_ShouldThrowBadRequest() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(10L)
                .build();

        TwitterException ex = assertThrows(TwitterException.class, () -> retweetService.retweet(request, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(retweetRepository, never()).save(any());
    }

    @Test
    void retweet_WhenTweetNotFound_ShouldThrowNotFound() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(999L)
                .build();

        when(tweetRepository.findById(999L)).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class, () -> retweetService.retweet(request, retweeter));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(retweetRepository, never()).save(any());
    }

    @Test
    void retweet_WhenOwnTweet_ShouldThrowBadRequest() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(10L)
                .build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));

        TwitterException ex = assertThrows(TwitterException.class, () -> retweetService.retweet(request, tweetAuthor));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("You cannot retweet your own tweet", ex.getMessage());
        verify(retweetRepository, never()).save(any());
    }

    @Test
    void retweet_WhenAlreadyRetweeted_ShouldThrowBadRequest() {
        RetweetRequest request = RetweetRequest.builder()
                .tweetId(10L)
                .build();

        when(tweetRepository.findById(10L)).thenReturn(Optional.of(tweet));
        when(retweetRepository.existsByUserIdAndTweetId(2L, 10L)).thenReturn(true);

        TwitterException ex = assertThrows(TwitterException.class, () -> retweetService.retweet(request, retweeter));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(retweetRepository, never()).save(any());
    }

    @Test
    void deleteRetweet_WhenRetweeter_ShouldDelete() {
        when(retweetRepository.findById(100L)).thenReturn(Optional.of(retweet));

        assertDoesNotThrow(() -> retweetService.deleteRetweet(100L, retweeter, null));
        verify(retweetRepository, times(1)).delete(retweet);
    }

    @Test
    void deleteRetweet_WhenOriginalTweetAuthor_ShouldDelete() {
        when(retweetRepository.findById(100L)).thenReturn(Optional.of(retweet));

        assertDoesNotThrow(() -> retweetService.deleteRetweet(100L, tweetAuthor, null));
        verify(retweetRepository, times(1)).delete(retweet);
    }

    @Test
    void deleteRetweet_WhenStranger_ShouldThrowForbidden() {
        User stranger = User.builder().id(99L).username("stranger").build();
        when(retweetRepository.findById(100L)).thenReturn(Optional.of(retweet));

        TwitterException ex = assertThrows(TwitterException.class,
                () -> retweetService.deleteRetweet(100L, stranger, null));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(retweetRepository, never()).delete(any());
    }

    @Test
    void deleteRetweet_WhenNotFound_ShouldThrowNotFound() {
        when(retweetRepository.findById(999L)).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class,
                () -> retweetService.deleteRetweet(999L, retweeter, null));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteByTweetId_WhenRetweeted_ShouldDelete() {
        when(tweetRepository.existsById(10L)).thenReturn(true);
        when(retweetRepository.findByUserIdAndTweetId(2L, 10L)).thenReturn(Optional.of(retweet));

        assertDoesNotThrow(() -> retweetService.deleteByTweetId(10L, null, 2L));
        verify(retweetRepository, times(1)).delete(retweet);
    }

    @Test
    void deleteByTweetId_WhenNotRetweeted_ShouldThrowBadRequest() {
        when(tweetRepository.existsById(10L)).thenReturn(true);
        when(retweetRepository.findByUserIdAndTweetId(2L, 10L)).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class,
                () -> retweetService.deleteByTweetId(10L, retweeter, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(retweetRepository, never()).delete(any());
    }

    @Test
    void findByUserId_WhenUserExists_ShouldReturnRetweets() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(retweetRepository.findAllByUserId(2L)).thenReturn(List.of(retweet));

        List<RetweetResponse> responses = retweetService.findByUserId(2L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getTweetId());
        assertEquals("retweet_user", responses.get(0).getRetweeterUsername());
    }

    @Test
    void findByUserId_WhenUserNotFound_ShouldThrowNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        TwitterException ex = assertThrows(TwitterException.class, () -> retweetService.findByUserId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}

