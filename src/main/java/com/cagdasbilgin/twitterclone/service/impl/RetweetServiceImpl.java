package com.cagdasbilgin.twitterclone.service.impl;

import com.cagdasbilgin.twitterclone.dto.RetweetRequest;
import com.cagdasbilgin.twitterclone.dto.RetweetResponse;
import com.cagdasbilgin.twitterclone.entity.Retweet;
import com.cagdasbilgin.twitterclone.entity.Tweet;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.RetweetRepository;
import com.cagdasbilgin.twitterclone.repository.TweetRepository;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.RetweetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetweetServiceImpl implements RetweetService {

    private final RetweetRepository retweetRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RetweetResponse retweet(RetweetRequest request, User currentUser) {
        User user = currentUser;
        if (user == null && request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TwitterException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        if (user == null) {
            throw new TwitterException("User must be authenticated or provided to retweet", HttpStatus.BAD_REQUEST);
        }

        Tweet tweet = tweetRepository.findById(request.getTweetId())
                .orElseThrow(() -> new TwitterException("Tweet not found with id: " + request.getTweetId(), HttpStatus.NOT_FOUND));

        if (tweet.getUser() != null && user.getId().equals(tweet.getUser().getId())) {
            throw new TwitterException("You cannot retweet your own tweet", HttpStatus.BAD_REQUEST);
        }

        if (retweetRepository.existsByUserIdAndTweetId(user.getId(), tweet.getId())) {
            throw new TwitterException("You have already retweeted this tweet", HttpStatus.BAD_REQUEST);
        }

        Retweet retweet = Retweet.builder()
                .user(user)
                .tweet(tweet)
                .build();

        Retweet saved = retweetRepository.save(retweet);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRetweet(Long id, User currentUser, Long userId) {
        Retweet retweet = retweetRepository.findById(id)
                .orElseThrow(() -> new TwitterException("Retweet not found with id: " + id, HttpStatus.NOT_FOUND));

        Long callerId = currentUser != null ? currentUser.getId() : userId;
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to delete a retweet", HttpStatus.UNAUTHORIZED);
        }

        boolean isRetweeter = retweet.getUser().getId().equals(callerId);
        boolean isOriginalAuthor = retweet.getTweet().getUser().getId().equals(callerId);

        if (!isRetweeter && !isOriginalAuthor) {
            throw new TwitterException("Only the retweeter or original tweet author can delete this retweet", HttpStatus.FORBIDDEN);
        }

        retweetRepository.delete(retweet);
    }

    @Override
    @Transactional
    public void deleteByTweetId(Long tweetId, User currentUser, Long userId) {
        Long callerId = currentUser != null ? currentUser.getId() : userId;
        if (callerId == null) {
            throw new TwitterException("Authentication or userId is required to undo a retweet", HttpStatus.UNAUTHORIZED);
        }

        if (!tweetRepository.existsById(tweetId)) {
            throw new TwitterException("Tweet not found with id: " + tweetId, HttpStatus.NOT_FOUND);
        }

        Retweet retweet = retweetRepository.findByUserIdAndTweetId(callerId, tweetId)
                .orElseThrow(() -> new TwitterException("You have not retweeted this tweet", HttpStatus.BAD_REQUEST));

        retweetRepository.delete(retweet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetweetResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new TwitterException("User not found with id: " + userId, HttpStatus.NOT_FOUND);
        }
        return retweetRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RetweetResponse mapToResponse(Retweet retweet) {
        return RetweetResponse.builder()
                .id(retweet.getId())
                .tweetId(retweet.getTweet().getId())
                .tweetContent(retweet.getTweet().getContent())
                .tweetAuthorId(retweet.getTweet().getUser().getId())
                .tweetAuthorUsername(retweet.getTweet().getUser().getUsername())
                .retweeterId(retweet.getUser().getId())
                .retweeterUsername(retweet.getUser().getUsername())
                .createdAt(retweet.getCreatedAt())
                .build();
    }
}
