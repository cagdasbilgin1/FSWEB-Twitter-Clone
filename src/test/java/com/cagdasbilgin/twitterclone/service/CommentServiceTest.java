package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.CommentRequest;
import com.cagdasbilgin.twitterclone.dto.CommentResponse;
import com.cagdasbilgin.twitterclone.entity.Comment;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.CommentRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User tweetAuthor;
    private User commentAuthor;
    private Tweet tweet;
    private Comment comment;

    @BeforeEach
    void setUp() {
        tweetAuthor = User.builder().id(1L).username("tweet_author").build();
        commentAuthor = User.builder().id(2L).username("comment_author").build();

        tweet = Tweet.builder()
                .id(100L)
                .content("Main tweet")
                .user(tweetAuthor)
                .build();

        comment = Comment.builder()
                .id(50L)
                .content("Nice tweet!")
                .user(commentAuthor)
                .tweet(tweet)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_WhenValid_ShouldSaveComment() {
        CommentRequest request = CommentRequest.builder()
                .content("Great post!")
                .tweetId(100L)
                .build();

        when(tweetRepository.findById(100L)).thenReturn(Optional.of(tweet));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.create(request, commentAuthor);

        assertNotNull(response);
        assertEquals("Nice tweet!", response.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void update_WhenAuthor_ShouldUpdateContent() {
        CommentRequest updateReq = CommentRequest.builder()
                .content("Updated comment")
                .build();

        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.update(50L, updateReq, commentAuthor);

        assertNotNull(response);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void update_WhenNotAuthor_ShouldThrowForbidden() {
        User stranger = User.builder().id(99L).username("stranger").build();
        CommentRequest updateReq = CommentRequest.builder().content("Hacked").build();

        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        TwitterException ex = assertThrows(TwitterException.class, () -> commentService.update(50L, updateReq, stranger));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void delete_WhenCommentAuthor_ShouldDelete() {
        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> commentService.delete(50L, commentAuthor, null));
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void delete_WhenTweetAuthor_ShouldDelete() {
        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        // Tweet author can delete any comment on their tweet
        assertDoesNotThrow(() -> commentService.delete(50L, tweetAuthor, null));
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void delete_WhenNeitherCommentNorTweetAuthor_ShouldThrowForbidden() {
        User stranger = User.builder().id(99L).username("stranger").build();
        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        TwitterException ex = assertThrows(TwitterException.class, () -> commentService.delete(50L, stranger, null));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(commentRepository, never()).delete(any());
    }
}
