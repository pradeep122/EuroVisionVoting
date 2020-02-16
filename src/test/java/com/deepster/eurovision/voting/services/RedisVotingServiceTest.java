package com.deepster.eurovision.voting.services;

import com.google.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.embedded.RedisServer;

import java.io.IOException;

class RedisVotingServiceTest {
    private static RedisServer redisServer;

    @BeforeAll
    static void beforeAll() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @AfterAll
    static void afterAll() {
        redisServer.stop();
    }

    @Test
    @Inject
    void validateYear(RedisVotingService service) {

    }

    @Test
    void validateVoteJson() {
    }

    @Test
    void prettifyCountry() {
    }

    @Test
    void prettifyWinners() {
    }

    @Test
    void validateAndCast() {

    }

    @Test
    void validateAndFetchResults() {
    }
}