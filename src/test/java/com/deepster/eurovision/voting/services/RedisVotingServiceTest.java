package com.deepster.eurovision.voting.services;

import com.deepster.eurovision.Application;
import com.deepster.eurovision.voting.dao.RedisVotingDAO;
import com.deepster.eurovision.voting.dao.VotingDAO;
import com.deepster.eurovision.voting.exceptions.InvalidCountryException;
import com.deepster.eurovision.voting.exceptions.InvalidVoteBodyException;
import com.deepster.eurovision.voting.exceptions.InvalidYearException;
import com.deepster.eurovision.voting.exceptions.SameCountryException;
import com.deepster.eurovision.voting.models.Vote;
import com.deepster.eurovision.voting.models.Winners;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

class RedisVotingServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(RedisVotingServiceTest.class);

    private static RedisVotingService votingService;
    private static VotingDAO votingDAO;

    @BeforeAll
    static void beforeAll() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        Set<String> countries = Set.of("Netherlands", "Spain", "France", "Belgium").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Load configuration from test.conf
        Config config = ConfigFactory.load("test");
        assertEquals("test", config.getString("env"));

        // create a mock VotindDAO object with minimal wiring to ensure tests dont fail
        votingDAO = mock(VotingDAO.class);
        when(votingDAO.incrementAggregateForCountry(anyInt(), anyString()))
                .thenReturn(20D);
        when(votingDAO.getResults(anyInt()))
                .thenReturn(new Winners(2020, "Netherlands", "Belgium", "Spain"));

        // create voting service with all dummy objects
        votingService = new RedisVotingService(votingDAO, countries, mapper, config);
    }

    @AfterAll
    static void afterAll() {
    }

    @Test
    @Inject
    void validateYear() {
        // TODO pick year string from current year
        // Valid inputs
        assertEquals(2020, votingService.validateYear("2020"));
        assertEquals(1956, votingService.validateYear("1956"));
        assertEquals(1985, votingService.validateYear("1985"));
        assertEquals(1982, votingService.validateYear("001982"));


        // Throw exceptions for invalid inputs
        assertThrows(InvalidYearException.class, () -> votingService.validateYear("1955"));
        assertThrows(InvalidYearException.class, () -> votingService.validateYear("2021"));
        assertThrows(InvalidYearException.class, () -> votingService.validateYear(""));
        assertThrows(InvalidYearException.class, () -> votingService.validateYear("sdfs"));
        assertThrows(InvalidYearException.class, () -> votingService.validateYear(null));

    }

    @Test
    void validateVoteJson() {

        String format = "{\"countryFrom\" : \"%s\", \"votedFor\" : \"%s\"}";

        // Valid JSON Data
        assertEquals(new Vote("Belgium", "Netherlands"),
                votingService.validateVoteJson(String.format(format, "Belgium", "Netherlands")));
        assertEquals(new Vote("Belgium", "Spain"),
                votingService.validateVoteJson(String.format(format, "Belgium", "spain")));
        assertEquals(new Vote("France", "Netherlands"),
                votingService.validateVoteJson(String.format(format, "france", "Netherlands")));
        assertEquals(new Vote("France", "Spain"),
                votingService.validateVoteJson(String.format(format, "france", "spain")));


        // Throw Exception for Invalid JSON
        assertThrows(InvalidVoteBodyException.class, () -> votingService.validateVoteJson(""));
        assertThrows(InvalidVoteBodyException.class, () -> votingService.validateVoteJson("{}"));
        assertThrows(InvalidVoteBodyException.class,
                () -> votingService.validateVoteJson("{'countryFrom': 'Belgium', 'votedFor': 'Spain'}"));
        assertThrows(InvalidVoteBodyException.class,
                () -> votingService.validateVoteJson("{\"countryFrom\": \"Belgium\"}"));
        assertThrows(InvalidVoteBodyException.class,
                () -> votingService.validateVoteJson("{ \"votedFor\": \"Spain\"}"));
        assertThrows(InvalidVoteBodyException.class,
                () -> votingService.validateVoteJson("{\"countryFrom\": \"Belgium\", \"votedFor\": \"Spain\", \"voterName\" : \"David\"}"));

        // Throw appropriate Exceptions for Invalid Data
        assertThrows(InvalidCountryException.class, () -> votingService.validateVoteJson(String.format(format, "Ireland", "Netherlands")));
        assertThrows(InvalidCountryException.class, () -> votingService.validateVoteJson(String.format(format, "Ireland", "Portugal")));
        assertThrows(InvalidCountryException.class, () -> votingService.validateVoteJson(String.format(format, "Spain", "Sweden")));
        assertThrows(SameCountryException.class, () -> votingService.validateVoteJson(String.format(format, "Spain", "Spain")));
        assertThrows(SameCountryException.class, () -> votingService.validateVoteJson(String.format(format, "Spain", "spain")));
        assertThrows(SameCountryException.class, () -> votingService.validateVoteJson(String.format(format, "spain", "Spain")));
        assertThrows(SameCountryException.class, () -> votingService.validateVoteJson(String.format(format, "spain", "spain")));
    }

    @Test
    void prettifyCountry() {

        // Null inputs
        assertThrows(NullPointerException.class, () -> votingService.prettifyCountry(null));

        // Valid inputs
        assertEquals("Spain", votingService.prettifyCountry("Spain"));
        assertEquals("Spain", votingService.prettifyCountry("spain"));
        assertEquals("The Netherlands", votingService.prettifyCountry("the netherlands"));
        assertEquals("The Netherlands", votingService.prettifyCountry("The netherlands"));
        assertEquals("Bosnia And Herzegovina", votingService.prettifyCountry("bosnia and herzegovina"));
    }

    @Test
    void prettifyWinners() {
        // Null inputs
        assertThrows(NullPointerException.class, () -> votingService.prettifyWinners(null));

        // Valid input
        assertEquals(new Winners(2000, "Denmark", "Russia", "Latvia"),
                votingService.prettifyWinners(new Winners(2000, "denmark", "Russia", "latvia")));
    }

    @Test
    void validateAndCast() {
        String format = "{\"countryFrom\" : \"%s\", \"votedFor\" : \"%s\"}";

        // Valid Input year and vote json

        ArgumentCaptor<Vote> capturedVote1 = ArgumentCaptor.forClass(Vote.class);
        votingService.validateAndCast("2020", String.format(format, "Belgium", "Spain"));
        verify(votingDAO).incrementAggregateForCountry(2020, "Spain");
        verify(votingDAO).saveVote(eq(2020), capturedVote1.capture());
        assertEquals("Belgium", capturedVote1.getValue().getCountryFrom());
        assertEquals("Spain", capturedVote1.getValue().getVotedFor());


        ArgumentCaptor<Vote> capturedVote2 = ArgumentCaptor.forClass(Vote.class);
        votingService.validateAndCast("1980", String.format(format, "netherlands", "france"));
        verify(votingDAO).incrementAggregateForCountry(1980, "France");
        verify(votingDAO).saveVote(eq(1980), capturedVote2.capture());
        assertEquals("Netherlands", capturedVote2.getValue().getCountryFrom());
        assertEquals("France", capturedVote2.getValue().getVotedFor());

        // Throw appropriate exceptions for invalid inputs
        assertThrows(InvalidYearException.class, () ->  votingService.validateAndCast("1945", String.format(format, "Spain", "France")));
        assertThrows(SameCountryException.class, () ->  votingService.validateAndCast("1980", String.format(format, "Spain", "Spain")));
        assertThrows(InvalidCountryException.class, () ->  votingService.validateAndCast("1999", String.format(format, "Spain", "Sweden")));
        assertThrows(InvalidCountryException.class, () ->  votingService.validateAndCast("1999", String.format(format, "Greece", "Belgium")));
    }

    @Test
    void validateAndFetchResults() {

        // Valid input
        votingService.validateAndFetchResults("2020");
        verify(votingDAO).getResults(2020);

        // Throw appropriate exceptions for invalid inputs
        assertThrows(InvalidYearException.class, () ->votingService.validateAndFetchResults("2022"));

    }
}