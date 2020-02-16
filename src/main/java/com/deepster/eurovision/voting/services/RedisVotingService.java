package com.deepster.eurovision.voting.services;

import com.deepster.eurovision.voting.exceptions.InvalidCountryException;
import com.deepster.eurovision.voting.exceptions.InvalidVoteBodyException;
import com.deepster.eurovision.voting.exceptions.InvalidYearException;
import com.deepster.eurovision.voting.exceptions.SameCountryException;
import com.deepster.eurovision.voting.models.Response;
import com.deepster.eurovision.voting.models.Vote;
import com.deepster.eurovision.voting.models.Winners;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Voting Service Implementation backed by a Redis Database.
 */
public final class RedisVotingService implements VotingService {

    private static final Logger LOG = LoggerFactory.getLogger(VotingService.class);
    private final RedisCommands<String, String> connection;
    private final ObjectMapper mapper;
    private final Set<String> countries;
    private final Config config;

    @Inject
    public RedisVotingService(RedisCommands<String, String> connection,
                              @Named("countries") Set<String> countries,
                              ObjectMapper mapper,
                              Config config) {
        this.connection = connection;
        this.countries = countries;
        this.mapper = mapper;
        this.config = config;
    }


    /**
     * Validates string version of year, usually passed as path parameters,
     * and returns an int version if valid.
     * <p>
     * When invalid, runtime exceptions are thrown which are captured by the Spark Router for Response handling.
     *
     * @param yearParam eurovision year as a path parameter
     * @return validated year as int
     */
    public int validateYear(final String yearParam) {

        LOG.debug("Validating Vote Year - {}", yearParam);
        int year;
        try {
            year = Integer.parseInt(yearParam);
        } catch (NumberFormatException ex) {
            throw new InvalidYearException(yearParam);
        }

        int currentYear = Year.now().getValue();
        if (year < config.getInt("euro_vision.start_year") || year > currentYear) {
            throw new InvalidYearException(yearParam);
        }

        return year;
    }


    /**
     * Validates JSON, constructs a Vote object and validates vote data
     * <p>
     * Checks for
     * - Duplicate, incorrect and missing fields in json
     * - Invalid countries
     * - Matching from and to countries
     * <p>
     * When invalid, runtime exceptions are thrown which are captured by the Spark Router for Response handling.
     *
     * @param body json string vote body
     * @return validated {@link Vote} object
     */
    Vote validateVoteJson(final String body) {
        LOG.debug("Validating Vote Body - {}", body);
        Vote vote;
        try {
            vote = mapper.readValue(body, Vote.class);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new InvalidVoteBodyException();
        }

        if (vote == null) {
            throw new InvalidVoteBodyException();
        }

        String from = vote.getCountryFrom().toLowerCase();
        String to = vote.getVotedFor().toLowerCase();

        if (from.equals(to)) {
            throw new SameCountryException(prettifyCountry(from));
        }

        if (!countries.contains(from)) {
            throw new InvalidCountryException(prettifyCountry(from));
        }

        if (!countries.contains(to)) {
            throw new InvalidCountryException(prettifyCountry(to));
        }

        return new Vote(from, to);
    }

    /**
     * Returns a camel case version of the country for pretty printing.
     *
     * @param country raw country string
     * @return Camel case country string
     */
    String prettifyCountry(final String country) {
        return Arrays.stream(country.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).reduce((s1, s2) -> s1 + " " + s2).orElse("");
    }

    /**
     * Returns a new Winners Object with pretty Countries.
     *
     * @param winners {@link Winners} object
     * @return {@link Winners} object with pretty Countries
     */
    Winners prettifyWinners(final Winners winners) {
        return new Winners(winners.getYear(), prettifyCountry(winners.getFirst()), prettifyCountry(winners.getSecond()), prettifyCountry(winners.getThird()));
    }

    @Override
    public long cast(final int year, final Vote vote) {
        LOG.debug("Casting vote - {}", vote);
        RedisCommands<String, String> commands = connection;
        double totalVotes = commands.zincrby(String.format("euro_vision:aggregate:%d", year), 1, vote.getVotedFor());
        commands.hmset(String.format("euro_vision:votes:%d:%d", year, System.currentTimeMillis()), Map.of("countryFrom", vote.getCountryFrom(), "votedFor", vote.getVotedFor()));
        LOG.debug("Total Votes Cast for {} - {}", vote.getVotedFor(), totalVotes);
        return (long) totalVotes;
    }

    @Override
    public Response validateAndCast(final String yearString, final String voteJson) {
        final int year = validateYear(yearString);
        Vote vote = validateVoteJson(voteJson);
        this.cast(year, vote);
        return Response.success(String.format("Thanks for voting for %s. Your vote is recorded.", vote.getVotedFor()));
    }

    @Override
    public Winners getResults(final int year) {
        LOG.debug("Fetching Results for {} ", year);
        List<String> result = connection.zrevrange(String.format("euro_vision:aggregate:%d", year), 0, 3);
        LOG.debug(result.toString());
        Winners winners = new Winners(year, result.get(0), result.get(1), result.get(2));
        LOG.debug("Winners for {} - {}", year, winners);
        return winners;
    }

    @Override
    public Winners validateAndFetchResults(final String yearString) {
        final int year = validateYear(yearString);
        return prettifyWinners(this.getResults(year));
    }
}